package com.alan.routineos.feature.stats

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alan.routineos.feature.stats.model.StatsPeriod

@Composable
fun StatsRoute(
    viewModel: StatsViewModel = viewModel(),
    bottomBar: @Composable () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    StatsScreen(
        uiState = uiState,
        onPeriodSelected = viewModel::onPeriodSelected,
        onDaySelected = viewModel::onDaySelected,
        onWeekSelected = viewModel::onWeekSelected,
        onMonthSelected = viewModel::onMonthSelected,
        bottomBar = bottomBar
    )
}
