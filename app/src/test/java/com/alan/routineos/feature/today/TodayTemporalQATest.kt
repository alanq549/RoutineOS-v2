package com.alan.routineos.feature.today

import com.alan.routineos.core.util.TimeProvider
import com.alan.routineos.domain.model.*
import com.alan.routineos.domain.repository.ActivityRepository
import com.alan.routineos.domain.usecase.*
import com.alan.routineos.feature.today.model.TimelineTemporalState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class TodayTemporalQATest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: TodayViewModel
    private lateinit var repository: FakeActivityRepository
    private lateinit var timeProvider: FakeTimeProvider

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeActivityRepository()
        timeProvider = FakeTimeProvider()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Upcoming activity is correctly identified`() = runTest {
        val now = LocalTime.of(10, 0)
        timeProvider.setTime(now)
        
        val upcomingRule = createRule("r1", 660) // 11:00
        repository.setRules(listOf(upcomingRule))
        
        initViewModel()
        
        val state = viewModel.uiState.value
        val item = state.timelineItems.first()
        assertEquals(TimelineTemporalState.UPCOMING, item.temporalState)
        assertEquals(item, state.nextActivity)
    }

    @Test
    fun `Current activity is correctly identified with end time`() = runTest {
        val now = LocalTime.of(10, 30)
        timeProvider.setTime(now)
        
        val currentRule = createRule("r1", 600, 720) // 10:00 - 12:00
        repository.setRules(listOf(currentRule))
        
        initViewModel()
        
        val item = viewModel.uiState.value.timelineItems.first()
        assertEquals(TimelineTemporalState.CURRENT, item.temporalState)
        assertEquals(item, viewModel.uiState.value.nextActivity)
    }

    @Test
    fun `Overdue activity is correctly identified with end time`() = runTest {
        val now = LocalTime.of(12, 0)
        timeProvider.setTime(now)
        
        val overdueRule = createRule("r1", 600, 660) // 10:00 - 11:00
        repository.setRules(listOf(overdueRule))
        
        initViewModel()
        
        val item = viewModel.uiState.value.timelineItems.first()
        assertEquals(TimelineTemporalState.OVERDUE, item.temporalState)
        assertEquals(null, viewModel.uiState.value.nextActivity)
    }

    @Test
    fun `Stale pending activity is identified when no end boundary exists`() = runTest {
        val now = LocalTime.of(11, 0)
        timeProvider.setTime(now)
        
        val staleRule = createRule("r1", 600) // 10:00, no end
        repository.setRules(listOf(staleRule))
        
        initViewModel()
        
        val item = viewModel.uiState.value.timelineItems.first()
        assertEquals(TimelineTemporalState.STALE_PENDING, item.temporalState)
        assertEquals(null, viewModel.uiState.value.nextActivity)
    }

    @Test
    fun `Inferred boundary from next activity correctly sets OVERDUE`() = runTest {
        val now = LocalTime.of(11, 0)
        timeProvider.setTime(now)
        
        val a1 = createRule("r1", 600) // 10:00
        val a2 = createRule("r2", 630) // 10:30
        repository.setRules(listOf(a1, a2))
        
        initViewModel()
        
        val items = viewModel.uiState.value.timelineItems
        assertEquals(TimelineTemporalState.OVERDUE, items[0].temporalState) // 10:00 was ended by 10:30
        assertEquals(TimelineTemporalState.STALE_PENDING, items[1].temporalState) // 10:30 has no end
    }

    @Test
    fun `Current priority over upcoming in Next Activity section`() = runTest {
        val now = LocalTime.of(10, 15)
        timeProvider.setTime(now)
        
        val a1 = createRule("r1", 600, 660) // 10:00 - 11:00 (CURRENT)
        val a2 = createRule("r2", 720) // 12:00 (UPCOMING)
        repository.setRules(listOf(a1, a2))
        
        initViewModel()
        
        assertEquals("r1", (viewModel.uiState.value.nextActivity?.id?.split("_")?.get(1)))
    }

    @Test
    fun `Completed overdue activity shows COMPLETED status treatment`() = runTest {
        val now = LocalTime.of(12, 0)
        timeProvider.setTime(now)
        
        val date = LocalDate.now()
        val materialized = DailyInstance(
            id = "m1",
            target = ScheduleTarget.Node("nr1"),
            scheduledDate = date.toEpochDay(),
            titleSnapshot = "Task",
            descriptionSnapshot = "",
            plannedStartTime = 600,
            plannedEndTime = 660,
            status = DailyInstanceStatus.COMPLETED,
            sourceRuleId = "r1"
        )
        val node = ActivityNode("n1", "act", null, 0, "Task")
        repository.setDefinitions(listOf(ActivityDefinition("act", "Act", "", "sys")))
        repository.setNodes(listOf(node))
        repository.setRules(listOf(createRule("r1", 600, 660)))
        repository.setDailyInstances(listOf(materialized))
        
        initViewModel()
        
        val item = viewModel.uiState.value.timelineItems.first()
        assertEquals(DailyInstanceStatus.COMPLETED, item.status)
    }

    private fun TestScope.initViewModel() {
        val resolveTimelineUseCase = ResolveTimelineUseCase(repository, ConflictDetectorUseCase())
        val getHierarchicalTimelineUseCase = GetHierarchicalTimelineUseCase(repository, resolveTimelineUseCase)
        val registerDailyActionUseCase = RegisterDailyActionUseCase(repository, MaterializeInstanceUseCase(repository))
        
        viewModel = TodayViewModel(repository, getHierarchicalTimelineUseCase, registerDailyActionUseCase, timeProvider)
        timeProvider.tick() // Trigger initial load in ViewModel
        advanceUntilIdle()
    }

    private fun createRule(id: String, start: Int, end: Int? = null): ScheduleRule {
        return ScheduleRule(
            id = id,
            target = ScheduleTarget.Node("n$id"),
            type = ScheduleRuleType.FIXED_DAYS,
            daysOfWeek = setOf(LocalDate.now().dayOfWeek.value),
            startTime = start,
            endTime = end
        )
    }

    private class FakeTimeProvider : TimeProvider {
        private var currentTime = LocalTime.now()
        private val _ticker = MutableSharedFlow<Unit>(replay = 1)
        override val minuteTicker: Flow<Unit> = _ticker

        fun setTime(time: LocalTime) { currentTime = time }
        fun tick() { _ticker.tryEmit(Unit) }
        override fun now(): LocalTime = currentTime
    }

    private class FakeActivityRepository : ActivityRepository {
        private val _nodes = MutableStateFlow<List<ActivityNode>>(emptyList())
        private val _rules = MutableStateFlow<List<ScheduleRule>>(emptyList())
        private val _instances = MutableStateFlow<List<DailyInstance>>(emptyList())
        private val _definitions = MutableStateFlow<List<ActivityDefinition>>(emptyList())

        fun setDefinitions(defs: List<ActivityDefinition>) { _definitions.value = defs }
        fun setNodes(nodes: List<ActivityNode>) { _nodes.value = nodes }
        fun setRules(rules: List<ScheduleRule>) { 
            _rules.value = rules 
            _nodes.value = rules.map { ActivityNode((it.target as ScheduleTarget.Node).id, "act", null, 0, "Title $it") }
            _definitions.value = listOf(ActivityDefinition("act", "Act", "", "sys"))
        }
        fun setDailyInstances(instances: List<DailyInstance>) { _instances.value = instances }

        override fun getActivityDefinitions(): Flow<List<ActivityDefinition>> = _definitions
        override fun getAllNodes(): Flow<List<ActivityNode>> = _nodes
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
        override suspend fun registerExecution(nodeId: String, scheduledDate: Long, metadataJson: String, dailyInstanceId: String?) {}
        override fun getAllExecutions(): Flow<List<ActivityExecution>> = flowOf(emptyList())
        override fun getExecutionsForNode(nodeId: String): Flow<List<ActivityExecution>> = flowOf(emptyList())
        override fun getExecutionsForNodeOnDate(nodeId: String, scheduledDate: Long): Flow<List<ActivityExecution>> = flowOf(emptyList())
        override suspend fun deleteExecutionsForNodeOnDate(nodeId: String, scheduledDate: Long) {}
        override fun getAllRules(): Flow<List<ScheduleRule>> = _rules
        override fun getAllExceptions(): Flow<List<ScheduleException>> = flowOf(emptyList())
        override fun getRulesForNode(nodeId: String): Flow<List<ScheduleRule>> = _rules.map { r -> r.filter { (it.target as? ScheduleTarget.Node)?.id == nodeId } }
        override suspend fun getRulesListForNode(nodeId: String): List<ScheduleRule> = _rules.value.filter { (it.target as? ScheduleTarget.Node)?.id == nodeId }
        override fun getRulesForDefinition(definitionId: String): Flow<List<ScheduleRule>> = flowOf(emptyList())
        override suspend fun getRulesListForDefinition(definitionId: String): List<ScheduleRule> = emptyList()
        override fun getRulesForActivityTree(definitionId: String): Flow<List<ScheduleRule>> = flowOf(emptyList())
        override suspend fun upsertRule(rule: ScheduleRule) {}
        override suspend fun deleteRule(rule: ScheduleRule) {}
        override fun getExceptionsForRule(ruleId: String): Flow<List<ScheduleException>> = flowOf(emptyList())
        override suspend fun upsertException(exception: ScheduleException) {}
        override suspend fun deleteException(exception: ScheduleException) {}
        override fun getDailyInstancesForDate(date: Long): Flow<List<DailyInstance>> = _instances
        override fun getDailyInstancesForDateRange(start: Long, end: Long): Flow<List<DailyInstance>> = _instances
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
