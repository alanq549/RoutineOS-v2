package com.alan.routineos.domain.usecase

import com.alan.routineos.domain.model.*
import java.time.LocalDate
import javax.inject.Inject

/**
 * Pure logic engine to resolve rules and materialized instances into logical occurrences.
 * Centralizes the semantic of "what should happen" and "what is currently scheduled".
 */
class TimelineResolutionEngine @Inject constructor() {

    fun resolve(
        date: LocalDate,
        rules: List<ScheduleRule>,
        exceptions: List<ScheduleException>,
        materialized: List<DailyInstance>,
        definitions: List<ActivityDefinition>,
        nodes: List<ActivityNode>
    ): List<ResolvedOccurrence> {
        val epochDay = date.toEpochDay()
        val defMap = definitions.associateBy { it.id }
        val nodeMap = nodes.associateBy { it.id }
        
        val validDefIds = defMap.keys
        val validNodeIds = nodeMap.keys

        // 1. Filter rules and materialized instances for existing targets
        val validRules = rules.filter { rule ->
            when (val target = rule.target) {
                is ScheduleTarget.Definition -> validDefIds.contains(target.id)
                is ScheduleTarget.Node -> validNodeIds.contains(target.id)
            }
        }

        val materializedByRule = materialized.filter { it.sourceRuleId != null }.associateBy { it.sourceRuleId }
        val adHocMaterialized = materialized.filter { it.isAdHoc || it.sourceRuleId == null }

        val results = mutableListOf<ResolvedOccurrence>()

        // 2. Resolve Scheduled Rules
        validRules.forEach { rule ->
            val existing = materializedByRule[rule.id]
            if (existing != null) {
                results.add(ResolvedOccurrence(existing.id, existing, true, date))
            } else if (shouldProjectVirtual(rule, epochDay, date, exceptions)) {
                val virtual = createVirtualInstance(rule, defMap, nodeMap, epochDay)
                results.add(ResolvedOccurrence(virtual.id, virtual, false, date))
            }
        }

        // 3. Resolve Ad-hoc
        adHocMaterialized.forEach { instance ->
            // Avoid duplicates if ad-hoc was already added (unlikely but safe)
            if (results.none { it.instance.id == instance.id }) {
                results.add(ResolvedOccurrence(instance.id, instance, true, date, isAdHoc = true))
            }
        }

        return results.sortedBy { it.instance.plannedStartTime ?: Int.MAX_VALUE }
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

    private fun createVirtualInstance(
        rule: ScheduleRule, 
        defMap: Map<String, ActivityDefinition>,
        nodeMap: Map<String, ActivityNode>, 
        epochDay: Long
    ): DailyInstance {
        val (title, desc) = getSnapshotData(rule.target, defMap, nodeMap)
        return DailyInstance(
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
}
