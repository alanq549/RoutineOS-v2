package com.alan.routineos.domain.usecase

import com.alan.routineos.domain.model.*
import com.alan.routineos.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class ExecutionHistoryLifecycleTest {

    private val date = LocalDate.of(2026, 9, 10)

    @Test
    fun `AC-01 & AC-02 - Complete creates DailyInstance and ActivityExecution and Stats counts execution in snapshot`() = runTest {
        val instances = mutableMapOf<String, DailyInstance>()
        val executions = mutableMapOf<String, ActivityExecution>()

        val def = ActivityDefinition("def_1", "Gym", "Desc")
        val node = ActivityNode("node_1", "def_1", null, 0, "Gym Step")
        val rule = ScheduleRule("rule_1", ScheduleTarget.Node("node_1"), ScheduleRuleType.FIXED_DAYS, daysOfWeek = setOf(date.dayOfWeek.value))

        val repository = object : FakeActivityRepository() {
            override fun getActivityDefinitions(): Flow<List<ActivityDefinition>> = flowOf(listOf(def))
            override fun getAllNodes(): Flow<List<ActivityNode>> = flowOf(listOf(node))
            override fun getAllRules(): Flow<List<ScheduleRule>> = flowOf(listOf(rule))
            override suspend fun upsertDailyInstance(instance: DailyInstance) {
                instances[instance.id] = instance
            }
            override suspend fun registerInstanceExecution(instance: DailyInstance, metadataJson: String) {
                val exec = ActivityExecution(
                    id = "exec_" + instance.id,
                    nodeId = "node_1",
                    dailyInstanceId = instance.id,
                    scheduledDate = instance.scheduledDate,
                    completedAt = System.currentTimeMillis(),
                    activityIdSnapshot = "def_1",
                    systemIdSnapshot = null,
                    titleSnapshot = instance.titleSnapshot
                )
                executions[exec.id] = exec
            }
            override fun getAllExecutions(): Flow<List<ActivityExecution>> = flowOf(executions.values.toList())
            override fun getDailyInstancesForDateRange(start: Long, end: Long): Flow<List<DailyInstance>> = flowOf(instances.values.toList())
        }

        val registerAction = RegisterDailyActionUseCase(repository, MaterializeInstanceUseCase(repository))
        val instance = DailyInstance("inst_1", ScheduleTarget.Node("node_1"), date.toEpochDay(), "Gym Step", "", status = DailyInstanceStatus.PLANNED, sourceRuleId = "rule_1")
        val entry = HierarchicalTimelineEntry(TimelineEntry(instance, true))

        registerAction(entry, DailyAction.Complete("{}"))

        // AC-01 Verifications
        assertEquals(DailyInstanceStatus.COMPLETED, instances["inst_1"]?.status)
        assertEquals(1, executions.size)
        assertEquals("inst_1", executions.values.first().dailyInstanceId)

        // AC-02 Verifications: Pipeline Analytics reflects completion
        val resolver = HistoricalOccurrenceResolver(repository, TimelineResolutionEngine())
        val analyticsUseCase = GetHistoryAnalyticsUseCase(repository, resolver)
        val snapshot = analyticsUseCase.execute(date, date)

        assertEquals(1, snapshot.completedCount)
        assertEquals(1.0f, snapshot.completionRate ?: 0f, 0.01f)
    }

    @Test
    fun `AC-03 - Reset deletes ActivityExecution surgical to dailyInstanceId`() = runTest {
        val instances = mutableMapOf<String, DailyInstance>()
        val executions = mutableMapOf<String, ActivityExecution>()

        val instance = DailyInstance("inst_1", ScheduleTarget.Node("node_1"), date.toEpochDay(), "Gym Step", "", status = DailyInstanceStatus.COMPLETED)
        instances[instance.id] = instance
        val exec = ActivityExecution("exec_1", "node_1", "inst_1", date.toEpochDay(), System.currentTimeMillis(), "{}", "def_1", null, "Gym Step")
        executions[exec.id] = exec

        val repository = object : FakeActivityRepository() {
            override suspend fun deleteExecutionsForDailyInstance(dailyInstanceId: String) {
                executions.values.removeAll { it.dailyInstanceId == dailyInstanceId }
            }
            override suspend fun upsertDailyInstance(instance: DailyInstance) {
                instances[instance.id] = instance
            }
            override fun getAllExecutions(): Flow<List<ActivityExecution>> = flowOf(executions.values.toList())
            override fun getDailyInstancesForDateRange(start: Long, end: Long): Flow<List<DailyInstance>> = flowOf(instances.values.toList())
        }

        val registerAction = RegisterDailyActionUseCase(repository, MaterializeInstanceUseCase(repository))
        val entry = HierarchicalTimelineEntry(TimelineEntry(instance, true))

        registerAction(entry, DailyAction.Reset)

        assertEquals(0, executions.size)
        assertEquals(DailyInstanceStatus.MODIFIED, instances["inst_1"]?.status)
    }

    @Test
    fun `AC-04 - Reactive Pipeline Flow Verification - Reset triggers flow emission updating StatsSnapshot`() = runTest {
        val instancesMap = mutableMapOf<String, DailyInstance>()
        val executionsMap = mutableMapOf<String, ActivityExecution>()

        val instancesFlow = MutableStateFlow<List<DailyInstance>>(emptyList())
        val executionsFlow = MutableStateFlow<List<ActivityExecution>>(emptyList())

        val def = ActivityDefinition("def_1", "Gym", "Desc")
        val node = ActivityNode("node_1", "def_1", null, 0, "Gym Step")
        val rule = ScheduleRule("rule_1", ScheduleTarget.Node("node_1"), ScheduleRuleType.FIXED_DAYS, daysOfWeek = setOf(date.dayOfWeek.value))

        val repository = object : FakeActivityRepository() {
            override fun getActivityDefinitions(): Flow<List<ActivityDefinition>> = flowOf(listOf(def))
            override fun getAllNodes(): Flow<List<ActivityNode>> = flowOf(listOf(node))
            override fun getAllRules(): Flow<List<ScheduleRule>> = flowOf(listOf(rule))
            override suspend fun upsertDailyInstance(instance: DailyInstance) {
                instancesMap[instance.id] = instance
                instancesFlow.value = instancesMap.values.toList()
            }
            override suspend fun registerInstanceExecution(instance: DailyInstance, metadataJson: String) {
                val exec = ActivityExecution("exec_" + instance.id, "node_1", instance.id, instance.scheduledDate, System.currentTimeMillis(), metadataJson, "def_1", null, instance.titleSnapshot)
                executionsMap[exec.id] = exec
                executionsFlow.value = executionsMap.values.toList()
            }
            override suspend fun deleteExecutionsForDailyInstance(dailyInstanceId: String) {
                executionsMap.values.removeAll { it.dailyInstanceId == dailyInstanceId }
                executionsFlow.value = executionsMap.values.toList()
            }
            override fun getAllExecutions(): Flow<List<ActivityExecution>> = executionsFlow
            override fun getDailyInstancesForDateRange(start: Long, end: Long): Flow<List<DailyInstance>> = instancesFlow
        }

        val registerAction = RegisterDailyActionUseCase(repository, MaterializeInstanceUseCase(repository))
        val resolver = HistoricalOccurrenceResolver(repository, TimelineResolutionEngine())
        val analyticsUseCase = GetHistoryAnalyticsUseCase(repository, resolver)

        val instance = DailyInstance("inst_1", ScheduleTarget.Node("node_1"), date.toEpochDay(), "Gym Step", "", status = DailyInstanceStatus.PLANNED, sourceRuleId = "rule_1")
        val entry = HierarchicalTimelineEntry(TimelineEntry(instance, true))

        // Step 1: Complete
        registerAction(entry, DailyAction.Complete("{}"))
        val snapshot1 = analyticsUseCase.execute(date, date)
        assertEquals(1, snapshot1.completedCount)
        assertEquals(1.0f, snapshot1.completionRate ?: 0f, 0.01f)

        // Step 2: Reset
        val completedEntry = HierarchicalTimelineEntry(TimelineEntry(instancesMap["inst_1"]!!, true))
        registerAction(completedEntry, DailyAction.Reset)

        val snapshot2 = analyticsUseCase.execute(date, date)
        assertEquals(0, snapshot2.completedCount)
        assertEquals(0.0f, snapshot2.completionRate ?: 0f, 0.01f)
    }

    @Test
    fun `AC-05 - Multi-Rule Isolation - Resetting Rule A deletes Execution A without affecting Execution B or Instance B`() = runTest {
        val instances = mutableMapOf<String, DailyInstance>()
        val executions = mutableMapOf<String, ActivityExecution>()

        val def = ActivityDefinition("def_gym", "Gym", "Desc")
        val nodeA = ActivityNode("node_A", "def_gym", null, 0, "Gym Morning Step")
        val nodeB = ActivityNode("node_B", "def_gym", null, 1, "Gym Evening Step")

        val ruleA = ScheduleRule("rule_A", ScheduleTarget.Node("node_A"), ScheduleRuleType.FIXED_DAYS, daysOfWeek = setOf(date.dayOfWeek.value))
        val ruleB = ScheduleRule("rule_B", ScheduleTarget.Node("node_B"), ScheduleRuleType.FIXED_DAYS, daysOfWeek = setOf(date.dayOfWeek.value))

        // Rule A (08:00) & Rule B (18:00) for same Gym definition
        val instanceA = DailyInstance("inst_A", ScheduleTarget.Node("node_A"), date.toEpochDay(), "Gym Morning Step", "", status = DailyInstanceStatus.COMPLETED, sourceRuleId = "rule_A")
        val instanceB = DailyInstance("inst_B", ScheduleTarget.Node("node_B"), date.toEpochDay(), "Gym Evening Step", "", status = DailyInstanceStatus.COMPLETED, sourceRuleId = "rule_B")
        instances["inst_A"] = instanceA
        instances["inst_B"] = instanceB

        val execA = ActivityExecution("exec_A", "node_A", "inst_A", date.toEpochDay(), System.currentTimeMillis(), "{}", "def_gym", null, "Gym Morning Step")
        val execB = ActivityExecution("exec_B", "node_B", "inst_B", date.toEpochDay(), System.currentTimeMillis(), "{}", "def_gym", null, "Gym Evening Step")
        executions["exec_A"] = execA
        executions["exec_B"] = execB

        val repository = object : FakeActivityRepository() {
            override fun getActivityDefinitions(): Flow<List<ActivityDefinition>> = flowOf(listOf(def))
            override fun getAllNodes(): Flow<List<ActivityNode>> = flowOf(listOf(nodeA, nodeB))
            override fun getAllRules(): Flow<List<ScheduleRule>> = flowOf(listOf(ruleA, ruleB))
            override suspend fun deleteExecutionsForDailyInstance(dailyInstanceId: String) {
                executions.values.removeAll { it.dailyInstanceId == dailyInstanceId }
            }
            override suspend fun deleteDailyInstance(id: String) {
                instances.remove(id)
            }
            override fun getAllExecutions(): Flow<List<ActivityExecution>> = flowOf(executions.values.toList())
            override fun getDailyInstancesForDateRange(start: Long, end: Long): Flow<List<DailyInstance>> = flowOf(instances.values.toList())
        }

        val registerAction = RegisterDailyActionUseCase(repository, MaterializeInstanceUseCase(repository))
        val resolver = HistoricalOccurrenceResolver(repository, TimelineResolutionEngine())
        val analyticsUseCase = GetHistoryAnalyticsUseCase(repository, resolver)

        // Before Reset: 2 completed
        val initialSnapshot = analyticsUseCase.execute(date, date)
        assertEquals(2, initialSnapshot.completedCount)

        // Reset A
        val entryA = HierarchicalTimelineEntry(TimelineEntry(instanceA, true))
        registerAction(entryA, DailyAction.Reset)

        // Execution A removed, Instance A removed
        assertNull(executions["exec_A"])
        assertNull(instances["inst_A"])

        // Execution B & Instance B completely intact!
        assertNotNull(executions["exec_B"])
        assertNotNull(instances["inst_B"])

        // Analytics snapshot strictly reflects B only (1 completed)
        val finalSnapshot = analyticsUseCase.execute(date, date)
        assertEquals(1, finalSnapshot.completedCount)
    }

    @Test
    fun `AC-06 - Materialized recurrent occurrence reset deletes instance and execution, reverting projection to virtual`() = runTest {
        val instances = mutableMapOf<String, DailyInstance>()
        val executions = mutableMapOf<String, ActivityExecution>()

        val def = ActivityDefinition("def_1", "Routine", "Desc")
        val node = ActivityNode("node_1", "def_1", null, 0, "Routine Step")
        val rule = ScheduleRule("rule_1", ScheduleTarget.Node("node_1"), ScheduleRuleType.FIXED_DAYS, daysOfWeek = setOf(date.dayOfWeek.value))

        val materialized = DailyInstance("inst_rec", ScheduleTarget.Node("node_1"), date.toEpochDay(), "Routine Step", "", status = DailyInstanceStatus.COMPLETED, sourceRuleId = "rule_1")
        instances[materialized.id] = materialized
        val exec = ActivityExecution("exec_rec", "node_1", "inst_rec", date.toEpochDay(), System.currentTimeMillis(), "{}", "def_1", null, "Routine Step")
        executions[exec.id] = exec

        val repository = object : FakeActivityRepository() {
            override fun getActivityDefinitions(): Flow<List<ActivityDefinition>> = flowOf(listOf(def))
            override fun getAllNodes(): Flow<List<ActivityNode>> = flowOf(listOf(node))
            override fun getAllRules(): Flow<List<ScheduleRule>> = flowOf(listOf(rule))
            override suspend fun deleteExecutionsForDailyInstance(dailyInstanceId: String) {
                executions.values.removeAll { it.dailyInstanceId == dailyInstanceId }
            }
            override suspend fun deleteDailyInstance(id: String) {
                instances.remove(id)
            }
            override fun getAllExecutions(): Flow<List<ActivityExecution>> = flowOf(executions.values.toList())
            override fun getDailyInstancesForDateRange(start: Long, end: Long): Flow<List<DailyInstance>> = flowOf(instances.values.toList())
        }

        val registerAction = RegisterDailyActionUseCase(repository, MaterializeInstanceUseCase(repository))
        val resolver = HistoricalOccurrenceResolver(repository, TimelineResolutionEngine())
        val analyticsUseCase = GetHistoryAnalyticsUseCase(repository, resolver)

        // Before Reset: 1 completed
        val initialSnapshot = analyticsUseCase.execute(date, date)
        assertEquals(1, initialSnapshot.completedCount)

        // Reset
        val entry = HierarchicalTimelineEntry(TimelineEntry(materialized, true))
        registerAction(entry, DailyAction.Reset)

        assertEquals(0, executions.size)
        assertEquals(0, instances.size)

        // After Reset: Instance deleted, projected as virtual PLANNED, execution == 0
        val finalSnapshot = analyticsUseCase.execute(date, date)
        assertEquals(0, finalSnapshot.completedCount)
        assertEquals(0.0f, finalSnapshot.completionRate ?: 0f, 0.01f)
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
        override suspend fun deleteExecutionsForDailyInstance(dailyInstanceId: String) {}
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
        override suspend fun getDailyInstanceBySourceRule(sourceRuleId: String, date: Long): DailyInstance? = null
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
    }
}
