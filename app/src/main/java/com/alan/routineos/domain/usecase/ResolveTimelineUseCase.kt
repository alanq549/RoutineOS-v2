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
            val materializedTargets = resolveMaterialized(materialized, entries)

            resolveVirtual(date, epochDay, rules, exceptions, materializedTargets, definitions, nodes, entries)

            detectConflicts(entries)
        }
    }

    private fun resolveMaterialized(
        materialized: List<DailyInstance>,
        entries: MutableList<TimelineEntry>
    ): Set<String> {
        val materializedTargets = mutableSetOf<String>()
        materialized.forEach { instance ->
            entries.add(TimelineEntry(instance, true))
            instance.target?.let { target ->
                materializedTargets.add(getTargetId(target))
            }
        }
        return materializedTargets
    }

    private fun resolveVirtual(
        date: LocalDate,
        epochDay: Long,
        rules: List<ScheduleRule>,
        exceptions: List<ScheduleException>,
        materializedTargets: Set<String>,
        definitions: List<ActivityDefinition>,
        nodes: List<ActivityNode>,
        entries: MutableList<TimelineEntry>
    ) {
        val defMap = definitions.associateBy { it.id }
        val nodeMap = nodes.associateBy { it.id }

        rules.forEach { rule ->
            val targetId = getTargetId(rule.target)
            if (materializedTargets.contains(targetId)) return@forEach

            if (shouldApplyRule(rule, epochDay, date, exceptions)) {
                entries.add(createVirtualEntry(rule, defMap, nodeMap, epochDay))
            }
        }
    }

    private fun shouldApplyRule(
        rule: ScheduleRule,
        epochDay: Long,
        date: LocalDate,
        exceptions: List<ScheduleException>
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
        rule: ScheduleRule,
        defMap: Map<String, ActivityDefinition>,
        nodeMap: Map<String, ActivityNode>,
        epochDay: Long
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
        target: ScheduleTarget,
        defMap: Map<String, ActivityDefinition>,
        nodeMap: Map<String, ActivityNode>
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

    private fun getTargetId(target: ScheduleTarget): String = when(target) {
        is ScheduleTarget.Definition -> target.id
        is ScheduleTarget.Node -> target.id
    }
}
