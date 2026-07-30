package com.alan.routineos.feature.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun DashboardRoute(
    onAddActivity: () -> Unit,
    onActivityClick: (String) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    DashboardScreen(
        uiState = uiState,
        onAddActivity = onAddActivity,
        onActivityClick = onActivityClick
    )
}
