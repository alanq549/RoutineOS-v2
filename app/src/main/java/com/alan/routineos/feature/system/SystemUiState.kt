package com.alan.routineos.feature.system

import com.alan.routineos.feature.system.model.*

data class SystemUiState(
    val isLoading: Boolean = false,
    val summary: SystemSummary = SystemSummary(0, 0, 0, 0),
    val lifeAreas: List<LifeArea> = emptyList()
)
