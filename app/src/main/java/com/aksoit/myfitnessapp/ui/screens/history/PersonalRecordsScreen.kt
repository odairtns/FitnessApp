package com.aksoit.myfitnessapp.ui.screens.history

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aksoit.myfitnessapp.ui.theme.CardBorder
import com.aksoit.myfitnessapp.ui.theme.CyanPulse
import com.aksoit.myfitnessapp.ui.theme.DarkCharcoal
import com.aksoit.myfitnessapp.ui.theme.DeepVoid
import com.aksoit.myfitnessapp.ui.theme.SprintGreen
import com.aksoit.myfitnessapp.ui.theme.TextPrimary
import com.aksoit.myfitnessapp.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalRecordsScreen(
    viewModel: HistoryViewModel,
    onNavigateBack: () -> Unit
) {
    val prs by viewModel.personalRecords.collectAsState()

    Scaffold(
        containerColor = DeepVoid,
        topBar = {
            TopAppBar(
                title = { Text("Recordes Pessoais (PR)", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepVoid, titleContentColor = TextPrimary)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            if (prs.isEmpty()) {
                Text(
                    text = "Nenhum recorde registrado ainda. Complete séries nos seus treinos para computar seus PRs.",
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 32.dp)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(prs) { pr ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkCharcoal),
                            border = BorderStroke(1.dp, CardBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = pr.exerciseNameSnapshot,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    val typeLabel = when (pr.recordType) {
                                        com.aksoit.myfitnessapp.domain.model.PersonalRecordType.MAX_LOAD -> "Carga Máxima (MAX_LOAD)"
                                        com.aksoit.myfitnessapp.domain.model.PersonalRecordType.MAX_REPS -> "Repetições Máximas (MAX_REPS)"
                                        com.aksoit.myfitnessapp.domain.model.PersonalRecordType.MAX_VOLUME -> "Volume Máximo (MAX_VOLUME)"
                                        com.aksoit.myfitnessapp.domain.model.PersonalRecordType.BEST_TIME -> "Melhor Tempo (BEST_TIME)"
                                        com.aksoit.myfitnessapp.domain.model.PersonalRecordType.BEST_PROTOCOL_RESULT -> "Melhor Resultado"
                                    }
                                    Text(
                                        text = typeLabel,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = CyanPulse
                                    )
                                }

                                val formattedValue = when (pr.recordType) {
                                    com.aksoit.myfitnessapp.domain.model.PersonalRecordType.MAX_LOAD -> "%.1f kg".format(pr.value).replace(',', '.')
                                    com.aksoit.myfitnessapp.domain.model.PersonalRecordType.MAX_REPS -> "${pr.value.toInt()} reps"
                                    com.aksoit.myfitnessapp.domain.model.PersonalRecordType.MAX_VOLUME -> "%.1f kg".format(pr.value).replace(',', '.')
                                    com.aksoit.myfitnessapp.domain.model.PersonalRecordType.BEST_TIME -> {
                                        val totalSec = pr.value.toLong()
                                        "%02d:%02d".format(totalSec / 60, totalSec % 60)
                                    }
                                    com.aksoit.myfitnessapp.domain.model.PersonalRecordType.BEST_PROTOCOL_RESULT -> "%.1f".format(pr.value).replace(',', '.')
                                }

                                Text(
                                    text = formattedValue,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = SprintGreen
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
