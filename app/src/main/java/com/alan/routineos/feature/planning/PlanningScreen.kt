package com.alan.routineos.feature.planning

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alan.routineos.core.designsystem.theme.RoutineTheme
import com.alan.routineos.feature.planning.components.PlanningExceptionCard
import com.alan.routineos.feature.planning.components.PlanningTimeBlock
import com.alan.routineos.feature.planning.components.PlanningUnscheduledCard
import com.alan.routineos.feature.planning.components.PlanningWeekHeader
import com.alan.routineos.feature.today.components.ConflictWarningDialog
import com.alan.routineos.feature.today.components.SpontaneousEditorSheet
import com.alan.routineos.feature.today.components.TimePicker
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanningScreen(
    uiState: PlanningUiState,
    onDaySelected: (String) -> Unit,
    onAction: (String, String) -> Unit,
    onAddAdHoc: (String, Int?, Int?) -> Unit,
    onAddFromCatalog: (String, Int?, Int?) -> Unit,
    onOpenCatalog: () -> Unit,
    onCloseCatalog: () -> Unit,
    onDismissSpontaneousEditor: () -> Unit,
    onUpdateSpontaneousTitle: (String, String) -> Unit,
    onUpdateSpontaneousSchedule: (String, Int?, Int?) -> Unit,
    onDeleteInstance: (String) -> Unit,
    onExpandClick: (String) -> Unit,
    onConfirmPendingMove: () -> Unit,
    onCancelPendingMove: () -> Unit,
    modifier: Modifier = Modifier
) {
    var moveTargetId by remember { mutableStateOf<String?>(null) }
    val timePickerState = rememberTimePickerState()

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp)
        ) {
            PlanningWeekHeader(
                days = uiState.weekDays,
                onDaySelected = onDaySelected
            )

            Spacer(modifier = Modifier.height(32.dp))

            PlanningSection(title = "BLOQUES DE RUTINA") {
                if (uiState.timelineEntries.isEmpty()) {
                    EmptySectionMessage("Sin actividades programadas")
                } else {
                    uiState.timelineEntries.forEach { entry ->
                        PlanningTimeBlock(
                            item = entry,
                            onAction = { id, type -> 
                                if (type == "MOVE_REQUEST") moveTargetId = id
                                else onAction(id, type)
                            },
                            onExpandClick = onExpandClick
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (uiState.exceptions.isNotEmpty()) {
                PlanningSection(title = "EXCEPCIONES") {
                    uiState.exceptions.forEach { entry ->
                        PlanningExceptionCard(item = entry)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            PlanningSection(
                title = "SIN HORARIO",
                subtitle = "PENDIENTES FLEXIBLES"
            ) {
                if (uiState.unscheduledItems.isEmpty()) {
                    EmptySectionMessage("No hay pendientes flexibles")
                } else {
                    uiState.unscheduledItems.chunked(2).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            rowItems.forEach { item ->
                                Box(modifier = Modifier.weight(1f)) {
                                    PlanningUnscheduledCard(
                                        title = item.title,
                                        description = item.description,
                                        onClick = { onAction(item.id, "MOVE_REQUEST") }
                                    )
                                }
                            }
                            if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onOpenCatalog,
            containerColor = RoutineTheme.colors.primary,
            contentColor = androidx.compose.ui.graphics.Color.Black,
            shape = RoutineTheme.shapes.pill,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(RoutineTheme.spacing.lg)
                .padding(bottom = 16.dp)
        ) {
            Icon(Icons.Default.Add, "Añadir actividad")
        }
    }

    if (uiState.isCatalogOpen) {
        ModalBottomSheet(onDismissRequest = onCloseCatalog) {
            CatalogBottomSheetContent(
                activities = uiState.availableActivities,
                onSelect = { onAddFromCatalog(it, null, null) }
            )
        }
    }

    if (uiState.editingSpontaneousEntry != null) {
        SpontaneousEditorSheet(
            entry = uiState.editingSpontaneousEntry,
            onUpdateTitle = onUpdateSpontaneousTitle,
            onUpdateSchedule = onUpdateSpontaneousSchedule,
            onDelete = onDeleteInstance,
            onDismiss = onDismissSpontaneousEditor
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
                    // Spontaneous editor is already wired in PlanningScreen
                    onAction(id, "EDIT_SPONTANEOUS")
                } else {
                    moveTargetId = id
                }
            }
        )
    }
}

@Composable
private fun CatalogBottomSheetContent(
    activities: List<com.alan.routineos.domain.model.ActivityDefinition>,
    onSelect: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp).navigationBarsPadding()) {
        Text("Catálogo de Actividades", style = RoutineTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(activities) { activity ->
                ListItem(
                    headlineContent = { Text(activity.title) },
                    supportingContent = { Text(activity.description) },
                    leadingContent = { Icon(Icons.Default.LibraryBooks, null) },
                    modifier = Modifier.clickable { onSelect(activity.id) }
                )
            }
        }
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
        modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth(),
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )
}
