package com.alan.routineos.domain.usecase

import com.alan.routineos.domain.model.DailyInstance
import com.alan.routineos.domain.model.ScheduleTarget
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConflictDetectorTest {

    private val detector = ConflictDetectorUseCase()

    @Test
    fun `detects overlapping instances`() {
        val instances = listOf(
            createInstance("1", 480), // 08:00
            createInstance("2", 500)  // 08:20 (Overlaps with 08:00 if duration is 30)
        )
        
        val results = detector.detectConflicts(instances)
        
        assertTrue(results["1"]?.hasConflict ?: false)
        assertTrue(results["2"]?.hasConflict ?: false)
    }

    @Test
    fun `no conflict for adjacent instances`() {
        val instances = listOf(
            createInstance("1", 480), // 08:00 - 08:30
            createInstance("2", 510)  // 08:30 - 09:00
        )
        
        val results = detector.detectConflicts(instances)
        
        assertFalse(results["1"]?.hasConflict ?: true)
        assertFalse(results["2"]?.hasConflict ?: true)
    }

    private fun createInstance(id: String, startTime: Int) = DailyInstance(
        id = id,
        target = ScheduleTarget.Node("node_$id"),
        scheduledDate = 0L,
        titleSnapshot = "Task $id",
        descriptionSnapshot = "",
        plannedStartTime = startTime
    )
}
