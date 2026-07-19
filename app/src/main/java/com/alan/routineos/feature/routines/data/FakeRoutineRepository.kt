package com.alan.routineos.feature.routines.data

import com.alan.routineos.feature.routines.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeRoutineRepository {
    fun getCategories(): Flow<List<RoutineCategory>> = flowOf(
        listOf(
            RoutineCategory("1", "Todas", isSelected = true),
            RoutineCategory("2", "Estudio"),
            RoutineCategory("3", "Salud"),
            RoutineCategory("4", "Trabajo"),
            RoutineCategory("5", "Personal"),
            RoutineCategory("6", "Descanso")
        )
    )

    fun getMyRoutines(): Flow<List<RoutineCardModel>> = flowOf(
        listOf(
            RoutineCardModel(
                id = "1",
                title = "Universidad",
                iconName = "school",
                frequency = "Lun - Vie",
                durationText = "5 Días",
                subtitle = "5 días configurados · 8 materias · 32 actividades",
                summaryItems = listOf(
                    RoutineSummaryDay("Lunes", listOf("Programación", "Bases de Datos")),
                    RoutineSummaryDay("Martes", listOf("IA"))
                )
            ),
            RoutineCardModel(
                id = "2",
                title = "Gimnasio",
                iconName = "fitness_center",
                frequency = "Frecuencia",
                durationText = "4 sesiones/sem",
                subtitle = "4 sesiones · 18 ejercicios · 2 plantillas",
                summaryItems = listOf(
                    RoutineSummaryDay("Lun: Push", listOf("5 ejercicios")),
                    RoutineSummaryDay("Mar: Pull", listOf("6 ejercicios"))
                )
            ),
            RoutineCardModel(
                id = "3",
                title = "Rutina Matutina",
                iconName = "wb_sunny",
                frequency = "Frecuencia",
                durationText = "Diario",
                subtitle = "1 sesión · 3 bloques · 45 min total",
                summaryItems = listOf(
                    RoutineSummaryDay("", listOf("Meditación (10 min)", "Journaling (15 min)", "Lectura técnica (20 min)"))
                )
            )
        )
    )

    fun getRecommendedTemplates(): Flow<List<RoutineTemplateModel>> = flowOf(
        listOf(
            RoutineTemplateModel(
                id = "1",
                title = "Developer Deep Work",
                description = "Estructura para bloques de 4h de programación sin interrupciones.",
                iconName = "terminal"
            ),
            RoutineTemplateModel(
                id = "2",
                title = "Higiene del Sueño",
                description = "Optimiza tu descanso con este protocolo de 90 min antes de dormir.",
                iconName = "bedtime"
            )
        )
    )
}
