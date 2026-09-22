package com.alan.routineos.feature.system.model

enum class LifeAreaStatus {
    ACTIVE, PAUSED, TEMPORARY, ARCHIVED
}

data class LifeArea(
    val id: String,
    val title: String,
    val iconName: String,
    val status: LifeAreaStatus,
    val colorHex: String,
    val activityCount: Int,
    val completedCount: Int,
    val skippedCount: Int,
    val pendingCount: Int,
    val successRate: Float?
)

data class SystemSummary(
    val systemsCount: Int,
    val routinesCount: Int,
    val activitiesCount: Int,
    val exceptionsCount: Int
)
