package com.alan.routineos.feature.stats

import com.alan.routineos.feature.stats.model.*

data class StatsUiState(
    val isLoading: Boolean = false,
    val selectedPeriod: StatsPeriod = StatsPeriod.WEEK,
    val weeklyData: WeeklyRhythmData? = null,
    val monthlyData: MonthlyCycleData? = null,
    val yearlyData: YearlyCycleData? = null,
    val selectedDayId: String? = "3",
    val selectedWeekId: String? = "3",
    val selectedMonthId: String? = "7"
)
