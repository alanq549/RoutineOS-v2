package com.alan.routineos.domain.usecase

import com.alan.routineos.domain.model.ActivityNode
import com.alan.routineos.domain.model.ActivityNodeTree
import com.alan.routineos.domain.model.NodeStatus
import com.alan.routineos.domain.repository.ActivityRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class GetActivityTreeUseCase @Inject constructor(
    private val repository: ActivityRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(
        activityDefinitionId: String,
        referenceDate: Long
    ): Flow<List<ActivityNodeTree>> {
        val nodesFlow = repository.getNodesForActivityDefinition(activityDefinitionId)

        return nodesFlow.flatMapLatest { nodes ->
            if (nodes.isEmpty()) return@flatMapLatest flowOf(emptyList())

            val executionFlows = nodes.map { node ->
                repository.getExecutionsForNodeOnDate(node.id, referenceDate).map { executions ->
                    node.id to executions.isNotEmpty()
                }
            }

            combine(executionFlows) { executionPairs ->
                val completedIds = executionPairs.filter { it.second }.map { it.first }.toSet()
                buildTree(nodes, null, completedIds)
            }
        }
    }

    private fun buildTree(
        allNodes: List<ActivityNode>,
        parentId: String?,
        completedIds: Set<String>
    ): List<ActivityNodeTree> {
        return allNodes
            .filter { it.parentId == parentId && !it.isDeleted }
            .sortedBy { it.position }
            .map { node ->
                val children = buildTree(allNodes, node.id, completedIds)
                val status = calculateStatus(node, children, completedIds)
                ActivityNodeTree(
                    node = node,
                    children = children,
                    status = status
                )
            }
    }

    private fun calculateStatus(
        node: ActivityNode,
        children: List<ActivityNodeTree>,
        completedIds: Set<String>
    ): NodeStatus {
        return if (children.isEmpty()) {
            // Leaf node: based on execution
            if (completedIds.contains(node.id)) NodeStatus.COMPLETED else NodeStatus.PENDING
        } else {
            // Container node: derived from children
            val allCompleted = children.all { it.status == NodeStatus.COMPLETED }
            val noneCompleted = children.all { it.status == NodeStatus.PENDING }
            
            when {
                allCompleted -> NodeStatus.COMPLETED
                noneCompleted -> NodeStatus.PENDING
                else -> NodeStatus.IN_PROGRESS
            }
        }
    }
}
