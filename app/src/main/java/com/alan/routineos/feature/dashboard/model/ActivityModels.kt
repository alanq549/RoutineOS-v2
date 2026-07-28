package com.alan.routineos.feature.dashboard.model

data class ActivityCardModel(
    val id: String,
    val title: String,
    val iconName: String,
    val frequency: String,
    val durationText: String,
    val subtitle: String,
    val summaryItems: List<ActivitySummaryDay> = emptyList()
)

data class ActivitySummaryDay(
    val dayName: String,
    val activities: List<String>
)
