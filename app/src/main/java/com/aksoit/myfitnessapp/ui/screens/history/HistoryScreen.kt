package com.aksoit.myfitnessapp.ui.screens.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aksoit.myfitnessapp.domain.model.SessionStatus
import com.aksoit.myfitnessapp.domain.model.WorkoutSessionSummary
import com.aksoit.myfitnessapp.ui.components.ProtocolBadge
import com.aksoit.myfitnessapp.ui.theme.CardBorder
import com.aksoit.myfitnessapp.ui.theme.CrimsonDanger
import com.aksoit.myfitnessapp.ui.theme.CyanPulse
import com.aksoit.myfitnessapp.ui.theme.DarkCharcoal
import com.aksoit.myfitnessapp.ui.theme.DeepVoid
import com.aksoit.myfitnessapp.ui.theme.SprintGreen
import com.aksoit.myfitnessapp.ui.theme.TextPrimary
import com.aksoit.myfitnessapp.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToPrs: () -> Unit
) {
    val sessions by viewModel.sessions.collectAsState()
    val dateFormat = SimpleDateFormat("dd/MM/yyyy • HH:mm", Locale.getDefault())

    Scaffold(
        containerColor = DeepVoid
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Histórico de Treinos",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Registros de sessões e progressão",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
                OutlinedButton(onClick = onNavigateToPrs) {
                    Text("Recordes (PR)", color = CyanPulse)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (sessions.isEmpty()) {
                Text(
                    text = "Nenhum treino realizado ainda. Complete um treino para ver o histórico aqui.",
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 32.dp)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(sessions) { session ->
                        SessionHistoryCard(
                            session = session,
                            formattedDate = dateFormat.format(Date(session.startedAtEpochMs)),
                            onClick = { onNavigateToDetail(session.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SessionHistoryCard(
    session: WorkoutSessionSummary,
    formattedDate: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                        text = session.templateNameSnapshot,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }

                val statusColor = when (session.status) {
                    SessionStatus.COMPLETED -> SprintGreen
                    SessionStatus.CANCELLED -> CrimsonDanger
                    else -> CyanPulse
                }
                ProtocolBadge(
                    protocolText = when (session.status) {
                        SessionStatus.COMPLETED -> "CONCLUÍDO"
                        SessionStatus.CANCELLED -> "CANCELADO"
                        else -> session.status.name
                    },
                    color = statusColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column {
                    Text("Volume Total", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                    Text(
                        text = "%.1f kg".format(session.totalVolumeKg).replace(',', '.'),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = SprintGreen
                    )
                }
                Column {
                    Text("Séries Válidas", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                    Text(
                        text = "${session.totalSets}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}
