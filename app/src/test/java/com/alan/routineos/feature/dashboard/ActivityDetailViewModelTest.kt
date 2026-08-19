package com.alan.routineos.feature.today

import androidx.lifecycle.SavedStateHandle
import com.alan.routineos.domain.model.*
import com.alan.routineos.domain.repository.ActivityRepository
import com.alan.routineos.domain.usecase.*
import com.alan.routineos.feature.dashboard.ActivityDetailViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
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
            assignActivityToSystemUseCase = AssignActivityToSystemUseCase(repository),
            unassignActivityFromSystemUseCase = UnassignActivityFromSystemUseCase(repository),
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

        override fun getActivityDefinitions(): Flow<List<ActivityDefinition>> = flowOf(emptyList())
        override fun getAllNodes(): Flow<List<ActivityNode>> = flowOf(_nodes.value)
        override suspend fun getActivityDefinitionById(id: String): ActivityDefinition? = null
        override suspend fun upsertActivityDefinition(activityDefinition: ActivityDefinition) {}
        override suspend fun deleteActivityDefinition(activityDefinition: ActivityDefinition) {}
        override fun getNodesForActivityDefinition(activityDefinitionId: String): Flow<List<ActivityNode>> = _nodes
        override suspend fun getNodesListForActivityDefinition(activityDefinitionId: String): List<ActivityNode> = _nodes.value
        override suspend fun getNodeById(id: String): ActivityNode? = _nodes.value.find { it.id == id }
        override suspend fun upsertNode(node: ActivityNode) {}
        override suspend fun deleteNode(node: ActivityNode) {}
        override suspend fun reorderNodes(nodeIds: List<String>) {}
        override suspend fun moveNode(nodeId: String, newParentId: String?) {}
        
        override suspend fun registerExecution(nodeId: String, scheduledDate: Long, metadataJson: String, dailyInstanceId: String?) {
            registerExecutionCallCount++
        }
        
        override fun getAllExecutions(): Flow<List<ActivityExecution>> = flowOf(emptyList())
        override fun getExecutionsForNode(nodeId: String): Flow<List<ActivityExecution>> = MutableStateFlow(emptyList())

        override fun getExecutionsForNodeOnDate(nodeId: String, scheduledDate: Long): Flow<List<ActivityExecution>> = MutableStateFlow(emptyList())

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
        override fun getDailyInstancesForDateRange(start: Long, end: Long): Flow<List<DailyInstance>> = flowOf(emptyList())
        override suspend fun upsertDailyInstance(instance: DailyInstance) {}
        override suspend fun getDailyInstanceByTarget(targetId: String, date: Long): DailyInstance? = null
        override fun getMetadataSchema(targetId: String, targetType: String): Flow<MetadataSchema?> = flowOf(null)
        override suspend fun upsertMetadataSchema(schema: MetadataSchema) {}
        override suspend fun deleteMetadataSchema(targetId: String, targetType: String) {}
        override fun getAllSystems(): Flow<List<LifeSystem>> = flowOf(emptyList())
        override suspend fun getSystemsList(): List<LifeSystem> = emptyList()
        override suspend fun getSystemById(id: String): LifeSystem? = null
        override suspend fun upsertSystem(system: LifeSystem) {}
        override suspend fun deleteSystem(system: LifeSystem) {}
    }
}
