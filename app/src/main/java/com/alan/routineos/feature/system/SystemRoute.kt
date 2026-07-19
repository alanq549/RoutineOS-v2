package com.alan.routineos.feature.system

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SystemRoute(
    viewModel: SystemViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    SystemScreen(
        uiState = uiState
    )
}
