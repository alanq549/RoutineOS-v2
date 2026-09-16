package com.alan.routineos.feature.today.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.domain.model.HierarchicalTimelineEntry
import com.alan.routineos.domain.model.ScheduleTarget
import com.alan.routineos.feature.planning.EditorRole
import com.alan.routineos.feature.planning.model.SearchTargetUiModel
import com.alan.routineos.feature.planning.model.UnifiedLinkingResult
import com.alan.routineos.feature.today.model.TodayTimelineUiModel

private enum class PickingType { START, END }

private val DarkSurface = Color(0xFF0D1017)
private val DarkCard = Color(0xFF171B26)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpontaneousEditorSheet(
    entry: HierarchicalTimelineEntry,
    role: EditorRole = EditorRole.EVENT,
    isCreationMode: Boolean = false,
    onSaveNew: () -> Unit = {},
    onUpdateTitle: (String, String) -> Unit,
    onUpdateSchedule: (String, Int?, Int?) -> Unit,
    onDelete: (String) -> Unit,
    onUpdateRole: (EditorRole) -> Unit = {},
    onUpdateCatalogSearch: (String) -> Unit = {},
    onLinkToDefinition: (SearchTargetUiModel?) -> Unit = {},
    onLinkToOccurrence: (TodayTimelineUiModel?) -> Unit = {},
    onSelectUnifiedResult: (UnifiedLinkingResult) -> Unit = {},
    onAddDraftTask: (String) -> Unit = {},
    onRemoveDraftTask: (String) -> Unit = {},
    onUpdateDraftNote: (String) -> Unit = {},
    onUpdateDraftReminder: (Int?, Int?) -> Unit = { _, _ -> },
    onSetTimeToNow: (String) -> Unit = {},
    onDismiss: () -> Unit,
    unifiedCatalog: List<UnifiedLinkingResult> = emptyList(),
    catalogSearchQuery: String = "",
    selectedDefinition: SearchTargetUiModel? = null,
    selectedOccurrence: TodayTimelineUiModel? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var pickingType by remember { mutableStateOf(PickingType.START) }
    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState()
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val root = entry.root.instance
    var titleState by remember(root.id) { 
        mutableStateOf(TextFieldValue(text = root.titleSnapshot, selection = TextRange(root.titleSnapshot.length))) 
    }

    LaunchedEffect(root.titleSnapshot) {
        if (titleState.text != root.titleSnapshot) {
            titleState = titleState.copy(text = root.titleSnapshot, selection = TextRange(root.titleSnapshot.length))
        }
    }

    var newTaskTitle by remember { mutableStateOf("") }
    
    // NOTE STATE with TextFieldValue for cursor stability and auto-bullets
    var noteState by remember(entry.note?.id) { 
        val content = entry.note?.content ?: ""
        mutableStateOf(TextFieldValue(text = content, selection = TextRange(content.length))) 
    }

    // Fix Catalog Search cursor bug using local TextFieldValue
    var searchQueryState by remember { 
        mutableStateOf(TextFieldValue(text = catalogSearchQuery, selection = TextRange(catalogSearchQuery.length))) 
    }

    // Sync search query if changed externally (e.g. clear)
    LaunchedEffect(catalogSearchQuery) {
        if (searchQueryState.text != catalogSearchQuery) {
            searchQueryState = searchQueryState.copy(text = catalogSearchQuery, selection = TextRange(catalogSearchQuery.length))
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
                .padding(horizontal = RoutineTheme.spacing.lg)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
        ) {
            // HEADER: Segmented Role Selector + Discard
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SegmentedRoleSelector(
                    selectedRole = role,
                    onRoleSelected = onUpdateRole
                )

                IconButton(
                    onClick = { onDelete(root.id) },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(RoutineTheme.colors.surface2)
                ) {
                    Icon(Icons.Default.Delete, "Eliminar", tint = RoutineTheme.colors.error.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

    // TITLE INPUT GROUP
    val accentColor = when (role) {
        EditorRole.TASK -> RoutineTheme.colors.roleTask
        EditorRole.EVENT -> RoutineTheme.colors.roleEvent
        EditorRole.REMINDER -> RoutineTheme.colors.roleReminder
    }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkSurface)
                    .border(1.dp, RoutineTheme.colors.border.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (role) {
                            EditorRole.EVENT -> Icons.Default.Event
                            EditorRole.REMINDER -> Icons.Default.Notifications
                            EditorRole.TASK -> Icons.Default.CheckCircle
                        },
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                TextField(
                    value = titleState,
                    onValueChange = { 
                        titleState = it
                        onUpdateTitle(root.id, it.text) 
                    },
                    placeholder = { 
                        Text(
                            text = when(role) {
                                EditorRole.TASK -> "Título de la tarea..."
                                EditorRole.EVENT -> "Título del evento futuro..."
                                EditorRole.REMINDER -> "Título del recordatorio..."
                            },
                            fontSize = 15.sp,
                            color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.5f)
                        ) 
                    },
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent
                    ),
                    textStyle = RoutineTheme.typography.bodyBase.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.weight(1f)
                )
            }

            // UNIFIED LINKING SELECTOR (Always Visible)
            Column(modifier = Modifier.padding(top = 16.dp)) {
                // Selection Chips
                if (selectedDefinition != null || selectedOccurrence != null) {
                    FlowRow(
                        modifier = Modifier.padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        selectedDefinition?.let {
                            LinkChip(
                                title = it.title,
                                subtitle = it.subtitle ?: "Actividad",
                                icon = if (it.target is ScheduleTarget.Node) Icons.Default.AccountTree else Icons.Default.Tag,
                                accentColor = accentColor,
                                onClear = { onLinkToDefinition(null) }
                            )
                        }
                        selectedOccurrence?.let {
                            LinkChip(
                                title = it.title,
                                subtitle = it.timeRangeText,
                                icon = Icons.Default.Event,
                                accentColor = accentColor,
                                onClear = { onLinkToOccurrence(null) }
                            )
                        }
                    }
                }

                // Unified Input
                TextField(
                    value = searchQueryState,
                    onValueChange = { 
                        searchQueryState = it
                        onUpdateCatalogSearch(it.text) 
                    },
                    placeholder = { Text("Relacionado con...", fontSize = 13.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurface),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent
                    ),
                    leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(16.dp), tint = RoutineTheme.colors.onSurfaceVariant) },
                    textStyle = RoutineTheme.typography.bodyBase.copy(fontSize = 13.sp),
                    singleLine = true
                )
                
                if (unifiedCatalog.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = DarkSurface,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RoutineTheme.colors.border.copy(alpha = 0.1f)),
                        modifier = Modifier.fillMaxWidth().heightIn(max = 240.dp)
                    ) {
                        LazyColumn(contentPadding = PaddingValues(4.dp)) {
                            // 1. ACTIVITIES / STRUCTURE
                            val semanticItems = unifiedCatalog.filter { it is UnifiedLinkingResult.SemanticDefinition || it is UnifiedLinkingResult.SemanticNode }
                            if (semanticItems.isNotEmpty()) {
                                item { ResultHeader("ACTIVIDADES / ESTRUCTURA") }
                                items(semanticItems) { item -> UnifiedResultRow(item, accentColor) { onSelectUnifiedResult(it); searchQueryState = TextFieldValue("") } }
                            }

                            // 2. OCCURRENCES
                            val occurrencesList = unifiedCatalog.filterIsInstance<UnifiedLinkingResult.ContextualOccurrence>()
                            if (occurrencesList.isNotEmpty()) {
                                item { ResultHeader("OCURRENCIAS") }
                                items(occurrencesList) { item -> UnifiedResultRow(item, accentColor) { onSelectUnifiedResult(it); searchQueryState = TextFieldValue("") } }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // TIME RANGE PICKER
            TimeRangePicker(
                startTime = root.plannedStartTime,
                endTime = root.plannedEndTime,
                accentColor = accentColor,
                onPickStart = { pickingType = PickingType.START; showTimePicker = true },
                onPickEnd = { pickingType = PickingType.END; showTimePicker = true },
                onSetTimeToNow = { onSetTimeToNow(root.id) },
                onClearTime = { onUpdateSchedule(root.id, null, null) },
                hideEnd = role != EditorRole.EVENT
            )

            // DYNAMIC SECTIONS: Sub-tasks (EVENT only)
            AnimatedVisibility(visible = role == EditorRole.EVENT) {
                Column {
                    Spacer(modifier = Modifier.height(24.dp))
                    SectionHeader("Contexto & Tarea Ligada")
                    
                    // Task List
                    entry.associatedItems.forEach { task ->
                        AssociatedTaskItem(
                            title = task.root.instance.titleSnapshot,
                            onRemove = { onRemoveDraftTask(task.root.instance.id) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Add Task Input
                    QuickAddTaskInput(
                        value = newTaskTitle,
                        onValueChange = { newTaskTitle = it },
                        onAdd = { 
                            onAddDraftTask(newTaskTitle)
                            newTaskTitle = "" 
                        },
                        accentColor = accentColor
                    )
                }
            }

            // DYNAMIC SECTIONS: Notes (TASK only)
            if (role == EditorRole.TASK) {
                Spacer(modifier = Modifier.height(24.dp))
                SectionHeader("Notas Rápidas")
                
                TextField(
                    value = noteState,
                    onValueChange = { newValue ->
                        val oldText = noteState.text
                        val newText = newValue.text
                        
                        val processedValue = when {
                            // 1. Convert "- " to "• " at start of any line
                            newText.length > oldText.length && newText.endsWith("- ") -> {
                                val selectionIndex = newValue.selection.end
                                val lineStart = newText.substring(0, selectionIndex).lastIndexOf('\n') + 1
                                if (newText.substring(lineStart, selectionIndex) == "- ") {
                                    val prefix = newText.substring(0, lineStart)
                                    val suffix = newText.substring(selectionIndex)
                                    val lineContent = "• "
                                    newValue.copy(
                                        text = prefix + lineContent + suffix,
                                        selection = TextRange(prefix.length + lineContent.length)
                                    )
                                } else newValue
                            }
                            // 2. Auto-bullet on Enter after a bullet line
                            newText.length > oldText.length && newText.length >= 1 && newText[newValue.selection.end - 1] == '\n' -> {
                                val selectionIndex = newValue.selection.end
                                val beforeCursor = newText.substring(0, selectionIndex - 1)
                                val lastNewlineBefore = beforeCursor.lastIndexOf('\n')
                                val lastLine = if (lastNewlineBefore == -1) beforeCursor else beforeCursor.substring(lastNewlineBefore + 1)
                                
                                if (lastLine.trimStart().startsWith("• ")) {
                                    val indent = lastLine.substringBefore("• ")
                                    val bullet = "$indent• "
                                    val prefix = newText.substring(0, selectionIndex)
                                    val suffix = newText.substring(selectionIndex)
                                    newValue.copy(
                                        text = prefix + bullet + suffix,
                                        selection = TextRange(prefix.length + bullet.length)
                                    )
                                } else newValue
                            }
                            else -> newValue
                        }
                        
                        noteState = processedValue
                        onUpdateDraftNote(processedValue.text)
                    },
                    placeholder = { Text("Añadir notas rápidas...", fontSize = 13.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurface)
                        .border(1.dp, RoutineTheme.colors.border.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent
                    ),
                    textStyle = RoutineTheme.typography.bodyBase.copy(fontSize = 13.sp),
                    minLines = 3
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // PRIMARY ACTION BUTTON
            val buttonColor = when (role) {
                EditorRole.EVENT -> RoutineTheme.colors.roleEvent
                EditorRole.TASK -> RoutineTheme.colors.roleTask
                EditorRole.REMINDER -> RoutineTheme.colors.roleReminder
            }
            
            Button(
                onClick = onSaveNew,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCreationMode) buttonColor else RoutineTheme.colors.primary.copy(alpha = 0.2f),
                    contentColor = if (isCreationMode) {
                        if (role == EditorRole.TASK) Color.White else Color.Black
                    } else RoutineTheme.colors.primary,
                    disabledContainerColor = RoutineTheme.colors.surface2,
                    disabledContentColor = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.4f)
                ),
                border = if (!isCreationMode) BorderStroke(1.dp, RoutineTheme.colors.primary.copy(alpha = 0.5f)) else null,
                enabled = root.titleSnapshot.isNotBlank()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Icon(
                        imageVector = if (isCreationMode) {
                            when(role) {
                                EditorRole.TASK -> Icons.Default.Check
                                EditorRole.EVENT -> Icons.Default.Event
                                EditorRole.REMINDER -> Icons.Default.Notifications
                            }
                        } else Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isCreationMode) {
                            when(role) {
                                EditorRole.TASK -> "Crear Tarea"
                                EditorRole.EVENT -> "Programar Evento"
                                EditorRole.REMINDER -> "Crear Recordatorio"
                            }
                        } else "Guardar Cambios",
                        style = RoutineTheme.typography.labelCaps.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
private fun SegmentedRoleSelector(
    selectedRole: EditorRole,
    onRoleSelected: (EditorRole) -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0B0E14))
            .border(1.dp, Color(0xFF1E2332), RoundedCornerShape(14.dp))
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RoleTab(
            label = "Tarea",
            icon = Icons.Default.Check,
            isSelected = selectedRole == EditorRole.TASK,
            onClick = { onRoleSelected(EditorRole.TASK) },
            activeColor = RoutineTheme.colors.roleTask
        )
        RoleTab(
            label = "Evento",
            icon = Icons.Default.Event,
            isSelected = selectedRole == EditorRole.EVENT,
            onClick = { onRoleSelected(EditorRole.EVENT) },
            activeColor = RoutineTheme.colors.roleEvent
        )
        RoleTab(
            label = "Recordatorio",
            icon = Icons.Default.Notifications,
            isSelected = selectedRole == EditorRole.REMINDER,
            onClick = { onRoleSelected(EditorRole.REMINDER) },
            activeColor = RoutineTheme.colors.roleReminder
        )
    }
}

@Composable
private fun RoleTab(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    activeColor: Color
) {
    val containerColor = if (isSelected) activeColor.copy(alpha = 0.15f) else Color.Transparent
    val contentColor = if (isSelected) activeColor else RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.6f)
    val borderModifier = if (isSelected) Modifier.border(1.dp, activeColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp)) else Modifier

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(containerColor)
            .then(borderModifier)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(icon, null, modifier = Modifier.size(14.dp), tint = contentColor)
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = RoutineTheme.typography.labelCaps.copy(
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            ),
            color = contentColor
        )
    }
}

@Composable
private fun TimeRangePicker(
    startTime: Int?,
    endTime: Int?,
    accentColor: Color,
    onPickStart: () -> Unit,
    onPickEnd: () -> Unit,
    onSetTimeToNow: () -> Unit,
    onClearTime: () -> Unit,
    hideEnd: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DarkCard)
            .border(1.dp, RoutineTheme.colors.border.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Schedule, null, tint = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Rango Temporal", style = RoutineTheme.typography.labelCaps.copy(fontSize = 10.sp), color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.8f))
            }
            
            if (startTime != null || endTime != null) {
                TextButton(
                    onClick = onClearTime,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    modifier = Modifier.height(24.dp)
                ) {
                    Text(
                        "Limpiar hora",
                        style = RoutineTheme.typography.labelCaps.copy(fontSize = 9.sp, color = RoutineTheme.colors.error)
                    )
                }
            }
        }

        if (startTime != null && endTime != null && !hideEnd) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = accentColor.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
            ) {
                Text(
                    text = "Duración: ${endTime - startTime} min",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = RoutineTheme.typography.labelCaps.copy(fontSize = 9.sp, color = accentColor, fontWeight = FontWeight.Bold)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TimeChip(
                label = "Hora Inicio",
                time = startTime?.let { formatMinutes(it) } ?: "--:--",
                onClick = onPickStart,
                modifier = Modifier.weight(1f)
            )
            
            if (!hideEnd) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = accentColor.copy(alpha = 0.4f), modifier = Modifier.size(20.dp))
                
                TimeChip(
                    label = "Hora Fin",
                    time = endTime?.let { formatMinutes(it) } ?: "--:--",
                    onClick = onPickEnd,
                    modifier = Modifier.weight(1f),
                    activeColor = accentColor
                )
            } else {
                // Shortcut Chip for Task mode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurface)
                        .border(1.dp, accentColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .clickable { onSetTimeToNow() }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Column {
                        Text("Atajo", style = RoutineTheme.typography.labelCaps.copy(fontSize = 9.sp), color = RoutineTheme.colors.onSurfaceVariant)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Ahora mismo", style = RoutineTheme.typography.bodyBase.copy(fontSize = 13.sp, fontWeight = FontWeight.Medium), color = accentColor)
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(Modifier.size(6.dp).clip(RoundedCornerShape(3.dp)).background(accentColor))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeChip(
    label: String,
    time: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = Color.White
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, style = RoutineTheme.typography.labelCaps.copy(fontSize = 9.sp), color = RoutineTheme.colors.onSurfaceVariant)
        Text(time, style = RoutineTheme.typography.dataLarge.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold), color = activeColor)
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = RoutineTheme.typography.labelCaps.copy(fontSize = 10.sp, letterSpacing = 1.2.sp),
        color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.padding(horizontal = 4.dp)
    )
    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
private fun AssociatedTaskItem(title: String, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.RadioButtonUnchecked, null, modifier = Modifier.size(16.dp), tint = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, style = RoutineTheme.typography.bodyBase.copy(fontSize = 14.sp), color = Color.White.copy(alpha = 0.9f))
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = onRemove, modifier = Modifier.size(20.dp)) {
            Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp), tint = RoutineTheme.colors.onSurfaceVariant)
        }
    }
}

@Composable
private fun QuickAddTaskInput(
    value: String,
    onValueChange: (String) -> Unit,
    onAdd: () -> Unit,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .border(1.dp, RoutineTheme.colors.border.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp), tint = accentColor)
        Spacer(modifier = Modifier.width(10.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text("Añadir sub-tarea rápida vinculada...", fontSize = 13.sp) },
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = RoutineTheme.typography.bodyBase.copy(fontSize = 13.sp)
        )
        if (value.isNotBlank()) {
            IconButton(onClick = onAdd) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = accentColor, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun ReminderPills(
    abs: Int?,
    rel: Int?,
    accentColor: Color,
    onUpdate: (Int?, Int?) -> Unit
) {
    val scrollState = rememberScrollState()
    Row(modifier = Modifier.horizontalScroll(scrollState), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ReminderPill(
            label = "10 min antes",
            isSelected = rel == 10,
            onClick = { onUpdate(null, 10) },
            accentColor = accentColor
        )
        ReminderPill(
            label = "Al momento",
            isSelected = rel == 0,
            onClick = { onUpdate(null, 0) },
            accentColor = accentColor
        )
        ReminderPill(
            label = "09:00",
            isSelected = abs == 540,
            onClick = { onUpdate(540, null) },
            accentColor = accentColor
        )
        
        if (abs != null || rel != null) {
            IconButton(onClick = { onUpdate(null, null) }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp), tint = RoutineTheme.colors.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ReminderPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    accentColor: Color
) {
    Surface(
        onClick = onClick,
        color = if (isSelected) accentColor.copy(alpha = 0.15f) else Color(0xFF141722),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) accentColor.copy(alpha = 0.4f) else RoutineTheme.colors.border.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelected) {
                Box(Modifier.size(6.dp).clip(RoundedCornerShape(3.dp)).background(accentColor))
                Spacer(Modifier.width(8.dp))
            }
            Text(label, style = RoutineTheme.typography.labelCaps.copy(fontSize = 11.sp, color = if (isSelected) accentColor else RoutineTheme.colors.onSurfaceVariant))
        }
    }
}

@Composable
private fun LinkChip(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClear: () -> Unit
) {
    Surface(
        color = accentColor.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = accentColor, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(title, style = RoutineTheme.typography.bodyBase.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = accentColor))
                Text(subtitle, style = RoutineTheme.typography.labelCaps.copy(fontSize = 8.sp, color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.6f)))
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onClear, modifier = Modifier.size(18.dp)) {
                Icon(Icons.Default.Close, null, tint = RoutineTheme.colors.onSurfaceVariant, modifier = Modifier.size(12.dp))
            }
        }
    }
}

@Composable
private fun ResultHeader(text: String) {
    Text(
        text = text,
        style = RoutineTheme.typography.labelCaps.copy(fontSize = 9.sp, letterSpacing = 1.2.sp, fontWeight = FontWeight.Bold),
        color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
    )
}

@Composable
private fun UnifiedResultRow(
    item: UnifiedLinkingResult,
    accentColor: Color,
    onClick: (UnifiedLinkingResult) -> Unit
) {
    val (title, subtitle, icon) = when (item) {
        is UnifiedLinkingResult.SemanticDefinition -> Triple(item.title, item.description ?: "Actividad", Icons.Default.Tag)
        is UnifiedLinkingResult.SemanticNode -> Triple(item.title, item.parentTitle ?: "Estructura", Icons.Default.AccountTree)
        is UnifiedLinkingResult.ContextualOccurrence -> Triple(item.title, item.item.timeRangeText, Icons.Default.Event)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(item) }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(accentColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = accentColor.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, style = RoutineTheme.typography.bodyBase.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium))
            Text(subtitle, style = RoutineTheme.typography.labelCaps.copy(fontSize = 10.sp, color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.6f)))
        }
    }
}

private fun formatMinutes(minutes: Int): String {
    val h = (minutes / 60) % 24
    val m = minutes % 60
    return "%02d:%02d".format(h, m)
}
