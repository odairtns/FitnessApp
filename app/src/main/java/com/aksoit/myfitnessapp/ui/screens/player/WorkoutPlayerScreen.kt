package com.aksoit.myfitnessapp.ui.screens.player

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aksoit.myfitnessapp.domain.execution.ExecutionStep
import com.aksoit.myfitnessapp.domain.execution.ExecutionStepType
import com.aksoit.myfitnessapp.domain.model.Side
import com.aksoit.myfitnessapp.ui.components.BigTimerDisplay
import com.aksoit.myfitnessapp.ui.components.ConfirmationDialog
import com.aksoit.myfitnessapp.ui.components.LargeActionButton
import com.aksoit.myfitnessapp.ui.components.StepperControl
import com.aksoit.myfitnessapp.ui.theme.CardBorder
import com.aksoit.myfitnessapp.ui.theme.CrimsonDanger
import com.aksoit.myfitnessapp.ui.theme.CyanPulse
import com.aksoit.myfitnessapp.ui.theme.DarkCharcoal
import com.aksoit.myfitnessapp.ui.theme.DeepVoid
import com.aksoit.myfitnessapp.ui.theme.SprintGreen
import com.aksoit.myfitnessapp.ui.theme.TextPrimary
import com.aksoit.myfitnessapp.ui.theme.TextSecondary
import com.aksoit.myfitnessapp.ui.theme.WarningAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutPlayerScreen(
    viewModel: WorkoutPlayerViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Long) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var showCancelDialog by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is WorkoutPlayerEffect.NavigateBack -> onNavigateBack()
                is WorkoutPlayerEffect.NavigateToDetail -> onNavigateToDetail(effect.sessionId)
                is WorkoutPlayerEffect.ShowToast -> {}
            }
        }
    }

    Scaffold(
        containerColor = DeepVoid,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = state.plan?.templateName ?: "Treino",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Passo ${state.currentStepIndex + 1} de ${state.totalSteps}",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { showCancelDialog = true }) {
                        Icon(Icons.Default.Close, contentDescription = "Cancelar", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.togglePlayPause() }) {
                        Icon(
                            imageVector = if (state.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = if (state.isPaused) "Continuar" else "Pausar",
                            tint = if (state.isPaused) WarningAmber else SprintGreen
                        )
                    }
                    IconButton(onClick = { viewModel.skipCurrentStep() }) {
                        Icon(Icons.Default.SkipNext, contentDescription = "Pular Passo", tint = TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepVoid)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            val progress = if (state.totalSteps > 0) (state.currentStepIndex + 1).toFloat() / state.totalSteps.toFloat() else 0f
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = SprintGreen,
                trackColor = CardBorder,
            )

            Spacer(modifier = Modifier.height(16.dp))

            val current = state.currentStep
            if (current != null) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    when (current.stepType) {
                        ExecutionStepType.SET_WORK -> StrengthStepContent(
                            step = current,
                            loadInput = state.actualLoadInput,
                            repsInput = state.actualRepsInput,
                            suggestedLoad = state.suggestedLoadKg,
                            onLoadChange = { viewModel.updateActualLoad(it) },
                            onRepsChange = { viewModel.updateActualReps(it) }
                        )

                        ExecutionStepType.REST_SET,
                        ExecutionStepType.REST_BLOCK,
                        ExecutionStepType.PREPARE -> RestStepContent(
                            step = current,
                            remainingSeconds = state.remainingSeconds
                        )

                        ExecutionStepType.AMRAP_CLOCK -> AmrapStepContent(
                            step = current,
                            remainingSeconds = state.remainingSeconds,
                            rounds = state.circuitRoundsCount,
                            onIncrement = { viewModel.incrementCircuitRound() },
                            onDecrement = { viewModel.decrementCircuitRound() }
                        )

                        ExecutionStepType.FOR_TIME_CLOCK -> ForTimeStepContent(
                            step = current,
                            elapsedSeconds = state.elapsedSeconds
                        )

                        ExecutionStepType.HIIT_WORK,
                        ExecutionStepType.HIIT_REST -> HiitStepContent(
                            step = current,
                            remainingSeconds = state.remainingSeconds
                        )

                        ExecutionStepType.STRETCH_HOLD,
                        ExecutionStepType.SWITCH_SIDE_REST -> StretchStepContent(
                            step = current,
                            remainingSeconds = state.remainingSeconds
                        )

                        else -> Text("Carregando...", color = TextSecondary)
                    }
                }
            } else {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text("Iniciando treino...", color = TextSecondary)
                }
            }

            // Ações na base da tela
            Column(modifier = Modifier.fillMaxWidth()) {
                val buttonText = when (current?.stepType) {
                    ExecutionStepType.SET_WORK -> "CONCLUIR SÉRIE"
                    ExecutionStepType.REST_SET,
                    ExecutionStepType.REST_BLOCK,
                    ExecutionStepType.PREPARE -> "PULAR DESCANSO"
                    ExecutionStepType.AMRAP_CLOCK -> "CONCLUIR AMRAP"
                    ExecutionStepType.FOR_TIME_CLOCK -> "FINALIZAR TEMPO"
                    else -> "AVANÇAR"
                }

                LargeActionButton(
                    text = buttonText,
                    onClick = { viewModel.completeCurrentStep() },
                    containerColor = when (current?.stepType) {
                        ExecutionStepType.REST_SET,
                        ExecutionStepType.REST_BLOCK,
                        ExecutionStepType.PREPARE -> CyanPulse
                        else -> SprintGreen
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = { showCancelDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Text("Interromper")
                    }

                    OutlinedButton(
                        onClick = { showDiscardDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonDanger),
                        border = BorderStroke(1.dp, CrimsonDanger)
                    ) {
                        Text("Descartar")
                    }
                }
            }
        }
    }

    if (showCancelDialog) {
        ConfirmationDialog(
            title = "Interromper Treino?",
            message = "O treino será finalizado como CANCELADO. Todas as séries concluídas até este momento serão salvas no seu histórico.",
            confirmText = "Interromper e Salvar",
            onConfirm = { viewModel.cancelWorkout() },
            onDismiss = { showCancelDialog = false }
        )
    }

    if (showDiscardDialog) {
        ConfirmationDialog(
            title = "Descartar Treino?",
            message = "Atenção: Todo o progresso deste treino será excluído permanentemente do banco de dados (DELETE CASCADE).",
            confirmText = "Descartar Tudo",
            isDestructive = true,
            onConfirm = { viewModel.discardWorkout() },
            onDismiss = { showDiscardDialog = false }
        )
    }
}

@Composable
fun StrengthStepContent(
    step: ExecutionStep,
    loadInput: String,
    repsInput: String,
    suggestedLoad: Double?,
    onLoadChange: (String) -> Unit,
    onRepsChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = step.exerciseName,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Série ${step.setNumber ?: 1} de ${step.totalSets ?: 1}",
            style = MaterialTheme.typography.titleMedium,
            color = CyanPulse,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StepperControl(
                label = "Carga (kg)",
                value = loadInput,
                onValueChange = onLoadChange,
                step = 2.0,
                modifier = Modifier.weight(1f)
            )
            StepperControl(
                label = "Repetições",
                value = repsInput,
                onValueChange = onRepsChange,
                step = 1.0,
                modifier = Modifier.weight(1f)
            )
        }

        if (suggestedLoad != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Última carga registrada: ${suggestedLoad} kg",
                style = MaterialTheme.typography.bodyMedium,
                color = WarningAmber
            )
        }
    }
}

@Composable
fun RestStepContent(
    step: ExecutionStep,
    remainingSeconds: Long
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BigTimerDisplay(
            remainingSeconds = remainingSeconds,
            color = CyanPulse,
            label = step.blockName.ifBlank { "Descanso" }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Respire e recupere-se",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary
        )
    }
}

@Composable
fun AmrapStepContent(
    step: ExecutionStep,
    remainingSeconds: Long,
    rounds: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BigTimerDisplay(
            remainingSeconds = remainingSeconds,
            color = WarningAmber,
            label = "TEMPO RESTANTE"
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "ROUNDS",
            style = MaterialTheme.typography.labelLarge,
            color = TextSecondary
        )
        Text(
            text = "$rounds",
            fontSize = 64.sp,
            fontWeight = FontWeight.Black,
            color = SprintGreen
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onDecrement,
                colors = ButtonDefaults.buttonColors(containerColor = DarkCharcoal, contentColor = TextPrimary),
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.weight(1f)
            ) {
                Text("- ROUND")
            }

            Button(
                onClick = onIncrement,
                colors = ButtonDefaults.buttonColors(containerColor = SprintGreen, contentColor = Color.Black),
                modifier = Modifier.weight(2f)
            ) {
                Text("+ ROUND", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    }
}

@Composable
fun ForTimeStepContent(
    step: ExecutionStep,
    elapsedSeconds: Long
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BigTimerDisplay(
            remainingSeconds = elapsedSeconds,
            color = SprintGreen,
            label = "TEMPO DECORRIDO"
        )
        step.durationSeconds?.let { cap ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Time Cap: %02d:%02d".format(cap / 60, cap % 60),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun HiitStepContent(
    step: ExecutionStep,
    remainingSeconds: Long
) {
    val isWork = step.stepType == ExecutionStepType.HIIT_WORK
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BigTimerDisplay(
            remainingSeconds = remainingSeconds,
            color = if (isWork) SprintGreen else CyanPulse,
            label = if (isWork) "TIRO (TRABALHO)" else "DESCANSO"
        )

        step.roundNumber?.let { r ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Round $r de ${step.totalRounds ?: 1}",
                style = MaterialTheme.typography.titleMedium,
                color = TextSecondary
            )
        }

        if (step.hiitTargets.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkCharcoal),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "Metas Planejadas:", color = TextSecondary, style = MaterialTheme.typography.labelLarge)
                    step.hiitTargets.forEach { target ->
                        Text(text = "• ${target.type}: ${target.value} ${target.unit}", color = TextPrimary)
                    }
                }
            }
        }
    }
}

@Composable
fun StretchStepContent(
    step: ExecutionStep,
    remainingSeconds: Long
) {
    val isSwitch = step.stepType == ExecutionStepType.SWITCH_SIDE_REST
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val sideLabel = when (step.side) {
            Side.RIGHT -> "LADO DIREITO"
            Side.LEFT -> "LADO ESQUERDO"
            Side.BILATERAL -> "AMBOS OS LADOS"
            else -> "ALONGAMENTO"
        }

        Text(
            text = if (isSwitch) "TROCA DE LADO" else sideLabel,
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = if (isSwitch) WarningAmber else SprintGreen
        )

        Spacer(modifier = Modifier.height(16.dp))

        BigTimerDisplay(
            remainingSeconds = remainingSeconds,
            color = if (isSwitch) WarningAmber else CyanPulse,
            label = if (isSwitch) "PREPARE-SE" else "SUSTENTAÇÃO"
        )
    }
}
