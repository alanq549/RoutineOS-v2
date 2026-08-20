package com.alan.routineos.domain.usecase

import com.alan.routineos.domain.model.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ValidateScheduleRuleTest {

    private val validator = ValidateScheduleRuleUseCase()
    private val target = ScheduleTarget.Node("n1")

    @Test
    fun `detects duplicate rule with same days and time`() {
        val existing = listOf(
            ScheduleRule("1", target, ScheduleRuleType.FIXED_DAYS, daysOfWeek = setOf(1, 2), startTime = 480)
        )
        val newRule = ScheduleRule("2", target, ScheduleRuleType.FIXED_DAYS, daysOfWeek = setOf(1, 2), startTime = 480)
        
        val error = validator(newRule, existing)
        assertEquals(ScheduleRuleValidationError.DuplicateRule, error)
    }

    @Test
    fun `detects duplicate rule with different days order`() {
        val existing = listOf(
            ScheduleRule("1", target, ScheduleRuleType.FIXED_DAYS, daysOfWeek = setOf(1, 2), startTime = 480)
        )
        // setOf(2, 1) is logically the same as setOf(1, 2)
        val newRule = ScheduleRule("2", target, ScheduleRuleType.FIXED_DAYS, daysOfWeek = setOf(2, 1), startTime = 480)
        
        val error = validator(newRule, existing)
        assertEquals(ScheduleRuleValidationError.DuplicateRule, error)
    }

    @Test
    fun `allows same time on different days`() {
        val existing = listOf(
            ScheduleRule("1", target, ScheduleRuleType.FIXED_DAYS, daysOfWeek = setOf(1), startTime = 480)
        )
        val newRule = ScheduleRule("2", target, ScheduleRuleType.FIXED_DAYS, daysOfWeek = setOf(2), startTime = 480)
        
        val error = validator(newRule, existing)
        assertNull(error)
    }

    @Test
    fun `allows different time on same days`() {
        val existing = listOf(
            ScheduleRule("1", target, ScheduleRuleType.FIXED_DAYS, daysOfWeek = setOf(1, 2), startTime = 480)
        )
        val newRule = ScheduleRule("2", target, ScheduleRuleType.FIXED_DAYS, daysOfWeek = setOf(1, 2), startTime = 500)
        
        val error = validator(newRule, existing)
        assertNull(error)
    }

    @Test
    fun `detects invalid time range`() {
        val rule = ScheduleRule("1", target, ScheduleRuleType.FIXED_DAYS, daysOfWeek = setOf(1), startTime = 500, endTime = 400)
        val error = validator(rule, emptyList())
        assertEquals(ScheduleRuleValidationError.InvalidTimeRange, error)
    }
}
