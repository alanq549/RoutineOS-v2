package com.alan.routineos.domain.usecase

import com.alan.routineos.domain.model.*
import com.alan.routineos.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ActivityNodeTreeMapperTest {

    @Test
    fun `reconstructs 3-level tree correctly`() = runTest {
        val defId = "act1"
        val nodes = listOf(
            ActivityNode("1", defId, null, 0, "Root"),
            ActivityNode("1.1", defId, "1", 0, "Child 1"),
            ActivityNode("1.1.1", defId, "1.1", 0, "Grandchild 1")
        )
        
        val repository = object : FakeActivityRepository() {
            override fun getNodesForActivityDefinition(activityDefinitionId: String) = flowOf(nodes)
            override suspend fun getNodesListForActivityDefinition(activityDefinitionId: String) = nodes
            override suspend fun getNodeById(id: String) = nodes.find { it.id == id }
        }
        
        val useCase = GetActivityTreeUseCase(repository)
        val result = useCase(defId, 0).first()

        assertEquals(1, result.size)
        val root = result[0]
        assertEquals("1", root.node.id)
        assertEquals(1, root.children.size)
        
        val child = root.children[0]
        assertEquals("1.1", child.node.id)
        assertEquals(1, child.children.size)
        
        val grandchild = child.children[0]
        assertEquals("1.1.1", grandchild.node.id)
        assertEquals(0, grandchild.children.size)
    }

    private open class FakeActivityRepository : ActivityRepository {
        override fun getActivityDefinitions(): Flow<List<ActivityDefinition>> = TODO()
        override fun getAllNodes(): Flow<List<ActivityNode>> = TODO()
        override suspend fun getActivityDefinitionById(id: String): ActivityDefinition? = null
        override suspend fun upsertActivityDefinition(activityDefinition: ActivityDefinition) {}
        override suspend fun deleteActivityDefinition(activityDefinition: ActivityDefinition) {}
        override fun getNodesForActivityDefinition(activityDefinitionId: String): Flow<List<ActivityNode>> = TODO()
        override suspend fun getNodesListForActivityDefinition(activityDefinitionId: String): List<ActivityNode> = emptyList()
        override suspend fun getNodeById(id: String): ActivityNode? = null
        override suspend fun upsertNode(node: ActivityNode) {}
        override suspend fun deleteNode(node: ActivityNode) {}
        override suspend fun reorderNodes(nodeIds: List<String>) {}
        override suspend fun moveNode(nodeId: String, newParentId: String?) {}
        override suspend fun registerExecution(nodeId: String, scheduledDate: Long, metadataJson: String, dailyInstanceId: String?) {}
        override fun getExecutionsForNode(nodeId: String): Flow<List<ActivityExecution>> = TODO()
        override fun getExecutionsForNodeOnDate(nodeId: String, scheduledDate: Long): Flow<List<ActivityExecution>> = flowOf(emptyList())
        override suspend fun deleteExecutionsForNodeOnDate(nodeId: String, scheduledDate: Long) {}
        override fun getAllRules(): Flow<List<ScheduleRule>> = flowOf(emptyList())
        override fun getAllExceptions(): Flow<List<ScheduleException>> = flowOf(emptyList())
        override fun getRulesForNode(nodeId: String): Flow<List<ScheduleRule>> = flowOf(emptyList())
        override suspend fun getRulesListForNode(nodeId: String): List<ScheduleRule> = emptyList()
        override fun getRulesForDefinition(definitionId: String): Flow<List<ScheduleRule>> = flowOf(emptyList())
        override suspend fun getRulesListForDefinition(definitionId: String): List<ScheduleRule> = emptyList()
        override fun getRulesForActivityTree(definitionId: String): Flow<List<ScheduleRule>> = flowOf(emptyList())
        override suspend fun upsertRule(rule: ScheduleRule) {}
        override suspend fun deleteRule(rule: ScheduleRule) {}
        override fun getExceptionsForRule(ruleId: String): Flow<List<ScheduleException>> = flowOf(emptyList())
        override suspend fun upsertException(exception: ScheduleException) {}
        override suspend fun deleteException(exception: ScheduleException) {}
        override fun getDailyInstancesForDate(date: Long): Flow<List<DailyInstance>> = flowOf(emptyList())
        override suspend fun upsertDailyInstance(instance: DailyInstance) {}
        override suspend fun getDailyInstanceByTarget(targetId: String, date: Long): DailyInstance? = null
    }
}
