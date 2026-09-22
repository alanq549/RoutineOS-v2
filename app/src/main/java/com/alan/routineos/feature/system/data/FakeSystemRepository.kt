package com.alan.routineos.feature.system.data

import com.alan.routineos.feature.system.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeSystemRepository {
    fun getSummary(): Flow<SystemSummary> = flowOf(
        SystemSummary(
            systemsCount = 6,
            routinesCount = 18,
            activitiesCount = 164,
            exceptionsCount = 12
        )
    )

    fun getLifeAreas(): Flow<List<LifeArea>> = flowOf(
        listOf(
            LifeArea(
                id = "1",
                title = "Universidad",
                iconName = "school",
                status = LifeAreaStatus.ACTIVE,
                colorHex = "#34D399",
                activityCount = 5,
                completedCount = 8,
                skippedCount = 1,
                pendingCount = 2,
                successRate = 0.88f
            ),
            LifeArea(
                id = "2",
                title = "Gym",
                iconName = "fitness_center",
                status = LifeAreaStatus.ACTIVE,
                colorHex = "#818CF8",
                activityCount = 4,
                completedCount = 18,
                skippedCount = 2,
                pendingCount = 0,
                successRate = 0.9f
            ),
            LifeArea(
                id = "3",
                title = "Trabajo",
                iconName = "work",
                status = LifeAreaStatus.ACTIVE,
                colorHex = "#FBBF24",
                activityCount = 3,
                completedCount = 10,
                skippedCount = 0,
                pendingCount = 5,
                successRate = 1.0f
            ),
            LifeArea(
                id = "4",
                title = "Salud",
                iconName = "favorite",
                status = LifeAreaStatus.ACTIVE,
                colorHex = "#F87171",
                activityCount = 2,
                completedCount = 4,
                skippedCount = 0,
                pendingCount = 1,
                successRate = 1.0f
            ),
            LifeArea(
                id = "5",
                title = "Proyecto Personal",
                iconName = "rocket",
                status = LifeAreaStatus.PAUSED,
                colorHex = "#94A3B8",
                activityCount = 1,
                completedCount = 0,
                skippedCount = 0,
                pendingCount = 0,
                successRate = null
            ),
            LifeArea(
                id = "6",
                title = "Descanso",
                iconName = "bedtime",
                status = LifeAreaStatus.TEMPORARY,
                colorHex = "#64748B",
                activityCount = 2,
                completedCount = 2,
                skippedCount = 0,
                pendingCount = 0,
                successRate = 1.0f
            )
        )
    )
}
