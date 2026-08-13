package com.alan.routineos.domain.usecase

import com.alan.routineos.domain.model.ActivityNode

sealed class ActivityNodeValidationError {
    object SelfParent : ActivityNodeValidationError()
    object AncestorCycle : ActivityNodeValidationError()
    object CrossDefinitionParent : ActivityNodeValidationError()
    object NonexistentParent : ActivityNodeValidationError()
}

/**
 * Validates ActivityNode relationships before persistence.
 */
class ValidateActivityNodeUseCase {

    operator fun invoke(
        node: ActivityNode,
        allNodes: List<ActivityNode>
    ): ActivityNodeValidationError? {
        val parentId = node.parentId ?: return null

        // 1. Self-parent check
        if (parentId == node.id) return ActivityNodeValidationError.SelfParent

        // 2. Existence check
        val parent = allNodes.find { it.id == parentId }
            ?: return ActivityNodeValidationError.NonexistentParent

        // 3. Cross-definition check
        if (parent.activityDefinitionId != node.activityDefinitionId) {
            return ActivityNodeValidationError.CrossDefinitionParent
        }

        // 4. Ancestor cycle check
        var currentParent: ActivityNode? = parent
        val visited = mutableSetOf(node.id)
        
        while (currentParent != null) {
            if (visited.contains(currentParent.id)) {
                return ActivityNodeValidationError.AncestorCycle
            }
            visited.add(currentParent.id)
            currentParent = allNodes.find { it.id == currentParent?.parentId }
        }

        return null
    }
}
