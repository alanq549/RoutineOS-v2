package com.alan.routineos.domain.usecase

import com.alan.routineos.domain.model.*
import com.alan.routineos.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class AddChildTest {

    @Test
    fun `AddChild creates node with correct parent and position`() = runTest {
        val defId = "act1"
        val parentId = "parent1"
        val existingNodes = listOf(
            ActivityNode("parent1", defId, null, 0, "Parent"),
            ActivityNode("child1", defId, parentId, 0, "Child 1")
        )
        
        var savedNode: ActivityNode? = null
        val repository = object : FakeActivityRepository() {
            override suspend fun getNodeById(id: String) = existingNodes.find { it.id == id }
            override suspend fun getNodesListForActivityDefinition(activityDefinitionId: String) = existingNodes
            override suspend fun upsertNode(node: ActivityNode) {
                savedNode = node
            }
        }
        
        val useCase = AddChildUseCase(repository, ValidateActivityNodeUseCase())
        val result = useCase(parentId, "New Child")

        assertEquals(parentId, savedNode?.parentId)
        assertEquals(1, savedNode?.position) // Next position after Child 1
        assertEquals(defId, savedNode?.activityDefinitionId)
        assertFalse(savedNode?.isDeleted ?: true)
        assertEquals("New Child", result.title)
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
        override fun getAllRules(): Flow<List<ScheduleRule>> = TODO()
        override fun getAllExceptions(): Flow<List<ScheduleException>> = TODO()
        override fun getRulesForNode(nodeId: String): Flow<List<ScheduleRule>> = flowOf(emptyList())
        override fun getRulesForDefinition(definitionId: String): Flow<List<ScheduleRule>> = flowOf(emptyList())
        override suspend fun upsertRule(rule: ScheduleRule) {}
        override suspend fun deleteRule(rule: ScheduleRule) {}
        override fun getExceptionsForRule(ruleId: String): Flow<List<ScheduleException>> = flowOf(emptyList())
        override suspend fun upsertException(exception: ScheduleException) {}
        override suspend fun deleteException(exception: ScheduleException) {}
        override fun getDailyInstancesForDate(date: Long): Flow<List<DailyInstance>> = TODO()
        override suspend fun upsertDailyInstance(instance: DailyInstance) {}
        override suspend fun getDailyInstanceByTarget(targetId: String, date: Long): DailyInstance? = null
    }
}
