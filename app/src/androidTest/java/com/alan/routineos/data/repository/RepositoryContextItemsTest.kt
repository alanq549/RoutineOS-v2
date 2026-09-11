package com.alan.routineos.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alan.routineos.data.local.RoutineOSDatabase
import com.alan.routineos.data.mapper.toDomain
import com.alan.routineos.domain.model.ActionProtocol
import com.alan.routineos.domain.model.DailyInstance
import com.alan.routineos.domain.model.DailyInstanceStatus
import com.alan.routineos.domain.model.Note
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

@RunWith(AndroidJUnit4::class)
class RepositoryContextItemsTest {
    private lateinit var db: RoutineOSDatabase
    private lateinit var repository: OfflineActivityRepository

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
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun upsertActivityWithContext_savesAllAtomically() = runBlocking {
        val anchor = createInstance("ancla", "Meeting")
        val task = createInstance("tarea", "Subir PDF").copy(
            associatedInstanceId = "ancla",
            actionProtocol = ActionProtocol.CHECK
        )
        val note = Note("note1", "Traer laptop", instanceId = "ancla", dateSnapshot = 0L, titleSnapshot = "Meeting")

        repository.upsertActivityWithContext(anchor, listOf(task), note)

        // Verify Anchor
        val savedAnchor = db.dailyInstanceDao().getInstancesForDate(0L).first().find { it.id == "ancla" }
        assertNotNull(savedAnchor)

        // Verify Task
        val savedTask = db.dailyInstanceDao().getInstancesForDate(0L).first().find { it.id == "tarea" }
        assertNotNull(savedTask)
        assertEquals("ancla", savedTask?.associatedInstanceId)
        assertEquals("CHECK", savedTask?.actionProtocol)

        // Verify Note
        val savedNote = db.noteDao().getAllNotes().first().find { it.id == "note1" }
        assertNotNull(savedNote)
        assertEquals("ancla", savedNote?.instanceId)
        assertEquals("Meeting", savedNote?.titleSnapshot)
    }

    @Test(expected = IllegalArgumentException::class)
    fun upsertActivityWithContext_failsOnDoubleReminders() = runBlocking {
        val anchor = createInstance("ancla", "Meeting").copy(
            reminderAbs = 600,
            reminderRel = -10
        )
        repository.upsertActivityWithContext(anchor, emptyList(), null)
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
