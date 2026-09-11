package com.alan.routineos.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alan.routineos.data.local.entities.DailyInstanceEntity
import com.alan.routineos.data.local.entities.ActivityExecutionEntity
import com.alan.routineos.domain.model.ActionProtocol
import com.alan.routineos.domain.model.DailyInstance
import com.alan.routineos.domain.model.DailyInstanceStatus
import com.alan.routineos.domain.model.HierarchicalTimelineEntry
import com.alan.routineos.domain.usecase.DailyAction
import com.alan.routineos.domain.usecase.MaterializeInstanceUseCase
import com.alan.routineos.domain.usecase.RegisterDailyActionUseCase
import com.alan.routineos.domain.usecase.TimelineEntry
import com.alan.routineos.data.repository.OfflineActivityRepository
import com.alan.routineos.domain.usecase.ValidateActivityNodeUseCase
import com.alan.routineos.domain.usecase.ValidateMetadataSchemaUseCase
import com.alan.routineos.domain.usecase.ValidateScheduleRuleUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class ContextItemsPolymorphismTest {
    private lateinit var db: RoutineOSDatabase
    private lateinit var repository: OfflineActivityRepository
    private lateinit var registerActionUseCase: RegisterDailyActionUseCase

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RoutineOSDatabase::class.java
        ).build()
        
        repository = OfflineActivityRepository(
            database = db,
            activityDefinitionDao = db.activityDefinitionDao(),
            activityNodeDao = db.activityNodeDao(),
            activityExecutionDao = db.activityExecutionDao(),
            scheduleRuleDao = db.scheduleRuleDao(),
            scheduleExceptionDao = db.scheduleExceptionDao(),
            dailyInstanceDao = db.dailyInstanceDao(),
            metadataSchemaDao = db.metadataSchemaDao(),
            systemDao = db.systemDao(),
            noteDao = db.noteDao(),
            backlogItemDao = db.backlogItemDao(),
            deadlineDao = db.deadlineDao(),
            validateActivityNodeUseCase = ValidateActivityNodeUseCase(),
            validateScheduleRuleUseCase = ValidateScheduleRuleUseCase(),
            validateMetadataSchemaUseCase = ValidateMetadataSchemaUseCase()
        )
        
        registerActionUseCase = RegisterDailyActionUseCase(repository, MaterializeInstanceUseCase(repository))
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun notify_protocol_does_not_register_execution() = runBlocking {
        val instance = createInstance("rem", "Reminder").copy(actionProtocol = ActionProtocol.NOTIFY)
        repository.upsertDailyInstance(instance)
        
        val entry = HierarchicalTimelineEntry(root = TimelineEntry(instance, true))
        registerActionUseCase(entry, DailyAction.Complete())
        
        val executions = db.activityExecutionDao().getAllExecutions().first()
        assertTrue("NOTIFY should not generate ActivityExecution", executions.isEmpty())
        
        val saved = db.dailyInstanceDao().getInstancesForDate(0L).first()[0]
        assertEquals("COMPLETED", saved.status)
    }

    @Test
    fun check_independent_registers_execution() = runBlocking {
        val instance = createInstance("task", "Independent Task").copy(
            actionProtocol = ActionProtocol.CHECK,
            associatedInstanceId = null
        )
        repository.upsertDailyInstance(instance)
        
        val entry = HierarchicalTimelineEntry(root = TimelineEntry(instance, true))
        registerActionUseCase(entry, DailyAction.Complete())
        
        val executions = db.activityExecutionDao().getAllExecutions().first()
        assertEquals(1, executions.size)
        assertEquals("Independent Task", executions[0].titleSnapshot)
    }

    @Test
    fun linking_task_to_activity_preserves_identity() = runBlocking {
        // 1. Create independent task
        val task = createInstance("t1", "Task").copy(actionProtocol = ActionProtocol.CHECK)
        repository.upsertDailyInstance(task)
        
        // 2. Create activity
        val activity = createInstance("a1", "Activity")
        repository.upsertDailyInstance(activity)
        
        // 3. Link them later
        val linkedTask = task.copy(associatedInstanceId = "a1")
        repository.upsertDailyInstance(linkedTask)
        
        val all = db.dailyInstanceDao().getInstancesForDate(0L).first()
        val retrievedTask = all.find { it.id == "t1" }
        assertEquals("a1", retrievedTask?.associatedInstanceId)
        assertEquals(2, all.size) // No duplication
    }

    private fun createInstance(id: String, title: String) = DailyInstance(
        id = id,
        target = null,
        scheduledDate = 0L,
        titleSnapshot = title,
        descriptionSnapshot = "",
        status = DailyInstanceStatus.PLANNED,
        actionProtocol = ActionProtocol.TIMER
    )
}
