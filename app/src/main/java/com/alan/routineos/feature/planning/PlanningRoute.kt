package com.alan.routineos.feature.planning

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun PlanningRoute(
    viewModel: PlanningViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    PlanningScreen(
        uiState = uiState,
        onDaySelected = viewModel::onDaySelected,
        onPrevWeek = viewModel::prevWeek,
        onNextWeek = viewModel::nextWeek,
        onGoToToday = viewModel::goToToday,
        onJumpToDate = viewModel::jumpToDate,
        onAction = viewModel::onActionTriggered,
        onAddEventClick = viewModel::onAddEventClick,
        onSaveNewEvent = viewModel::onSaveNewEvent,
        onDismissSpontaneousEditor = viewModel::onDismissSpontaneousEditor,
        onUpdateSpontaneousTitle = viewModel::onUpdateSpontaneousTitle,
        onUpdateSpontaneousSchedule = viewModel::onUpdateSpontaneousSchedule,
        onDeleteInstance = viewModel::onDeleteInstance,
        onUpdateEditorRole = viewModel::onUpdateEditorRole,
        onUpdateCatalogSearch = viewModel::onUpdateCatalogSearch,
        onLinkToDefinition = viewModel::onLinkToDefinition,
        onLinkToOccurrence = viewModel::onLinkToOccurrence,
        onSelectUnifiedResult = viewModel::onSelectUnifiedResult,
        onExpandClick = viewModel::toggleExpand,
        onAddDraftTask = viewModel::onAddDraftTask,
        onRemoveDraftTask = viewModel::onRemoveDraftTask,
        onUpdateDraftNote = viewModel::onUpdateDraftNote,
        onUpdateDraftReminder = viewModel::onUpdateDraftReminder,
        onSetTimeToNow = viewModel::onSetTimeToNow,
        onConfirmPendingMove = viewModel::onConfirmPendingMove,
        onCancelPendingMove = viewModel::onCancelPendingMove
    )
}
