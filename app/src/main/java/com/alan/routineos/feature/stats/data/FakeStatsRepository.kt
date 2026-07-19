package com.alan.routineos.feature.stats.data

import com.alan.routineos.feature.stats.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeStatsRepository {
    fun getWeeklyRhythm(): Flow<WeeklyRhythmData> = flowOf(
        WeeklyRhythmData(
            dateRange = "15–21 de julio",
            summaryMetrics = listOf(
                StatsSummaryMetric("CUMPLIMIENTO", "76%", isPrimary = true),
                StatsSummaryMetric("DÍAS ACTIVOS", "6 de 7", isPrimary = true),
                StatsSummaryMetric("CAMBIOS", "4"),
                StatsSummaryMetric("PARCIALES", "3")
            ),
            days = listOf(
                createRhythmDay("1", "LUN", listOf(RhythmBlock(RhythmBlockType.COMPLETED, 0.6f), RhythmBlock(RhythmBlockType.PARTIAL, 0.2f))),
                createRhythmDay("2", "MAR", listOf(RhythmBlock(RhythmBlockType.SKIPPED, 0.4f), RhythmBlock(RhythmBlockType.COMPLETED, 0.4f))),
                createRhythmDay("3", "MIÉ", listOf(RhythmBlock(RhythmBlockType.PARTIAL, 0.2f), RhythmBlock(RhythmBlockType.SPONTANEOUS, 0.1f), RhythmBlock(RhythmBlockType.COMPLETED, 0.4f), RhythmBlock(RhythmBlockType.SKIPPED, 0.3f)), isSelected = true),
                createRhythmDay("4", "JUE", listOf(RhythmBlock(RhythmBlockType.COMPLETED, 0.5f), RhythmBlock(RhythmBlockType.COMPLETED, 0.4f))),
                createRhythmDay("5", "VIE", listOf(RhythmBlock(RhythmBlockType.PARTIAL, 0.3f), RhythmBlock(RhythmBlockType.COMPLETED, 0.6f))),
                createRhythmDay("6", "SÁB", listOf(RhythmBlock(RhythmBlockType.SKIPPED, 1f))),
                createRhythmDay("7", "DOM", listOf(RhythmBlock(RhythmBlockType.COMPLETED, 1f)))
            ),
            comparison = listOf(
                StatsComparison("Universidad", "28h", "25h", 0.89f),
                StatsComparison("Gym", "4 sesiones", "3", 0.75f, partialPercentage = 0.15f)
            ),
            systems = listOf(
                StatsSystemMetric("Universidad", 82, "4 de 5 días", "school"),
                StatsSystemMetric("Gym", 75, "3 completas, 1 parcial", "fitness_center")
            ),
            insights = listOf(
                StatsInsight("El miércoles concentró la mayoría de tus cambios.", "edit_calendar"),
                StatsInsight("Gym tuvo una sesión parcial esta semana.", "fitness_center")
            )
        )
    )

    fun getMonthlyCycle(): Flow<MonthlyCycleData> = flowOf(
        MonthlyCycleData(
            monthName = "Julio de 2026",
            summaryMetrics = listOf(
                StatsSummaryMetric("DÍAS ACTIVOS", "18 días", isPrimary = true),
                StatsSummaryMetric("CUMPLIMIENTO", "72%", isPrimary = true),
                StatsSummaryMetric("RACHA", "5 días"),
                StatsSummaryMetric("CAMBIOS", "6 ajustes")
            ),
            completionPercentage = 76,
            activeDaysText = "23 de 31 días activos",
            weeks = listOf(
                CycleWeek("1", "Semana 1", 82, bodyLoad = createBodyLoadMock("Semana 1", false)),
                CycleWeek("2", "Semana 2", 74, bodyLoad = createBodyLoadMock("Semana 2", false)),
                CycleWeek("3", "Semana 3", 68, isSelected = true, comparisonText = "-8% vs anterior", bodyLoad = createBodyLoadMock("Semana 3", true)),
                CycleWeek("4", "Semana 4", 79, bodyLoad = createBodyLoadMock("Semana 4", false))
            ),
            outcomeBreakdown = listOf(
                StatsSummaryMetric("COMPLETADAS", "96"),
                StatsSummaryMetric("PARCIALES", "14"),
                StatsSummaryMetric("OMITIDAS", "11"),
                StatsSummaryMetric("ESPONTÁNEAS", "8")
            ),
            comparison = listOf(
                StatsComparison("Planificado", "126 h", "126 h", 1f),
                StatsComparison("Real", "126 h", "112 h 40 min", 0.89f)
            ),
            systems = listOf(
                StatsSystemMetric("Universidad", 84, "18 de 22 días", "school"),
                StatsSystemMetric("Gym", 78, "11 sesiones", "fitness_center"),
                StatsSystemMetric("Proyecto personal", 61, "7 de 12 bloques", "terminal")
            ),
            insights = listOf(
                StatsInsight("La segunda semana tuvo la mayor constancia.", "insights"),
                StatsInsight("La mayoría de los cambios ocurrieron en Universidad.", "school")
            )
        )
    )

    fun getYearlyCycle(): Flow<YearlyCycleData> = flowOf(
        YearlyCycleData(
            year = "2026",
            months = listOf(
                createCycleMonth("1", "ENE", 72),
                createCycleMonth("2", "FEB", 68),
                createCycleMonth("3", "MAR", 84),
                createCycleMonth("4", "ABR", 79),
                createCycleMonth("5", "MAY", 81),
                createCycleMonth("6", "JUN", 75),
                createCycleMonth("7", "JUL", 76, isSelected = true),
                createCycleMonth("8", "AGO", 0),
                createCycleMonth("9", "SEP", 0),
                createCycleMonth("10", "OCT", 0),
                createCycleMonth("11", "NOV", 0),
                createCycleMonth("12", "DIC", 0)
            )
        )
    )

    private fun createRhythmDay(id: String, label: String, blocks: List<RhythmBlock>, isSelected: Boolean = false) = RhythmDay(
        id = id,
        name = "Día $id",
        label = label,
        isSelected = isSelected,
        blocks = blocks,
        details = RhythmDayDetail(
            title = if (label == "MIÉ") "Miércoles 17" else "Día $label",
            subtitle = "4 rutinas planificadas, 1 espontánea",
            completionPercentage = 68,
            items = listOf(
                RhythmDetailItem("Universidad", RhythmBlockType.PARTIAL, "school"),
                RhythmDetailItem("Reunión", RhythmBlockType.SPONTANEOUS, "add"),
                RhythmDetailItem("Gym", RhythmBlockType.COMPLETED, "check_circle"),
                RhythmDetailItem("Lectura", RhythmBlockType.SKIPPED, "cancel")
            ),
            bodyLoad = createBodyLoadMock(if (label == "MIÉ") "Miércoles 17" else label, label == "SÁB")
        )
    )

    private fun createCycleMonth(id: String, name: String, percentage: Int, isSelected: Boolean = false) = CycleMonth(
        id = id,
        name = name,
        percentage = percentage,
        isSelected = isSelected,
        activeDays = 23,
        totalDays = 31,
        trendText = "+5% vs mes anterior",
        principalSystems = listOf(
            StatsSystemMetric("Universidad", 84, "18 de 22 días", "school"),
            StatsSystemMetric("Gym", 78, "11 sesiones", "fitness_center")
        ),
        bodyLoad = createBodyLoadMock(name, false)
    )

    private fun createBodyLoadMock(context: String, isWarning: Boolean) = BodyLoadUiModel(
        contextLabel = context,
        dominantZone = "Tren Superior",
        activeZonesCount = 5,
        loadLevel = if (isWarning) BodyLoadLevel.RECOVERY_REQUIRED else BodyLoadLevel.HIGH,
        highlightedZones = setOf(BodyZone.CHEST, BodyZone.ARMS_FRONT, BodyZone.BACK_UPPER),
        rankedZones = listOf(
            ZoneRankItem("Pecho", 85, BodyLoadLevel.HIGH),
            ZoneRankItem("Brazos", 70, BodyLoadLevel.MEDIUM),
            ZoneRankItem("Espalda", 60, BodyLoadLevel.MEDIUM)
        ),
        recoveryInfo = if (isWarning) "Se recomienda descanso activo" else "Nivel de carga óptimo",
        isWarning = isWarning,
        isEmpty = false
    )
}
