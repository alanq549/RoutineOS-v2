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
        onAction = viewModel::onActionTriggered,
        onAddAdHoc = viewModel::onAddAdHoc,
        onAddFromCatalog = viewModel::onAddFromCatalog,
        onOpenCatalog = viewModel::onOpenCatalog,
        onCloseCatalog = viewModel::onCloseCatalog,
        onDismissSpontaneousEditor = viewModel::onDismissSpontaneousEditor,
        onUpdateSpontaneousTitle = viewModel::onUpdateSpontaneousTitle,
        onUpdateSpontaneousSchedule = viewModel::onUpdateSpontaneousSchedule,
        onDeleteInstance = viewModel::onDeleteInstance,
        onExpandClick = viewModel::toggleExpand,
        onConfirmPendingMove = viewModel::onConfirmPendingMove,
        onCancelPendingMove = viewModel::onCancelPendingMove
    )
}
