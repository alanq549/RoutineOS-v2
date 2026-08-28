package com.alan.routineos.feature.today.model

import com.alan.routineos.domain.model.DailyInstanceStatus

enum class TimelineTemporalState {
    UPCOMING,
    CURRENT,
    OVERDUE,
    STALE_PENDING
}

data class TodaySubNodeUiModel(
    val id: String,
    val title: String,
    val timeText: String,
    val status: DailyInstanceStatus,
    val contextMetadata: List<Pair<String, String>> = emptyList(),
    val operationalMetadata: List<Pair<String, String>> = emptyList()
)

data class TodayTimelineUiModel(
    val id: String,
    val title: String,
    val description: String = "",
    val timeRangeText: String,
    val status: DailyInstanceStatus,
    val isMaterialized: Boolean,
    val hasConflict: Boolean = false,
    val subNodes: List<TodaySubNodeUiModel> = emptyList(),
    val contextMetadata: List<Pair<String, String>> = emptyList(),
    val operationalMetadata: List<Pair<String, String>> = emptyList(),
    val isExpandable: Boolean = false,
    val isExpanded: Boolean = false,
    val isAdHoc: Boolean = false,
    val completedSubNodesCount: Int = 0,
    val totalSubNodesCount: Int = 0,
    val temporalState: TimelineTemporalState = TimelineTemporalState.UPCOMING
)
