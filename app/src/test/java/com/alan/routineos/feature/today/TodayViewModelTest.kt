package com.alan.routineos.feature.today

import com.alan.routineos.domain.model.*
import com.alan.routineos.domain.repository.ActivityRepository
import com.alan.routineos.domain.usecase.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class TodayViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: TodayViewModel
    private lateinit var repository: FakeActivityRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeActivityRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadData populates uiState with resolved timeline`() = runTest {
        val date = LocalDate.now()
        val node = ActivityNode("n1", "act1", null, 0, "Task 1")
        val rule = ScheduleRule(
            id = "r1",
            target = ScheduleTarget.Node("n1"),
            type = ScheduleRuleType.FIXED_DAYS,
            daysOfWeek = setOf(date.dayOfWeek.value),
            startTime = 480
        )

        repository.setNodes(listOf(node))
        repository.setRules(listOf(rule))
        
        val resolveTimelineUseCase = ResolveTimelineUseCase(repository, ConflictDetectorUseCase())
        val getHierarchicalTimelineUseCase = GetHierarchicalTimelineUseCase(repository, resolveTimelineUseCase)
        val registerDailyActionUseCase = RegisterDailyActionUseCase(repository, MaterializeInstanceUseCase(repository))
        
        viewModel = TodayViewModel(repository, getHierarchicalTimelineUseCase, registerDailyActionUseCase)
        
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.timelineItems.size)
        assertEquals("Task 1", state.timelineItems[0].title)
        assertEquals("08:00", state.timelineItems[0].timeRangeText)
    }

    private class FakeActivityRepository : ActivityRepository {
        private var nodes = emptyList<ActivityNode>()
        private var rules = emptyList<ScheduleRule>()

        fun setNodes(nodes: List<ActivityNode>) { this.nodes = nodes }
        fun setRules(rules: List<ScheduleRule>) { this.rules = rules }

        override fun getActivityDefinitions(): Flow<List<ActivityDefinition>> = flowOf(emptyList())
        override fun getAllNodes(): Flow<List<ActivityNode>> = flowOf(nodes)
        override suspend fun getActivityDefinitionById(id: String): ActivityDefinition? = null
        override suspend fun upsertActivityDefinition(activityDefinition: ActivityDefinition) {}
        override suspend fun deleteActivityDefinition(activityDefinition: ActivityDefinition) {}
        override fun getNodesForActivityDefinition(activityDefinitionId: String): Flow<List<ActivityNode>> = flowOf(nodes)
        override suspend fun getNodesListForActivityDefinition(activityDefinitionId: String): List<ActivityNode> = nodes
        override suspend fun getNodeById(id: String): ActivityNode? = nodes.find { it.id == id }
        override suspend fun upsertNode(node: ActivityNode) {}
        override suspend fun deleteNode(node: ActivityNode) {}
        override suspend fun reorderNodes(nodeIds: List<String>) {}
        override suspend fun moveNode(nodeId: String, newParentId: String?) {}
        override suspend fun registerExecution(nodeId: String, scheduledDate: Long, metadataJson: String, dailyInstanceId: String?) {}
        override fun getExecutionsForNode(nodeId: String): Flow<List<ActivityExecution>> = flowOf(emptyList())
        override fun getExecutionsForNodeOnDate(nodeId: String, scheduledDate: Long): Flow<List<ActivityExecution>> = flowOf(emptyList())
        override suspend fun deleteExecutionsForNodeOnDate(nodeId: String, scheduledDate: Long) {}
        override fun getAllRules(): Flow<List<ScheduleRule>> = flowOf(rules)
        override fun getAllExceptions(): Flow<List<ScheduleException>> = flowOf(emptyList())
        override fun getRulesForNode(nodeId: String): Flow<List<ScheduleRule>> = flowOf(rules.filter { (it.target as? ScheduleTarget.Node)?.id == nodeId })
        override suspend fun getRulesListForNode(nodeId: String): List<ScheduleRule> = rules.filter { (it.target as? ScheduleTarget.Node)?.id == nodeId }
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
        override fun getMetadataSchema(targetId: String, targetType: String): Flow<MetadataSchema?> = flowOf(null)
        override suspend fun upsertMetadataSchema(schema: MetadataSchema) {}
        override suspend fun deleteMetadataSchema(targetId: String, targetType: String) {}
    }
}
