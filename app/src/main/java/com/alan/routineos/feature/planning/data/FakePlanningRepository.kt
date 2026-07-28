package com.alan.routineos.feature.planning.data

import com.alan.routineos.feature.planning.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakePlanningRepository {
    fun getPlanningDays(): Flow<List<PlanningDay>> = flowOf(
        listOf(
            PlanningDay("1", "Lun", "12", isSelected = true),
            PlanningDay("2", "Mar", "13"),
            PlanningDay("3", "Mié", "14"),
            PlanningDay("4", "Jue", "15"),
            PlanningDay("5", "Vie", "16"),
            PlanningDay("6", "Sáb", "17"),
            PlanningDay("7", "Dom", "18")
        )
    )

    fun getPlanningBlocks(): Flow<List<PlanningBlock>> = flowOf(
        listOf(
            PlanningBlock(
                id = "1",
                title = "Universidad",
                type = PlanningBlockType.FLEXIBLE,
                startTime = "07:00",
                endTime = "14:00",
                description = "3 materias: Algoritmos II, Diseño de Sistemas, Probabilidad.",
                location = "Campus Central",
                conflictMessage = "Conflicto: Coincide con Cita médica (10:00 - 11:00)"
            ),
            PlanningBlock(
                id = "2",
                title = "Gym",
                type = PlanningBlockType.FLEXIBLE,
                startTime = "16:00",
                endTime = "18:00",
                description = "Sesión de Push Day: Enfocado en hipertrofia y movilidad."
            ),
            PlanningBlock(
                id = "3",
                title = "Dormir",
                type = PlanningBlockType.EXACT,
                startTime = "22:30",
                endTime = null,
                description = "Actividad nocturna: Sin pantallas 30min antes, lectura ligera."
            )
        )
    )

    fun getExceptions(): Flow<List<PlanningException>> = flowOf(
        listOf(
            PlanningException(
                id = "1",
                dateText = "Mié 14 Oct",
                label = "SOLO MIÉRCOLES",
                title = "Universidad",
                timeRange = "07:00 - 12:00"
            )
        )
    )

    fun getUnscheduled(): Flow<List<PlanningUnscheduled>> = flowOf(
        listOf(
            PlanningUnscheduled(
                id = "1",
                title = "Proyecto personal",
                description = "Avance en arquitectura v2",
                icon = "rocket_launch"
            ),
            PlanningUnscheduled(
                id = "2",
                title = "Lectura",
                description = "Capítulos 4 y 5",
                icon = "auto_stories"
            )
        )
    )
}
