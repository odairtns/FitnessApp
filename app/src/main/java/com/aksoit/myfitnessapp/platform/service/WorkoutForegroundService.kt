package com.aksoit.myfitnessapp.platform.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import com.aksoit.myfitnessapp.domain.execution.ExecutionPlan
import com.aksoit.myfitnessapp.domain.execution.ExecutionStep
import com.aksoit.myfitnessapp.domain.timer.TimerEvent
import com.aksoit.myfitnessapp.domain.timer.TimerState
import com.aksoit.myfitnessapp.platform.audio.AudioFocusController
import com.aksoit.myfitnessapp.platform.audio.TonePlayer
import com.aksoit.myfitnessapp.platform.audio.VoiceCoachTts
import com.aksoit.myfitnessapp.platform.haptics.HapticFeedbackController
import com.aksoit.myfitnessapp.platform.notification.WorkoutNotificationManager
import com.aksoit.myfitnessapp.platform.timer.MonotonicTimerEngineImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WorkoutForegroundService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val binder = LocalBinder()

    lateinit var timerEngine: MonotonicTimerEngineImpl
        private set

    lateinit var voiceCoach: VoiceCoachTts
        private set

    lateinit var tonePlayer: TonePlayer
        private set

    lateinit var haptics: HapticFeedbackController
        private set

    lateinit var audioFocus: AudioFocusController
        private set

    lateinit var notificationManager: WorkoutNotificationManager
        private set

    private val _serviceActions = MutableSharedFlow<String>(extraBufferCapacity = 16)
    val serviceActions: SharedFlow<String> = _serviceActions.asSharedFlow()

    private val _currentWorkoutTitle = MutableStateFlow("Treino em Andamento")
    val currentWorkoutTitle: StateFlow<String> = _currentWorkoutTitle.asStateFlow()

    private val _currentStepInfo = MutableStateFlow("")
    val currentStepInfo: StateFlow<String> = _currentStepInfo.asStateFlow()

    inner class LocalBinder : Binder() {
        fun getService(): WorkoutForegroundService = this@WorkoutForegroundService
    }

    override fun onCreate() {
        super.onCreate()
        timerEngine = MonotonicTimerEngineImpl(serviceScope)
        voiceCoach = VoiceCoachTts(this)
        tonePlayer = TonePlayer()
        haptics = HapticFeedbackController(this)
        audioFocus = AudioFocusController(this)
        notificationManager = WorkoutNotificationManager(this)

        observeTimerEvents()
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action != null) {
            handleAction(action)
        } else {
            startInForeground()
        }
        return START_STICKY
    }

    private fun startInForeground() {
        val notification = notificationManager.buildNotification(
            title = _currentWorkoutTitle.value,
            content = _currentStepInfo.value.ifBlank { "Treinando..." },
            isPaused = timerEngine.state.value == TimerState.PAUSED
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                WorkoutNotificationManager.NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(WorkoutNotificationManager.NOTIFICATION_ID, notification)
        }
    }

    private fun handleAction(action: String) {
        when (action) {
            ACTION_PAUSE -> {
                timerEngine.pause()
                updateNotification()
                _serviceActions.tryEmit(ACTION_PAUSE)
            }
            ACTION_RESUME -> {
                timerEngine.resume()
                updateNotification()
                _serviceActions.tryEmit(ACTION_RESUME)
            }
            ACTION_SKIP -> {
                _serviceActions.tryEmit(ACTION_SKIP)
            }
            ACTION_FINISH -> {
                _serviceActions.tryEmit(ACTION_FINISH)
            }
        }
    }

    fun updateStepDisplay(title: String, stepText: String) {
        _currentWorkoutTitle.value = title
        _currentStepInfo.value = stepText
        updateNotification()
    }

    private fun updateNotification() {
        notificationManager.updateNotification(
            title = _currentWorkoutTitle.value,
            content = _currentStepInfo.value.ifBlank { "Treinando..." },
            isPaused = timerEngine.state.value == TimerState.PAUSED
        )
    }

    private fun observeTimerEvents() {
        serviceScope.launch {
            timerEngine.events.collect { event ->
                when (event) {
                    is TimerEvent.Started -> {
                        tonePlayer.playStart()
                        haptics.vibrateTick()
                    }
                    is TimerEvent.Warning10Seconds -> {
                        audioFocus.requestFocus(onLossTransient = {}, onGain = {})
                        tonePlayer.playWarning()
                        haptics.vibrateWarning()
                        voiceCoach.speak("Atenção: faltam 10 segundos")
                    }
                    is TimerEvent.CountdownTick -> {
                        tonePlayer.playTick()
                        haptics.vibrateTick()
                    }
                    is TimerEvent.Completed -> {
                        tonePlayer.playFinish()
                        haptics.vibrateFinish()
                    }
                    else -> {}
                }
            }
        }

        serviceScope.launch {
            timerEngine.remainingMs.collect { remainingMs ->
                val seconds = (remainingMs + 999L) / 1000L
                val timeFormatted = String.format("%02d:%02d", seconds / 60, seconds % 60)
                val fullText = if (_currentStepInfo.value.isNotBlank()) "${_currentStepInfo.value} • $timeFormatted" else timeFormatted
                notificationManager.updateNotification(
                    title = _currentWorkoutTitle.value,
                    content = fullText,
                    isPaused = timerEngine.state.value == TimerState.PAUSED
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceCoach.shutdown()
        tonePlayer.release()
        audioFocus.abandonFocus()
        serviceScope.cancel()
    }

    companion object {
        const val ACTION_PAUSE = "com.aksoit.myfitnessapp.action.PAUSE"
        const val ACTION_RESUME = "com.aksoit.myfitnessapp.action.RESUME"
        const val ACTION_SKIP = "com.aksoit.myfitnessapp.action.SKIP"
        const val ACTION_FINISH = "com.aksoit.myfitnessapp.action.FINISH"

        fun startService(context: Context) {
            val intent = Intent(context, WorkoutForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, WorkoutForegroundService::class.java)
            context.stopService(intent)
        }
    }
}
