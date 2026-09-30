package com.alan.routineos.feature.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ActivityCatalogRoute(
    onAddActivity: () -> Unit,
    onActivityClick: (String) -> Unit,
    viewModel: ActivityCatalogViewModel = hiltViewModel(),
    isSheetMode: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsState()

    ActivityCatalogScreen(
        uiState = uiState,
        onAddActivity = onAddActivity,
        onActivityClick = onActivityClick,
        onSystemSelected = viewModel::onSystemSelected,
        onAddSystem = viewModel::onAddSystemClick,
        onEditSystem = viewModel::onEditSystemClick,
        onUpdateSystemFields = viewModel::onUpdateSystemFields,
        onSaveSystem = viewModel::onSaveSystem,
        onDeleteSystem = viewModel::onDeleteSystem,
        onDismissSystemEditor = viewModel::onDismissSystemEditor,
        isSheetMode = isSheetMode
    )
}
