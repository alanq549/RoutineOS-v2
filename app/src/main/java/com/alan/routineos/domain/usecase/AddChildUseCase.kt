package com.alan.routineos.domain.usecase

import com.alan.routineos.domain.model.ActivityNode
import com.alan.routineos.domain.repository.ActivityRepository
import java.util.UUID
import javax.inject.Inject

/**
 * Adds a new child node to a parent node.
 * Validates existence and same activity definition.
 */
class AddChildUseCase @Inject constructor(
    private val repository: ActivityRepository,
    private val validateActivityNodeUseCase: ValidateActivityNodeUseCase
) {
    suspend operator fun invoke(
        parentId: String,
        title: String,
        description: String = ""
    ): ActivityNode {
        val parentNode = repository.getNodeById(parentId)
            ?: throw IllegalArgumentException("Parent node $parentId not found")

        // Fetching list for validation and position calculation
        val allNodesList = repository.getNodesListForActivityDefinition(parentNode.activityDefinitionId)
        
        val newId = UUID.randomUUID().toString()
        val nextPosition = allNodesList.filter { it.parentId == parentId }.size

        val newNode = ActivityNode(
            id = newId,
            activityDefinitionId = parentNode.activityDefinitionId,
            parentId = parentId,
            position = nextPosition,
            title = title,
            description = description,
            isDeleted = false
        )

        val validationError = validateActivityNodeUseCase(newNode, allNodesList)
        if (validationError != null) {
            throw IllegalArgumentException("Node validation failed: $validationError")
        }

        repository.upsertNode(newNode)
        return newNode
    }
}
