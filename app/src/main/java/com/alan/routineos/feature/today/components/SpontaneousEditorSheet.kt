package com.alan.routineos.feature.today.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.domain.model.HierarchicalTimelineEntry

private enum class PickingType { START, END }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpontaneousEditorSheet(
    entry: HierarchicalTimelineEntry,
    isCreationMode: Boolean = false,
    onSaveNew: () -> Unit = {},
    onUpdateTitle: (String, String) -> Unit,
    onUpdateSchedule: (String, Int?, Int?) -> Unit,
    onDelete: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Unified Time Picker Logic
    var pickingType by remember { mutableStateOf(PickingType.START) }
    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState()
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val root = entry.root.instance

    // Text field state with selection management to avoid cursor jumping bugs
    var titleState by remember(root.id) { 
        mutableStateOf(TextFieldValue(text = root.titleSnapshot, selection = TextRange(root.titleSnapshot.length))) 
    }

    // Sync from external state only if it changes independently (e.g. loading or reset)
    LaunchedEffect(root.titleSnapshot) {
        if (titleState.text != root.titleSnapshot) {
            titleState = titleState.copy(text = root.titleSnapshot, selection = TextRange(root.titleSnapshot.length))
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val minutes = timePickerState.hour * 60 + timePickerState.minute
                    
                    if (pickingType == PickingType.START) {
                        val currentEnd = root.plannedEndTime
                        // Clear end if new start is after it
                        val finalEnd = if (currentEnd != null && minutes >= currentEnd) null else currentEnd
                        onUpdateSchedule(root.id, minutes, finalEnd)
                        errorMessage = null
                        showTimePicker = false
                    } else {
                        val currentStart = root.plannedStartTime ?: 0
                        if (minutes > currentStart) {
                            onUpdateSchedule(root.id, currentStart, minutes)
                            errorMessage = null
                            showTimePicker = false
                        } else {
                            errorMessage = "La hora de fin debe ser posterior al inicio"
                        }
                    }
                }) { Text("Confirmar") }
            },
            dismissButton = { 
                TextButton(onClick = { 
                    showTimePicker = false 
                    errorMessage = null
                }) { Text("Cancelar") } 
            },
            text = { 
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TimePicker(state = timePickerState) 
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage!!,
                            color = RoutineTheme.colors.error,
                            style = RoutineTheme.typography.labelCaps.copy(fontSize = 10.sp)
                        )
                    }
                }
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = RoutineTheme.colors.surface1,
        dragHandle = { BottomSheetDefaults.DragHandle(color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.4f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(RoutineTheme.spacing.lg)
                .navigationBarsPadding()
        ) {
            // Root Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FlashOn, null, tint = Color(0xFFB894E6))
                Spacer(modifier = Modifier.width(12.dp))
                TextField(
                    value = titleState,
                    onValueChange = { 
                        titleState = it
                        onUpdateTitle(root.id, it.text) 
                    },
                    placeholder = { Text("Título del evento") },
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent
                    ),
                    textStyle = RoutineTheme.typography.headlineMedium.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { onDelete(root.id) }) {
                    Icon(Icons.Default.Delete, "Eliminar", tint = RoutineTheme.colors.error)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SchedulePickerRow(
                startTime = root.plannedStartTime,
                endTime = root.plannedEndTime,
                onPickStart = { 
                    pickingType = PickingType.START
                    showTimePicker = true
                },
                onPickEnd = {
                    pickingType = PickingType.END
                    showTimePicker = true
                },
                onClear = { onUpdateSchedule(root.id, null, null) }
            )

            if (isCreationMode) {
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = onSaveNew,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoutineTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(containerColor = RoutineTheme.colors.primary, contentColor = Color.Black),
                    enabled = root.titleSnapshot.isNotBlank()
                ) {
                    Text("Programar Evento", style = RoutineTheme.typography.labelCaps)
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun SchedulePickerRow(
    startTime: Int?,
    endTime: Int?,
    onPickStart: () -> Unit,
    onPickEnd: () -> Unit,
    onClear: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Schedule, null, tint = RoutineTheme.colors.onSurfaceVariant, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        
        TextButton(onClick = onPickStart) {
            Text(startTime?.let { formatMinutes(it) } ?: "Hora Inicio", style = RoutineTheme.typography.dataLarge)
        }
        
        Text("-", color = RoutineTheme.colors.onSurfaceVariant)
        
        TextButton(onClick = onPickEnd) {
            Text(endTime?.let { formatMinutes(it) } ?: "Hora Fin", style = RoutineTheme.typography.dataLarge)
        }

        Spacer(modifier = Modifier.weight(1f))

        if (startTime != null || endTime != null) {
            IconButton(onClick = onClear) {
                Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

private fun formatMinutes(minutes: Int): String {
    val h = (minutes / 60) % 24
    val m = minutes % 60
    return "%02d:%02d".format(h, m)
}
