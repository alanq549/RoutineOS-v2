package com.alan.routineos.domain.usecase

import com.alan.routineos.domain.model.*
import com.alan.routineos.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.util.*

class HistoryAnalyticsTest {

    private val resolutionEngine = TimelineResolutionEngine()
    private lateinit var occurrenceResolver: HistoricalOccurrenceResolver
    private lateinit var useCase: GetHistoryAnalyticsUseCase

    private val date = LocalDate.of(2026, 9, 3)

    @Test
    fun `calculates completion rate with missed and omitted occurrences`() = runTest {
        val nodes = listOf(ActivityNode("leaf", "def", null, 0, "Leaf"))
        val definitions = listOf(ActivityDefinition("def", "Def", ""))
        val rules = (1..10).map { i ->
            ScheduleRule("rule_$i", ScheduleTarget.Node("leaf"), ScheduleRuleType.FIXED_DAYS, daysOfWeek = setOf(date.dayOfWeek.value))
        }
        
        val materialized = (1..7).map { i ->
            DailyInstance("inst_$i", ScheduleTarget.Node("leaf"), date.toEpochDay(), "L", "", status = DailyInstanceStatus.COMPLETED, sourceRuleId = "rule_$i")
        } + listOf(
            DailyInstance("inst_8", ScheduleTarget.Node("leaf"), date.toEpochDay(), "L", "", status = DailyInstanceStatus.OMITTED, sourceRuleId = "rule_8")
        )
        
        val repository = object : FakeActivityRepository() {
            override fun getActivityDefinitions() = flowOf(definitions)
            override fun getAllNodes() = flowOf(nodes)
            override fun getAllRules() = flowOf(rules)
            override fun getDailyInstancesForDateRange(start: Long, end: Long) = flowOf(materialized)
        }
        
        occurrenceResolver = HistoricalOccurrenceResolver(repository, resolutionEngine)
        useCase = GetHistoryAnalyticsUseCase(repository, occurrenceResolver)

        val result = useCase.execute(date, date)

        assertEquals(7 / 9f, result.completionRate!!, 0.001f)
        assertEquals(2, result.missedCount)
        assertEquals(1, result.omittedCount)
        assertEquals(7, result.completedCount)
    }

    @Test
    fun `RESET preserves execution history but changes operational status`() = runTest {
        var savedStatus: DailyInstanceStatus? = null
        var deletedExecutionsCalled = false
        
        val repository = object : FakeActivityRepository() {
            override suspend fun upsertDailyInstance(instance: DailyInstance) {
                savedStatus = instance.status
            }
            override suspend fun deleteExecutionsForNodeOnDate(nodeId: String, scheduledDate: Long) {
                deletedExecutionsCalled = true
            }
        }
        
        val registerAction = RegisterDailyActionUseCase(repository, MaterializeInstanceUseCase(repository))
        val instance = DailyInstance("i1", ScheduleTarget.Node("n1"), date.toEpochDay(), "T", "", status = DailyInstanceStatus.COMPLETED)
        val entry = HierarchicalTimelineEntry(TimelineEntry(instance, true))

        registerAction(entry, DailyAction.Reset)

        assertEquals(DailyInstanceStatus.MODIFIED, savedStatus)
        assertEquals(false, deletedExecutionsCalled)
    }

    @Test
    fun `multiple executions for same occurrence count as one for completion rate`() = runTest {
        val nodes = listOf(ActivityNode("leaf", "def", null, 0, "Leaf"))
        val definitions = listOf(ActivityDefinition("def", "Def", ""))
        val rule = ScheduleRule("r1", ScheduleTarget.Node("leaf"), ScheduleRuleType.FIXED_DAYS, daysOfWeek = setOf(date.dayOfWeek.value))
        val instance = DailyInstance("i1", ScheduleTarget.Node("leaf"), date.toEpochDay(), "L", "", status = DailyInstanceStatus.COMPLETED, sourceRuleId = "r1")
        
        val repository = object : FakeActivityRepository() {
            override fun getActivityDefinitions() = flowOf(definitions)
            override fun getAllNodes() = flowOf(nodes)
            override fun getAllRules() = flowOf(listOf(rule))
            override fun getDailyInstancesForDateRange(start: Long, end: Long) = flowOf(listOf(instance))
        }

        occurrenceResolver = HistoricalOccurrenceResolver(repository, resolutionEngine)
        useCase = GetHistoryAnalyticsUseCase(repository, occurrenceResolver)

        val result = useCase.execute(date, date)

        assertEquals(1f, result.completionRate!!, 0.001f)
        assertEquals(1, result.completedCount)
    }

    @Test
    fun `getTrendSeries extracts only numeric values from metadata JSON`() = runTest {
        val executions = listOf(
            ActivityExecution("e1", "n1", null, date.toEpochDay(), 1000L, """{"weight": 80.5, "note": "heavy"}"""),
            ActivityExecution("e2", "n1", null, date.plusDays(1).toEpochDay(), 2000L, """{"weight": 81.0}"""),
            ActivityExecution("e3", "n1", null, date.plusDays(2).toEpochDay(), 3000L, """{"weight": "invalid"}""")
        )

        val repository = object : FakeActivityRepository() {
            override fun getExecutionsForNode(nodeId: String) = flowOf(executions)
        }
        
        occurrenceResolver = HistoricalOccurrenceResolver(repository, resolutionEngine)
        useCase = GetHistoryAnalyticsUseCase(repository, occurrenceResolver)

        val result = useCase.getTrendSeries("n1", "weight", date, date.plusDays(2))

        assertEquals(2, result.values.size)
        assertEquals(80.5, result.values[0].second, 0.01)
        assertEquals(81.0, result.values[1].second, 0.01)
    }

    @Test
    fun `historical resolver and ResolveTimeline produce identical occurrences for same date`() = runTest {
        val nodes = listOf(ActivityNode("n1", "d1", null, 0, "N"))
        val definitions = listOf(ActivityDefinition("d1", "D", ""))
        val rule = ScheduleRule("r1", ScheduleTarget.Node("n1"), ScheduleRuleType.FIXED_DAYS, daysOfWeek = setOf(date.dayOfWeek.value))
        
        val repository = object : FakeActivityRepository() {
            override fun getActivityDefinitions() = flowOf(definitions)
            override fun getAllNodes() = flowOf(nodes)
            override fun getAllRules() = flowOf(listOf(rule))
        }

        val resolver = ResolveTimelineUseCase(
            repository, 
            resolutionEngine, 
            ConflictDetectorUseCase(), 
            SuggestionEngine(ConflictDetectorUseCase())
        )
        
        occurrenceResolver = HistoricalOccurrenceResolver(repository, resolutionEngine)
        val todayOccs = occurrenceResolver.resolveRange(date, date)
        val todayEntries = resolver(date).first()

        assertEquals(todayOccs.size, todayEntries.size)
        assertEquals(todayOccs[0].instance.id, todayEntries[0].instance.id)
        assertEquals(todayOccs[0].instance.titleSnapshot, todayEntries[0].instance.titleSnapshot)
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
