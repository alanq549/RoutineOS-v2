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
import java.util.*

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class PlanningEventFlowTest {

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
        val savedInstances = mutableListOf<DailyInstance>()
        var deletedId: String? = null
        
        private val _definitions = MutableStateFlow<List<ActivityDefinition>>(emptyList())
        private val _nodes = MutableStateFlow<List<ActivityNode>>(emptyList())
        private val _rules = MutableStateFlow<List<ScheduleRule>>(emptyList())
        private val _instances = MutableStateFlow<List<DailyInstance>>(emptyList())
        private val _exceptions = MutableStateFlow<List<ScheduleException>>(emptyList())

        override fun getActivityDefinitions(): Flow<List<ActivityDefinition>> = _definitions
        override fun getAllNodes(): Flow<List<ActivityNode>> = _nodes
        override suspend fun getActivityDefinitionById(id: String): ActivityDefinition? = null
        override suspend fun upsertActivityDefinition(activityDefinition: ActivityDefinition) {}
        override suspend fun deleteActivityDefinition(activityDefinition: ActivityDefinition) {}
        override fun getNodesForActivityDefinition(activityDefinitionId: String): Flow<List<ActivityNode>> = _nodes
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
        override fun getAllRules(): Flow<List<ScheduleRule>> = _rules
        override fun getAllExceptions(): Flow<List<ScheduleException>> = _exceptions
        override fun getRulesForNode(nodeId: String): Flow<List<ScheduleRule>> = _rules
        override suspend fun getRulesListForNode(nodeId: String): List<ScheduleRule> = emptyList()
        override fun getRulesForDefinition(definitionId: String): Flow<List<ScheduleRule>> = _rules
        override suspend fun getRulesListForDefinition(definitionId: String): List<ScheduleRule> = emptyList()
        override fun getRulesForActivityTree(definitionId: String): Flow<List<ScheduleRule>> = _rules
        override suspend fun upsertRule(rule: ScheduleRule) {}
        override suspend fun deleteRule(rule: ScheduleRule) {}
        override fun getExceptionsForRule(ruleId: String): Flow<List<ScheduleException>> = _exceptions
        override suspend fun upsertException(exception: ScheduleException) {}
        override suspend fun deleteException(exception: ScheduleException) {}
        override fun getDailyInstancesForDate(date: Long): Flow<List<DailyInstance>> = _instances.map { l -> l.filter { it.scheduledDate == date } }
        override fun getDailyInstancesForDateRange(start: Long, end: Long): Flow<List<DailyInstance>> = _instances
        override suspend fun upsertDailyInstance(instance: DailyInstance) {
            savedInstances.add(instance)
            _instances.update { it + instance }
        }
        override suspend fun deleteDailyInstance(id: String) {
            deletedId = id
        }
        override suspend fun getDailyInstanceByTarget(targetId: String, date: Long): DailyInstance? = null
        override fun getMetadataSchema(targetId: String, targetType: String): Flow<MetadataSchema?> = flowOf(null)
        override suspend fun upsertMetadataSchema(schema: MetadataSchema) {}
        override suspend fun deleteMetadataSchema(targetId: String, targetType: String) {}
        override fun getAllSystems(): Flow<List<LifeSystem>> = flowOf(emptyList())
        override suspend fun getSystemsList() = emptyList<LifeSystem>()
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

    private suspend fun waitReady(viewModel: PlanningViewModel) {
        viewModel.uiState.first { !it.isLoading }
    }

    @Test
    fun `CREATE - Saving new event creates an ad-hoc DailyInstance`() = runTest {
        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }
        waitReady(viewModel)

        // 1. Trigger Add
        viewModel.onAddEventClick()
        advanceUntilIdle()

        val entry = viewModel.uiState.value.editingSpontaneousEntry
        assertNotNull("Editor entry should not be null", entry)
        val tempId = entry!!.root.instance.id
        
        // 2. Update Data
        viewModel.onUpdateSpontaneousTitle(tempId, "Cena")
        viewModel.onUpdateSpontaneousSchedule(tempId, 1200, 1320)
        advanceUntilIdle()

        // 3. Save
        viewModel.onSaveNewEvent()
        advanceUntilIdle()

        // 4. Verify
        assertEquals("Should have saved exactly one instance", 1, repository.savedInstances.size)
        val saved = repository.savedInstances[0]
        assertEquals("Cena", saved.titleSnapshot)
    }

    @Test
    fun `DELETE - Deleting instance calls repository`() = runTest {
        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }
        waitReady(viewModel)

        viewModel.onDeleteInstance("target_id")
        advanceUntilIdle()
        assertEquals("target_id", repository.deletedId)
    }
}
