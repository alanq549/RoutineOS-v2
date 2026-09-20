package com.alan.routineos.feature.dashboard.model

data class ActivityCardModel(
    val id: String,
    val title: String,
    val iconName: String,
    val iconColorHex: String,
    val frequency: String,
    val durationText: String,
    val subtitle: String,
    val statsLine: String = "",
    val summaryItems: List<ActivitySummaryDay> = emptyList(),
    val moreDaysCount: Int = 0
)

data class ActivitySummaryDay(
    val dayName: String,
    val activities: List<String>,
    val detailText: String? = null
)
