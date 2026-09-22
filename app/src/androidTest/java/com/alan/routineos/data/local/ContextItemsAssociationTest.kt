package com.alan.routineos.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alan.routineos.data.local.entities.DailyInstanceEntity
import com.alan.routineos.data.local.entities.NoteEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ContextItemsAssociationTest {
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
    fun v9_association_linksCorrectly() = runBlocking {
        val anchor = createInstance("ancla", "Meeting")
        val satellite = createInstance("sat", "Task").copy(associatedInstanceId = "ancla", actionProtocol = "CHECK")
        
        db.dailyInstanceDao().insertInstance(anchor)
        db.dailyInstanceDao().insertInstance(satellite)
        
        val retrieved = db.dailyInstanceDao().getInstancesForDate(0L).first()
        val sat = retrieved.find { it.id == "sat" }
        assertEquals("ancla", sat?.associatedInstanceId)
        assertEquals("CHECK", sat?.actionProtocol)
    }

    @Test
    fun v9_onDeleteAnchor_setsSatelliteToNull() = runBlocking {
        val anchor = createInstance("ancla", "Meeting")
        val satellite = createInstance("sat", "Task").copy(associatedInstanceId = "ancla")
        
        db.dailyInstanceDao().insertInstance(anchor)
        db.dailyInstanceDao().insertInstance(satellite)
        
        db.dailyInstanceDao().deleteInstanceById("ancla")
        
        val retrieved = db.dailyInstanceDao().getInstancesForDate(0L).first()
        val sat = retrieved.find { it.id == "sat" }
        assertNotNull("Satellite should still exist", sat)
        assertNull("associatedInstanceId should be NULL after anchor deletion", sat?.associatedInstanceId)
    }

    @Test
    fun v9_historyPreservation_completedTaskSurvives() = runBlocking {
        // 1. Create and complete task
        val anchor = createInstance("ancla", "Meeting")
        val satellite = createInstance("sat", "Task").copy(associatedInstanceId = "ancla", status = "COMPLETED")
        
        db.dailyInstanceDao().insertInstance(anchor)
        db.dailyInstanceDao().insertInstance(satellite)
        
        // 2. Delete anchor
        db.dailyInstanceDao().deleteInstanceById("ancla")
        
        // 3. Verify survival
        val all = db.dailyInstanceDao().getInstancesForDate(0L).first()
        val sat = all.find { it.id == "sat" }
        assertNotNull(sat)
        assertEquals("COMPLETED", sat?.status)
        assertNull(sat?.associatedInstanceId)
    }

    @Test
    fun v8_noteSnapshot_survivesAnchorReset() = runBlocking {
        // 1. Create anchor and note with snapshots
        val anchor = createInstance("ancla", "Meeting")
        val note = NoteEntity(
            id = "n1",
            content = "Important info",
            instanceId = "ancla",
            dateSnapshot = 0L,
            titleSnapshot = "Meeting"
        )
        
        db.dailyInstanceDao().insertInstance(anchor)
        db.noteDao().upsertNote(note)
        
        // 2. Reset (Delete) anchor
        db.dailyInstanceDao().deleteInstanceById("ancla")
        
        // 3. Verify note persists with context
        val allNotes = db.noteDao().getAllNotes().first()
        val n = allNotes.find { it.id == "n1" }
        assertNotNull(n)
        assertNull(n?.instanceId)
        assertEquals("Meeting", n?.titleSnapshot)
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
