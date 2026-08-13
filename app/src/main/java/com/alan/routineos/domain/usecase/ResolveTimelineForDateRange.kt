package com.alan.routineos.domain.usecase

import com.alan.routineos.domain.model.*
import java.time.LocalDate

/**
 * Pure logic to resolve scheduled instances for a specific node within a date range,
 * applying its recurrence rule and any exceptions.
 */
class ResolveTimelineForDateRange {

    operator fun invoke(
        node: ActivityNode,
        rule: ScheduleRule?,
        exceptions: List<ScheduleException>,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<TimelineInstance> {
        if (rule == null) return emptyList()

        val instances = mutableListOf<TimelineInstance>()
        var currentDate = startDate

        while (!currentDate.isAfter(endDate)) {
            val epochDay = currentDate.toEpochDay()
            
            // Check for exceptions on this specific date
            val hasException = exceptions.any { it.originalDate == epochDay }

            if (!hasException) {
                when {
                    // 1. If it's a FIXED_DAYS rule
                    rule.type == ScheduleRuleType.FIXED_DAYS -> {
                        // daysOfWeek: 1 (Mon) to 7 (Sun) matching LocalDate's dayOfWeek.value
                        if (rule.daysOfWeek.contains(currentDate.dayOfWeek.value)) {
                            instances.add(
                                TimelineInstance(
                                    node = node,
                                    scheduledDate = epochDay
                                )
                            )
                        }
                    }

                    // 2. FLEXIBLE_FREQUENCY logic
                    rule.type == ScheduleRuleType.FLEXIBLE_FREQUENCY -> {
                        instances.add(
                            TimelineInstance(
                                node = node,
                                scheduledDate = epochDay
                            )
                        )
                    }
                }
            }

            currentDate = currentDate.plusDays(1)
        }

        // 4. Add RESCHEDULED instances that were moved INTO this range
        exceptions.forEach { ex ->
            if (ex.type == ScheduleExceptionType.RESCHEDULED && ex.newDate != null) {
                val newLocalDate = LocalDate.ofEpochDay(ex.newDate)
                if (!newLocalDate.isBefore(startDate) && !newLocalDate.isAfter(endDate)) {
                    instances.add(
                        TimelineInstance(
                            node = node,
                            scheduledDate = ex.newDate
                        )
                    )
                }
            }
        }

        return instances.sortedBy { it.scheduledDate }
    }
}
