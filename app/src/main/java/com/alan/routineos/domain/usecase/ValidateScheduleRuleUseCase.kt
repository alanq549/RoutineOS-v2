package com.alan.routineos.domain.usecase

import com.alan.routineos.domain.model.ScheduleRule
import com.alan.routineos.domain.model.ScheduleTarget
import javax.inject.Inject

sealed class ScheduleRuleValidationError(val userMessage: String) {
    object DuplicateRule : ScheduleRuleValidationError("Este horario ya existe para esta actividad")
    object InvalidTimeRange : ScheduleRuleValidationError("La hora de fin debe ser posterior a la de inicio")
}

/**
 * Validates ScheduleRule integrity and prevents duplicates.
 */
class ValidateScheduleRuleUseCase @Inject constructor() {

    operator fun invoke(
        rule: ScheduleRule,
        existingRules: List<ScheduleRule>
    ): ScheduleRuleValidationError? {
        // 1. Time range check
        if (rule.endTime != null && rule.startTime != null && rule.startTime >= rule.endTime) {
            return ScheduleRuleValidationError.InvalidTimeRange
        }

        // 2. Duplicate check (same target, same days, same start time)
        val isDuplicate = existingRules.any { existing ->
            existing.id != rule.id &&
            existing.target == rule.target &&
            existing.daysOfWeek == rule.daysOfWeek &&
            existing.startTime == rule.startTime
        }

        if (isDuplicate) return ScheduleRuleValidationError.DuplicateRule

        return null
    }
}
