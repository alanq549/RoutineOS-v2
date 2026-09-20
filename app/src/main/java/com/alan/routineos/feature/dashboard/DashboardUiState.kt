package com.alan.routineos.feature.dashboard

import com.alan.routineos.domain.model.LifeSystem
import com.alan.routineos.feature.dashboard.model.*

data class DashboardUiState(
    val isLoading: Boolean = false,
    val searchQueries: String = "",
    val myActivities: List<ActivityCardModel> = emptyList(),
    val allSystems: List<LifeSystem> = emptyList(),
    val systemCounts: Map<String, Int> = emptyMap(), // systemId -> activityCount
    val totalActivitiesCount: Int = 0,
    val selectedSystemId: String? = null,
    val editingSystem: LifeSystem? = null,
    val isCreatingNewSystem: Boolean = false
)
