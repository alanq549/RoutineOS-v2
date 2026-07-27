package com.alan.routineos.feature.dashboard

import com.alan.routineos.feature.dashboard.model.*

data class DashboardUiState(
    val isLoading: Boolean = false,
    val searchQueries: String = "",
    val categories: List<ActivityCategory> = emptyList(),
    val myActivities: List<ActivityCardModel> = emptyList(),
    val recommendedTemplates: List<ActivityTemplateModel> = emptyList()
)
