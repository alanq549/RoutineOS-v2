package com.alan.routineos.feature.routines

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun RoutineLibraryRoute(
    viewModel: RoutineLibraryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    RoutineLibraryScreen(
        uiState = uiState,
        onCategorySelected = viewModel::onCategorySelected
    )
}
