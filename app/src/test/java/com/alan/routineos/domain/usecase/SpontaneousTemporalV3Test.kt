package com.alan.routineos.domain.usecase

import com.alan.routineos.domain.model.*
import com.alan.routineos.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class SpontaneousTemporalV3Test {

    private val conflictDetector = ConflictDetectorUseCase()

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

    private class FakeResolveTimelineUseCase(val entries: List<TimelineEntry>) : ResolveTimelineUseCase(
        FakeRepository(), TimelineResolutionEngine(), ConflictDetectorUseCase(), SuggestionEngine(ConflictDetectorUseCase())
    ) {
        override fun invoke(date: LocalDate): Flow<List<TimelineEntry>> = flowOf(entries)
    }

    private fun createAdHoc(id: String, start: Int? = null, end: Int? = null, dur: Int? = null, parentId: String? = null): TimelineEntry {
        return TimelineEntry(
            instance = DailyInstance(
                id = id,
                target = null,
                scheduledDate = LocalDate.now().toEpochDay(),
                titleSnapshot = "Event $id",
                descriptionSnapshot = "",
                plannedStartTime = start,
                plannedEndTime = end,
                plannedDurationMinutes = dur,
                parentInstanceId = parentId,
                isAdHoc = true
            ),
            isMaterialized = true
        )
    }

    @Test
    fun `V3 Rule - Point stays point`() = runTest {
        val parent = createAdHoc("P", start = 1080) // 18:00
        val useCase = GetHierarchicalTimelineUseCase(FakeRepository(), FakeResolveTimelineUseCase(listOf(parent)))
        
        val result = useCase(LocalDate.now()).first()
        val entry = result.first()
        
        assertEquals(1080, entry.effectiveStartTimeMinutes)
        assertNull(entry.totalDurationMinutes)
    }

    @Test
    fun `V3 Rule - Derived range does not overwrite explicit point`() = runTest {
        val parent = createAdHoc("P", start = 1080) // 18:00 Point
        val child = createAdHoc("C", start = 1100, dur = 15, parentId = "P") // 18:20 Range
        
        val useCase = GetHierarchicalTimelineUseCase(FakeRepository(), FakeResolveTimelineUseCase(listOf(parent, child)))
        
        val result = useCase(LocalDate.now()).first()
        val parentEntry = result.find { it.root.instance.id == "P" }!!
        
        // Still 18:00 (explicit point), duration remains null because derivation is subordinate to explicit decisions
        assertEquals(1080, parentEntry.effectiveStartTimeMinutes)
        assertNull(parentEntry.totalDurationMinutes)
    }
}
