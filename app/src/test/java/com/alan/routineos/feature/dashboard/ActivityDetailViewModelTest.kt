package com.alan.routineos.feature.dashboard

import androidx.lifecycle.SavedStateHandle
import com.alan.routineos.domain.model.ActivityDefinition
import com.alan.routineos.domain.model.ActivityExecution
import com.alan.routineos.domain.model.ActivityNode
import com.alan.routineos.domain.repository.ActivityRepository
import com.alan.routineos.domain.usecase.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ActivityDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeActivityRepository
    private lateinit var viewModel: ActivityDetailViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeActivityRepository()
        viewModel = ActivityDetailViewModel(
            repository = repository,
            getActivityTreeUseCase = GetActivityTreeUseCase(repository),
            addChildUseCase = AddChildUseCase(repository, ValidateActivityNodeUseCase()),
            updateNodeUseCase = UpdateNodeUseCase(repository),
            deleteBranchUseCase = DeleteBranchUseCase(repository),
            restoreBranchUseCase = RestoreBranchUseCase(repository),
            savedStateHandle = SavedStateHandle(mapOf("activityId" to "act1"))
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `toggleNodeCompletion avoids duplicate calls on fast double-tap`() = runTest {
        val nodeId = "node1"
        // Initial state: 1 node, not completed
        repository.setNodes(listOf(ActivityNode(
            id = "node1",
            activityDefinitionId = "act1",
            parentId = null,
            position = 0,
            title = "Node 1"
        )))
        
        // Advance to collect initial state in the ViewModel's flow
        advanceUntilIdle()

        // Trigger toggle twice very fast (before first one finishes)
        viewModel.toggleNodeCompletion(nodeId)
        viewModel.toggleNodeCompletion(nodeId)

        // Advance until all coroutines finish
        advanceUntilIdle()

        // Repository should only have been called once for registration
        assertEquals("Should only register once", 1, repository.registerExecutionCallCount)
    }

    private class FakeActivityRepository : ActivityRepository {
        var registerExecutionCallCount = 0
        private val _nodes = MutableStateFlow<List<ActivityNode>>(emptyList())

        fun setNodes(nodes: List<ActivityNode>) {
            _nodes.value = nodes
        }

        override fun getActivityDefinitions(): Flow<List<ActivityDefinition>> = TODO()
        override suspend fun getActivityDefinitionById(id: String): ActivityDefinition? = null
        override suspend fun upsertActivityDefinition(activityDefinition: ActivityDefinition) = TODO()
        override suspend fun deleteActivityDefinition(activityDefinition: ActivityDefinition) = TODO()
        override fun getNodesForActivityDefinition(activityDefinitionId: String): Flow<List<ActivityNode>> = _nodes
        override suspend fun getNodesListForActivityDefinition(activityDefinitionId: String): List<ActivityNode> = _nodes.value
        override suspend fun getNodeById(id: String): ActivityNode? = _nodes.value.find { it.id == id }
        override suspend fun upsertNode(node: ActivityNode) {}
        override suspend fun deleteNode(node: ActivityNode) = TODO()
        override suspend fun reorderNodes(nodeIds: List<String>) = TODO()
        override suspend fun moveNode(nodeId: String, newParentId: String?) = TODO()
        
        override suspend fun registerExecution(nodeId: String, scheduledDate: Long, metadataJson: String) {
            registerExecutionCallCount++
        }
        
        override fun getExecutionsForNode(nodeId: String): Flow<List<ActivityExecution>> = MutableStateFlow(emptyList())

        override fun getExecutionsForNodeOnDate(nodeId: String, scheduledDate: Long): Flow<List<ActivityExecution>> = MutableStateFlow(emptyList())

        override suspend fun deleteExecutionsForNodeOnDate(nodeId: String, scheduledDate: Long) {
            // Not used in this specific test case
        }

        override fun getRulesForNode(nodeId: String): Flow<List<com.alan.routineos.domain.model.ScheduleRule>> = TODO()
        override fun getRulesForDefinition(definitionId: String): Flow<List<com.alan.routineos.domain.model.ScheduleRule>> = TODO()
        override suspend fun upsertRule(rule: com.alan.routineos.domain.model.ScheduleRule) = TODO()
        override suspend fun deleteRule(rule: com.alan.routineos.domain.model.ScheduleRule) = TODO()
        override fun getExceptionsForRule(ruleId: String): Flow<List<com.alan.routineos.domain.model.ScheduleException>> = TODO()
        override suspend fun upsertException(exception: com.alan.routineos.domain.model.ScheduleException) = TODO()
        override suspend fun deleteException(exception: com.alan.routineos.domain.model.ScheduleException) = TODO()
    }
}
