package com.aksoit.myfitnessapp.ui.screens.player

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.input.KeyboardType
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

    val current = state.currentStep

    // Subtítulo do header de acordo com conceitos do domínio (COR-005)
    val headerSubtitle = when (current?.stepType) {
        ExecutionStepType.SET_WORK ->
            "Exercício ${state.currentExerciseDisplayIndex} de ${state.totalExercisesCount} • Série ${current.setNumber ?: 1} de ${current.totalSets ?: 1}"
        ExecutionStepType.AMRAP_CLOCK ->
            "AMRAP • Round ${state.circuitRoundsCount}"
        ExecutionStepType.REST_SET,
        ExecutionStepType.REST_BLOCK ->
            "Descanso • Exercício ${state.currentExerciseDisplayIndex} de ${state.totalExercisesCount}"
        ExecutionStepType.FOR_TIME_CLOCK ->
            "For Time • Exercício ${state.currentExerciseDisplayIndex} de ${state.totalExercisesCount}"
        ExecutionStepType.HIIT_WORK,
        ExecutionStepType.HIIT_REST ->
            "HIIT • Tiro ${current.roundNumber ?: 1} de ${current.totalRounds ?: 1}"
        ExecutionStepType.STRETCH_HOLD,
        ExecutionStepType.SWITCH_SIDE_REST ->
            "Alongamento • Exercício ${state.currentExerciseDisplayIndex} de ${state.totalExercisesCount}"
        else ->
            "Exercício ${state.currentExerciseDisplayIndex} de ${state.totalExercisesCount}"
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
                            text = headerSubtitle,
                            style = MaterialTheme.typography.labelMedium,
                            color = CyanPulse
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { showCancelDialog = true }) {
                        Icon(Icons.Default.Close, contentDescription = "Cancelar", tint = TextPrimary)
                    }
                },
                actions = {
                    // Botão PLANO (COR-003)
                    Button(
                        onClick = { viewModel.openPlanSheet() },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkCharcoal, contentColor = SprintGreen),
                        border = BorderStroke(1.dp, SprintGreen),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Icon(Icons.Default.FormatListBulleted, contentDescription = null, modifier = Modifier.height(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PLANO", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    IconButton(onClick = { viewModel.togglePlayPause() }) {
                        Icon(
                            imageVector = if (state.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = if (state.isPaused) "Continuar" else "Pausar",
                            tint = if (state.isPaused) WarningAmber else SprintGreen
                        )
                    }
                    IconButton(onClick = { viewModel.skipCurrentStep() }) {
                        Icon(Icons.Default.SkipNext, contentDescription = "Pular", tint = TextSecondary)
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

            Spacer(modifier = Modifier.height(12.dp))

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

    // Modal Bottom Sheet do Plano do Treino (COR-003, COR-004, COR-010)
    if (state.isPlanSheetOpen) {
        WorkoutPlanBottomSheet(
            items = state.planExerciseItems,
            onSelectExercise = { blockIdx, exPos -> viewModel.selectExercise(blockIdx, exPos) },
            onDismiss = { viewModel.closePlanSheet() }
        )
    }

    // Diálogo de Registro de Resultado AMRAP (COR-002)
    if (state.isAmrapResultDialogOpen) {
        AmrapResultDialog(
            currentStep = current,
            roundsCount = state.circuitRoundsCount,
            partialReps = state.amrapPartialRepsInput,
            selectedExerciseName = state.selectedAmrapPartialExerciseName,
            onPartialRepsChange = { viewModel.updateAmrapPartialReps(it) },
            onSelectExercise = { id, name -> viewModel.selectAmrapPartialExercise(id, name) },
            onConfirm = { viewModel.confirmAmrapResult() },
            onDismiss = { viewModel.closeAmrapResultDialog() }
        )
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

/**
 * Visão completa do plano de treino em Bottom Sheet (COR-003, COR-004, COR-010).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutPlanBottomSheet(
    items: List<PlanExerciseItem>,
    onSelectExercise: (blockIndex: Int, exercisePosition: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkCharcoal,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PLANO DO TREINO",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = SprintGreen
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Fechar", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(items) { item ->
                    val (statusIcon, statusText, statusColor) = when (item.status) {
                        PlanExerciseStatus.COMPLETED -> Triple("✓", "CONCLUÍDO", SprintGreen)
                        PlanExerciseStatus.CURRENT -> Triple("→", "ATUAL", CyanPulse)
                        PlanExerciseStatus.IN_PROGRESS -> Triple("◔", "EM ANDAMENTO", WarningAmber)
                        PlanExerciseStatus.SKIPPED -> Triple("↷", "PULADO", CrimsonDanger)
                        PlanExerciseStatus.PENDING -> Triple("○", "PENDENTE", TextSecondary)
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectExercise(item.blockIndex, item.exercisePosition) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (item.status == PlanExerciseStatus.CURRENT) DeepVoid else DarkCharcoal
                        ),
                        border = BorderStroke(
                            width = if (item.status == PlanExerciseStatus.CURRENT) 2.dp else 1.dp,
                            color = if (item.status == PlanExerciseStatus.CURRENT) CyanPulse else CardBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "$statusIcon ",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp,
                                        color = statusColor
                                    )
                                    Text(
                                        text = item.exerciseName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${item.completedSets}/${item.totalSets} séries",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = "• $statusText",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = statusColor,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            if (item.status != PlanExerciseStatus.CURRENT) {
                                Button(
                                    onClick = { onSelectExercise(item.blockIndex, item.exercisePosition) },
                                    colors = ButtonDefaults.buttonColors(containerColor = DeepVoid, contentColor = SprintGreen),
                                    border = BorderStroke(1.dp, SprintGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("IR", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Diálogo para confirmação do resultado do AMRAP com suporte a reps parciais (COR-002).
 */
@Composable
fun AmrapResultDialog(
    currentStep: ExecutionStep?,
    roundsCount: Int,
    partialReps: String,
    selectedExerciseName: String,
    onPartialRepsChange: (String) -> Unit,
    onSelectExercise: (Long?, String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val items = currentStep?.circuitItems.orEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkCharcoal,
        title = {
            Text(text = "Finalizar AMRAP", fontWeight = FontWeight.Bold, color = TextPrimary)
        },
        text = {
            Column {
                Text(
                    text = "Rounds completos: $roundsCount",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = SprintGreen
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Reps Parciais (opcional):",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (items.isNotEmpty()) {
                    Text(
                        text = "Exercício das reps parciais:",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyanPulse
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items.forEach { item ->
                            val isSelected = item.exerciseName == selectedExerciseName
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSelectExercise(item.exerciseId, item.exerciseName) },
                                label = { Text(item.exerciseName, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanPulse,
                                    selectedLabelColor = Color.Black,
                                    labelColor = TextPrimary
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = partialReps,
                    onValueChange = onPartialRepsChange,
                    label = { Text("Quantidade de Repetições") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = SprintGreen,
                        unfocusedBorderColor = CardBorder
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = SprintGreen, contentColor = Color.Black)
            ) {
                Text("Confirmar e Salvar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Voltar", color = TextSecondary)
            }
        }
    )
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

/**
 * AMRAP Execution Screen (COR-002):
 * Apresenta timer contínuo, contador de rounds e a lista completa dos exercícios
 * do circuito com seus respectivos alvos (reps/carga).
 */
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

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ROUNDS",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Text(
                    text = "$rounds",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                    color = SprintGreen
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onDecrement,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkCharcoal, contentColor = TextPrimary),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Text("- ROUND")
                }

                Button(
                    onClick = onIncrement,
                    colors = ButtonDefaults.buttonColors(containerColor = SprintGreen, contentColor = Color.Black)
                ) {
                    Text("+ ROUND", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Exibição do Circuito Completo (COR-002)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCharcoal),
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "CIRCUITO",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black,
                    color = CyanPulse
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (step.circuitItems.isEmpty()) {
                    Text("Nenhum exercício no circuito.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                } else {
                    step.circuitItems.forEachIndexed { idx, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${idx + 1}. ${item.exerciseName}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            val targetStr = buildString {
                                if (item.targetReps != null) append("${item.targetReps} reps")
                                if (item.targetLoadKg != null && item.targetLoadKg > 0.0) append(" • ${item.targetLoadKg} kg")
                            }
                            Text(
                                text = targetStr.ifBlank { "Livre" },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = SprintGreen
                            )
                        }
                    }
                }
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
