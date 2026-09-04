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

class HierarchicalSchedulingTest {

    private val date = LocalDate.of(2023, 10, 23) // Monday

    @Test
    fun `Case 1 Parent has rule, Children don't - Only Parent appears`() = runTest {
        val def = ActivityDefinition("d1", "Uni", "Desc")
        val node1 = ActivityNode("n1", "d1", null, 0, "Root")
        val child1 = ActivityNode("c1", "d1", "n1", 0, "Sub")
        
        val rule = ScheduleRule(
            id = "r1",
            target = ScheduleTarget.Node("n1"),
            type = ScheduleRuleType.FIXED_DAYS,
            daysOfWeek = setOf(1),
            startTime = 480
        )

        val repository = object : FakeActivityRepository() {
            override fun getActivityDefinitions() = flowOf(listOf(def))
            override fun getAllNodes() = flowOf(listOf(node1, child1))
            override fun getAllRules() = flowOf(listOf(rule))
        }

        val conflictDetector = ConflictDetectorUseCase()
        val useCase = ResolveTimelineUseCase(repository, TimelineResolutionEngine(), conflictDetector, SuggestionEngine(conflictDetector))
        val timeline = useCase(date).first()

        assertEquals(1, timeline.size)
        assertEquals("Root", timeline[0].instance.titleSnapshot)
    }

    @Test
    fun `Case 3 Both have independent rules - Both appear`() = runTest {
        val def = ActivityDefinition("d1", "Uni", "Desc")
        val node1 = ActivityNode("n1", "d1", null, 0, "Root")
        val child1 = ActivityNode("c1", "d1", "n1", 0, "Sub")
        
        val ruleParent = ScheduleRule(
            id = "rp",
            target = ScheduleTarget.Node("n1"),
            type = ScheduleRuleType.FIXED_DAYS,
            daysOfWeek = setOf(1),
            startTime = 480
        )
        
        val ruleChild = ScheduleRule(
            id = "rc",
            target = ScheduleTarget.Node("c1"),
            type = ScheduleRuleType.FIXED_DAYS,
            daysOfWeek = setOf(1),
            startTime = 600
        )

        val repository = object : FakeActivityRepository() {
            override fun getActivityDefinitions() = flowOf(listOf(def))
            override fun getAllNodes() = flowOf(listOf(node1, child1))
            override fun getAllRules() = flowOf(listOf(ruleParent, ruleChild))
        }

        val conflictDetector = ConflictDetectorUseCase()
        val useCase = ResolveTimelineUseCase(repository, TimelineResolutionEngine(), conflictDetector, SuggestionEngine(conflictDetector))
        val timeline = useCase(date).first()

        assertEquals(2, timeline.size)
        assertEquals("Root", timeline[0].instance.titleSnapshot)
        assertEquals("Sub", timeline[1].instance.titleSnapshot)
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
