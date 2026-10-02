package com.alan.routineos.domain.usecase

import com.alan.routineos.domain.model.*
import com.alan.routineos.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class AssignBacklogItemToDayUseCaseTest {

    @Test
    fun `assigning new backlog item creates DailyInstance with backlogId`() = runTest {
        val backlogItem = BacklogItem("b1", null, "Fix car", BacklogItemStatus.OPEN)
        val savedInstances = mutableListOf<DailyInstance>()

        val repository = object : FakeActivityRepository() {
            override fun getDailyInstancesForDateRange(start: Long, end: Long) = flowOf(emptyList<DailyInstance>())
            override suspend fun upsertDailyInstance(instance: DailyInstance) {
                savedInstances.add(instance)
            }
        }

        val useCase = AssignBacklogItemToDayUseCase(repository)
        val today = LocalDate.now()
        useCase(backlogItem, today)

        assertEquals(1, savedInstances.size)
        val created = savedInstances[0]
        assertEquals("b1", created.backlogId)
        assertEquals("Fix car", created.titleSnapshot)
        assertEquals(today.toEpochDay(), created.scheduledDate)
        assertEquals(DailyInstanceRole.TASK, created.role)
        assertEquals(ActionProtocol.CHECK, created.actionProtocol)
        assertTrue(created.isAdHoc)
    }

    @Test
    fun `assigning backlog item with definitionId inherits ACTIVITY role and TIMER protocol`() = runTest {
        val backlogItem = BacklogItem("b2", "def_1", "Weekly Review", BacklogItemStatus.OPEN)
        val savedInstances = mutableListOf<DailyInstance>()

        val repository = object : FakeActivityRepository() {
            override fun getDailyInstancesForDateRange(start: Long, end: Long) = flowOf(emptyList<DailyInstance>())
            override suspend fun upsertDailyInstance(instance: DailyInstance) {
                savedInstances.add(instance)
            }
        }

        val useCase = AssignBacklogItemToDayUseCase(repository)
        val today = LocalDate.now()
        useCase(backlogItem, today)

        assertEquals(1, savedInstances.size)
        val created = savedInstances[0]
        assertEquals("b2", created.backlogId)
        assertEquals(ScheduleTarget.Definition("def_1"), created.target)
        assertEquals(DailyInstanceRole.ACTIVITY, created.role)
        assertEquals(ActionProtocol.TIMER, created.actionProtocol)
        assertFalse(created.isAdHoc)
    }

    private open class FakeActivityRepository : ActivityRepository {
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
        override suspend fun registerInstanceExecution(instance: DailyInstance, metadataJson: String) {}
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
        override fun getNotesByQuery(instanceId: String?, date: Long, title: String): Flow<List<Note>> = flowOf(emptyList())
        override fun getNotesForDate(date: Long): Flow<List<Note>> = flowOf(emptyList())
        override suspend fun upsertNote(note: Note) {}
        override suspend fun deleteNote(note: Note) {}
        override fun getMetadataSchema(targetId: String, targetType: String): Flow<MetadataSchema?> = flowOf(null)
        override suspend fun upsertMetadataSchema(schema: MetadataSchema) {}
        override suspend fun deleteMetadataSchema(targetId: String, targetType: String) {}
        override fun getAllSystems(): Flow<List<LifeSystem>> = flowOf(emptyList())
        override suspend fun getSystemsList(): List<LifeSystem> = emptyList()
        override suspend fun getSystemById(id: String): LifeSystem? = null
        override suspend fun upsertSystem(system: LifeSystem) {}
        override suspend fun deleteSystem(system: LifeSystem) {}
        override fun getAllBacklogItems(): Flow<List<BacklogItem>> = flowOf(emptyList())
        override suspend fun getBacklogItemById(id: String): BacklogItem? = null
        override suspend fun upsertBacklogItem(item: BacklogItem) {}
        override suspend fun deleteBacklogItem(id: String) {}
    }
}
