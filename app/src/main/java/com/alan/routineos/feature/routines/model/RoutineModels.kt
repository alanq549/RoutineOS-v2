package com.alan.routineos.feature.routines.model

data class RoutineCardModel(
    val id: String,
    val title: String,
    val iconName: String,
    val frequency: String,
    val durationText: String,
    val subtitle: String,
    val summaryItems: List<RoutineSummaryDay> = emptyList()
)

data class RoutineSummaryDay(
    val dayName: String,
    val activities: List<String>
)

data class RoutineTemplateModel(
    val id: String,
    val title: String,
    val description: String,
    val iconName: String,
    val imageUrl: String? = null
)

data class RoutineCategory(
    val id: String,
    val name: String,
    val isSelected: Boolean = false
)
