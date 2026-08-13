package com.alan.routineos.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alan.routineos.data.local.dao.ActivityDefinitionDao
import com.alan.routineos.data.local.dao.ActivityNodeDao
import com.alan.routineos.data.local.entities.ActivityDefinitionEntity
import com.alan.routineos.data.local.entities.ActivityNodeEntity
import com.alan.routineos.data.mapper.toDomain
import com.alan.routineos.data.repository.OfflineActivityRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class HierarchyIntegrityTest {

    private lateinit var db: RoutineOSDatabase
    private lateinit var definitionDao: ActivityDefinitionDao
    private lateinit var nodeDao: ActivityNodeDao
    private lateinit var repository: OfflineActivityRepository

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, RoutineOSDatabase::class.java).build()
        definitionDao = db.activityDefinitionDao()
        nodeDao = db.activityNodeDao()
        repository = OfflineActivityRepository(
            activityDefinitionDao = definitionDao,
            activityNodeDao = nodeDao,
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
    fun updateParentTitle_preservesChildren() = runBlocking {
        val defId = "act1"
        definitionDao.insertActivityDefinition(ActivityDefinitionEntity(defId, "Title", "Desc"))
        
        val parentNode = ActivityNodeEntity("parent", defId, null, 0, "Original Parent", "Desc", false)
        val childNode = ActivityNodeEntity("child", defId, "parent", 0, "Child", "Desc", false)
        
        nodeDao.insertNode(parentNode)
        nodeDao.insertNode(childNode)

        // Verify initial state
        val nodesBefore = nodeDao.getNodesListForActivityDefinition(defId)
        assertEquals(2, nodesBefore.size)
        assertTrue(nodesBefore.any { it.id == "child" && it.parentId == "parent" })

        // 1. Fetch parent as domain model (this is what UpdateNodeUseCase does)
        val parentDomain = nodeDao.getNodeById("parent")!!.toDomain()
        
        // 2. Modify title
        val updatedParent = parentDomain.copy(title = "New Title")
        
        // 3. Upsert through repository (this triggers the dangerous REPLACE)
        repository.upsertNode(updatedParent)

        // 4. Verify state after update
        val nodesAfter = nodeDao.getNodesListForActivityDefinition(defId)
        
        // REPRODUCTION OF THE BUG: If REPLACE triggered CASCADE, child will be gone
        val parentAfter = nodesAfter.find { it.id == "parent" }
        val childAfter = nodesAfter.find { it.id == "child" }

        assertNotNull("Parent should still exist", parentAfter)
        assertEquals("New Title", parentAfter?.title)
        assertNotNull("BUG DETECTED: Child was deleted due to REPLACE cascade", childAfter)
        assertEquals("parent", childAfter?.parentId)
    }
}
