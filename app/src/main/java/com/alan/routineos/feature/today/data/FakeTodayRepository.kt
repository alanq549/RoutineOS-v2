package com.alan.routineos.feature.today.data

import com.alan.routineos.feature.today.model.SubTask
import com.alan.routineos.feature.today.model.TimelineItemStatus
import com.alan.routineos.feature.today.model.TodayProgress
import com.alan.routineos.feature.today.model.TodayTimelineItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeTodayRepository {
    fun getTimelineItems(): Flow<List<TodayTimelineItem>> = flowOf(
        listOf(
            TodayTimelineItem.Routine(
                id = "1",
                title = "Entrenamiento Mañanero",
                startTime = "08:00",
                endTime = null,
                status = TimelineItemStatus.COMPLETED
            ),
            TodayTimelineItem.Routine(
                id = "2",
                title = "Universidad",
                startTime = "09:30",
                endTime = "15:00",
                status = TimelineItemStatus.ACTIVE,
                subTasks = listOf(
                    SubTask("2.1", "Programación", "09:30", "10:30", TimelineItemStatus.COMPLETED),
                    SubTask("2.2", "Bases de datos", "11:00", "12:00", TimelineItemStatus.ACTIVE),
                    SubTask("2.3", "Redes", "12:00", "14:00", TimelineItemStatus.PENDING)
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
            TodayTimelineItem.Routine(
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
