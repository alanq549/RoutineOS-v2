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
import com.alan.routineos.feature.planning.EditorRole

private enum class PickingType { START, END }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpontaneousEditorSheet(
    entry: HierarchicalTimelineEntry,
    role: EditorRole = EditorRole.ACTIVITY,
    isCreationMode: Boolean = false,
    onSaveNew: () -> Unit = {},
    onUpdateTitle: (String, String) -> Unit,
    onUpdateSchedule: (String, Int?, Int?) -> Unit,
    onDelete: (String) -> Unit,
    onUpdateRole: (EditorRole) -> Unit = {},
    onAddDraftTask: (String) -> Unit = {},
    onRemoveDraftTask: (String) -> Unit = {},
    onUpdateDraftNote: (String) -> Unit = {},
    onUpdateDraftReminder: (Int?, Int?) -> Unit = { _, _ -> },
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

    // Context UI State
    var newTaskTitle by remember { mutableStateOf("") }
    var noteState by remember(entry.note?.id) { mutableStateOf(entry.note?.content ?: "") }
    var showRoleMenu by remember { mutableStateOf(false) }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val minutes = timePickerState.hour * 60 + timePickerState.minute
                    
                    if (pickingType == PickingType.START) {
                        val currentEnd = root.plannedEndTime
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
                Box {
                    IconButton(onClick = { showRoleMenu = true }) {
                        Icon(
                            imageVector = when (role) {
                                EditorRole.ACTIVITY -> Icons.Default.FlashOn
                                EditorRole.TASK -> Icons.Default.CheckBox
                                EditorRole.REMINDER -> Icons.Default.Notifications
                            },
                            contentDescription = "Tipo de entrada",
                            tint = when (role) {
                                EditorRole.ACTIVITY -> Color(0xFFB894E6)
                                EditorRole.TASK -> RoutineTheme.colors.primary
                                EditorRole.REMINDER -> RoutineTheme.colors.secondary
                            }
                        )
                    }
                    DropdownMenu(
                        expanded = showRoleMenu,
                        onDismissRequest = { showRoleMenu = false },
                        containerColor = RoutineTheme.colors.surface2
                    ) {
                        DropdownMenuItem(
                            text = { Text("Actividad") },
                            onClick = { onUpdateRole(EditorRole.ACTIVITY); showRoleMenu = false },
                            leadingIcon = { Icon(Icons.Default.FlashOn, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Tarea") },
                            onClick = { onUpdateRole(EditorRole.TASK); showRoleMenu = false },
                            leadingIcon = { Icon(Icons.Default.CheckBox, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Recordatorio") },
                            onClick = { onUpdateRole(EditorRole.REMINDER); showRoleMenu = false },
                            leadingIcon = { Icon(Icons.Default.Notifications, null) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                TextField(
                    value = titleState,
                    onValueChange = { 
                        titleState = it
                        onUpdateTitle(root.id, it.text) 
                    },
                    placeholder = { 
                        Text(when(role) {
                            EditorRole.ACTIVITY -> "Título del evento"
                            EditorRole.TASK -> "Título de la tarea"
                            EditorRole.REMINDER -> "Título del aviso"
                        }) 
                    },
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
                endTime = if (role == EditorRole.ACTIVITY) root.plannedEndTime else null,
                onPickStart = { 
                    pickingType = PickingType.START
                    showTimePicker = true
                },
                onPickEnd = {
                    pickingType = PickingType.END
                    showTimePicker = true
                },
                onClear = { onUpdateSchedule(root.id, null, null) },
                hideEnd = role != EditorRole.ACTIVITY
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.2f))
            
            // CONTEXT SECTION
            if (role == EditorRole.ACTIVITY) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "CONTEXTO",
                    style = RoutineTheme.typography.labelCaps,
                    color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Tasks
                entry.associatedItems.forEach { task ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.RadioButtonUnchecked, null, modifier = Modifier.size(16.dp), tint = RoutineTheme.colors.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(task.root.instance.titleSnapshot, style = RoutineTheme.typography.bodyBase)
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = { onRemoveDraftTask(task.root.instance.id) }) {
                            Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp))
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp), tint = RoutineTheme.colors.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    TextField(
                        value = newTaskTitle,
                        onValueChange = { newTaskTitle = it },
                        placeholder = { Text("Añadir tarea rápida...", fontSize = 14.sp) },
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = RoutineTheme.typography.bodyBase
                    )
                    if (newTaskTitle.isNotBlank()) {
                        IconButton(onClick = { 
                            onAddDraftTask(newTaskTitle)
                            newTaskTitle = ""
                        }) {
                            Icon(Icons.Default.Check, null, tint = RoutineTheme.colors.primary)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (role != EditorRole.REMINDER) {
                if (role == EditorRole.ACTIVITY) {
                    // Reminder
                    ReminderSelector(
                        abs = root.reminderAbs,
                        rel = root.reminderRel,
                        onUpdate = onUpdateDraftReminder
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Note
                TextField(
                    value = noteState,
                    onValueChange = { 
                        noteState = it
                        onUpdateDraftNote(it)
                    },
                    placeholder = { Text("Añadir notas...", fontSize = 14.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = RoutineTheme.colors.surface2,
                        focusedContainerColor = RoutineTheme.colors.surface2,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoutineTheme.shapes.small,
                    minLines = 3
                )
            } else {
                // In REMINDER mode, we only show absolute time hint if not set
                if (root.reminderAbs == null) {
                    Text(
                        "Define una hora para el recordatorio.",
                        style = RoutineTheme.typography.bodyBase,
                        color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }

            if (isCreationMode) {
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = onSaveNew,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoutineTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(containerColor = RoutineTheme.colors.primary, contentColor = Color.Black),
                    enabled = root.titleSnapshot.isNotBlank() && (role != EditorRole.REMINDER || root.plannedStartTime != null)
                ) {
                    Text(
                        text = when(role) {
                            EditorRole.ACTIVITY -> "Programar Evento"
                            EditorRole.TASK -> "Crear Tarea"
                            EditorRole.REMINDER -> "Crear Recordatorio"
                        },
                        style = RoutineTheme.typography.labelCaps
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun ReminderSelector(
    abs: Int?,
    rel: Int?,
    onUpdate: (Int?, Int?) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Notifications, null, tint = RoutineTheme.colors.onSurfaceVariant, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(12.dp))
        
        AssistChip(
            onClick = { onUpdate(null, 10) }, // Simplified for MVP: relative 10 min
            label = { Text("10 min antes") },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = if (rel != null) RoutineTheme.colors.primary.copy(alpha = 0.2f) else Color.Transparent,
                labelColor = if (rel != null) RoutineTheme.colors.primary else RoutineTheme.colors.onSurfaceVariant
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        AssistChip(
            onClick = { onUpdate(540, null) }, // Simplified for MVP: Fixed 09:00
            label = { Text("09:00") },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = if (abs != null) RoutineTheme.colors.primary.copy(alpha = 0.2f) else Color.Transparent,
                labelColor = if (abs != null) RoutineTheme.colors.primary else RoutineTheme.colors.onSurfaceVariant
            )
        )
        
        if (abs != null || rel != null) {
            IconButton(onClick = { onUpdate(null, null) }) {
                Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun SchedulePickerRow(
    startTime: Int?,
    endTime: Int?,
    onPickStart: () -> Unit,
    onPickEnd: () -> Unit,
    onClear: () -> Unit,
    hideEnd: Boolean = false
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Schedule, null, tint = RoutineTheme.colors.onSurfaceVariant, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        
        TextButton(onClick = onPickStart) {
            Text(startTime?.let { formatMinutes(it) } ?: "Hora Inicio", style = RoutineTheme.typography.dataLarge)
        }
        
        if (!hideEnd) {
            Text("-", color = RoutineTheme.colors.onSurfaceVariant)
            
            TextButton(onClick = onPickEnd) {
                Text(endTime?.let { formatMinutes(it) } ?: "Hora Fin", style = RoutineTheme.typography.dataLarge)
            }
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
