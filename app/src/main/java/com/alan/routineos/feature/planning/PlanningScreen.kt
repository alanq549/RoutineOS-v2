package com.alan.routineos.feature.planning

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Inbox
import com.alan.routineos.feature.planning.components.BacklogPanelSheet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.domain.model.BacklogItem
import com.alan.routineos.feature.dashboard.ActivityCatalogRoute
import com.alan.routineos.feature.planning.components.PlanningExceptionCard
import com.alan.routineos.feature.planning.components.PlanningReminderCard
import com.alan.routineos.feature.planning.components.PlanningTimeBlock
import com.alan.routineos.feature.planning.components.PlanningWeekHeader
import com.alan.routineos.feature.planning.model.SearchTargetUiModel
import com.alan.routineos.feature.planning.model.UnifiedLinkingResult
import com.alan.routineos.feature.today.components.ConflictWarningDialog
import com.alan.routineos.feature.today.components.SpontaneousEditorSheet
import com.alan.routineos.feature.today.components.TimePicker
import com.alan.routineos.feature.today.model.PlanningItemType
import com.alan.routineos.feature.today.model.TodayTimelineUiModel

private const val FLOATING_VISIBLE_LIMIT = 2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanningScreen(
    uiState: PlanningUiState,
    onDaySelected: (String) -> Unit,
    onPrevWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onGoToToday: () -> Unit,
    onJumpToDate: (java.time.LocalDate) -> Unit,
    onAction: (String, String) -> Unit,
    onAddEventClick: () -> Unit,
    onSaveNewEvent: () -> Unit,
    onDismissSpontaneousEditor: () -> Unit,
    onUpdateSpontaneousTitle: (String, String) -> Unit,
    onUpdateSpontaneousSchedule: (String, Int?, Int?) -> Unit,
    onDeleteInstance: (String) -> Unit,
    onUpdateEditorRole: (EditorRole) -> Unit = {},
    onUpdateCatalogSearch: (String) -> Unit = {},
    onLinkToDefinition: (SearchTargetUiModel?) -> Unit = {},
    onLinkToOccurrence: (TodayTimelineUiModel?) -> Unit = {},
    onSelectUnifiedResult: (UnifiedLinkingResult) -> Unit = {},
    onExpandClick: (String) -> Unit,
    onAddDraftTask: (String) -> Unit = {},
    onRemoveDraftTask: (String) -> Unit = {},
    onUpdateDraftNote: (String) -> Unit = {},
    onUpdateDraftReminder: (Int?, Int?) -> Unit = { _, _ -> },
    onSetTimeToNow: (String) -> Unit = {},
    onConfirmPendingMove: () -> Unit,
    onCancelPendingMove: () -> Unit,
    onAddActivityFromCatalog: (String) -> Unit = {},
    onNavigateToActivityCreation: () -> Unit = {},
    onNavigateToActivityDetail: (String) -> Unit = {},
    onAssignBacklogItemToDay: (BacklogItem) -> Unit = {},
    onCreateBacklogItem: (String) -> Unit = {},
    onDeleteBacklogItem: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var moveTargetId by remember { mutableStateOf<String?>(null) }
    val timePickerState = rememberTimePickerState()

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    var fabMenuExpanded by remember { mutableStateOf(false) }
    var showCatalogSheet by remember { mutableStateOf(false) }
    var showBacklogSheet by remember { mutableStateOf(false) }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val selected = java.time.Instant.ofEpochMilli(it)
                            .atZone(java.time.ZoneId.of("UTC")) // DatePicker uses UTC
                            .toLocalDate()
                        onJumpToDate(selected)
                    }
                    showDatePicker = false
                }) { Text("Ir") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp)
        ) {
            PlanningWeekHeader(
                days = uiState.weekDays,
                weekRangeText = uiState.weekRangeText,
                onDaySelected = onDaySelected,
                onPrevWeek = onPrevWeek,
                onNextWeek = onNextWeek,
                onRangeClick = { showDatePicker = true }
            )

            Spacer(modifier = Modifier.height(24.dp))

                        // Floating Section: SOLO Recordatorios (sin hora por naturaleza)
            val unscheduledReminders =
                uiState.unscheduledItems.filter { it.itemType == PlanningItemType.REMINDER }
            if (unscheduledReminders.isNotEmpty()) {
                var showAllFloating by remember { mutableStateOf(false) }
                val visibleFloating =
                    if (showAllFloating) unscheduledReminders else unscheduledReminders.take(
                        FLOATING_VISIBLE_LIMIT
                    )

                Column(
                    modifier = Modifier.padding(horizontal = RoutineTheme.spacing.md),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    visibleFloating.forEach { item ->
                        PlanningReminderCard(item = item, onAction = onAction)
                    }

                    if (!showAllFloating && unscheduledReminders.size > FLOATING_VISIBLE_LIMIT) {
                        Text(
                            text = "+${unscheduledReminders.size - FLOATING_VISIBLE_LIMIT} más",
                            style = RoutineTheme.typography.labelCaps.copy(fontSize = 9.sp),
                            color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                .clickable { showAllFloating = true }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Cronograma: Tareas sin hora (pineadas arriba) + Actividades/Tareas programadas
            val unscheduledTasks =
                uiState.unscheduledItems.filter { it.itemType == PlanningItemType.TASK }
            
            PlanningSection(
                title = "CRONOGRAMA DEL DÍA",
                subtitle = if (uiState.timelineEntries.isNotEmpty()) "${uiState.timelineEntries.size} Bloques Activos" else null
            ) {
                if (unscheduledTasks.isEmpty() && uiState.timelineEntries.isEmpty()) {
                    EmptySectionMessage("Sin eventos programados")
                } else {
                    // Stitch Base Spine: Sharp and Dark to allow semantic signal layers to pop
                    val spineNeutral = Color(0xFF0F172A)
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .drawBehind {
                                // 1. Draw Global Neutral Spine Line (36dp axis)
                                val x = 36.dp.toPx()
                                drawLine(
                                    color = spineNeutral,
                                    start = androidx.compose.ui.geometry.Offset(x, 0f),
                                    end = androidx.compose.ui.geometry.Offset(x, size.height),
                                    strokeWidth = 1.5.dp.toPx()
                                )
                            }
                    ) {
                        Column {
                            // 1. Pinned tasks (No time)
                            unscheduledTasks.forEach { entry ->
                                PlanningTimeBlock(
                                    item = entry,
                                    onAction = onAction,
                                    onExpandClick = onExpandClick
                                )
                            }
                            // 2. Scheduled timeline
                            uiState.timelineEntries.forEach { entry ->
                                PlanningTimeBlock(
                                    item = entry,
                                    onAction = { id, type ->
                                        if (type == "MOVE_REQUEST") moveTargetId = id
                                        else onAction(id, type)
                                    },
                                    onExpandClick = onExpandClick
                                )
                            }
                        }
                    }
                }
            }

            if (uiState.exceptions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))
                PlanningSection(
                    title = "AJUSTES DE RUTINA",
                    subtitle = "Modificaciones de hoy"
                ) {
                    uiState.exceptions.forEach { entry ->
                        PlanningExceptionCard(item = entry)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(RoutineTheme.spacing.lg)
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!uiState.isShowingToday) {
                SmallFloatingActionButton(
                    onClick = onGoToToday,
                    containerColor = RoutineTheme.colors.surface2,
                    contentColor = RoutineTheme.colors.onSurface,
                    shape = RoutineTheme.shapes.pill
                ) {
                    Text(
                        text = "HOY",
                        modifier = Modifier.padding(horizontal = 12.dp),
                        style = RoutineTheme.typography.labelCaps.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            AnimatedVisibility(visible = fabMenuExpanded) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Option 1: Catálogo de Actividades
                    Surface(
                        onClick = {
                            fabMenuExpanded = false
                            showCatalogSheet = true
                        },
                        color = RoutineTheme.colors.surface2,
                        shape = RoutineTheme.shapes.pill,
                        border = BorderStroke(1.dp, RoutineTheme.colors.border)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountTree,
                                contentDescription = null,
                                tint = RoutineTheme.colors.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AÑADIR DE CATÁLOGO",
                                style = RoutineTheme.typography.labelCaps.copy(fontWeight = FontWeight.Bold),
                                color = RoutineTheme.colors.onSurface
                            )
                        }
                    }

                    // Option 2: Pendientes (Backlog)
                    Surface(
                        onClick = {
                            fabMenuExpanded = false
                            showBacklogSheet = true
                        },
                        color = RoutineTheme.colors.surface2,
                        shape = RoutineTheme.shapes.pill,
                        border = BorderStroke(1.dp, RoutineTheme.colors.border)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inbox,
                                contentDescription = null,
                                tint = RoutineTheme.colors.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "PENDIENTES (BACKLOG)",
                                style = RoutineTheme.typography.labelCaps.copy(fontWeight = FontWeight.Bold),
                                color = RoutineTheme.colors.onSurface
                            )
                        }
                    }

                    // Option 3: Evento Espontáneo
                    Surface(
                        onClick = {
                            fabMenuExpanded = false
                            onAddEventClick()
                        },
                        color = RoutineTheme.colors.surface2,
                        shape = RoutineTheme.shapes.pill,
                        border = BorderStroke(1.dp, RoutineTheme.colors.border)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = null,
                                tint = RoutineTheme.colors.roleEvent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "EVENTO ESPONTÁNEO",
                                style = RoutineTheme.typography.labelCaps.copy(fontWeight = FontWeight.Bold),
                                color = RoutineTheme.colors.onSurface
                            )
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = { fabMenuExpanded = !fabMenuExpanded },
                containerColor = RoutineTheme.colors.primary,
                contentColor = Color.Black,
                shape = RoutineTheme.shapes.pill
            ) {
                Icon(
                    imageVector = if (fabMenuExpanded) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = "Opciones de nuevo elemento"
                )
            }
        }
    }

    if (showCatalogSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCatalogSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = RoutineTheme.colors.background
        ) {
            ActivityCatalogRoute(
                onAddActivity = {
                    showCatalogSheet = false
                    onNavigateToActivityCreation()
                },
                onActivityClick = { activityId ->
                    onAddActivityFromCatalog(activityId)
                    showCatalogSheet = false
                },
                onOpenDetail = { activityId ->
                    showCatalogSheet = false
                    onNavigateToActivityDetail(activityId)
                },
                isSheetMode = true
            )
        }
    }

    if (showBacklogSheet) {
        BacklogPanelSheet(
            openBacklogItems = uiState.openBacklogItems,
            onAssignToDay = onAssignBacklogItemToDay,
            onCreateBacklogItem = onCreateBacklogItem,
            onDeleteBacklogItem = onDeleteBacklogItem,
            onDismiss = { showBacklogSheet = false }
        )
    }

    if (uiState.editingSpontaneousEntry != null) {
        SpontaneousEditorSheet(
            entry = uiState.editingSpontaneousEntry,
            role = uiState.editorRole,
            isCreationMode = uiState.isCreatingNewEvent,
            onSaveNew = onSaveNewEvent,
            onUpdateTitle = onUpdateSpontaneousTitle,
            onUpdateSchedule = onUpdateSpontaneousSchedule,
            onDelete = onDeleteInstance,
            onUpdateRole = onUpdateEditorRole,
            onUpdateCatalogSearch = onUpdateCatalogSearch,
            onLinkToDefinition = onLinkToDefinition,
            onLinkToOccurrence = onLinkToOccurrence,
            onSelectUnifiedResult = onSelectUnifiedResult,
            onAddDraftTask = onAddDraftTask,
            onRemoveDraftTask = onRemoveDraftTask,
            onUpdateDraftNote = onUpdateDraftNote,
            onUpdateDraftReminder = onUpdateDraftReminder,
            onSetTimeToNow = onSetTimeToNow,
            onDismiss = onDismissSpontaneousEditor,
            unifiedCatalog = uiState.unifiedCatalog,
            catalogSearchQuery = uiState.catalogSearchQuery,
            selectedDefinition = uiState.selectedSemanticTarget,
            selectedOccurrence = uiState.selectedContextualOccurrence
        )
    }

    if (moveTargetId != null) {
        AlertDialog(
            onDismissRequest = { moveTargetId = null },
            confirmButton = {
                TextButton(onClick = {
                    val mins = timePickerState.hour * 60 + timePickerState.minute
                    onAction(moveTargetId!!, "MOVE_CONFIRM:$mins")
                    moveTargetId = null
                }) { Text("Confirmar") }
            },
            dismissButton = { TextButton(onClick = { moveTargetId = null }) { Text("Cancelar") } },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timePickerState)
                }
            }
        )
    }

    if (uiState.pendingMove != null) {
        ConflictWarningDialog(
            pendingMove = uiState.pendingMove,
            onConfirm = onConfirmPendingMove,
            onDismiss = onCancelPendingMove,
            onChooseOther = {
                val id = uiState.pendingMove.entry.root.instance.id
                onCancelPendingMove()
                if (uiState.pendingMove.entry.root.instance.isAdHoc) {
                    onAction(id, "EDIT_SPONTANEOUS")
                } else {
                    moveTargetId = id
                }
            }
        )
    }
}

@Composable
private fun PlanningSection(
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = RoutineTheme.spacing.md)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = title,
                style = RoutineTheme.typography.labelCaps.copy(letterSpacing = 2.sp),
                color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.6f)
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = RoutineTheme.typography.labelCaps.copy(fontSize = 10.sp),
                    color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        content()
    }
}

@Composable
private fun EmptySectionMessage(message: String) {
    Text(
        text = message,
        style = RoutineTheme.typography.bodyBase,
        color = RoutineTheme.colors.onSurfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier
            .padding(vertical = 16.dp)
            .fillMaxWidth(),
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )
}
