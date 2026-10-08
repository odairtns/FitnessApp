package com.aksoit.myfitnessapp.ui.screens.workouts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aksoit.myfitnessapp.domain.model.WorkoutTemplate
import com.aksoit.myfitnessapp.ui.components.ConfirmationDialog
import com.aksoit.myfitnessapp.ui.components.ProtocolBadge
import com.aksoit.myfitnessapp.ui.theme.CardBorder
import com.aksoit.myfitnessapp.ui.theme.CyanPulse
import com.aksoit.myfitnessapp.ui.theme.DarkCharcoal
import com.aksoit.myfitnessapp.ui.theme.DeepVoid
import com.aksoit.myfitnessapp.ui.theme.SprintGreen
import com.aksoit.myfitnessapp.ui.theme.TextPrimary
import com.aksoit.myfitnessapp.ui.theme.TextSecondary
import com.aksoit.myfitnessapp.ui.theme.WarningAmber

@Composable
fun WorkoutsListScreen(
    viewModel: WorkoutsViewModel,
    onStartWorkout: (Long) -> Unit,
    onEditTemplate: (Long) -> Unit,
    onCreateTemplate: () -> Unit,
    onExportXml: (String) -> Unit
) {
    val templates by viewModel.templates.collectAsState()
    var templateToDelete by remember { mutableStateOf<WorkoutTemplate?>(null) }

    Scaffold(
        containerColor = DeepVoid,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateTemplate,
                containerColor = SprintGreen,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Criar Treino")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Rotinas de Treino",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Selecione ou edite seus moldes de treino",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (templates.isEmpty()) {
                Text(
                    text = "Nenhum treino disponível. Toque no botão '+' abaixo para criar um.",
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 32.dp)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(templates) { template ->
                        TemplateItemRow(
                            template = template,
                            onStart = { onStartWorkout(template.id) },
                            onEdit = { onEditTemplate(template.id) },
                            onDuplicate = { viewModel.duplicateTemplate(template.id) },
                            onExport = { viewModel.exportTemplate(template.id, onExportXml) },
                            onDelete = { templateToDelete = template }
                        )
                    }
                }
            }
        }
    }

    templateToDelete?.let { template ->
        ConfirmationDialog(
            title = "Excluir Treino",
            message = "Deseja excluir o treino \"${template.name}\"? Se houver sessões já realizadas no histórico, ele será arquivado com segurança para não danificar o histórico.",
            confirmText = "Excluir / Arquivar",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteTemplate(template.id) {
                    templateToDelete = null
                }
            },
            onDismiss = { templateToDelete = null }
        )
    }
}

@Composable
fun TemplateItemRow(
    template: WorkoutTemplate,
    onStart: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = template.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    ProtocolBadge(
                        protocolText = template.protocol.name,
                        color = when (template.protocol.name) {
                            "AMRAP", "FOR_TIME", "EMOM" -> CyanPulse
                            "HIIT_INTERVALS" -> WarningAmber
                            else -> SprintGreen
                        },
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Opções", tint = TextSecondary)
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.background(DarkCharcoal)
                ) {
                    DropdownMenuItem(
                        text = { Text("Editar", color = TextPrimary) },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = TextPrimary) },
                        onClick = {
                            menuExpanded = false
                            onEdit()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Duplicar", color = TextPrimary) },
                        leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, tint = TextPrimary) },
                        onClick = {
                            menuExpanded = false
                            onDuplicate()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Exportar XML", color = TextPrimary) },
                        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, tint = TextPrimary) },
                        onClick = {
                            menuExpanded = false
                            onExport()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Excluir", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val totalExercises = template.blocks.sumOf { it.exercises.size }
            Text(
                text = "${template.blocks.size} blocos • $totalExercises exercícios",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = SprintGreen, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.padding(4.dp))
                Text("INICIAR", fontWeight = FontWeight.Bold)
            }
        }
    }
}
