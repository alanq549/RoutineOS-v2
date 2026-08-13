package com.alan.routineos.domain.usecase

import com.alan.routineos.domain.model.*
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class ResolveTimelineForDateRangeTest {

    private val resolver = ResolveTimelineForDateRange()
    private val testNode = ActivityNode("node1", "act1", "Node 1")

    @Test
    fun `FIXED_DAYS rule resolves correctly`() {
        val rule = ScheduleRule(
            id = "r1",
            nodeId = "node1",
            type = ScheduleRuleType.FIXED_DAYS,
            daysOfWeek = setOf(1, 3, 5) // Mon, Wed, Fri
        )

        // Mon Oct 23 to Sun Oct 29, 2023
        val start = LocalDate.of(2023, 10, 23)
        val end = LocalDate.of(2023, 10, 29)

        val result = resolver(testNode, rule, emptyList(), start, end)

        assertEquals(3, result.size)
        assertEquals(start.toEpochDay(), result[0].scheduledDate) // Mon
        assertEquals(start.plusDays(2).toEpochDay(), result[1].scheduledDate) // Wed
        assertEquals(start.plusDays(4).toEpochDay(), result[2].scheduledDate) // Fri
    }

    @Test
    fun `SKIPPED exception removes instance`() {
        val rule = ScheduleRule(
            id = "r1",
            nodeId = "node1",
            type = ScheduleRuleType.FIXED_DAYS,
            daysOfWeek = setOf(1) // Mon
        )
        val start = LocalDate.of(2023, 10, 23) // Mon
        val end = LocalDate.of(2023, 10, 23)

        val exceptions = listOf(
            ScheduleException("ex1", "r1", start.toEpochDay(), ScheduleExceptionType.SKIPPED)
        )

        val result = resolver(testNode, rule, exceptions, start, end)

        assertEquals(0, result.size)
    }

    @Test
    fun `RESCHEDULED exception moves instance`() {
        val rule = ScheduleRule(
            id = "r1",
            nodeId = "node1",
            type = ScheduleRuleType.FIXED_DAYS,
            daysOfWeek = setOf(1) // Mon
        )
        val mon = LocalDate.of(2023, 10, 23)
        val tue = mon.plusDays(1)
        val end = mon.plusDays(6)

        val exceptions = listOf(
            ScheduleException(
                id = "ex1",
                scheduleRuleId = "r1",
                originalDate = mon.toEpochDay(),
                type = ScheduleExceptionType.RESCHEDULED,
                newDate = tue.toEpochDay()
            )
        )

        // Range includes both Mon and Tue
        val result = resolver(testNode, rule, exceptions, mon, end)

        assertEquals(1, result.size)
        assertEquals(tue.toEpochDay(), result[0].scheduledDate)
    }
}
