package com.alan.routineos.feature.system.model

enum class LifeAreaStatus {
    ACTIVE, PAUSED, TEMPORARY, ARCHIVED
}

sealed class LifeArea {
    abstract val id: String
    abstract val title: String
    abstract val iconName: String
    abstract val status: LifeAreaStatus

    data class Large(
        override val id: String,
        override val title: String,
        override val iconName: String,
        override val status: LifeAreaStatus,
        val routinesCount: Int,
        val subjectsCount: Int,
        val nextExecution: String
    ) : LifeArea()

    data class Medium(
        override val id: String,
        override val title: String,
        override val iconName: String,
        override val status: LifeAreaStatus,
        val sessionsCount: Int,
        val exercisesCount: Int,
        val templatesCount: Int,
        val nextExecution: String
    ) : LifeArea()

    data class Small(
        override val id: String,
        override val title: String,
        override val iconName: String,
        override val status: LifeAreaStatus,
        val description: String // e.g. "9:00 - 18:00"
    ) : LifeArea()
}

data class SystemSummary(
    val systemsCount: Int,
    val routinesCount: Int,
    val activitiesCount: Int,
    val exceptionsCount: Int
)
