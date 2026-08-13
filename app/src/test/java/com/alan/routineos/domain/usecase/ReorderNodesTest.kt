package com.alan.routineos.domain.usecase

import com.alan.routineos.data.local.dao.ActivityDefinitionDao
import com.alan.routineos.data.local.dao.ActivityExecutionDao
import com.alan.routineos.data.local.dao.ActivityNodeDao
import com.alan.routineos.data.local.dao.ScheduleExceptionDao
import com.alan.routineos.data.local.dao.ScheduleRuleDao
import com.alan.routineos.data.local.entities.ActivityNodeEntity
import com.alan.routineos.data.repository.OfflineActivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ReorderNodesTest {

    @Test
    fun `reorderNodes only affects siblings within same definition`() = runTest {
        val defId = "act1"
        val parentId = "parent1"
        
        val node1 = ActivityNodeEntity("1", defId, parentId, 0, "N1")
        val node2 = ActivityNodeEntity("2", defId, parentId, 1, "N2")
        val nodeOther = ActivityNodeEntity("3", "act_other", parentId, 0, "Other")
        
        val updatedNodes = mutableListOf<ActivityNodeEntity>()
        
        val nodeDao = object : FakeActivityNodeDao() {
            override suspend fun getNodeById(id: String): ActivityNodeEntity? {
                return when(id) {
                    "1" -> node1
                    "2" -> node2
                    "3" -> nodeOther
                    else -> null
                }
            }
            override suspend fun updateNodes(nodes: List<ActivityNodeEntity>) {
                updatedNodes.addAll(nodes)
            }
        }
        
        val repository = OfflineActivityRepository(
            activityDefinitionDao = FakeActivityDefinitionDao(),
            activityNodeDao = nodeDao,
            activityExecutionDao = FakeActivityExecutionDao(),
            scheduleRuleDao = FakeScheduleRuleDao(),
            scheduleExceptionDao = FakeScheduleExceptionDao(),
            validateActivityNodeUseCase = ValidateActivityNodeUseCase()
        )

        // Try to reorder 1, 2, and 3. 3 should be ignored because it belongs to another definition.
        repository.reorderNodes(listOf("2", "1", "3"))

        assertEquals(2, updatedNodes.size)
        assertEquals("2", updatedNodes[0].id)
        assertEquals(0, updatedNodes[0].position)
        assertEquals("1", updatedNodes[1].id)
        assertEquals(1, updatedNodes[1].position)
    }

    private open class FakeActivityNodeDao : ActivityNodeDao {
        override fun getNodesForActivityDefinition(activityDefinitionId: String): Flow<List<ActivityNodeEntity>> = TODO()
        override suspend fun insertNode(node: ActivityNodeEntity) {}
        override suspend fun deleteNode(node: ActivityNodeEntity) {}
        override suspend fun updateNodes(nodes: List<ActivityNodeEntity>) {}
        override suspend fun getNodeById(id: String): ActivityNodeEntity? = null
        override suspend fun getNodesListForActivityDefinition(activityDefinitionId: String): List<ActivityNodeEntity> = emptyList()
    }
    
    private class FakeActivityDefinitionDao : ActivityDefinitionDao {
        override fun getAllActivityDefinitions(): Flow<List<com.alan.routineos.data.local.entities.ActivityDefinitionEntity>> = TODO()
        override suspend fun getActivityDefinitionById(id: String): com.alan.routineos.data.local.entities.ActivityDefinitionEntity? = null
        override suspend fun insertActivityDefinition(activityDefinition: com.alan.routineos.data.local.entities.ActivityDefinitionEntity) {}
        override suspend fun deleteActivityDefinition(activityDefinition: com.alan.routineos.data.local.entities.ActivityDefinitionEntity) {}
        override suspend fun getDefinitionsList(): List<com.alan.routineos.data.local.entities.ActivityDefinitionEntity> = emptyList()
    }
    
    private class FakeActivityExecutionDao : ActivityExecutionDao {
        override suspend fun insertExecution(execution: com.alan.routineos.data.local.entities.ActivityExecutionEntity) = TODO()
        override fun getExecutionsForNode(nodeId: String): Flow<List<com.alan.routineos.data.local.entities.ActivityExecutionEntity>> = TODO()
        override fun getExecutionsForNodeOnDate(nodeId: String, scheduledDate: Long): Flow<List<com.alan.routineos.data.local.entities.ActivityExecutionEntity>> = TODO()
        override suspend fun deleteExecutionsForNodeOnDate(nodeId: String, scheduledDate: Long) = TODO()
        override suspend fun deleteExecutionsForNode(nodeId: String) = TODO()
    }
    
    private class FakeScheduleRuleDao : ScheduleRuleDao {
        override fun getRulesForNode(nodeId: String): Flow<List<com.alan.routineos.data.local.entities.ScheduleRuleEntity>> = TODO()
        override fun getRulesForDefinition(definitionId: String): Flow<List<com.alan.routineos.data.local.entities.ScheduleRuleEntity>> = TODO()
        override suspend fun insertRule(rule: com.alan.routineos.data.local.entities.ScheduleRuleEntity) = TODO()
        override suspend fun deleteRule(rule: com.alan.routineos.data.local.entities.ScheduleRuleEntity) = TODO()
    }
    
    private class FakeScheduleExceptionDao : ScheduleExceptionDao {
        override fun getExceptionsForRule(ruleId: String): Flow<List<com.alan.routineos.data.local.entities.ScheduleExceptionEntity>> = TODO()
        override suspend fun insertException(exception: com.alan.routineos.data.local.entities.ScheduleExceptionEntity) = TODO()
        override suspend fun deleteException(exception: com.alan.routineos.data.local.entities.ScheduleExceptionEntity) = TODO()
    }
}
