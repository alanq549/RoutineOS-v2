package com.alan.routineos.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alan.routineos.data.local.dao.ActivityDefinitionDao
import com.alan.routineos.data.local.dao.ActivityExecutionDao
import com.alan.routineos.data.local.dao.ActivityNodeDao
import com.alan.routineos.data.local.dao.ScheduleRuleDao
import com.alan.routineos.data.local.entities.ActivityDefinitionEntity
import com.alan.routineos.data.local.entities.ActivityExecutionEntity
import com.alan.routineos.data.local.entities.ActivityNodeEntity
import com.alan.routineos.data.local.entities.ScheduleRuleEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class SoftDeletePersistenceTest {

    private lateinit var db: RoutineOSDatabase
    private lateinit var definitionDao: ActivityDefinitionDao
    private lateinit var nodeDao: ActivityNodeDao
    private lateinit var ruleDao: ScheduleRuleDao
    private lateinit var executionDao: ActivityExecutionDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, RoutineOSDatabase::class.java).build()
        definitionDao = db.activityDefinitionDao()
        nodeDao = db.activityNodeDao()
        ruleDao = db.scheduleRuleDao()
        executionDao = db.activityExecutionDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun getNodesForActivityDefinition_excludesDeletedNodes() = runBlocking {
        val defId = "def1"
        definitionDao.insertActivityDefinition(ActivityDefinitionEntity(defId, "Title", "Desc"))
        
        val node1 = ActivityNodeEntity("n1", defId, null, 0, "Active", isDeleted = false)
        val node2 = ActivityNodeEntity("n2", defId, null, 1, "Deleted", isDeleted = true)
        
        nodeDao.insertNode(node1)
        nodeDao.insertNode(node2)

        val resultFlow = nodeDao.getNodesForActivityDefinition(defId).first()
        val resultList = nodeDao.getNodesListForActivityDefinition(defId)

        assertEquals("Operational Flow should only contain 1 node", 1, resultFlow.size)
        assertEquals("Operational List should only contain 1 node", 1, resultList.size)
        assertEquals("n1", resultFlow[0].id)
        assertEquals("n1", resultList[0].id)
    }

    @Test
    fun getRulesForNode_excludesRulesOfDeletedNodes() = runBlocking {
        val defId = "def1"
        definitionDao.insertActivityDefinition(ActivityDefinitionEntity(defId, "Title", "Desc"))
        
        val nodeDeleted = ActivityNodeEntity("n_del", defId, null, 0, "Deleted", isDeleted = true)
        nodeDao.insertNode(nodeDeleted)
        
        val rule = ScheduleRuleEntity("r1", activityNodeId = "n_del", type = "FIXED_DAYS")
        ruleDao.insertRule(rule)

        val result = ruleDao.getRulesForNode("n_del").first()
        
        assertTrue("Rules for deleted nodes should not be returned by operational query", result.isEmpty())
    }

    @Test
    fun executions_persistEvenIfNodeIsDeleted() = runBlocking {
        val defId = "def1"
        definitionDao.insertActivityDefinition(ActivityDefinitionEntity(defId, "Title", "Desc"))
        
        val node = ActivityNodeEntity("n1", defId, null, 0, "Title", isDeleted = true)
        nodeDao.insertNode(node)
        
        val execution = ActivityExecutionEntity("e1", "n1", scheduledDate = 0L, completedAt = 1000L)
        executionDao.insertExecution(execution)

        val result = executionDao.getExecutionsForNode("n1").first()
        
        assertEquals(1, result.size)
        assertEquals("e1", result[0].id)
    }
}
