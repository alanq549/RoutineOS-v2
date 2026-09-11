package com.alan.routineos.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alan.routineos.data.local.entities.DailyInstanceEntity
import com.alan.routineos.data.local.entities.ActivityExecutionEntity
import com.alan.routineos.data.local.entities.BacklogItemEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class TaskHistoryIntegrityTest {
    private lateinit var db: RoutineOSDatabase

    @Before
    fun createDb() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RoutineOSDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun sq6mbj_full_cycle_integrity_check() = runBlocking {
        // 1. Setup: Create Activity "Proyecto" (Ancla)
        val anchorId = "activity_proyecto"
        val anchor = createInstance(anchorId, "Proyecto").copy(actionProtocol = "TIMER")
        db.dailyInstanceDao().insertInstance(anchor)

        // 2. Setup: Associate Task CHECK "Subir PDF"
        val taskId = "task_pdf"
        val task = createInstance(taskId, "Subir PDF").copy(
            associatedInstanceId = anchorId,
            actionProtocol = "CHECK"
        )
        db.dailyInstanceDao().insertInstance(task)

        // 3. Action: Complete Task in Today (Simulate UseCase behavior)
        // Record the Fact
        val executionId = UUID.randomUUID().toString()
        val execution = ActivityExecutionEntity(
            id = executionId,
            nodeId = null,
            dailyInstanceId = taskId,
            scheduledDate = 0L,
            completedAt = System.currentTimeMillis(),
            metadataJson = "{}",
            activityIdSnapshot = "TASK_AD_HOC",
            systemIdSnapshot = null,
            titleSnapshot = "Subir PDF"
        )
        db.activityExecutionDao().insertExecution(execution)
        
        // Update Status
        db.dailyInstanceDao().insertInstance(task.copy(status = "COMPLETED"))

        // 4. State Check: Verify Execution exists
        val execsBefore = db.activityExecutionDao().getAllExecutions().first()
        assertEquals(1, execsBefore.size)
        assertEquals("Subir PDF", execsBefore[0].titleSnapshot)

        // 5. Elimination: Delete/Reset Activity "Proyecto" (Ancla)
        db.dailyInstanceDao().deleteInstanceById(anchorId)

        // 6. Validation: Task survives (via V9 associatedInstanceId SET NULL)
        val instancesAfter = db.dailyInstanceDao().getInstancesForDate(0L).first()
        val taskAfter = instancesAfter.find { it.id == taskId }
        assertNotNull("Task must survive anchor deletion", taskAfter)
        assertNull("Association must be cleared (SET NULL)", taskAfter?.associatedInstanceId)

        // 7. Validation: Execution survives
        val execsAfter = db.activityExecutionDao().getAllExecutions().first()
        assertEquals("Historical execution must survive", 1, execsAfter.size)
        assertEquals("Subir PDF", execsAfter[0].titleSnapshot)
        assertNull("Execution should lose link to instance if instance deleted", execsAfter[0].dailyInstanceId)

        // 8. Stats Simulation: Confirm it doesn't add to TIMER duration
        // (Conceptual check: durationMinutes in daily_instances is NULL or 0 for POINT tasks)
        assertNull("CHECK Task should not have planned duration", taskAfter?.plannedDurationMinutes)
    }

    @Test
    fun task_from_backlog_history_preserves_backlog_identity() = runBlocking {
        // 1. Setup: Backlog Item
        val backlogId = "bi_1"
        db.backlogItemDao().upsertBacklogItem(BacklogItemEntity(backlogId, null, "Fix code", "OPEN"))

        // 2. Plan: Create Instance from Backlog
        val taskId = "inst_bi_1"
        val task = createInstance(taskId, "Fix code").copy(
            backlogId = backlogId,
            actionProtocol = "CHECK"
        )
        db.dailyInstanceDao().insertInstance(task)

        // 3. Action: Complete
        val execution = ActivityExecutionEntity(
            id = "exec_1",
            nodeId = null,
            dailyInstanceId = taskId,
            scheduledDate = 0L,
            completedAt = System.currentTimeMillis(),
            metadataJson = "{}",
            activityIdSnapshot = "BACKLOG:$backlogId",
            systemIdSnapshot = null,
            titleSnapshot = "Fix code"
        )
        db.activityExecutionDao().insertExecution(execution)

        // 4. Verify History Identity
        val retrieved = db.activityExecutionDao().getAllExecutions().first()[0]
        assertEquals("BACKLOG:bi_1", retrieved.activityIdSnapshot)
    }

    private fun createInstance(id: String, title: String) = DailyInstanceEntity(
        id = id,
        targetId = null,
        targetType = "AD_HOC",
        scheduledDate = 0L,
        titleSnapshot = title,
        descriptionSnapshot = "",
        status = "PLANNED",
        mobility = "FLEXIBLE",
        actionProtocol = "TIMER"
    )
}
