package com.alan.routineos.feature.today.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.alan.routineos.core.designsystem.theme.RoutineTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddDialog(
    onConfirm: (String, Int?) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf("") }
    var isScheduleMode by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState()
    var showTimePicker by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = RoutineTheme.colors.surface1,
            border = androidx.compose.foundation.BorderStroke(1.dp, RoutineTheme.colors.border.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                // HEADER
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = null,
                            tint = RoutineTheme.colors.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Nueva Actividad",
                            style = RoutineTheme.typography.headlineMedium.copy(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = RoutineTheme.colors.onSurface
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, null, tint = RoutineTheme.colors.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // QUESTION
                Text(
                    text = "¿QUÉ VAS A HACER?",
                    style = RoutineTheme.typography.labelCaps.copy(
                        fontSize = 11.sp,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = RoutineTheme.colors.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // INPUT
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { 
                        Text(
                            "Ej: Meditación, Leer artículo...",
                            color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.5f)
                        ) 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = RoutineTheme.colors.primary,
                        unfocusedIndicatorColor = RoutineTheme.colors.border.copy(alpha = 0.3f),
                        cursorColor = RoutineTheme.colors.primary,
                        focusedTextColor = RoutineTheme.colors.onSurface,
                        unfocusedTextColor = RoutineTheme.colors.onSurface
                    ),
                    textStyle = RoutineTheme.typography.headlineMedium.copy(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Medium
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // CONTEXT & ENERGY (Design Placeholders)
                Text(
                    text = "CONTEXTO",
                    style = RoutineTheme.typography.labelCaps.copy(fontSize = 10.sp),
                    color = RoutineTheme.colors.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DesignChip("CASA")
                    DesignChip("TRABAJO")
                    DesignChip("GYM")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "ENERGÍA",
                    style = RoutineTheme.typography.labelCaps.copy(fontSize = 10.sp),
                    color = RoutineTheme.colors.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DesignChip("BAJA")
                    DesignChip("ALTA")
                }

                Spacer(modifier = Modifier.height(32.dp))

                // MODE SELECTOR
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .background(RoutineTheme.colors.surface2, RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    SelectorOption(
                        label = "AHORA MISMO",
                        isSelected = !isScheduleMode,
                        modifier = Modifier.weight(1f),
                        onClick = { isScheduleMode = false }
                    )
                    SelectorOption(
                        label = "PROGRAMAR",
                        isSelected = isScheduleMode,
                        modifier = Modifier.weight(1f),
                        onClick = { 
                            isScheduleMode = true
                            showTimePicker = true
                        }
                    )
                }

                if (isScheduleMode) {
                    Text(
                        text = "Hora: ${formatTime(timePickerState.hour, timePickerState.minute)}",
                        style = RoutineTheme.typography.dataLarge.copy(fontSize = 12.sp),
                        color = RoutineTheme.colors.primary,
                        modifier = Modifier.padding(top = 8.dp).clickable { showTimePicker = true }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // PRIMARY ACTION
                Button(
                    onClick = { 
                        val time = if (isScheduleMode) timePickerState.hour * 60 + timePickerState.minute else null
                        onConfirm(text, time) 
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RoutineTheme.colors.primary,
                        contentColor = Color.Black
                    ),
                    enabled = text.isNotBlank()
                ) {
                    Text(
                        "Añadir Actividad",
                        style = RoutineTheme.typography.bodyBase.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // CANCEL LINK
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "CANCELAR",
                        style = RoutineTheme.typography.labelCaps.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = RoutineTheme.colors.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (showTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("OK") }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }
}

@Composable
private fun DesignChip(label: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = RoutineTheme.colors.surface2,
        border = androidx.compose.foundation.BorderStroke(1.dp, RoutineTheme.colors.border.copy(alpha = 0.3f))
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = RoutineTheme.typography.labelCaps.copy(fontSize = 9.sp),
            color = RoutineTheme.colors.onSurfaceVariant
        )
    }
}

@Composable
private fun SelectorOption(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val containerColor = if (isSelected) RoutineTheme.colors.primary else Color.Transparent
    val contentColor = if (isSelected) Color.Black else RoutineTheme.colors.onSurfaceVariant
    
    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(containerColor, RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = RoutineTheme.typography.labelCaps.copy(
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            ),
            color = contentColor
        )
    }
}

private fun formatTime(h: Int, m: Int): String = "%02d:%02d".format(h, m)

@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        containerColor = containerColor,
        text = content
    )
}
