package com.aksoit.myfitnessapp.ui.screens.tools

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aksoit.myfitnessapp.ui.components.LargeActionButton
import com.aksoit.myfitnessapp.ui.theme.CardBorder
import com.aksoit.myfitnessapp.ui.theme.CyanPulse
import com.aksoit.myfitnessapp.ui.theme.DarkCharcoal
import com.aksoit.myfitnessapp.ui.theme.DeepVoid
import com.aksoit.myfitnessapp.ui.theme.SprintGreen
import com.aksoit.myfitnessapp.ui.theme.TextPrimary
import com.aksoit.myfitnessapp.ui.theme.TextSecondary
import com.aksoit.myfitnessapp.ui.theme.WarningAmber

@Composable
fun ToolsScreen(
    viewModel: ToolsViewModel
) {
    val state by viewModel.uiState.collectAsState()
    var xmlInput by remember { mutableStateOf("") }

    Scaffold(
        containerColor = DeepVoid
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Ferramentas Avulsas",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Timers independentes e intercâmbio de dados XML",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            // Seleção de Ferramenta
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = state.timerMode == ToolMode.STOPWATCH,
                        onClick = { viewModel.setMode(ToolMode.STOPWATCH) },
                        label = { Text("Cronômetro") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SprintGreen,
                            selectedLabelColor = Color.Black
                        )
                    )
                    FilterChip(
                        selected = state.timerMode == ToolMode.COUNTDOWN,
                        onClick = { viewModel.setMode(ToolMode.COUNTDOWN) },
                        label = { Text("Timer") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyanPulse,
                            selectedLabelColor = Color.Black
                        )
                    )
                    FilterChip(
                        selected = state.timerMode == ToolMode.XML_EXCHANGE,
                        onClick = { viewModel.setMode(ToolMode.XML_EXCHANGE) },
                        label = { Text("XML Import") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = WarningAmber,
                            selectedLabelColor = Color.Black
                        )
                    )
                }
            }

            if (state.timerMode == ToolMode.STOPWATCH || state.timerMode == ToolMode.COUNTDOWN) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCharcoal),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (state.timerMode == ToolMode.STOPWATCH) "CRONÔMETRO" else "CONTAGEM REGRESSIVA",
                                style = MaterialTheme.typography.labelLarge,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = state.formattedTime,
                                fontSize = 64.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = if (state.timerMode == ToolMode.STOPWATCH) SprintGreen else CyanPulse
                            )
                            Spacer(modifier = Modifier.height(24.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (!state.isRunning && !state.isPaused) {
                                    Button(
                                        onClick = {
                                            if (state.timerMode == ToolMode.STOPWATCH) viewModel.startStopwatch()
                                            else viewModel.startCountdown(5)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SprintGreen, contentColor = Color.Black),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("INICIAR", fontWeight = FontWeight.Bold)
                                    }
                                } else if (state.isRunning) {
                                    Button(
                                        onClick = { viewModel.pause() },
                                        colors = ButtonDefaults.buttonColors(containerColor = WarningAmber, contentColor = Color.Black),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("PAUSAR", fontWeight = FontWeight.Bold)
                                    }
                                } else if (state.isPaused) {
                                    Button(
                                        onClick = { viewModel.resume() },
                                        colors = ButtonDefaults.buttonColors(containerColor = SprintGreen, contentColor = Color.Black),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("CONTINUAR", fontWeight = FontWeight.Bold)
                                    }
                                }

                                OutlinedButton(
                                    onClick = { viewModel.reset() },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                                    border = BorderStroke(1.dp, CardBorder),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("ZERAR")
                                }
                            }
                        }
                    }
                }
            } else {
                // Modo XML EXCHANGE
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCharcoal),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Importar Rotina de Treino (XML)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Cole o código XML abaixo para validar e visualizar os exercícios antes de importar:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = xmlInput,
                                onValueChange = { xmlInput = it },
                                placeholder = { Text("Cole o XML aqui...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedBorderColor = SprintGreen,
                                    unfocusedBorderColor = CardBorder
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = { viewModel.previewXml(xmlInput) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = WarningAmber, contentColor = Color.Black)
                            ) {
                                Text("VALIDAR E ANALISAR XML", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                state.importPreview?.let { preview ->
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkCharcoal),
                            border = BorderStroke(1.dp, CardBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Preview do Treino: ${preview.template.name}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SprintGreen
                                )
                                Text(
                                    text = "Modalidade: ${preview.template.modality.name} • Protocolo: ${preview.template.protocol.name}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Exercícios encontrados: ${preview.exerciseMatches.size}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = TextPrimary
                                )
                                preview.exerciseMatches.forEach { match ->
                                    val status = if (match.isExactStableKeyMatch) "Vinculado (Chave)"
                                    else if (match.isNormalizedNameMatch) "Vinculado (Nome)"
                                    else "Novo (Custom)"
                                    Text(
                                        text = "• ${match.xmlExerciseName} -> $status",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (match.matchedExercise != null) SprintGreen else WarningAmber
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.confirmImport(replaceExisting = false) },
                                        colors = ButtonDefaults.buttonColors(containerColor = SprintGreen, contentColor = Color.Black),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Importar como Novo")
                                    }
                                    if (preview.isDuplicateUuid) {
                                        Button(
                                            onClick = { viewModel.confirmImport(replaceExisting = true) },
                                            colors = ButtonDefaults.buttonColors(containerColor = WarningAmber, contentColor = Color.Black),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Substituir Existente")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                state.importSuccessMessage?.let { msg ->
                    item {
                        Text(text = msg, color = SprintGreen, fontWeight = FontWeight.Bold)
                    }
                }

                state.importErrorMessage?.let { err ->
                    item {
                        Text(text = err, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
