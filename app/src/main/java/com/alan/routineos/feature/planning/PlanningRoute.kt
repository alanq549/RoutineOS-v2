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
        onDaySelected = viewModel::onDaySelected
    )
}
