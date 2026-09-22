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
import com.alan.routineos.feature.planning.model.SearchTargetUiModel
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
        
        val _definitions = MutableStateFlow<List<ActivityDefinition>>(emptyList())
        val _nodes = MutableStateFlow<List<ActivityNode>>(emptyList())
        private val _rules = MutableStateFlow<List<ScheduleRule>>(emptyList())
        val _instances = MutableStateFlow<List<DailyInstance>>(emptyList())
        private val _exceptions = MutableStateFlow<List<ScheduleException>>(emptyList())

        override fun getActivityDefinitions(): Flow<List<ActivityDefinition>> = _definitions
        override fun getAllNodes(): Flow<List<ActivityNode>> = _nodes
        override suspend fun getActivityDefinitionById(id: String): ActivityDefinition? = _definitions.value.find { it.id == id }
        override suspend fun upsertActivityDefinition(activityDefinition: ActivityDefinition) {}
        override suspend fun deleteActivityDefinition(activityDefinition: ActivityDefinition) {}
        override fun getNodesForActivityDefinition(activityDefinitionId: String): Flow<List<ActivityNode>> = _nodes
        override suspend fun getNodesListForActivityDefinition(activityDefinitionId: String): List<ActivityNode> = emptyList()
        override suspend fun getNodeById(id: String): ActivityNode? = _nodes.value.find { it.id == id }
        override suspend fun upsertNode(node: ActivityNode) {}
        override suspend fun deleteNode(node: ActivityNode) {}
        override suspend fun reorderNodes(nodeIds: List<String>) {}
        override suspend fun moveNode(nodeId: String, newParentId: String?) {}
        override suspend fun registerInstanceExecution(instance: DailyInstance, metadataJson: String) {}
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
        
        override suspend fun upsertActivityWithContext(instance: DailyInstance, tasks: List<DailyInstance>, note: Note?) {
            savedInstances.add(instance)
            savedInstances.addAll(tasks)
            _instances.update { it + instance + tasks }
        }

        override fun getNotesByQuery(instanceId: String?, date: Long, title: String): Flow<List<Note>> = flowOf(emptyList())
        override fun getNotesForDate(date: Long): Flow<List<Note>> = flowOf(emptyList())
        override suspend fun upsertNote(note: Note) {}
        override suspend fun deleteNote(note: Note) {}
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
    fun `ROLE SWITCH - Changing role preserves context drafts`() = runTest {
        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }
        waitReady(viewModel)

        viewModel.onAddEventClick()
        advanceUntilIdle()

        // 1. Add Note in Activity mode
        viewModel.onUpdateDraftNote("Important Activity Note")
        advanceUntilIdle()
        
        assertEquals(EditorRole.EVENT, viewModel.uiState.value.editorRole)
        assertEquals("Important Activity Note", viewModel.uiState.value.editingSpontaneousEntry?.note?.content)

        // 2. Switch to Task mode
        viewModel.onUpdateEditorRole(EditorRole.TASK)
        advanceUntilIdle()
        
        assertEquals(EditorRole.TASK, viewModel.uiState.value.editorRole)
        // Context is hidden in Task mode in the sheet UI, but draft should remain in state
        // Actually, my mapping logic in VM currently enriches the entry based on draft.
        // Let's check if it still has the note in the HierarchicalTimelineEntry
        assertEquals("Important Activity Note", viewModel.uiState.value.editingSpontaneousEntry?.note?.content)

        // 3. Switch back to Event
        viewModel.onUpdateEditorRole(EditorRole.EVENT)
        advanceUntilIdle()
        assertEquals("Important Activity Note", viewModel.uiState.value.editingSpontaneousEntry?.note?.content)
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

    @Test
    fun `SAVE - Mixed Case - CHECK + DEFINITION + OCCURRENCE coexistence`() = runTest {
        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }
        
        repository._definitions.value = listOf(ActivityDefinition("m1", "Microeconomia", ""))
        val anchorInstance = createInstance("a1", "Asesoria")
        repository.upsertDailyInstance(anchorInstance)
        waitReady(viewModel)

        viewModel.onAddEventClick()
        advanceUntilIdle()
        val tempId = viewModel.uiState.value.editingSpontaneousEntry!!.root.instance.id

        // 1. Set Title & Role Task (CHECK)
        viewModel.onUpdateSpontaneousTitle(tempId, "Subir reporte")
        viewModel.onUpdateEditorRole(EditorRole.TASK)
        
        // 2. Set Semantic Target (Microeconomia)
        viewModel.onLinkToDefinition(SearchTargetUiModel("m1", "Microeconomia", null, ScheduleTarget.Definition("m1")))
        
        // 3. Set Contextual Occurrence (Asesoria)
        viewModel.onLinkToOccurrence(anchorInstance.toUiModelTest())
        advanceUntilIdle()

        viewModel.onSaveNewEvent()
        advanceUntilIdle()

        val saved = repository.savedInstances.last()
        assertEquals("Subir reporte", saved.titleSnapshot)
        assertEquals(ScheduleTarget.Definition("m1"), saved.target)
        assertEquals("a1", saved.associatedInstanceId)
        assertEquals(ActionProtocol.CHECK, saved.actionProtocol)
    }

    @Test
    fun `EDIT - Reconstructs both semantic target and contextual association correctly`() = runTest {
        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }
        
        // 1. Setup existing definitions and occurrences
        val def = ActivityDefinition("d1", "Materia", "")
        repository._definitions.value = listOf(def)
        
        val anchor = createInstance("a1", "Ocurrencia")
        repository._instances.value = listOf(anchor)
        
        // 2. Setup Task with both links
        val existingTask = createInstance("t1", "Tarea Mixta").copy(
            actionProtocol = ActionProtocol.CHECK,
            target = ScheduleTarget.Definition("d1"),
            associatedInstanceId = "a1"
        )
        repository._instances.update { it + existingTask }
        waitReady(viewModel)

        // 3. Trigger EDIT
        viewModel.onActionTriggered("t1", "EDIT_SPONTANEOUS")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Materia", state.selectedSemanticTarget?.title)
        assertEquals("Ocurrencia", state.selectedContextualOccurrence?.title)

        // 4. Modify ONLY Semantic Target (clear it)
        viewModel.onLinkToDefinition(null)
        advanceUntilIdle()
        
        viewModel.onSaveNewEvent()
        advanceUntilIdle()

        // 5. Verify: Target is null, but Association is preserved
        val saved = repository.savedInstances.last()
        assertNull("Target should be cleared", saved.target)
        assertEquals("a1", saved.associatedInstanceId)
    }

    @Test
    fun `HIERARCHY - Multiple instances for same target should all coexist without overwriting`() = runTest {
        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }

        // 1. Setup Activity Definition
        val def = ActivityDefinition("d1", "Universidad", "")
        repository._definitions.value = listOf(def)

        // 2. Create Task (CHECK) at 12:00 and Reminder (CHECK) at 22:00 for the same definition
        val task = createInstance("t1", "Tarea 12pm").copy(
            actionProtocol = ActionProtocol.CHECK,
            target = ScheduleTarget.Definition("d1"),
            plannedStartTime = 720 // 12:00
        )
        val reminder = createInstance("r1", "Recordatorio 10pm").copy(
            actionProtocol = ActionProtocol.CHECK, // Reminders currently use CHECK/TIMER roles
            target = ScheduleTarget.Definition("d1"),
            plannedStartTime = 1320 // 22:00
        )
        
        repository._instances.value = listOf(task, reminder)
        waitReady(viewModel)

        // 3. Verify both exist in timeline entries
        val timeline = viewModel.uiState.value.timelineEntries
        
        // They should both be present as independent entries because they are at root level 
        // (or mapped as such by the use case if they share a target)
        val matches = timeline.filter { it.title == "Tarea 12pm" || it.title == "Recordatorio 10pm" }
        assertEquals("Both instances for the same target should be visible", 2, matches.size)
        
        val titles = matches.map { it.title }.toSet()
        assertTrue(titles.contains("Tarea 12pm"))
        assertTrue(titles.contains("Recordatorio 10pm"))
    }

    @Test
    fun `HIERARCHY - Task linked to Activity should NOT inherit its structural children`() = runTest {
        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }

        // 1. Setup Activity with steps
        val def = ActivityDefinition("d1", "Leg Day", "")
        repository._definitions.value = listOf(def)
        repository._nodes.value = listOf(
            ActivityNode("n1", "d1", null, 0, "Squats")
        )

        // 2. Create Task linked to Leg Day
        val task = createInstance("t1", "Test Task").copy(
            actionProtocol = ActionProtocol.CHECK,
            role = DailyInstanceRole.TASK,
            target = ScheduleTarget.Definition("d1")
        )
        repository._instances.value = listOf(task)
        waitReady(viewModel)

        // 3. Verify task exists but has NO children
        val entry = viewModel.uiState.value.timelineEntries.find { it.id == "t1" }
        assertNotNull(entry)
        assertTrue("Task should not have structural sub-nodes", entry!!.subNodes.isEmpty())
        assertFalse("Task should not be expandable", entry.isExpandable)
    }

    @Test
    fun `HIERARCHY - Ad-hoc item with no links should be strictly independent`() = runTest {
        val viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }

        // 1. Setup an Activity
        repository._definitions.value = listOf(ActivityDefinition("d1", "University", ""))
        
        // 2. Create Ad-hoc Reminder (target=null, associated=null)
        val reminder = createInstance("r1", "Brush Teeth").copy(
            actionProtocol = ActionProtocol.CHECK,
            target = null,
            associatedInstanceId = null
        )
        repository._instances.value = listOf(reminder)
        waitReady(viewModel)

        // 3. Verify it's a root element and not grouped
        val timeline = viewModel.uiState.value.timelineEntries
        assertEquals(1, timeline.size)
        assertEquals("Brush Teeth", timeline[0].title)
    }

    private fun createInstance(id: String, title: String) = DailyInstance(
        id = id,
        target = null,
        scheduledDate = LocalDate.now().toEpochDay(),
        titleSnapshot = title,
        descriptionSnapshot = "",
        plannedStartTime = 720,
        status = DailyInstanceStatus.PLANNED,
        actionProtocol = ActionProtocol.TIMER
    )

    private fun DailyInstance.toUiModelTest() = com.alan.routineos.feature.today.model.TodayTimelineUiModel(
        id = id,
        title = titleSnapshot,
        timeRangeText = "",
        status = status,
        isMaterialized = true
    )
}
