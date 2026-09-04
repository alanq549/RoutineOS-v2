package com.alan.routineos.feature.stats

import com.alan.routineos.domain.model.HistorySnapshot
import com.alan.routineos.domain.model.TrendSeries
import com.alan.routineos.feature.stats.model.*

data class StatsUiState(
    val isLoading: Boolean = false,
    val selectedPeriod: StatsPeriod = StatsPeriod.WEEK,
    val historySnapshot: HistorySnapshot? = null,
    val trendSeries: List<TrendSeries> = emptyList(),
    val errorMessage: String? = null
)
