package com.alan.routineos.feature.today.model

import com.alan.routineos.domain.model.DailyInstanceStatus

data class TodaySubNodeUiModel(
    val id: String,
    val title: String,
    val timeText: String,
    val status: DailyInstanceStatus
)

data class TodayTimelineUiModel(
    val id: String,
    val title: String,
    val timeRangeText: String,
    val status: DailyInstanceStatus,
    val isMaterialized: Boolean,
    val hasConflict: Boolean = false,
    val subNodes: List<TodaySubNodeUiModel> = emptyList()
)
