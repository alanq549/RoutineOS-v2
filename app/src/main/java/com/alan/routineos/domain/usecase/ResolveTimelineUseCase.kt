package com.alan.routineos.domain.usecase

import com.alan.routineos.domain.model.*
import com.alan.routineos.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import javax.inject.Inject

data class TimelineEntry(
    val instance: DailyInstance,
    val isMaterialized: Boolean,
    val conflict: ConflictResult? = null
)

/**
 * Resolves the timeline for a specific date by merging rules, exceptions, and persisted instances.
 */
class ResolveTimelineUseCase @Inject constructor(
    private val repository: ActivityRepository,
    private val conflictDetector: ConflictDetectorUseCase
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
            val materializedByRule = materialized.associateBy { it.sourceRuleId }

            resolveScheduled(date, epochDay, rules, exceptions, materializedByRule, definitions, nodes, entries)
            resolveAdHoc(materialized, entries)

            detectConflicts(entries)
        }
    }

    private fun resolveScheduled(
        date: LocalDate, epochDay: Long,
        rules: List<ScheduleRule>, exceptions: List<ScheduleException>,
        materializedByRule: Map<String?, DailyInstance>,
        definitions: List<ActivityDefinition>, nodes: List<ActivityNode>,
        entries: MutableList<TimelineEntry>
    ) {
        val defMap = definitions.associateBy { it.id }
        val nodeMap = nodes.associateBy { it.id }

        rules.forEach { rule ->
            val materialized = materializedByRule[rule.id]
            if (materialized != null) {
                entries.add(TimelineEntry(materialized, true))
            } else if (shouldProjectVirtual(rule, epochDay, date, exceptions)) {
                entries.add(createVirtualEntry(rule, defMap, nodeMap, epochDay))
            }
        }
    }

    private fun resolveAdHoc(materialized: List<DailyInstance>, entries: MutableList<TimelineEntry>) {
        materialized.filter { it.isAdHoc || it.sourceRuleId == null }.forEach {
            // Already added in resolveScheduled if it had a sourceRuleId
            if (!entries.any { existing -> it.id == existing.instance.id }) {
                entries.add(TimelineEntry(it, true))
            }
        }
    }

    private fun shouldProjectVirtual(
        rule: ScheduleRule, epochDay: Long, date: LocalDate, exceptions: List<ScheduleException>
    ): Boolean {
        val ruleExceptions = exceptions.filter { it.scheduleRuleId == rule.id }
        if (ruleExceptions.any { it.originalDate == epochDay }) return false

        val isFixedToday = rule.type == ScheduleRuleType.FIXED_DAYS && 
                rule.daysOfWeek.contains(date.dayOfWeek.value)
        
        val isMovedToToday = ruleExceptions.any { 
            it.newDate == epochDay && it.type == ScheduleExceptionType.RESCHEDULED 
        }

        return isFixedToday || isMovedToToday
    }

    private fun createVirtualEntry(
        rule: ScheduleRule, defMap: Map<String, ActivityDefinition>,
        nodeMap: Map<String, ActivityNode>, epochDay: Long
    ): TimelineEntry {
        val (title, desc) = getSnapshotData(rule.target, defMap, nodeMap)
        return TimelineEntry(
            instance = DailyInstance(
                id = "virtual_${rule.id}_$epochDay",
                target = rule.target,
                scheduledDate = epochDay,
                titleSnapshot = title,
                descriptionSnapshot = desc,
                plannedStartTime = rule.startTime,
                plannedEndTime = rule.endTime,
                plannedDurationMinutes = rule.durationMinutes,
                status = DailyInstanceStatus.PLANNED,
                sourceRuleId = rule.id
            ),
            isMaterialized = false
        )
    }

    private fun getSnapshotData(
        target: ScheduleTarget, defMap: Map<String, ActivityDefinition>, nodeMap: Map<String, ActivityNode>
    ): Pair<String, String> {
        return when(target) {
            is ScheduleTarget.Definition -> {
                val def = defMap[target.id]
                (def?.title ?: "Unknown") to (def?.description ?: "")
            }
            is ScheduleTarget.Node -> {
                val node = nodeMap[target.id]
                (node?.title ?: "Unknown") to (node?.description ?: "")
            }
        }
    }

    private fun detectConflicts(entries: List<TimelineEntry>): List<TimelineEntry> {
        val instances = entries.map { it.instance }
        val conflicts = conflictDetector.detectConflicts(instances)
        
        return entries.map { entry ->
            entry.copy(conflict = conflicts[entry.instance.id])
        }.sortedBy { it.instance.plannedStartTime ?: Int.MAX_VALUE }
    }
}
