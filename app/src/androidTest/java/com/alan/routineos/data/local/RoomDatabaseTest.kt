package com.alan.routineos.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alan.routineos.data.local.dao.ActivityDefinitionDao
import com.alan.routineos.data.local.dao.ActivityNodeDao
import com.alan.routineos.data.local.entities.ActivityDefinitionEntity
import com.alan.routineos.data.local.entities.ActivityNodeEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class RoomDatabaseTest {

    private lateinit var db: RoutineOSDatabase
    private lateinit var activityDefinitionDao: ActivityDefinitionDao
    private lateinit var activityNodeDao: ActivityNodeDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, RoutineOSDatabase::class.java).build()
        activityDefinitionDao = db.activityDefinitionDao()
        activityNodeDao = db.activityNodeDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndReadActivityDefinition() = runBlocking {
        val activityDefinition = ActivityDefinitionEntity(id = "1", title = "Morning Activity", description = "Test description")
        activityDefinitionDao.insertActivityDefinition(activityDefinition)
        val allActivityDefinitions = activityDefinitionDao.getAllActivityDefinitions().first()
        assertEquals(1, allActivityDefinitions.size)
        assertEquals("Morning Activity", allActivityDefinitions[0].title)
    }

    @Test
    @Throws(Exception::class)
    fun updateActivityDefinition() = runBlocking {
        val activityDefinition = ActivityDefinitionEntity(id = "1", title = "Old Title", description = "Old")
        activityDefinitionDao.insertActivityDefinition(activityDefinition)
        
        val updatedActivityDefinition = activityDefinition.copy(title = "New Title")
        activityDefinitionDao.insertActivityDefinition(updatedActivityDefinition)
        
        val result = activityDefinitionDao.getActivityDefinitionById("1")
        assertEquals("New Title", result?.title)
    }

    @Test
    @Throws(Exception::class)
    fun deleteActivityDefinition() = runBlocking {
        val activityDefinition = ActivityDefinitionEntity(id = "1", title = "Delete Me", description = "Test")
        activityDefinitionDao.insertActivityDefinition(activityDefinition)
        activityDefinitionDao.deleteActivityDefinition(activityDefinition)
        
        val allActivityDefinitions = activityDefinitionDao.getAllActivityDefinitions().first()
        assertTrue(allActivityDefinitions.isEmpty())
    }

    @Test
    @Throws(Exception::class)
    fun cascadeDeleteNodes() = runBlocking {
        val activityDefinition = ActivityDefinitionEntity(id = "ad_1", title = "Parent", description = "Parent description")
        activityDefinitionDao.insertActivityDefinition(activityDefinition)
        
        val node = ActivityNodeEntity(id = "node_1", activityDefinitionId = "ad_1", parentId = null, position = 0, title = "Child Node")
        activityNodeDao.insertNode(node)
        
        // Verify node exists
        val nodesBefore = activityNodeDao.getNodesForActivityDefinition("ad_1").first()
        assertEquals(1, nodesBefore.size)
        
        // Delete parent
        activityDefinitionDao.deleteActivityDefinition(activityDefinition)
        
        // Verify node is gone
        val nodesAfter = activityNodeDao.getNodesForActivityDefinition("ad_1").first()
        assertTrue(nodesAfter.isEmpty())
    }

    @Test
    @Throws(Exception::class)
    fun readNonExistentActivityDefinitionReturnsNull() = runBlocking {
        val result = activityDefinitionDao.getActivityDefinitionById("999")
        assertNull(result)
    }
}
