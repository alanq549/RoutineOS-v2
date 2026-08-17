package com.alan.routineos.feature.today

import com.alan.routineos.domain.model.MetadataSchema
import com.alan.routineos.feature.today.model.TodayProgress
import com.alan.routineos.feature.today.model.TodayTimelineUiModel

data class TodayUiState(
    val isLoading: Boolean = false,
    val dateText: String = "",
    val progress: TodayProgress = TodayProgress(0, 0),
    val timelineItems: List<TodayTimelineUiModel> = emptyList(),
    val nextActivity: TodayTimelineUiModel? = null,
    val captureSchema: MetadataSchema? = null,
    val captureTargetId: String? = null
)
