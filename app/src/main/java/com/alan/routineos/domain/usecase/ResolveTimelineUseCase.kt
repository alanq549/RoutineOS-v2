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
    private val conflictDetector: ConflictDetectorUseCase,
    private val suggestionEngine: SuggestionEngine
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
            val nodeMap = nodes.associateBy { it.id }
            
            // Filter out rules for targets that don\u0027t exist
            val validDefIds = definitions.map { it.id }.toSet()
            val validNodeIds = nodes.map { it.id }.toSet()
            
            val validRules = rules.filter { rule ->
                when (val target = rule.target) {
                    is ScheduleTarget.Definition -> validDefIds.contains(target.id)
                    is ScheduleTarget.Node -> validNodeIds.contains(target.id)
                }
            }

            val validMaterialized = materialized.filter { instance ->
                val target = instance.target
                target == null || when (target) {
                    is ScheduleTarget.Definition -> validDefIds.contains(target.id)
                    is ScheduleTarget.Node -> validNodeIds.contains(target.id)
                }
            }

            val materializedByRule = validMaterialized.associateBy { it.sourceRuleId }

            resolveScheduled(date, epochDay, validRules, exceptions, materializedByRule, definitions, nodes, entries)
            resolveAdHoc(validMaterialized, entries)

            detectConflicts(entries, nodeMap)
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

    private fun detectConflicts(entries: List<TimelineEntry>, nodeMap: Map<String, ActivityNode>): List<TimelineEntry> {
        val instances = entries.map { it.instance }
        val initialConflicts = conflictDetector.detectConflicts(instances, nodeMap)
        
        return entries.map { entry ->
            val result = initialConflicts[entry.instance.id] ?: ConflictResult(false)
            val suggestions = if (result.impact == TemporalImpact.WARNING) {
                suggestionEngine.generateSuggestions(entry.instance, instances, nodeMap)
            } else emptyList()
            
            entry.copy(conflict = result.copy(suggestions = suggestions))
        }.sortedBy { it.instance.plannedStartTime ?: Int.MAX_VALUE }
    }
}
