package com.alan.routineos.feature.today

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun TodayRoute(
    viewModel: TodayViewModel = viewModel(),
    bottomBar: @Composable () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    TodayScreen(
        uiState = uiState,
        bottomBar = bottomBar
    )
}
