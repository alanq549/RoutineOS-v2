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

class GoldTestEC008 {

    private val date = LocalDate.of(2023, 10, 23) // Monday
    private val epochDay = date.toEpochDay()

    @Test
    fun `Materialized instance overrides Rule with same sourceRuleId`() = runTest {
        val target = ScheduleTarget.Node("n1")
        val rule = ScheduleRule(
            id = "rule1",
            target = target,
            type = ScheduleRuleType.FIXED_DAYS,
            daysOfWeek = setOf(1), // Monday
            startTime = 480 // 08:00
        )
        
        val materialized = DailyInstance(
            id = "real1",
            target = target,
            scheduledDate = epochDay,
            titleSnapshot = "Moved Title",
            descriptionSnapshot = "",
            plannedStartTime = 570, // 09:30
            status = DailyInstanceStatus.MODIFIED,
            sourceRuleId = "rule1"
        )
        
        val repository = object : FakeActivityRepository() {
            override fun getAllRules() = flowOf(listOf(rule))
            override fun getDailyInstancesForDate(date: Long) = flowOf(listOf(materialized))
            override fun getAllNodes() = flowOf(listOf(ActivityNode("n1", "a1", null, 0, "Node 1")))
        }
        
        val useCase = ResolveTimelineUseCase(repository, ConflictDetectorUseCase())
        val result = useCase(date).first()

        // 1. Verify only 1 entry exists
        assertEquals(1, result.size)
        // 2. Verify it's the materialized one
        assertEquals("real1", result[0].instance.id)
        assertEquals(570, result[0].instance.plannedStartTime)
        assertEquals(true, result[0].isMaterialized)
    }

    @Test
    fun `Multiple rules for same target on same day coexist independently`() = runTest {
        val target = ScheduleTarget.Node("n1")
        val ruleA = ScheduleRule(id = "ruleA", target = target, type = ScheduleRuleType.FIXED_DAYS, daysOfWeek = setOf(1), startTime = 480)
        val ruleB = ScheduleRule(id = "ruleB", target = target, type = ScheduleRuleType.FIXED_DAYS, daysOfWeek = setOf(1), startTime = 1080)
        
        val repository = object : FakeActivityRepository() {
            override fun getAllRules() = flowOf(listOf(ruleA, ruleB))
            override fun getAllNodes() = flowOf(listOf(ActivityNode("n1", "a1", null, 0, "Node 1")))
        }
        
        val useCase = ResolveTimelineUseCase(repository, ConflictDetectorUseCase())
        
        // Initial state
        val result1 = useCase(date).first()
        assertEquals(2, result1.size)
        assertEquals(480, result1[0].instance.plannedStartTime)
        assertEquals(1080, result1[1].instance.plannedStartTime)

        // Materialize Rule A at a different time
        val materializedA = DailyInstance(
            id = "realA", target = target, scheduledDate = epochDay, 
            titleSnapshot = "A", descriptionSnapshot = "", 
            plannedStartTime = 570, status = DailyInstanceStatus.MODIFIED, 
            sourceRuleId = "ruleA"
        )

        val repositoryWithA = object : FakeActivityRepository() {
            override fun getAllRules() = flowOf(listOf(ruleA, ruleB))
            override fun getDailyInstancesForDate(date: Long) = flowOf(listOf(materializedA))
            override fun getAllNodes() = flowOf(listOf(ActivityNode("n1", "a1", null, 0, "Node 1")))
        }

        val useCaseWithA = ResolveTimelineUseCase(repositoryWithA, ConflictDetectorUseCase())
        val result2 = useCaseWithA(date).first()

        assertEquals(2, result2.size)
        // Rule A materialization (09:30)
        assertEquals(570, result2[0].instance.plannedStartTime)
        assertEquals("realA", result2[0].instance.id)
        // Rule B virtual (18:00)
        assertEquals(1080, result2[1].instance.plannedStartTime)
        assertEquals("virtual_ruleB_$epochDay", result2[1].instance.id)
    }

    @Test
    fun `Materialized instance with same rule ID does not generate false conflicts`() = runTest {
        val target = ScheduleTarget.Node("n1")
        val rule = ScheduleRule(
            id = "rule1",
            target = target,
            type = ScheduleRuleType.FIXED_DAYS,
            daysOfWeek = setOf(1),
            startTime = 480 // 08:00
        )
        
        // Persisted version of the SAME rule
        val materialized = DailyInstance(
            id = "persisted1",
            target = target,
            scheduledDate = epochDay,
            titleSnapshot = "Title",
            descriptionSnapshot = "",
            plannedStartTime = 480, // Same time
            status = DailyInstanceStatus.MODIFIED,
            sourceRuleId = "rule1"
        )
        
        val repository = object : FakeActivityRepository() {
            override fun getAllRules() = flowOf(listOf(rule))
            override fun getDailyInstancesForDate(date: Long) = flowOf(listOf(materialized))
            override fun getAllNodes() = flowOf(listOf(ActivityNode("n1", "a1", null, 0, "Node 1")))
        }
        
        val useCase = ResolveTimelineUseCase(repository, ConflictDetectorUseCase())
        val result = useCase(date).first()

        // 1. Verify only 1 entry (Identity replacement)
        assertEquals("Should only have one entry when rule is materialized", 1, result.size)
        // 2. Verify no conflict is detected against itself/virtual
        assertEquals("Should not have conflicts", false, result[0].conflict?.hasConflict)
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
