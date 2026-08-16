package com.alan.routineos.domain.usecase

import com.alan.routineos.domain.model.*
import com.alan.routineos.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HierarchyVerificationTest {

    private lateinit var getTreeUseCase: GetActivityTreeUseCase
    private lateinit var validateUseCase: ValidateActivityNodeUseCase
    private lateinit var fakeRepository: FakeActivityRepository

    @Before
    fun setup() {
        fakeRepository = FakeActivityRepository()
        getTreeUseCase = GetActivityTreeUseCase(fakeRepository)
        validateUseCase = ValidateActivityNodeUseCase()
    }

    @Test
    fun `reconstructs complex university hierarchy correctly`() = runBlocking {
        val defId = "univ1"
        val nodes = listOf(
            ActivityNode("1", defId, null, 0, "Universidad"),
            ActivityNode("1.1", defId, "1", 0, "Bases de Datos"),
            ActivityNode("1.1.1", defId, "1.1", 0, "SQL Lab"),
            ActivityNode("1.1.2", defId, "1.1", 1, "NoSQL Lab"),
            ActivityNode("1.2", defId, "1", 1, "Redes"),
            ActivityNode("1.2.1", defId, "1.2", 0, "VLAN"),
            ActivityNode("1.2.2", defId, "1.2", 1, "EIGRP"),
            ActivityNode("1.3", defId, "1", 2, "Ingeniería de Software")
        )
        fakeRepository.setNodes(nodes)

        val tree = getTreeUseCase(defId, 0).first()

        assertEquals(1, tree.size)
        val root = tree[0]
        assertEquals("Universidad", root.node.title)
        assertEquals(3, root.children.size)

        val db = root.children[0]
        assertEquals("Bases de Datos", db.node.title)
        assertEquals(2, db.children.size)
        assertEquals("SQL Lab", db.children[0].node.title)
        assertEquals("NoSQL Lab", db.children[1].node.title)

        val redes = root.children[1]
        assertEquals("Redes", redes.node.title)
        assertEquals(2, redes.children.size)

        val software = root.children[2]
        assertEquals("Ingeniería de Software", software.node.title)
        assertTrue(software.children.isEmpty())
    }

    @Test
    fun `validation rejects self-parenting`() {
        val node = ActivityNode("1", "def1", "1", 0, "Self")
        val error = validateUseCase(node, emptyList())
        assertEquals(ActivityNodeValidationError.SelfParent, error)
    }

    @Test
    fun `validation rejects cycles`() {
        val nodes = listOf(
            ActivityNode("1", "def1", "2", 0, "A"),
            ActivityNode("2", "def1", null, 0, "B")
        )
        val nodeAUpdated = ActivityNode("2", "def1", "1", 0, "B") // Try to set B's parent to A
        val error = validateUseCase(nodeAUpdated, nodes)
        assertEquals(ActivityNodeValidationError.AncestorCycle, error)
    }

    @Test
    fun `validation rejects cross-definition parent`() {
        val allNodes = listOf(
            ActivityNode("parent", "def_other", null, 0, "Parent")
        )
        val node = ActivityNode("child", "def_main", "parent", 0, "Child")
        val error = validateUseCase(node, allNodes)
        assertEquals(ActivityNodeValidationError.CrossDefinitionParent, error)
    }

    @Test
    fun `completion propagates correctly`() = runBlocking {
        val defId = "def1"
        val nodes = listOf(
            ActivityNode("parent", defId, null, 0, "Parent"),
            ActivityNode("child1", defId, "parent", 0, "Child 1"),
            ActivityNode("child2", defId, "parent", 1, "Child 2")
        )
        fakeRepository.setNodes(nodes)

        // Case 1: Partial completion
        fakeRepository.setExecutions(mapOf("child1" to true))
        val tree1 = getTreeUseCase(defId, 0).first()
        assertEquals(NodeStatus.IN_PROGRESS, tree1[0].status)

        // Case 2: Full completion
        fakeRepository.setExecutions(mapOf("child1" to true, "child2" to true))
        val tree2 = getTreeUseCase(defId, 0).first()
        assertEquals(NodeStatus.COMPLETED, tree2[0].status)
        
        // Case 3: No completion
        fakeRepository.setExecutions(emptyMap())
        val tree3 = getTreeUseCase(defId, 0).first()
        assertEquals(NodeStatus.PENDING, tree3[0].status)
    }

    @Test
    fun `node movement between parents works correctly`() = runBlocking {
        val defId = "def1"
        val nodes = listOf(
            ActivityNode("parentA", defId, null, 0, "Parent A"),
            ActivityNode("nodeB", defId, "parentA", 0, "Node B"),
            ActivityNode("nodeC", defId, "parentA", 1, "Node C"),
            ActivityNode("nodeD", defId, null, 1, "Node D")
        )
        
        // Initial tree check
        fakeRepository.setNodes(nodes)
        val treeInit = getTreeUseCase(defId, 0).first()
        assertEquals(2, treeInit.size) // parentA, nodeD
        assertEquals(2, treeInit[0].children.size) // nodeB, nodeC

        // Move nodeD to be a child of parentA
        val updatedNodes = nodes.map { if (it.id == "nodeD") it.copy(parentId = "parentA", position = 2) else it }
        fakeRepository.setNodes(updatedNodes)
        
        val treeUpdated = getTreeUseCase(defId, 0).first()
        assertEquals(1, treeUpdated.size) // only parentA
        assertEquals(3, treeUpdated[0].children.size) // nodeB, nodeC, nodeD
        assertEquals("Node D", treeUpdated[0].children[2].node.title)
    }

    private open class FakeActivityRepository : ActivityRepository {
        private var nodes = emptyList<ActivityNode>()
        private var completedIds = setOf<String>()

        fun setNodes(nodes: List<ActivityNode>) { this.nodes = nodes }
        fun setExecutions(executions: Map<String, Boolean>) {
            completedIds = executions.filter { it.value }.keys
        }

        override fun getActivityDefinitions(): Flow<List<ActivityDefinition>> = flowOf(emptyList())
        override fun getAllNodes(): Flow<List<ActivityNode>> = flowOf(nodes)
        override suspend fun getActivityDefinitionById(id: String): ActivityDefinition? = null
        override suspend fun upsertActivityDefinition(activityDefinition: ActivityDefinition) {}
        override suspend fun deleteActivityDefinition(activityDefinition: ActivityDefinition) {}
        override fun getNodesForActivityDefinition(activityDefinitionId: String): Flow<List<ActivityNode>> = flowOf(nodes)
        override suspend fun getNodesListForActivityDefinition(activityDefinitionId: String): List<ActivityNode> = nodes
        override suspend fun getNodeById(id: String): ActivityNode? = nodes.find { it.id == id }
        override suspend fun upsertNode(node: ActivityNode) {}
        override suspend fun deleteNode(node: ActivityNode) {}
        override suspend fun reorderNodes(nodeIds: List<String>) {}
        override suspend fun moveNode(nodeId: String, newParentId: String?) {}
        override suspend fun registerExecution(nodeId: String, scheduledDate: Long, metadataJson: String, dailyInstanceId: String?) {}
        override fun getExecutionsForNode(nodeId: String): Flow<List<ActivityExecution>> = flowOf(emptyList())
        override fun getExecutionsForNodeOnDate(nodeId: String, scheduledDate: Long): Flow<List<ActivityExecution>> {
            return if (completedIds.contains(nodeId)) {
                flowOf(listOf(ActivityExecution("e1", nodeId, null, scheduledDate, 0L, "{}")))
            } else {
                flowOf(emptyList())
            }
        }
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
        override suspend fun upsertDailyInstance(instance: DailyInstance) {}
        override suspend fun getDailyInstanceByTarget(targetId: String, date: Long): DailyInstance? = null
    }
}
