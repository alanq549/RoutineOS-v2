package com.alan.routineos.domain.usecase

import com.alan.routineos.domain.model.ActivityNode
import com.alan.routineos.domain.repository.ActivityRepository
import javax.inject.Inject

/**
 * Performs a recursive soft-delete of a node and all its descendants.
 */
class DeleteBranchUseCase @Inject constructor(
    private val repository: ActivityRepository
) {
    suspend operator fun invoke(nodeId: String): List<String> {
        val targetNode = repository.getNodeById(nodeId)
            ?: throw IllegalArgumentException("Node $nodeId not found")

        val allNodes = repository.getNodesListForActivityDefinition(targetNode.activityDefinitionId)
        val nodesToDelete = mutableListOf<ActivityNode>()
        
        fun collectDescendants(parentId: String) {
            allNodes.filter { it.parentId == parentId && !it.isDeleted }.forEach { child ->
                nodesToDelete.add(child)
                collectDescendants(child.id)
            }
        }

        nodesToDelete.add(targetNode)
        collectDescendants(nodeId)

        val affectedIds = nodesToDelete.map { it.id }
        
        nodesToDelete.forEach { node ->
            repository.upsertNode(node.copy(isDeleted = true))
        }

        return affectedIds
    }
}
