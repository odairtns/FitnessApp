package com.aksoit.myfitnessapp.platform.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.aksoit.myfitnessapp.platform.service.WorkoutForegroundService

class WorkoutNotificationManager(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createChannel()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Treino Ativo",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificação persistente durante a execução de treinos"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun buildNotification(
        title: String,
        content: String,
        isPaused: Boolean
    ): Notification {
        // Intent para abrir o App quando tocar na notificação
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseAction = if (isPaused) {
            val resumeIntent = Intent(context, WorkoutForegroundService::class.java).apply {
                action = WorkoutForegroundService.ACTION_RESUME
            }
            NotificationCompat.Action.Builder(
                android.R.drawable.ic_media_play,
                "Continuar",
                PendingIntent.getService(context, 1, resumeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            ).build()
        } else {
            val pauseIntent = Intent(context, WorkoutForegroundService::class.java).apply {
                action = WorkoutForegroundService.ACTION_PAUSE
            }
            NotificationCompat.Action.Builder(
                android.R.drawable.ic_media_pause,
                "Pausar",
                PendingIntent.getService(context, 2, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            ).build()
        }

        val skipIntent = Intent(context, WorkoutForegroundService::class.java).apply {
            action = WorkoutForegroundService.ACTION_SKIP
        }
        val skipAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_media_next,
            "Pular",
            PendingIntent.getService(context, 3, skipIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        ).build()

        val finishIntent = Intent(context, WorkoutForegroundService::class.java).apply {
            action = WorkoutForegroundService.ACTION_FINISH
        }
        val finishAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_close_clear_cancel,
            "Finalizar",
            PendingIntent.getService(context, 4, finishIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        ).build()

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(contentPendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(playPauseAction)
            .addAction(skipAction)
            .addAction(finishAction)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    fun updateNotification(title: String, content: String, isPaused: Boolean) {
        notificationManager.notify(NOTIFICATION_ID, buildNotification(title, content, isPaused))
    }

    companion object {
        const val CHANNEL_ID = "workout_playback_channel"
        const val NOTIFICATION_ID = 1001
    }
}
