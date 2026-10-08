package com.aksoit.myfitnessapp.ui.screens.workouts

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import com.aksoit.myfitnessapp.domain.model.WorkoutBlock
import com.aksoit.myfitnessapp.ui.components.LargeActionButton
import com.aksoit.myfitnessapp.ui.theme.CardBorder
import com.aksoit.myfitnessapp.ui.theme.CrimsonDanger
import com.aksoit.myfitnessapp.ui.theme.DarkCharcoal
import com.aksoit.myfitnessapp.ui.theme.DeepVoid
import com.aksoit.myfitnessapp.ui.theme.SprintGreen
import com.aksoit.myfitnessapp.ui.theme.TextPrimary
import com.aksoit.myfitnessapp.ui.theme.TextSecondary

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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = TextPrimary)
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
            item {
                OutlinedTextField(
                    value = state.name,
                    onValueChange = { viewModel.updateName(it) },
                    label = { Text("Nome da Rotina") },
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

            itemsIndexed(state.blocks) { blockIdx, block ->
                BlockEditorCard(
                    block = block,
                    onAddExercise = { onSelectExercise(blockIdx) },
                    onRemoveBlock = { viewModel.removeBlock(blockIdx) },
                    onRemoveExercise = { exIdx -> viewModel.removeExercise(blockIdx, exIdx) },
                    onAddSet = { exIdx -> viewModel.addSet(blockIdx, exIdx) }
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
                    Text("Adicionar Bloco", color = SprintGreen, fontWeight = FontWeight.Bold)
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

@Composable
fun BlockEditorCard(
    block: WorkoutBlock,
    onAddExercise: () -> Unit,
    onRemoveBlock: () -> Unit,
    onRemoveExercise: (Int) -> Unit,
    onAddSet: (Int) -> Unit
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
