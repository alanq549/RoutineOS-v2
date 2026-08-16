package com.alan.routineos.domain.usecase

import com.alan.routineos.domain.model.*
import com.alan.routineos.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import javax.inject.Inject

data class TimelineEntry(
    val instance: DailyInstance,
    val isMaterialized: Boolean
)

class ResolveTimelineUseCase @Inject constructor(
    private val repository: ActivityRepository
) {
    operator fun invoke(date: LocalDate): Flow<List<TimelineEntry>> {
        val epochDay = date.toEpochDay()

        return combine(
            repository.getActivityDefinitions(),
            repository.getAllNodes(),
            repository.getAllRules(),
            repository.getAllExceptions(),
            repository.getDailyInstancesForDate(epochDay)
        ) { definitions, nodes, rules, exceptions, materialized ->
            
            val entries = mutableListOf<TimelineEntry>()
            val materializedTargets = mutableSetOf<String>()

            // 1. Add Materialized Instances
            materialized.forEach { instance ->
                entries.add(TimelineEntry(instance, true))
                instance.target?.let { target ->
                    val targetId = when(target) {
                        is ScheduleTarget.Definition -> target.id
                        is ScheduleTarget.Node -> target.id
                    }
                    materializedTargets.add(targetId)
                }
            }

            // 2. Resolve Virtual Instances
            val defMap = definitions.associateBy { it.id }
            val nodeMap = nodes.associateBy { it.id }

            rules.forEach { rule ->
                val targetId = when(val target = rule.target) {
                    is ScheduleTarget.Definition -> target.id
                    is ScheduleTarget.Node -> target.id
                }

                // If already materialized for this day, skip rule resolution (Precedence)
                if (materializedTargets.contains(targetId)) return@forEach

                // Resolve rule for this date
                // For now: only FIXED_DAYS and check exceptions
                val hasException = exceptions.any { it.scheduleRuleId == rule.id && it.originalDate == epochDay }
                if (hasException) return@forEach // Simplified: assume exception means skip for now

                if (rule.type == ScheduleRuleType.FIXED_DAYS && rule.daysOfWeek.contains(date.dayOfWeek.value)) {
                    val title: String
                    val desc: String
                    when(val target = rule.target) {
                        is ScheduleTarget.Definition -> {
                            title = defMap[target.id]?.title ?: "Unknown"
                            desc = defMap[target.id]?.description ?: ""
                        }
                        is ScheduleTarget.Node -> {
                            title = nodeMap[target.id]?.title ?: "Unknown"
                            desc = nodeMap[target.id]?.description ?: ""
                        }
                    }

                    entries.add(
                        TimelineEntry(
                            instance = DailyInstance(
                                id = "virtual_${rule.id}_$epochDay",
                                target = rule.target,
                                scheduledDate = epochDay,
                                titleSnapshot = title,
                                descriptionSnapshot = desc,
                                status = DailyInstanceStatus.PLANNED,
                                sourceRuleId = rule.id
                            ),
                            isMaterialized = false
                        )
                    )
                }
            }

            entries.sortedBy { it.instance.plannedStartTime }
        }
    }
}
