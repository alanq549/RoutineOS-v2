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
            LifeArea.Large(
                id = "1",
                title = "Universidad",
                iconName = "school",
                status = LifeAreaStatus.ACTIVE,
                routinesCount = 5,
                subjectsCount = 8,
                nextExecution = "Lunes 07:00"
            ),
            LifeArea.Medium(
                id = "2",
                title = "Gym",
                iconName = "fitness_center",
                status = LifeAreaStatus.ACTIVE,
                sessionsCount = 4,
                exercisesCount = 18,
                templatesCount = 3,
                nextExecution = "Hoy 16:00"
            ),
            LifeArea.Small(
                id = "3",
                title = "Trabajo",
                iconName = "work",
                status = LifeAreaStatus.ACTIVE,
                description = "9:00 - 18:00"
            ),
            LifeArea.Small(
                id = "4",
                title = "Salud",
                iconName = "favorite",
                status = LifeAreaStatus.ACTIVE,
                description = "Check-up semanal"
            ),
            LifeArea.Small(
                id = "5",
                title = "Proyecto Personal",
                iconName = "rocket_launch",
                status = LifeAreaStatus.PAUSED,
                description = "Pausado"
            ),
            LifeArea.Small(
                id = "6",
                title = "Descanso",
                iconName = "bedtime",
                status = LifeAreaStatus.TEMPORARY,
                description = "Sábados libres"
            )
        )
    )
}
