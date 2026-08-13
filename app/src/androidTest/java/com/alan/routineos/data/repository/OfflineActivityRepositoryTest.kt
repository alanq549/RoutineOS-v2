package com.alan.routineos.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alan.routineos.data.local.RoutineOSDatabase
import com.alan.routineos.data.local.dao.ActivityDefinitionDao
import com.alan.routineos.data.local.dao.ActivityNodeDao
import com.alan.routineos.domain.model.ActivityDefinition
import com.alan.routineos.domain.model.ActivityNode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class OfflineActivityRepositoryTest {

    private lateinit var db: RoutineOSDatabase
    private lateinit var activityDefinitionDao: ActivityDefinitionDao
    private lateinit var activityNodeDao: ActivityNodeDao
    private lateinit var repository: OfflineActivityRepository

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, RoutineOSDatabase::class.java).build()
        activityDefinitionDao = db.activityDefinitionDao()
        activityNodeDao = db.activityNodeDao()
        repository = OfflineActivityRepository(
            activityDefinitionDao = activityDefinitionDao,
            activityNodeDao = activityNodeDao,
            activityExecutionDao = db.activityExecutionDao(),
            scheduleRuleDao = db.scheduleRuleDao(),
            scheduleExceptionDao = db.scheduleExceptionDao(),
            validateActivityNodeUseCase = com.alan.routineos.domain.usecase.ValidateActivityNodeUseCase()
        )
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndReadActivityDefinitionFlow() = runBlocking {
        val activityDefinition = ActivityDefinition(id = "1", title = "Morning", description = "Desc")
        repository.upsertActivityDefinition(activityDefinition)
        
        val result = repository.getActivityDefinitions().first()
        assertEquals(1, result.size)
        assertEquals("Morning", result[0].title)
    }

    @Test
    fun getActivityDefinitionById() = runBlocking {
        val activityDefinition = ActivityDefinition(id = "1", title = "Morning", description = "Desc")
        repository.upsertActivityDefinition(activityDefinition)
        
        val result = repository.getActivityDefinitionById("1")
        assertNotNull(result)
        assertEquals("Morning", result?.title)
    }

    @Test
    fun upsertUpdatesExistingActivityDefinition() = runBlocking {
        val activityDefinition = ActivityDefinition(id = "1", title = "Old", description = "Desc")
        repository.upsertActivityDefinition(activityDefinition)
        
        val updated = activityDefinition.copy(title = "New")
        repository.upsertActivityDefinition(updated)
        
        val result = repository.getActivityDefinitionById("1")
        assertEquals("New", result?.title)
    }

    @Test
    fun deleteActivityDefinition() = runBlocking {
        val activityDefinition = ActivityDefinition(id = "1", title = "Delete", description = "Desc")
        repository.upsertActivityDefinition(activityDefinition)
        repository.deleteActivityDefinition(activityDefinition)
        
        val result = repository.getActivityDefinitions().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun getNodesForActivityDefinition() = runBlocking {
        val activityDefinition = ActivityDefinition(id = "ad1", title = "Activity", description = "Desc")
        repository.upsertActivityDefinition(activityDefinition)
        
        val node = ActivityNode(id = "n1", activityDefinitionId = "ad1", parentId = null, position = 0, title = "Node")
        repository.upsertNode(node)
        
        val result = repository.getNodesForActivityDefinition("ad1").first()
        assertEquals(1, result.size)
        assertEquals("Node", result[0].title)
    }

    @Test
    fun upsertNodeUpdatesExistingNode() = runBlocking {
        val activityDefinition = ActivityDefinition(id = "ad1", title = "Activity", description = "Desc")
        repository.upsertActivityDefinition(activityDefinition)

        val node = ActivityNode(id = "n1", activityDefinitionId = "ad1", parentId = null, position = 0, title = "Old Node")
        repository.upsertNode(node)

        val updated = node.copy(title = "New Node")
        repository.upsertNode(updated)

        val result = repository.getNodesForActivityDefinition("ad1").first()
        assertEquals(1, result.size)
        assertEquals("New Node", result[0].title)
    }

    @Test
    fun deleteNode() = runBlocking {
        val activityDefinition = ActivityDefinition(id = "ad1", title = "Activity", description = "Desc")
        repository.upsertActivityDefinition(activityDefinition)

        val node = ActivityNode(id = "n1", activityDefinitionId = "ad1", parentId = null, position = 0, title = "Node")
        repository.upsertNode(node)
        repository.deleteNode(node)

        val result = repository.getNodesForActivityDefinition("ad1").first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun deleteActivityDefinitionCascadesToNodes() = runBlocking {
        val activityDefinition = ActivityDefinition(id = "ad1", title = "Activity", description = "Desc")
        repository.upsertActivityDefinition(activityDefinition)

        val node = ActivityNode(id = "n1", activityDefinitionId = "ad1", parentId = null, position = 0, title = "Node")
        repository.upsertNode(node)

        repository.deleteActivityDefinition(activityDefinition)

        val result = repository.getNodesForActivityDefinition("ad1").first()
        assertTrue(result.isEmpty())
    }
}
