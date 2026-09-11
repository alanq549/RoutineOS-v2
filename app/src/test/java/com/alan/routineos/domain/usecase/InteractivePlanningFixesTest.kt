package com.alan.routineos.domain.usecase

import com.alan.routineos.domain.model.*
import com.alan.routineos.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.util.*

class InteractivePlanningFixesTest {

    private val deletedInstanceIds = mutableListOf<String>()

    @Test
    fun `RESET - Deletes DailyInstance if it is a rule override`() = runTest {
        val repository = FakeActivityRepo(deletedInstanceIds)
        val useCase = RegisterDailyActionUseCase(repository, MaterializeInstanceUseCase(repository))
        
        val overrideInstance = DailyInstance(
            id = "instance_1",
            target = ScheduleTarget.Node("node_1"),
            scheduledDate = 0,
            titleSnapshot = "Gym",
            descriptionSnapshot = "",
            plannedStartTime = 540,
            sourceRuleId = "rule_1"
        )
        
        val entry = HierarchicalTimelineEntry(
            root = TimelineEntry(instance = overrideInstance, isMaterialized = true)
        )
        
        useCase(entry, DailyAction.Reset)
        
        assertTrue("DailyInstance override should be deleted", deletedInstanceIds.contains("instance_1"))
    }

    @Test
    fun `PREVENTIVE - Simulation detects conflict correctly`() {
        val conflictDetector = ConflictDetectorUseCase()
        val simulateMoveUseCase = SimulateMoveUseCase(conflictDetector)
        
        val gym = createInstance("gym", 480, 60) // 08:00 - 09:00
        val work = createInstance("work", 540, 480) // 09:00 - 17:00
        
        val result = simulateMoveUseCase(
            currentInstances = listOf(gym, work),
            targetId = "gym",
            newStartTime = 510, // 08:30
            newEndTime = 570    // 09:30 -> OVERLAPS Work
        )
        
        assertTrue("Simulation should detect conflict", result.hasConflict)
        assertEquals(TemporalImpact.WARNING, result.impact)
    }

    @Test
    fun `V3 CONFLICTS - POINT does not generate overlap with BLOCK`() {
        val detector = ConflictDetectorUseCase()
        
        val block = createInstance("block", 600, 60) // 10:00 - 11:00
        val point = createInstance("point", 630, 0).copy(plannedDurationMinutes = null, plannedEndTime = null) // 10:30 (POINT)
        
        val results = detector.detectConflicts(listOf(block, point))
        
        assertFalse("Block should NOT have conflict with Point", results["block"]?.hasConflict ?: true)
        assertFalse("Point should NOT have conflict with Block", results["point"]?.hasConflict ?: true)
    }

    private fun createInstance(id: String, start: Int, dur: Int) = DailyInstance(
        id = id,
        target = null,
        scheduledDate = 0,
        titleSnapshot = id,
        descriptionSnapshot = "",
        plannedStartTime = start,
        plannedDurationMinutes = dur
    )

    private class FakeActivityRepo(private val deletedIds: MutableList<String>) : ActivityRepository {
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
        override suspend fun deleteDailyInstance(id: String) {
            deletedIds.add(id)
        }
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
