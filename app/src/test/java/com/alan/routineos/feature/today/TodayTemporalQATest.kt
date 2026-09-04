package com.alan.routineos.feature.today

import com.alan.routineos.core.util.TimeProvider
import com.alan.routineos.domain.model.*
import com.alan.routineos.domain.repository.ActivityRepository
import com.alan.routineos.domain.usecase.*
import com.alan.routineos.feature.today.model.*
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
    fun `Intervals 10-11 and 11-12 have NONE relationship`() = runTest {
        val a = createRule("A", 600, 660)
        val b = createRule("B", 660, 720, defId = "other")
        repository.setRules(listOf(a, b))
        initViewModel()
        
        val uiA = findDeep(viewModel.uiState.value.timelineItems, "A") ?: throw AssertionError("Item A not found")
        val rel = uiA.conflict.details.firstOrNull()?.relationship ?: TemporalRelationship.NONE
        assertEquals(TemporalRelationship.NONE, rel)
    }

    @Test
    fun `Independent task inside IMMOBILE task has WARNING impact`() = runTest {
        val date = LocalDate.now()
        val nodeA = ActivityNode("nA", "act1", null, 0, "Immobile Parent")
        val nodeB = ActivityNode("nB", "act2", null, 0, "Independent Child")
        repository.setNodes(listOf(nodeA, nodeB))
        
        val instA = DailyInstance("vA", ScheduleTarget.Node("nA"), date.toEpochDay(), "A", "", 
            plannedStartTime = 420, plannedEndTime = 840, mobility = TemporalMobility.IMMOBILE)
        
        val instB = DailyInstance("vB", ScheduleTarget.Node("nB"), date.toEpochDay(), "B", "", 
            plannedStartTime = 600, plannedEndTime = 660, mobility = TemporalMobility.FLEXIBLE)
            
        repository.setDailyInstances(listOf(instA, instB))
        initViewModel()
        
        val uiB = findDeep(viewModel.uiState.value.timelineItems, "vB") ?: throw AssertionError("Item vB not found")
        val rel = uiB.conflict.details.firstOrNull()?.relationship ?: TemporalRelationship.NONE
        assertEquals(TemporalRelationship.CONTAINED_BY, rel)
        assertEquals(TemporalImpact.WARNING, uiB.conflict.impact)
    }

    @Test
    fun `Structural child inside parent has INFO impact`() = runTest {
        val date = LocalDate.now()
        val nodeA = ActivityNode("nA", "act", null, 0, "Parent")
        val nodeB = ActivityNode("nB", "act", "nA", 0, "Structural Child")
        repository.setNodes(listOf(nodeA, nodeB))
        
        val instA = DailyInstance("vA", ScheduleTarget.Node("nA"), date.toEpochDay(), "A", "", 
            plannedStartTime = 420, plannedEndTime = 840)
        
        val instB = DailyInstance("vB", ScheduleTarget.Node("nB"), date.toEpochDay(), "B", "", 
            plannedStartTime = 600, plannedEndTime = 660)
            
        repository.setDailyInstances(listOf(instA, instB))
        initViewModel()
        
        val uiA = findDeep(viewModel.uiState.value.timelineItems, "vA") ?: throw AssertionError("Item vA not found")
        val uiB = uiA.subNodes.find { it.id == "vB" }!!
        val rel = uiB.conflict.details.firstOrNull()?.relationship ?: TemporalRelationship.NONE
        assertEquals(TemporalRelationship.CONTAINED_BY, rel)
        assertEquals(TemporalImpact.INFO, uiB.conflict.impact)
    }

    @Test
    fun `Equal intervals result in OVERLAP relationship`() = runTest {
        val a = createRule("A", 600, 660, defId = "defA")
        val b = createRule("B", 600, 660, defId = "defB")
        repository.setRules(listOf(a, b))
        initViewModel()
        
        val uiA = findDeep(viewModel.uiState.value.timelineItems, "A") ?: throw AssertionError("Item A not found")
        val rel = uiA.conflict.details.firstOrNull()?.relationship ?: TemporalRelationship.NONE
        assertEquals(TemporalRelationship.OVERLAP, rel)
    }

    @Test
    fun `Upcoming activity is correctly identified`() = runTest {
        timeProvider.setTime(LocalTime.of(10, 0))
        val upcomingRule = createRule("r1", 660)
        repository.setRules(listOf(upcomingRule))
        initViewModel()
        val state = viewModel.uiState.value
        val item = findDeep(state.timelineItems, "r1")!!
        assertEquals(TimelineTemporalState.UPCOMING, item.temporalState)
    }

    @Test
    fun `Current activity is correctly identified with end time`() = runTest {
        timeProvider.setTime(LocalTime.of(10, 30))
        val currentRule = createRule("r1", 600, 720)
        repository.setRules(listOf(currentRule))
        initViewModel()
        val item = findDeep(viewModel.uiState.value.timelineItems, "r1")!!
        assertEquals(TimelineTemporalState.CURRENT, item.temporalState)
    }

    @Test
    fun `Move suggestion is validated against full timeline`() = runTest {
        val date = LocalDate.now()
        val nodeA = ActivityNode("nA", "act1", null, 0, "A")
        val nodeB = ActivityNode("nB", "act2", null, 0, "B")
        val nodeC = ActivityNode("nC", "act3", null, 0, "C")
        repository.setNodes(listOf(nodeA, nodeB, nodeC))
        
        val instA = DailyInstance("vA", ScheduleTarget.Node("nA"), date.toEpochDay(), "A", "", plannedStartTime = 420, plannedEndTime = 840, mobility = TemporalMobility.IMMOBILE)
        val instB = DailyInstance("vB", ScheduleTarget.Node("nB"), date.toEpochDay(), "B", "", plannedStartTime = 780, plannedEndTime = 900, mobility = TemporalMobility.FLEXIBLE, plannedDurationMinutes = 120)
        val instC = DailyInstance("vC", ScheduleTarget.Node("nC"), date.toEpochDay(), "C", "", plannedStartTime = 840, plannedEndTime = 960, mobility = TemporalMobility.IMMOBILE)
        
        repository.setDailyInstances(listOf(instA, instB, instC))
        initViewModel()
        
        val uiB = findDeep(viewModel.uiState.value.timelineItems, "vB") ?: throw AssertionError("Item vB not found")
        val moveSuggestion = uiB.conflict.suggestions.find { it.newStartTimeMinutes == 960 }
        assertEquals("Mover a las 16:00", moveSuggestion?.message)
    }

    @Test
    fun `Multiple simultaneous intersections handled correctly`() = runTest {
        val date = LocalDate.now()
        val nodeA = ActivityNode("nA", "act1", null, 0, "A")
        val nodeB = ActivityNode("nB", "act2", null, 0, "B")
        val nodeC = ActivityNode("nC", "act3", null, 0, "C")
        repository.setNodes(listOf(nodeA, nodeB, nodeC))
        
        val instA = DailyInstance("vA", ScheduleTarget.Node("nA"), date.toEpochDay(), "A", "", plannedStartTime = 480, plannedEndTime = 720, mobility = TemporalMobility.IMMOBILE)
        val instB = DailyInstance("vB", ScheduleTarget.Node("nB"), date.toEpochDay(), "B", "", plannedStartTime = 600, plannedEndTime = 840, mobility = TemporalMobility.FLEXIBLE)
        val instC = DailyInstance("vC", ScheduleTarget.Node("nC"), date.toEpochDay(), "C", "", plannedStartTime = 660, plannedEndTime = 780, mobility = TemporalMobility.IMMOBILE)
        
        repository.setDailyInstances(listOf(instA, instB, instC))
        initViewModel()
        
        val uiB = findDeep(viewModel.uiState.value.timelineItems, "vB") ?: throw AssertionError("Item vB not found")
        assertEquals(true, uiB.conflict.hasConflict)
        val rel = uiB.conflict.details.maxByOrNull { it.relationship.ordinal }?.relationship ?: TemporalRelationship.NONE
        assertEquals(TemporalRelationship.CONTAINS, rel)
    }

    private fun findDeep(items: List<TodayTimelineUiModel>, idPart: String): TodayTimelineUiModel? {
        items.forEach { item ->
            if (item.id.contains(idPart, ignoreCase = true)) return item
            findDeepInSubs(item.subNodes, idPart)?.let { return it }
        }
        return null
    }

    private fun findDeepInSubs(subs: List<TodaySubNodeUiModel>, idPart: String): TodayTimelineUiModel? {
        subs.forEach { sub ->
            if (sub.id.contains(idPart, ignoreCase = true)) {
                return TodayTimelineUiModel(
                    id = sub.id, title = sub.title, status = sub.status, 
                    isMaterialized = true, conflict = sub.conflict, timeRangeText = sub.timeText,
                    subNodes = sub.children 
                )
            }
            findDeepInSubs(sub.children, idPart)?.let { return it }
        }
        return null
    }

    private fun TestScope.initViewModel() {
        val conflictDetector = ConflictDetectorUseCase()
        val resolveTimelineUseCase = ResolveTimelineUseCase(repository, TimelineResolutionEngine(), conflictDetector, SuggestionEngine(conflictDetector))
        val getHierarchicalTimelineUseCase = GetHierarchicalTimelineUseCase(repository, resolveTimelineUseCase)
        val registerDailyActionUseCase = RegisterDailyActionUseCase(repository, MaterializeInstanceUseCase(repository))
        viewModel = TodayViewModel(repository, getHierarchicalTimelineUseCase, registerDailyActionUseCase, timeProvider)
        timeProvider.tick() 
        advanceUntilIdle()
    }

    private fun createRule(id: String, start: Int, end: Int? = null, defId: String = "act"): ScheduleRule {
        return ScheduleRule(id = id, target = ScheduleTarget.Node("n$id"), type = ScheduleRuleType.FIXED_DAYS, daysOfWeek = setOf(LocalDate.now().dayOfWeek.value), startTime = start, endTime = end)
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
        var executionsCount = 0

        fun setNodes(nodes: List<ActivityNode>) { 
            _nodes.value = nodes 
            _definitions.value = nodes.map { it.activityDefinitionId }.distinct().map { ActivityDefinition(it, it, "", "sys") }
        }
        fun setRules(rules: List<ScheduleRule>) { 
            _rules.value = rules 
            if (_nodes.value.isEmpty()) {
                _nodes.value = rules.map { r ->
                    val nodeId = (r.target as ScheduleTarget.Node).id
                    val defId = if (nodeId == "nA") "defA" else if (nodeId == "nB") "defB" else "act"
                    ActivityNode(nodeId, defId, null, 0, "Title $nodeId") 
                }
            }
            _definitions.value = _nodes.value.map { it.activityDefinitionId }.distinct().map { ActivityDefinition(it, it, "", "sys") }
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
        override suspend fun registerExecution(nodeId: String, scheduledDate: Long, metadataJson: String, dailyInstanceId: String?) { executionsCount++ }
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
