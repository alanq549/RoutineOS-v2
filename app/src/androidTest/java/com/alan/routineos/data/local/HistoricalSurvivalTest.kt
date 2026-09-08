package com.alan.routineos.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alan.routineos.data.local.dao.*
import com.alan.routineos.data.local.entities.*
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class HistoricalSurvivalTest {

    private lateinit var db: RoutineOSDatabase
    private lateinit var definitionDao: ActivityDefinitionDao
    private lateinit var nodeDao: ActivityNodeDao
    private lateinit var instanceDao: DailyInstanceDao
    private lateinit var executionDao: ActivityExecutionDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, RoutineOSDatabase::class.java).build()
        definitionDao = db.activityDefinitionDao()
        nodeDao = db.activityNodeDao()
        instanceDao = db.dailyInstanceDao()
        executionDao = db.activityExecutionDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testHistoricalSurvival_DeleteNode_ExecutionPersistsWithSnapshot() = runBlocking {
        // 1. Setup Data
        val def = ActivityDefinitionEntity("def_1", "Gym", "Description", isDeleted = false)
        val node = ActivityNodeEntity("node_1", "def_1", null, 0, "Push Day", isDeleted = false)
        definitionDao.insertActivityDefinition(def)
        nodeDao.insertNode(node)

        // 2. Register Execution with Snapshots
        val exec = ActivityExecutionEntity(
            id = "exec_1",
            nodeId = "node_1",
            dailyInstanceId = null,
            scheduledDate = 100L,
            completedAt = 200L,
            activityIdSnapshot = "def_1",
            systemIdSnapshot = null,
            titleSnapshot = "Push Day"
        )
        executionDao.insertExecution(exec)

        // 3. Delete Node
        nodeDao.deleteNode(node)

        // 4. Verify Survival
        val allExecs = db.query("SELECT * FROM activity_executions", null)
        assertTrue(allExecs.moveToFirst())
        assertEquals("exec_1", allExecs.getString(allExecs.getColumnIndexOrThrow("id")))
        // nodeId should be NULL due to SET_NULL
        assertNull(allExecs.getString(allExecs.getColumnIndexOrThrow("nodeId")))
        // Snapshots should be intact
        assertEquals("def_1", allExecs.getString(allExecs.getColumnIndexOrThrow("activityIdSnapshot")))
        assertEquals("Push Day", allExecs.getString(allExecs.getColumnIndexOrThrow("titleSnapshot")))
    }

    @Test
    fun testHistoricalSurvival_DeleteInstance_ExecutionPersists() = runBlocking {
        // 1. Setup
        val instance = DailyInstanceEntity(
            id = "inst_1", targetId = null, targetType = "AD_HOC", scheduledDate = 100L,
            titleSnapshot = "Plan", descriptionSnapshot = "", status = "PLANNED", mobility = "FLEXIBLE"
        )
        instanceDao.insertInstance(instance)

        val exec = ActivityExecutionEntity(
            id = "exec_1", nodeId = null, dailyInstanceId = "inst_1",
            scheduledDate = 100L, completedAt = 200L,
            activityIdSnapshot = "UNKNOWN", systemIdSnapshot = null, titleSnapshot = "Ad-hoc"
        )
        executionDao.insertExecution(exec)

        // 2. Delete Instance
        instanceDao.deleteInstanceById("inst_1")

        // 3. Verify
        val allExecs = db.query("SELECT * FROM activity_executions", null)
        assertTrue(allExecs.moveToFirst())
        assertNull(allExecs.getString(allExecs.getColumnIndexOrThrow("dailyInstanceId")))
    }

    @Test(expected = android.database.sqlite.SQLiteConstraintException::class)
    fun testIntegrity_DeadlineRequiresExactlyOneOwner() = runBlocking {
        val deadline = DeadlineEntity(
            id = "dead_1",
            dueAt = 1000L,
            definitionId = "def_1",
            backlogId = "back_1" // VIOLATION: Two owners
        )
        db.deadlineDao().upsertDeadline(deadline)
    }

    @Test
    fun testIntegrity_NoteAllowsGlobal() = runBlocking {
        val note = NoteEntity(id = "note_1", content = "Hello world")
        db.noteDao().upsertNote(note)
        
        val allNotes = db.query("SELECT * FROM notes", null)
        assertTrue(allNotes.moveToFirst())
        assertEquals("note_1", allNotes.getString(allNotes.getColumnIndexOrThrow("id")))
    }
}
