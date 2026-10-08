package com.aksoit.myfitnessapp.ui.screens.workouts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aksoit.myfitnessapp.domain.model.WorkoutBlock
import com.aksoit.myfitnessapp.domain.model.WorkoutModality
import com.aksoit.myfitnessapp.domain.model.WorkoutProtocol
import com.aksoit.myfitnessapp.ui.components.LargeActionButton
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
fun TemplateEditorScreen(
    templateId: Long?,
    viewModel: TemplateEditorViewModel,
    onNavigateBack: () -> Unit,
    onSelectExercise: (blockIndex: Int) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(templateId) {
        viewModel.loadTemplate(templateId)
    }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onNavigateBack()
    }

    Scaffold(
        containerColor = DeepVoid,
        topBar = {
            TopAppBar(
                title = { Text(if (templateId != null) "Editar Treino" else "Novo Treino", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.save() }) {
                        Icon(Icons.Default.Check, contentDescription = "Salvar", tint = SprintGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepVoid, titleContentColor = TextPrimary)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Identificação do Treino
            item {
                OutlinedTextField(
                    value = state.name,
                    onValueChange = { viewModel.updateName(it) },
                    label = { Text("Nome do Treino") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = SprintGreen,
                        unfocusedBorderColor = CardBorder
                    )
                )
            }

            item {
                OutlinedTextField(
                    value = state.description,
                    onValueChange = { viewModel.updateDescription(it) },
                    label = { Text("Descrição / Foco") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = SprintGreen,
                        unfocusedBorderColor = CardBorder
                    )
                )
            }

            // 2. Seleção de Modalidade (COR-001)
            item {
                Column {
                    Text("Modalidade:", color = TextSecondary, style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WorkoutModality.entries.forEach { modality ->
                            val label = when (modality) {
                                WorkoutModality.STRENGTH -> "Musculação"
                                WorkoutModality.CARDIO -> "Cardio"
                                WorkoutModality.CROSS_TRAINING -> "Funcional / Cross"
                                WorkoutModality.MOBILITY -> "Mobilidade"
                            }
                            FilterChip(
                                selected = state.modality == modality,
                                onClick = { viewModel.updateModality(modality) },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SprintGreen,
                                    selectedLabelColor = Color.Black,
                                    labelColor = TextPrimary
                                )
                            )
                        }
                    }
                }
            }

            // 3. Seleção de Protocolo (COR-001, COR-012)
            item {
                Column {
                    Text("Protocolo de Treino:", color = TextSecondary, style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WorkoutProtocol.entries.forEach { protocol ->
                            val label = when (protocol) {
                                WorkoutProtocol.STANDARD_STRENGTH -> "Séries Padrão"
                                WorkoutProtocol.HYPERTROPHY -> "Hipertrofia"
                                WorkoutProtocol.HIIT_INTERVALS -> "HIIT"
                                WorkoutProtocol.AMRAP -> "AMRAP"
                                WorkoutProtocol.EMOM -> "EMOM"
                                WorkoutProtocol.FOR_TIME -> "For Time"
                                WorkoutProtocol.STRETCH -> "Alongamento"
                            }
                            FilterChip(
                                selected = state.protocol == protocol,
                                onClick = { viewModel.updateProtocol(protocol) },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanPulse,
                                    selectedLabelColor = Color.Black,
                                    labelColor = TextPrimary
                                )
                            )
                        }
                    }
                }
            }

            // 4. Editor Específico do Protocolo (COR-012)
            when (state.protocol) {
                WorkoutProtocol.AMRAP -> {
                    item {
                        AmrapProtocolEditor(
                            block = state.blocks.firstOrNull(),
                            onUpdateDuration = { dur -> viewModel.updateBlockWorkDurationSeconds(0, dur) },
                            onAddExercise = { onSelectExercise(0) },
                            onRemoveExercise = { exIdx -> viewModel.removeExercise(0, exIdx) },
                            onUpdateReps = { exIdx, reps -> viewModel.updateCircuitExerciseTargetReps(0, exIdx, reps) }
                        )
                    }
                }

                WorkoutProtocol.HIIT_INTERVALS -> {
                    item {
                        HiitProtocolEditor(
                            block = state.blocks.firstOrNull(),
                            onUpdateRounds = { r -> viewModel.updateBlockRounds(0, r) },
                            onUpdateWork = { w -> viewModel.updateBlockWorkDurationSeconds(0, w) },
                            onUpdateRest = { r -> viewModel.updateBlockRestDurationSeconds(0, r) },
                            onAddExercise = { onSelectExercise(0) },
                            onRemoveExercise = { exIdx -> viewModel.removeExercise(0, exIdx) }
                        )
                    }
                }

                WorkoutProtocol.EMOM -> {
                    item {
                        EmomProtocolEditor(
                            block = state.blocks.firstOrNull(),
                            onUpdateRounds = { r -> viewModel.updateBlockRounds(0, r) },
                            onAddExercise = { onSelectExercise(0) },
                            onRemoveExercise = { exIdx -> viewModel.removeExercise(0, exIdx) },
                            onUpdateReps = { exIdx, reps -> viewModel.updateCircuitExerciseTargetReps(0, exIdx, reps) }
                        )
                    }
                }

                WorkoutProtocol.FOR_TIME -> {
                    item {
                        ForTimeProtocolEditor(
                            block = state.blocks.firstOrNull(),
                            onUpdateTimeCap = { cap -> viewModel.updateBlockWorkDurationSeconds(0, cap) },
                            onAddExercise = { onSelectExercise(0) },
                            onRemoveExercise = { exIdx -> viewModel.removeExercise(0, exIdx) },
                            onUpdateReps = { exIdx, reps -> viewModel.updateCircuitExerciseTargetReps(0, exIdx, reps) }
                        )
                    }
                }

                WorkoutProtocol.STRETCH -> {
                    item {
                        StretchProtocolEditor(
                            block = state.blocks.firstOrNull(),
                            onUpdateHold = { h -> viewModel.updateBlockWorkDurationSeconds(0, h) },
                            onAddExercise = { onSelectExercise(0) },
                            onRemoveExercise = { exIdx -> viewModel.removeExercise(0, exIdx) }
                        )
                    }
                }

                WorkoutProtocol.STANDARD_STRENGTH,
                WorkoutProtocol.HYPERTROPHY -> {
                    itemsIndexed(state.blocks) { blockIdx, block ->
                        StrengthBlockEditorCard(
                            block = block,
                            onAddExercise = { onSelectExercise(blockIdx) },
                            onRemoveBlock = { viewModel.removeBlock(blockIdx) },
                            onRemoveExercise = { exIdx -> viewModel.removeExercise(blockIdx, exIdx) },
                            onAddSet = { exIdx -> viewModel.addSet(blockIdx, exIdx) },
                            onRemoveSet = { exIdx, sIdx -> viewModel.removeSet(blockIdx, exIdx, sIdx) }
                        )
                    }

                    item {
                        OutlinedButton(
                            onClick = { viewModel.addBlock("Bloco ${state.blocks.size + 1}") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, SprintGreen)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = SprintGreen)
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text("Adicionar Bloco de Força", color = SprintGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                LargeActionButton(
                    text = "SALVAR TREINO",
                    onClick = { viewModel.save() }
                )
            }
        }
    }
}

/**
 * Editor específico de AMRAP (COR-006 & COR-012).
 * Exibe duração e circuito com exercícios e alvos de reps, SEM séries de musculação.
 */
@Composable
fun AmrapProtocolEditor(
    block: WorkoutBlock?,
    onUpdateDuration: (Int) -> Unit,
    onAddExercise: () -> Unit,
    onRemoveExercise: (Int) -> Unit,
    onUpdateReps: (Int, Int) -> Unit
) {
    val durationMinutes = ((block?.workDurationSeconds ?: 600) / 60)

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCharcoal),
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "CONFIGURAÇÃO AMRAP",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = WarningAmber
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Duração Total:", color = TextPrimary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(5, 10, 15, 20).forEach { mins ->
                            FilterChip(
                                selected = durationMinutes == mins,
                                onClick = { onUpdateDuration(mins * 60) },
                                label = { Text("${mins} min") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = WarningAmber,
                                    selectedLabelColor = Color.Black,
                                    labelColor = TextPrimary
                                )
                            )
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCharcoal),
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "CIRCUITO AMRAP",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SprintGreen
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Adicione os exercícios em sequência e defina as repetições por round.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                val exercises = block?.exercises.orEmpty()
                if (exercises.isEmpty()) {
                    Text(
                        text = "Nenhum exercício no circuito ainda.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    exercises.forEachIndexed { exIdx, exercise ->
                        val targetReps = exercise.plannedSets.firstOrNull()?.targetReps ?: 10
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = DeepVoid),
                            border = BorderStroke(1.dp, CardBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${exIdx + 1}. ${exercise.displayName}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "$targetReps reps por round",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CyanPulse
                                    )
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedButton(
                                        onClick = { onUpdateReps(exIdx, maxOf(1, targetReps - 5)) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("-5", fontSize = 12.sp)
                                    }
                                    OutlinedButton(
                                        onClick = { onUpdateReps(exIdx, targetReps + 5) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("+5", fontSize = 12.sp)
                                    }
                                    IconButton(onClick = { onRemoveExercise(exIdx) }) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = CrimsonDanger)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onAddExercise,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepVoid, contentColor = SprintGreen),
                    border = BorderStroke(1.dp, SprintGreen),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text("Adicionar Exercício ao Circuito", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Editor específico de HIIT (COR-012).
 */
@Composable
fun HiitProtocolEditor(
    block: WorkoutBlock?,
    onUpdateRounds: (Int) -> Unit,
    onUpdateWork: (Int) -> Unit,
    onUpdateRest: (Int) -> Unit,
    onAddExercise: () -> Unit,
    onRemoveExercise: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCharcoal),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "CONFIGURAÇÃO INTERVALOS HIIT",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SprintGreen
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Número de Rounds:", color = TextPrimary)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(6, 8, 10, 12).forEach { r ->
                        FilterChip(
                            selected = (block?.rounds ?: 8) == r,
                            onClick = { onUpdateRounds(r) },
                            label = { Text("$r") }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Tiro (Trabalho):", color = TextPrimary)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(20, 30, 45, 60).forEach { s ->
                        FilterChip(
                            selected = (block?.workDurationSeconds ?: 30) == s,
                            onClick = { onUpdateWork(s) },
                            label = { Text("${s}s") }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Descanso:", color = TextPrimary)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(10, 15, 30).forEach { s ->
                        FilterChip(
                            selected = (block?.restDurationSeconds ?: 15) == s,
                            onClick = { onUpdateRest(s) },
                            label = { Text("${s}s") }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Estações / Exercícios (Opcional):", color = TextSecondary, style = MaterialTheme.typography.labelMedium)
            val exercises = block?.exercises.orEmpty()
            exercises.forEachIndexed { exIdx, ex ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(ex.displayName, color = TextPrimary)
                    IconButton(onClick = { onRemoveExercise(exIdx) }) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = CrimsonDanger)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onAddExercise,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = DeepVoid, contentColor = SprintGreen),
                border = BorderStroke(1.dp, SprintGreen),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.padding(4.dp))
                Text("Adicionar Estação HIIT")
            }
        }
    }
}

/**
 * Editor específico de EMOM (COR-012).
 */
@Composable
fun EmomProtocolEditor(
    block: WorkoutBlock?,
    onUpdateRounds: (Int) -> Unit,
    onAddExercise: () -> Unit,
    onRemoveExercise: (Int) -> Unit,
    onUpdateReps: (Int, Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCharcoal),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "CONFIGURAÇÃO EMOM (Every Minute on the Minute)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = WarningAmber
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total de Minutos (Rounds):", color = TextPrimary)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(5, 10, 15, 20).forEach { r ->
                        FilterChip(
                            selected = (block?.rounds ?: 10) == r,
                            onClick = { onUpdateRounds(r) },
                            label = { Text("${r} min") }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Exercícios por minuto:", color = TextSecondary, style = MaterialTheme.typography.labelMedium)

            val exercises = block?.exercises.orEmpty()
            exercises.forEachIndexed { exIdx, exercise ->
                val targetReps = exercise.plannedSets.firstOrNull()?.targetReps ?: 10
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = DeepVoid),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(exercise.displayName, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("$targetReps reps por minuto", color = CyanPulse, style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { onRemoveExercise(exIdx) }) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = CrimsonDanger)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onAddExercise,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = DeepVoid, contentColor = SprintGreen),
                border = BorderStroke(1.dp, SprintGreen),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.padding(4.dp))
                Text("Adicionar Exercício ao EMOM")
            }
        }
    }
}

/**
 * Editor específico de For Time (COR-012).
 */
@Composable
fun ForTimeProtocolEditor(
    block: WorkoutBlock?,
    onUpdateTimeCap: (Int) -> Unit,
    onAddExercise: () -> Unit,
    onRemoveExercise: (Int) -> Unit,
    onUpdateReps: (Int, Int) -> Unit
) {
    val timeCapMinutes = ((block?.workDurationSeconds ?: 900) / 60)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCharcoal),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "CONFIGURAÇÃO FOR TIME",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SprintGreen
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Time Cap:", color = TextPrimary)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(10, 15, 20, 30).forEach { mins ->
                        FilterChip(
                            selected = timeCapMinutes == mins,
                            onClick = { onUpdateTimeCap(mins * 60) },
                            label = { Text("${mins} min") }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Tarefas a concluir:", color = TextSecondary, style = MaterialTheme.typography.labelMedium)

            val exercises = block?.exercises.orEmpty()
            exercises.forEachIndexed { exIdx, exercise ->
                val targetReps = exercise.plannedSets.firstOrNull()?.targetReps ?: 20
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${exIdx + 1}. ${exercise.displayName} ($targetReps reps)", color = TextPrimary)
                    IconButton(onClick = { onRemoveExercise(exIdx) }) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = CrimsonDanger)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onAddExercise,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = DeepVoid, contentColor = SprintGreen),
                border = BorderStroke(1.dp, SprintGreen),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.padding(4.dp))
                Text("Adicionar Exercício")
            }
        }
    }
}

/**
 * Editor específico de Alongamento (COR-012).
 */
@Composable
fun StretchProtocolEditor(
    block: WorkoutBlock?,
    onUpdateHold: (Int) -> Unit,
    onAddExercise: () -> Unit,
    onRemoveExercise: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCharcoal),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "CONFIGURAÇÃO DE ALONGAMENTO",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CyanPulse
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Sustentação:", color = TextPrimary)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(20, 30, 45, 60).forEach { s ->
                        FilterChip(
                            selected = (block?.workDurationSeconds ?: 30) == s,
                            onClick = { onUpdateHold(s) },
                            label = { Text("${s}s") }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Exercícios de Alongamento:", color = TextSecondary, style = MaterialTheme.typography.labelMedium)

            val exercises = block?.exercises.orEmpty()
            exercises.forEachIndexed { exIdx, exercise ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(exercise.displayName, color = TextPrimary)
                    IconButton(onClick = { onRemoveExercise(exIdx) }) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = CrimsonDanger)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onAddExercise,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = DeepVoid, contentColor = SprintGreen),
                border = BorderStroke(1.dp, SprintGreen),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.padding(4.dp))
                Text("Adicionar Alongamento")
            }
        }
    }
}

/**
 * Editor de bloco para Musculação / Strength / Hypertrophy (COR-001, COR-012).
 */
@Composable
fun StrengthBlockEditorCard(
    block: WorkoutBlock,
    onAddExercise: () -> Unit,
    onRemoveBlock: () -> Unit,
    onRemoveExercise: (Int) -> Unit,
    onAddSet: (Int) -> Unit,
    onRemoveSet: (Int, Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCharcoal),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = block.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                IconButton(onClick = onRemoveBlock) {
                    Icon(Icons.Default.Delete, contentDescription = "Remover Bloco", tint = CrimsonDanger)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            block.exercises.forEachIndexed { exIdx, exercise ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = DeepVoid),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = exercise.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            IconButton(onClick = { onRemoveExercise(exIdx) }) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = TextSecondary)
                            }
                        }

                        Text(
                            text = "${exercise.plannedSets.size} série(s) planejada(s)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = { onAddSet(exIdx) },
                                colors = ButtonDefaults.buttonColors(containerColor = CardBorder, contentColor = TextPrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("+ Série", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onAddExercise,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = CardBorder, contentColor = TextPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.padding(4.dp))
                Text("Adicionar Exercício ao Bloco")
            }
        }
    }
}
