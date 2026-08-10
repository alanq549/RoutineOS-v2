package com.alan.routineos.feature.dashboard

import androidx.lifecycle.SavedStateHandle
import com.alan.routineos.domain.model.ActivityDefinition
import com.alan.routineos.domain.model.ActivityExecution
import com.alan.routineos.domain.model.ActivityNode
import com.alan.routineos.domain.repository.ActivityRepository
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
        repository.setNodes(listOf(ActivityNode("node1", "act1", "Node 1")))
        
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
        override suspend fun upsertNode(node: ActivityNode) = TODO()
        override suspend fun deleteNode(node: ActivityNode) = TODO()
        
        override suspend fun registerExecution(nodeId: String, metadataJson: String) {
            registerExecutionCallCount++
        }
        
        override fun getExecutionsForNode(nodeId: String): Flow<List<ActivityExecution>> = MutableStateFlow(emptyList())
        override suspend fun deleteExecutionsForNode(nodeId: String) {
            // Not used in this specific test case
        }
    }
}
