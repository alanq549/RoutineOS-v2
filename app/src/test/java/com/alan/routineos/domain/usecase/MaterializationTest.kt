package com.alan.routineos.domain.usecase

import com.alan.routineos.domain.model.*
import com.alan.routineos.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class MaterializationTest {

    @Test
    fun `MaterializeInstanceUseCase creates persistent instance with new ID`() = runTest {
        val virtual = DailyInstance(
            id = "virtual_1",
            target = ScheduleTarget.Node("node1"),
            scheduledDate = 100L,
            titleSnapshot = "Virtual Title",
            descriptionSnapshot = "Desc",
            sourceRuleId = "rule_1"
        )
        
        var saved: DailyInstance? = null
        val repository = object : FakeActivityRepository() {
            override suspend fun getDailyInstanceBySourceRule(sourceRuleId: String, date: Long) = null
            override suspend fun upsertDailyInstance(instance: DailyInstance) {
                saved = instance
            }
        }
        
        val useCase = MaterializeInstanceUseCase(repository)
        val result = useCase(virtual, DailyInstanceStatus.MODIFIED)

        assertEquals("Virtual Title", saved?.titleSnapshot)
        assertEquals(DailyInstanceStatus.MODIFIED, saved?.status)
        assertNotEquals("virtual_1", saved?.id) // ID must be generated
        assertEquals(saved?.id, result.id)
    }

    @Test
    fun `Caso E - Regression Test - Materialize Rule B does NOT return existing instance of Rule A for same target and date`() = runTest {
        val gymTarget = ScheduleTarget.Definition("gym_def")
        val date = 200L

        // Instance A for Rule A already in DB
        val instanceA = DailyInstance(
            id = "instance_A_id",
            target = gymTarget,
            scheduledDate = date,
            titleSnapshot = "Gym Morning",
            descriptionSnapshot = "",
            sourceRuleId = "rule_A",
            status = DailyInstanceStatus.COMPLETED
        )

        // Virtual occurrence B for Rule B
        val virtualB = DailyInstance(
            id = "virtual_B",
            target = gymTarget,
            scheduledDate = date,
            titleSnapshot = "Gym Evening",
            descriptionSnapshot = "",
            sourceRuleId = "rule_B"
        )

        val db = mutableMapOf<String, DailyInstance>()
        db["rule_A"] = instanceA

        val repository = object : FakeActivityRepository() {
            override suspend fun getDailyInstanceBySourceRule(sourceRuleId: String, date: Long): DailyInstance? {
                return if (sourceRuleId == "rule_A") instanceA else null
            }
            override suspend fun upsertDailyInstance(instance: DailyInstance) {
                if (instance.sourceRuleId != null) {
                    db[instance.sourceRuleId!!] = instance
                }
            }
        }

        val useCase = MaterializeInstanceUseCase(repository)
        val result = useCase(virtualB, DailyInstanceStatus.PLANNED)

        // MUST be for Rule B, NEVER for Rule A!
        assertEquals("rule_B", result.sourceRuleId)
        assertNotEquals("instance_A_id", result.id)
        assertNotEquals("rule_A", result.sourceRuleId)
        assertNotNull(db["rule_B"])
    }

    @Test
    fun `Caso B - Materialization idempotency returns existing instance when called twice for same rule and date`() = runTest {
        val date = 300L
        val ruleId = "rule_idempotent"
        val virtual = DailyInstance(
            id = "virtual_1",
            target = ScheduleTarget.Definition("def_1"),
            scheduledDate = date,
            titleSnapshot = "Routine",
            descriptionSnapshot = "",
            sourceRuleId = ruleId
        )

        val db = mutableMapOf<Pair<String, Long>, DailyInstance>()

        val repository = object : FakeActivityRepository() {
            override suspend fun getDailyInstanceBySourceRule(sourceRuleId: String, date: Long): DailyInstance? {
                return db[Pair(sourceRuleId, date)]
            }
            override suspend fun upsertDailyInstance(instance: DailyInstance) {
                if (instance.sourceRuleId != null) {
                    db[Pair(instance.sourceRuleId!!, instance.scheduledDate)] = instance
                }
            }
        }

        val useCase = MaterializeInstanceUseCase(repository)
        val first = useCase(virtual, DailyInstanceStatus.PLANNED)
        val second = useCase(virtual, DailyInstanceStatus.PLANNED)

        assertEquals(first.id, second.id)
        assertEquals(ruleId, second.sourceRuleId)
    }

    @Test
    fun `Caso C - Instance with sourceRuleId null is treated as punctual instance without rule lookup`() = runTest {
        val punctual = DailyInstance(
            id = "punctual_1",
            target = ScheduleTarget.Definition("def_1"),
            scheduledDate = 400L,
            titleSnapshot = "Punctual Gym",
            descriptionSnapshot = "",
            sourceRuleId = null
        )

        var searchedSourceRule = false
        var savedInstance: DailyInstance? = null

        val repository = object : FakeActivityRepository() {
            override suspend fun getDailyInstanceBySourceRule(sourceRuleId: String, date: Long): DailyInstance? {
                searchedSourceRule = true
                return null
            }
            override suspend fun upsertDailyInstance(instance: DailyInstance) {
                savedInstance = instance
            }
        }

        val useCase = MaterializeInstanceUseCase(repository)
        val result = useCase(punctual, DailyInstanceStatus.PLANNED)

        assertEquals(false, searchedSourceRule)
        assertEquals("punctual_1", result.id)
        assertEquals(null, result.sourceRuleId)
        assertNotNull(savedInstance)
    }

    @Test
    fun `Caso D - Multiple punctual instances of same target on same date coexist without target-date uniqueness enforcement`() = runTest {
        val target = ScheduleTarget.Definition("gym_def")
        val date = 600L

        val punctual1 = DailyInstance(
            id = "p1",
            target = target,
            scheduledDate = date,
            titleSnapshot = "Gym Session 1",
            descriptionSnapshot = "",
            sourceRuleId = null,
            isAdHoc = false
        )

        val punctual2 = DailyInstance(
            id = "p2",
            target = target,
            scheduledDate = date,
            titleSnapshot = "Gym Session 2",
            descriptionSnapshot = "",
            sourceRuleId = null,
            isAdHoc = false
        )

        val savedInstances = mutableListOf<DailyInstance>()
        val repository = object : FakeActivityRepository() {
            override suspend fun upsertDailyInstance(instance: DailyInstance) {
                savedInstances.add(instance)
            }
        }

        val useCase = MaterializeInstanceUseCase(repository)
        val res1 = useCase(punctual1, DailyInstanceStatus.PLANNED)
        val res2 = useCase(punctual2, DailyInstanceStatus.PLANNED)

        assertEquals(2, savedInstances.size)
        assertEquals("p1", res1.id)
        assertEquals("p2", res2.id)
        assertNotEquals(res1.id, res2.id)
        assertEquals(null, res1.sourceRuleId)
        assertEquals(null, res2.sourceRuleId)
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
        override suspend fun registerInstanceExecution(instance: DailyInstance, metadataJson: String) {}
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
