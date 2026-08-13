package com.alan.routineos.domain.usecase

import com.alan.routineos.domain.model.*
import com.alan.routineos.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ParentCompletionRuleTest {

    @Test
    fun `parent is COMPLETED only if all children are COMPLETED`() = runTest {
        val defId = "act1"
        val nodes = listOf(
            ActivityNode("1", defId, null, 0, "Parent"),
            ActivityNode("1.1", defId, "1", 0, "Child 1"),
            ActivityNode("1.2", defId, "1", 1, "Child 2")
        )
        
        val repository = object : FakeActivityRepository() {
            override fun getNodesForActivityDefinition(activityDefinitionId: String) = flowOf(nodes)
            override suspend fun getNodesListForActivityDefinition(activityDefinitionId: String) = nodes
            override fun getExecutionsForNodeOnDate(nodeId: String, scheduledDate: Long): Flow<List<ActivityExecution>> {
                // Return execution only for Child 1
                return if (nodeId == "1.1") flowOf(listOf(ActivityExecution("e1", "1.1", 0, 0))) 
                       else flowOf(emptyList())
            }
        }
        
        val useCase = GetActivityTreeUseCase(repository)
        val result = useCase(defId, 0).first()

        assertEquals(NodeStatus.IN_PROGRESS, result[0].status)
    }

    @Test
    fun `parent is COMPLETED if all children are COMPLETED`() = runTest {
        val defId = "act1"
        val nodes = listOf(
            ActivityNode("1", defId, null, 0, "Parent"),
            ActivityNode("1.1", defId, "1", 0, "Child 1")
        )
        
        val repository = object : FakeActivityRepository() {
            override fun getNodesForActivityDefinition(activityDefinitionId: String) = flowOf(nodes)
            override suspend fun getNodesListForActivityDefinition(activityDefinitionId: String) = nodes
            override fun getExecutionsForNodeOnDate(nodeId: String, scheduledDate: Long) = 
                flowOf(listOf(ActivityExecution("e1", nodeId, 0, 0)))
        }
        
        val useCase = GetActivityTreeUseCase(repository)
        val result = useCase(defId, 0).first()

        assertEquals(NodeStatus.COMPLETED, result[0].status)
    }

    private open class FakeActivityRepository : ActivityRepository {
        override fun getActivityDefinitions(): Flow<List<ActivityDefinition>> = TODO()
        override suspend fun getActivityDefinitionById(id: String): ActivityDefinition? = TODO()
        override suspend fun upsertActivityDefinition(activityDefinition: ActivityDefinition) = TODO()
        override suspend fun deleteActivityDefinition(activityDefinition: ActivityDefinition) = TODO()
        override fun getNodesForActivityDefinition(activityDefinitionId: String): Flow<List<ActivityNode>> = TODO()
        override suspend fun getNodesListForActivityDefinition(activityDefinitionId: String): List<ActivityNode> = emptyList()
        override suspend fun getNodeById(id: String): ActivityNode? = null
        override suspend fun upsertNode(node: ActivityNode) {}
        override suspend fun deleteNode(node: ActivityNode) = TODO()
        override suspend fun reorderNodes(nodeIds: List<String>) = TODO()
        override suspend fun moveNode(nodeId: String, newParentId: String?) = TODO()
        override suspend fun registerExecution(nodeId: String, scheduledDate: Long, metadataJson: String) = TODO()
        override fun getExecutionsForNode(nodeId: String): Flow<List<ActivityExecution>> = TODO()
        override fun getExecutionsForNodeOnDate(nodeId: String, scheduledDate: Long): Flow<List<ActivityExecution>> = flowOf(emptyList())
        override suspend fun deleteExecutionsForNodeOnDate(nodeId: String, scheduledDate: Long) = TODO()
        override fun getRulesForNode(nodeId: String): Flow<List<ScheduleRule>> = TODO()
        override fun getRulesForDefinition(definitionId: String): Flow<List<ScheduleRule>> = TODO()
        override suspend fun upsertRule(rule: ScheduleRule) = TODO()
        override suspend fun deleteRule(rule: ScheduleRule) = TODO()
        override fun getExceptionsForRule(ruleId: String): Flow<List<ScheduleException>> = TODO()
        override suspend fun upsertException(exception: ScheduleException) = TODO()
        override suspend fun deleteException(exception: ScheduleException) = TODO()
    }
}
