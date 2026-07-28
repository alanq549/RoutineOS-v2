package com.alan.routineos.feature.dashboard

import com.alan.routineos.feature.dashboard.model.*

data class DashboardUiState(
    val isLoading: Boolean = false,
    val searchQueries: String = "",
    val myActivities: List<ActivityCardModel> = emptyList()
)
