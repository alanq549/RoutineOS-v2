package com.alan.routineos.feature.system

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SystemRoute(
    viewModel: SystemViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    SystemScreen(
        uiState = uiState
    )
}
