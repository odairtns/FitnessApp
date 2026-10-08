package com.aksoit.myfitnessapp.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aksoit.myfitnessapp.domain.model.WorkoutTemplate
import com.aksoit.myfitnessapp.ui.components.LargeActionButton
import com.aksoit.myfitnessapp.ui.components.ProtocolBadge
import com.aksoit.myfitnessapp.ui.theme.CardBorder
import com.aksoit.myfitnessapp.ui.theme.CrimsonDanger
import com.aksoit.myfitnessapp.ui.theme.CyanPulse
import com.aksoit.myfitnessapp.ui.theme.DarkCharcoal
import com.aksoit.myfitnessapp.ui.theme.DeepVoid
import com.aksoit.myfitnessapp.ui.theme.SprintGreen
import com.aksoit.myfitnessapp.ui.theme.TextPrimary
import com.aksoit.myfitnessapp.ui.theme.TextSecondary
import com.aksoit.myfitnessapp.ui.theme.WarningAmber

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onStartWorkout: (Long) -> Unit,
    onNavigateToWorkouts: () -> Unit,
    onNavigateToPrs: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.checkRecoverableSession()
    }

    Scaffold(
        containerColor = DeepVoid
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
                Text(
                    text = "Fitness Tracker Pro",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Painel de Treinamento",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            // Banner de Recuperação de Sessão Interrompida
            state.recoverableSession?.let { recoverable ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = WarningAmber.copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, WarningAmber)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = WarningAmber)
                                Spacer(modifier = Modifier.padding(4.dp))
                                Text(
                                    text = "Treino Interrompido Encontrado",
                                    fontWeight = FontWeight.Bold,
                                    color = WarningAmber
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "O treino \"${recoverable.templateName}\" foi interrompido. Deseja retomar de onde parou?",
                                color = TextPrimary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onStartWorkout(recoverable.templateId ?: 0L) },
                                    colors = ButtonDefaults.buttonColors(containerColor = WarningAmber, contentColor = Color.Black),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Retomar")
                                }
                                OutlinedButton(
                                    onClick = { viewModel.discardRecoverableSession(recoverable.sessionId) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonDanger),
                                    border = BorderStroke(1.dp, CrimsonDanger),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Descartar")
                                }
                            }
                        }
                    }
                }
            }

            // Card de Volume Semanal
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCharcoal),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "VOLUME TOTAL DA SEMANA",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "%.1f".format(state.weeklyTonnageKg).replace(',', '.'),
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black,
                                color = SprintGreen
                            )
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text(
                                text = "kg levantados",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextSecondary,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                    }
                }
            }

            // Seção de Treinos Disponíveis
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Meus Treinos",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    OutlinedButton(onClick = onNavigateToWorkouts) {
                        Text("Ver Todos", color = SprintGreen)
                    }
                }
            }

            if (state.activeTemplates.isEmpty()) {
                item {
                    Text(
                        text = "Nenhum treino cadastrado. Crie um novo treino na aba 'Treinos'.",
                        color = TextSecondary,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            } else {
                items(state.activeTemplates.take(4)) { template ->
                    WorkoutTemplateCard(
                        template = template,
                        onStart = { onStartWorkout(template.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun WorkoutTemplateCard(
    template: WorkoutTemplate,
    onStart: () -> Unit
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
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = template.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    if (template.description.isNotBlank()) {
                        Text(
                            text = template.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            maxLines = 2
                        )
                    }
                }
                ProtocolBadge(
                    protocolText = template.protocol.name,
                    color = when (template.protocol.name) {
                        "AMRAP", "FOR_TIME", "EMOM" -> CyanPulse
                        "HIIT_INTERVALS" -> WarningAmber
                        else -> SprintGreen
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val totalExercises = template.blocks.sumOf { it.exercises.size }
            val totalBlocks = template.blocks.size
            Text(
                text = "$totalBlocks bloco(s) • $totalExercises exercício(s)",
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
                Text("INICIAR TREINO", fontWeight = FontWeight.Bold)
            }
        }
    }
}
