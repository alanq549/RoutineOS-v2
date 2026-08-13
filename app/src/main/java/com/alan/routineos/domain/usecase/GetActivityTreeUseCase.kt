package com.alan.routineos.domain.usecase

import com.alan.routineos.domain.model.ActivityNode
import com.alan.routineos.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class ActivityNodeTree(
    val node: ActivityNode,
    val children: List<ActivityNodeTree> = emptyList(),
    val depth: Int = 0
)

class GetActivityTreeUseCase @Inject constructor(
    private val repository: ActivityRepository
) {
    operator fun invoke(activityDefinitionId: String): Flow<List<ActivityNodeTree>> {
        return repository.getNodesForActivityDefinition(activityDefinitionId).map { nodes ->
            buildTree(nodes, null, 0)
        }
    }

    private fun buildTree(
        allNodes: List<ActivityNode>,
        parentId: String?,
        currentDepth: Int
    ): List<ActivityNodeTree> {
        return allNodes
            .filter { it.parentId == parentId }
            .sortedBy { it.position }
            .map { node ->
                ActivityNodeTree(
                    node = node,
                    children = buildTree(allNodes, node.id, currentDepth + 1),
                    depth = currentDepth
                )
            }
    }
}
