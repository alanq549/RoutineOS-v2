package com.alan.routineos.domain.usecase

import com.alan.routineos.domain.model.ActivityNode
import com.alan.routineos.domain.repository.ActivityRepository
import javax.inject.Inject

/**
 * Updates an existing node's title and description.
 * Preserves structural identity: parentId, position, and activityDefinitionId.
 */
class UpdateNodeUseCase @Inject constructor(
    private val repository: ActivityRepository
) {
    suspend operator fun invoke(
        nodeId: String,
        newTitle: String,
        newDescription: String? = null
    ) {
        val existingNode = repository.getNodeById(nodeId) 
            ?: throw IllegalArgumentException("Node with id $nodeId not found")

        // Only update title/description, keep everything else exactly as it was.
        val updatedNode = existingNode.copy(
            title = newTitle,
            description = newDescription ?: existingNode.description
        )
        
        repository.upsertNode(updatedNode)
    }
}
