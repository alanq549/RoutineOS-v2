package com.alan.routineos.feature.today.data

import com.alan.routineos.feature.today.model.ActivityNodeSnapshot
import com.alan.routineos.feature.today.model.TimelineItemStatus
import com.alan.routineos.feature.today.model.TodayProgress
import com.alan.routineos.feature.today.model.TodayTimelineItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeTodayRepository {
    fun getTimelineItems(): Flow<List<TodayTimelineItem>> = flowOf(
        listOf(
            TodayTimelineItem.Activity(
                id = "1",
                title = "Entrenamiento Mañanero",
                startTime = "08:00",
                endTime = null,
                status = TimelineItemStatus.COMPLETED
            ),
            TodayTimelineItem.Activity(
                id = "2",
                title = "Universidad",
                startTime = "09:30",
                endTime = "15:00",
                status = TimelineItemStatus.ACTIVE,
                nodes = listOf(
                    ActivityNodeSnapshot("2.1", "Bases de datos", "09:30", "11:00", TimelineItemStatus.ACTIVE),
                    ActivityNodeSnapshot("2.1.1", "SQL Lab", "09:30", "10:15", TimelineItemStatus.COMPLETED),
                    ActivityNodeSnapshot("2.1.2", "NoSQL Lab", "10:15", "11:00", TimelineItemStatus.PENDING),
                    ActivityNodeSnapshot("2.2", "Redes", "11:00", "12:00", TimelineItemStatus.PENDING),
                    ActivityNodeSnapshot("2.3", "Ingeniería de Software", "12:00", "14:00", TimelineItemStatus.PENDING)
                )
            ),
            TodayTimelineItem.Spontaneous(
                id = "3",
                title = "Reunión inesperada",
                interruptionInfo = "Interrumpe Universidad",
                startTime = "10:30",
                endTime = "11:00",
                status = TimelineItemStatus.COMPLETED
            ),
            TodayTimelineItem.Flexible(
                id = "4",
                title = "Flexible",
                activity = "Gym • Push Day",
                description = "Pecho, hombro y tríceps",
                startTime = "16:00",
                endTime = "18:00",
                status = TimelineItemStatus.PENDING,
                progress = "2 de 3"
            ),
            TodayTimelineItem.Activity(
                id = "5",
                title = "Lectura Técnica",
                startTime = "19:00",
                endTime = null,
                status = TimelineItemStatus.SKIPPED
            )
        )
    )

    fun getTodayProgress(): Flow<TodayProgress> = flowOf(
        TodayProgress(completed = 3, total = 8)
    )
}
