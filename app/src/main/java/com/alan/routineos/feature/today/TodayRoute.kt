package com.alan.routineos.feature.today

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun TodayRoute(
    viewModel: TodayViewModel = hiltViewModel(),
    bottomBar: @Composable () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    TodayScreen(
        uiState = uiState,
        onAction = viewModel::onActionTriggered,
        onExpandClick = viewModel::toggleExpand,
        onMetadataCaptured = viewModel::onMetadataCaptured,
        onCloseCapture = viewModel::onCloseCapture,
        onAddAdHoc = viewModel::onAddAdHoc,
        uiEvent = viewModel.uiEvent,
        bottomBar = bottomBar
    )
}
