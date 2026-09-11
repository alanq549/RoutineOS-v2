package com.alan.routineos.feature.planning

import com.alan.routineos.domain.model.*
import com.alan.routineos.domain.repository.ActivityRepository
import com.alan.routineos.domain.usecase.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class PlanningNavigationTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakeRepository : ActivityRepository {
        override fun getActivityDefinitions(): Flow<List<ActivityDefinition>> = flowOf(emptyList())
        override fun getAllNodes(): Flow<List<ActivityNode>> = flowOf(emptyList())
        override suspend fun getActivityDefinitionById(id: String): ActivityDefinition? = null
        override suspend fun upsertActivityDefinition(activityDefinition: ActivityDefinition) {}
        override suspend fun deleteActivityDefinition(activityDefinition: ActivityDefinition) {}
        override fun getNodesForActivityDefinition(activityDefinitionId: String): Flow<List<ActivityNode>> = flowOf(emptyList())
        override suspend fun getNodesListForActivityDefinition(activityDefinitionId: String): List<ActivityNode> = emptyList()
        override suspend fun getNodeById(id: String): ActivityNode? = null
        override suspend fun upsertNode(node: ActivityNode) {}
        override suspend fun deleteNode(node: ActivityNode) {}
        override suspend fun reorderNodes(nodeIds: List<String>) {}
        override suspend fun moveNode(nodeId: String, newParentId: String?) {}
        override suspend fun registerExecution(nodeId: String, scheduledDate: Long, metadataJson: String, dailyInstanceId: String?) {}
        override fun getAllExecutions(): Flow<List<ActivityExecution>> = flowOf(emptyList())
        override fun getExecutionsForNode(nodeId: String): Flow<List<ActivityExecution>> = flowOf(emptyList())
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
        override fun getDailyInstancesForDateRange(start: Long, end: Long): Flow<List<DailyInstance>> = flowOf(emptyList())
        override suspend fun upsertDailyInstance(instance: DailyInstance) {}
        override suspend fun deleteDailyInstance(id: String) {}
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

    private fun createViewModel(): PlanningViewModel {
        return PlanningViewModel(
            repository = repository,
            getHierarchicalTimelineUseCase = GetHierarchicalTimelineUseCase(repository, ResolveTimelineUseCase(repository, TimelineResolutionEngine(), ConflictDetectorUseCase(), SuggestionEngine(ConflictDetectorUseCase()))),
            registerDailyActionUseCase = RegisterDailyActionUseCase(repository, MaterializeInstanceUseCase(repository)),
            simulateMoveUseCase = SimulateMoveUseCase(ConflictDetectorUseCase())
        )
    }

    @Test
    fun `nextWeek() increases date by 7 days`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val initialDate = viewModel.uiState.value.selectedDate
        viewModel.nextWeek()
        advanceUntilIdle()
        
        assertEquals(initialDate.plusDays(7), viewModel.uiState.value.selectedDate)
    }

    @Test
    fun `prevWeek() decreases date by 7 days`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val initialDate = viewModel.uiState.value.selectedDate
        viewModel.prevWeek()
        advanceUntilIdle()
        
        assertEquals(initialDate.minusDays(7), viewModel.uiState.value.selectedDate)
    }

    @Test
    fun `goToToday() resets date to today`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.nextWeek()
        advanceUntilIdle()
        
        viewModel.goToToday()
        advanceUntilIdle()
        
        assertEquals(LocalDate.now(), viewModel.uiState.value.selectedDate)
    }

    @Test
    fun `jumpToDate() sets date correctly`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val target = LocalDate.of(2027, 12, 25)
        viewModel.jumpToDate(target)
        advanceUntilIdle()
        
        assertEquals(target, viewModel.uiState.value.selectedDate)
    }

    @Test
    fun `isShowingToday is true only if current week contains today`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue("Initial week should contain today", viewModel.uiState.value.isShowingToday)
        
        viewModel.jumpToDate(LocalDate.now().plusWeeks(2))
        advanceUntilIdle()
        assertFalse("Week 2 weeks from now should NOT contain today", viewModel.uiState.value.isShowingToday)
    }

    @Test
    fun `weekRangeText formats correctly for single month`() = runTest {
        val viewModel = createViewModel()
        val midMonth = LocalDate.of(2026, 9, 10) // Jueves. Lunes es 7, Domingo es 13.
        viewModel.jumpToDate(midMonth)
        advanceUntilIdle()
        
        val text = viewModel.uiState.value.weekRangeText
        assertTrue("Should contain '7' in '$text'", text.contains("7"))
        assertTrue("Should contain '13' in '$text'", text.contains("13"))
        assertTrue("Should contain month in '$text'", text.lowercase().contains("septiembre"))
    }

    @Test
    fun `weekRangeText formats correctly for month transition`() = runTest {
        val viewModel = createViewModel()
        val transition = LocalDate.of(2026, 9, 30) // Mié. Lun es 28 Sep, Dom es 4 Oct.
        viewModel.jumpToDate(transition)
        advanceUntilIdle()
        
        val text = viewModel.uiState.value.weekRangeText
        assertTrue("Should contain '28' in '$text'", text.contains("28"))
        assertTrue("Should contain '4' in '$text'", text.contains("4"))
        assertTrue("Should contain September in '$text'", text.lowercase().contains("septiembre"))
        assertTrue("Should contain October in '$text'", text.lowercase().contains("octubre"))
    }
}
