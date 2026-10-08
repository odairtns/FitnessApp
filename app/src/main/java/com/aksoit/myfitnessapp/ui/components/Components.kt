package com.aksoit.myfitnessapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aksoit.myfitnessapp.ui.theme.CardBorder
import com.aksoit.myfitnessapp.ui.theme.CrimsonDanger
import com.aksoit.myfitnessapp.ui.theme.DarkCharcoal
import com.aksoit.myfitnessapp.ui.theme.SprintGreen
import com.aksoit.myfitnessapp.ui.theme.TextPrimary
import com.aksoit.myfitnessapp.ui.theme.TextSecondary

@Composable
fun LargeActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = SprintGreen,
    contentColor: Color = Color.Black,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = DarkCharcoal,
            disabledContentColor = TextSecondary
        ),
        enabled = enabled
    ) {
        Text(
            text = text,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun BigTimerDisplay(
    remainingSeconds: Long,
    modifier: Modifier = Modifier,
    color: Color = SprintGreen,
    label: String? = null
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!label.isNullOrBlank()) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        val mins = remainingSeconds / 60
        val secs = remainingSeconds % 60
        val textFormatted = String.format("%02d:%02d", mins, secs)
        Text(
            text = textFormatted,
            fontSize = 72.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
            color = color,
            letterSpacing = 2.sp
        )
    }
}

@Composable
fun StepperControl(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    step: Double = 1.0,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCharcoal),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, style = MaterialTheme.typography.labelLarge, color = TextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(
                    onClick = {
                        val curr = value.toDoubleOrNull() ?: 0.0
                        val next = (curr - step).coerceAtLeast(0.0)
                        onValueChange(if (next % 1.0 == 0.0) next.toLong().toString() else "%.1f".format(next).replace(',', '.'))
                    }
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Diminuir", tint = TextPrimary)
                }

                Text(
                    text = value.ifBlank { "0" },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                IconButton(
                    onClick = {
                        val curr = value.toDoubleOrNull() ?: 0.0
                        val next = curr + step
                        onValueChange(if (next % 1.0 == 0.0) next.toLong().toString() else "%.1f".format(next).replace(',', '.'))
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Aumentar", tint = TextPrimary)
                }
            }
        }
    }
}

@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmText: String = "Confirmar",
    cancelText: String = "Voltar",
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, fontWeight = FontWeight.Bold) },
        text = { Text(text = message, color = TextSecondary) },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDestructive) CrimsonDanger else SprintGreen,
                    contentColor = if (isDestructive) TextPrimary else Color.Black
                )
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(cancelText, color = TextSecondary)
            }
        },
        containerColor = DarkCharcoal,
        textContentColor = TextPrimary
    )
}

@Composable
fun ProtocolBadge(
    protocolText: String,
    modifier: Modifier = Modifier,
    color: Color = SprintGreen
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Text(
            text = protocolText,
            color = color,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
