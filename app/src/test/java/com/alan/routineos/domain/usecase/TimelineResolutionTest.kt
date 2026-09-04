package com.alan.routineos.domain.usecase

import com.alan.routineos.domain.model.*
import com.alan.routineos.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class TimelineResolutionTest {

    @Test
    fun `resolves virtual fixed days rule correctly`() = runTest {
        val date = LocalDate.of(2023, 10, 23) // Monday
        val rule = ScheduleRule(
            id = "r1",
            target = ScheduleTarget.Node("node1"),
            type = ScheduleRuleType.FIXED_DAYS,
            daysOfWeek = setOf(1), // Monday
            startTime = 480
        )
        
        val node = ActivityNode("node1", "act1", null, 0, "Node 1")
        
        val repository = object : FakeActivityRepository() {
            override fun getAllRules() = flowOf(listOf(rule))
            override fun getAllNodes() = flowOf(listOf(node))
            override fun getDailyInstancesForDate(date: Long) = flowOf(emptyList<DailyInstance>())
            override fun getActivityDefinitions() = flowOf(emptyList<ActivityDefinition>())
            override fun getAllExceptions() = flowOf(emptyList<ScheduleException>())
        }
        
        val conflictDetector = ConflictDetectorUseCase()
        val useCase = ResolveTimelineUseCase(repository, TimelineResolutionEngine(), conflictDetector, SuggestionEngine(conflictDetector))
        val result = useCase(date).first()

        assertEquals(1, result.size)
        assertEquals("Node 1", result[0].instance.titleSnapshot)
        assertEquals(false, result[0].isMaterialized)
    }

    @Test
    fun `materialized instance has precedence over rule`() = runTest {
        val date = LocalDate.of(2023, 10, 23) // Monday
        val rule = ScheduleRule(
            id = "r1",
            target = ScheduleTarget.Node("node1"),
            type = ScheduleRuleType.FIXED_DAYS,
            daysOfWeek = setOf(1),
            startTime = 480
        )
        
        val materialized = DailyInstance(
            id = "m1",
            target = ScheduleTarget.Node("node1"),
            scheduledDate = date.toEpochDay(),
            titleSnapshot = "Overridden Title",
            descriptionSnapshot = "",
            status = DailyInstanceStatus.MODIFIED,
            sourceRuleId = "r1"
        )
        
        val node = ActivityNode("node1", "act1", null, 0, "Node 1")
        
        val repository = object : FakeActivityRepository() {
            override fun getAllRules() = flowOf(listOf(rule))
            override fun getDailyInstancesForDate(date: Long) = flowOf(listOf(materialized))
            override fun getAllNodes() = flowOf(listOf(node))
            override fun getActivityDefinitions() = flowOf(emptyList<ActivityDefinition>())
            override fun getAllExceptions() = flowOf(emptyList<ScheduleException>())
        }
        
        val conflictDetector = ConflictDetectorUseCase()
        val useCase = ResolveTimelineUseCase(repository, TimelineResolutionEngine(), conflictDetector, SuggestionEngine(conflictDetector))
        val result = useCase(date).first()

        // Should only have the materialized one, not the virtual one
        assertEquals(1, result.size)
        assertEquals("Overridden Title", result[0].instance.titleSnapshot)
        assertEquals(true, result[0].isMaterialized)
    }

    @Test
    fun `projects an ad hoc interruption conflict into the resolved timeline`() = runTest {
        val date = LocalDate.of(2023, 10, 23)
        val university = DailyInstance(
            id = "university", target = null, scheduledDate = date.toEpochDay(),
            titleSnapshot = "Universidad", descriptionSnapshot = "",
            plannedStartTime = 420, plannedEndTime = 600,
            mobility = TemporalMobility.IMMOBILE
        )
        val adHoc = DailyInstance(
            id = "ad-hoc", target = null, scheduledDate = date.toEpochDay(),
            titleSnapshot = "Salida Express", descriptionSnapshot = "",
            plannedStartTime = 480, plannedDurationMinutes = 30, isAdHoc = true
        )
        val repository = object : FakeActivityRepository() {
            override fun getActivityDefinitions() = flowOf(emptyList<ActivityDefinition>())
            override fun getAllNodes() = flowOf(emptyList<ActivityNode>())
            override fun getAllRules() = flowOf(emptyList<ScheduleRule>())
            override fun getAllExceptions() = flowOf(emptyList<ScheduleException>())
            override fun getDailyInstancesForDate(date: Long) = flowOf(listOf(university, adHoc))
        }

        val detector = ConflictDetectorUseCase()
        val result = ResolveTimelineUseCase(repository, TimelineResolutionEngine(), detector, SuggestionEngine(detector))(date).first()

        assertEquals(2, result.size)
        assertTrue(result.all { it.conflict?.details?.any { detail -> detail.isInterruption } == true })
    }

    private open class FakeActivityRepository : ActivityRepository {
        override fun getActivityDefinitions(): Flow<List<ActivityDefinition>> = TODO()
        override fun getAllNodes(): Flow<List<ActivityNode>> = TODO()
        override suspend fun getActivityDefinitionById(id: String): ActivityDefinition? = null
        override suspend fun upsertActivityDefinition(activityDefinition: ActivityDefinition) {}
        override suspend fun deleteActivityDefinition(activityDefinition: ActivityDefinition) {}
        override fun getNodesForActivityDefinition(activityDefinitionId: String): Flow<List<ActivityNode>> = TODO()
        override suspend fun getNodesListForActivityDefinition(activityDefinitionId: String): List<ActivityNode> = emptyList()
        override suspend fun getNodeById(id: String): ActivityNode? = null
        override suspend fun upsertNode(node: ActivityNode) {}
        override suspend fun deleteNode(node: ActivityNode) {}
        override suspend fun reorderNodes(nodeIds: List<String>) {}
        override suspend fun moveNode(nodeId: String, newParentId: String?) {}
        override suspend fun registerExecution(nodeId: String, scheduledDate: Long, metadataJson: String, dailyInstanceId: String?) {}
        override fun getAllExecutions(): Flow<List<ActivityExecution>> = flowOf(emptyList())
        override fun getExecutionsForNode(nodeId: String): Flow<List<ActivityExecution>> = TODO()
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
