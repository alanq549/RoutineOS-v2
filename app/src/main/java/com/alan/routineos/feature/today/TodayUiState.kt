package com.alan.routineos.feature.today

import com.alan.routineos.feature.today.model.TodayProgress
import com.alan.routineos.feature.today.model.TodayTimelineItem

data class TodayUiState(
    val isLoading: Boolean = false,
    val dateText: String = "",
    val progress: TodayProgress = TodayProgress(0, 0),
    val timelineItems: List<TodayTimelineItem> = emptyList(),
    val nextActivity: TodayTimelineItem? = null
)
