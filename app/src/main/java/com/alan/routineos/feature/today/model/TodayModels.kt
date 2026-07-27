package com.alan.routineos.feature.today.model

enum class TimelineItemStatus {
    COMPLETED, ACTIVE, PENDING, SKIPPED
}

sealed class TodayTimelineItem {
    abstract val id: String
    abstract val startTime: String
    abstract val endTime: String?
    abstract val status: TimelineItemStatus

    data class Activity(
        override val id: String,
        val title: String,
        override val startTime: String,
        override val endTime: String?,
        override val status: TimelineItemStatus,
        val nodes: List<ActivityNodeSnapshot> = emptyList()
    ) : TodayTimelineItem()

    data class Flexible(
        override val id: String,
        val title: String,
        val activity: String,
        val description: String?,
        override val startTime: String,
        override val endTime: String?,
        override val status: TimelineItemStatus,
        val progress: String? = null // e.g. "2 de 3"
    ) : TodayTimelineItem()

    data class Spontaneous(
        override val id: String,
        val title: String,
        val interruptionInfo: String?,
        override val startTime: String,
        override val endTime: String?,
        override val status: TimelineItemStatus
    ) : TodayTimelineItem()
}

data class ActivityNodeSnapshot(
    val id: String,
    val title: String,
    val startTime: String,
    val endTime: String,
    val status: TimelineItemStatus
)

data class TodayProgress(
    val completed: Int,
    val total: Int
)
