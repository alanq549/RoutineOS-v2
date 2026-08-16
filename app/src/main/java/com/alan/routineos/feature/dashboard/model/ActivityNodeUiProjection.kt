package com.alan.routineos.feature.dashboard.model

import com.alan.routineos.domain.model.ActivityNodeTree
import com.alan.routineos.domain.model.NodeStatus
import com.alan.routineos.domain.model.ScheduleRule

data class ActivityNodeUiProjection(
    val id: String,
    val title: String,
    val status: NodeStatus,
    val depth: Int,
    val isExpanded: Boolean,
    val isLeaf: Boolean,
    val rules: List<ScheduleRule> = emptyList(),
    val lastCompletionTimestamp: Long? = null
)

fun List<ActivityNodeTree>.toUiProjection(
    depth: Int = 0,
    expandedNodes: Set<String> = emptySet()
): List<ActivityNodeUiProjection> {
    val result = mutableListOf<ActivityNodeUiProjection>()
    
    this.forEach { treeNode ->
        val isExpanded = expandedNodes.contains(treeNode.node.id) || depth == 0 
        
        result.add(
            ActivityNodeUiProjection(
                id = treeNode.node.id,
                title = treeNode.node.title,
                status = treeNode.status,
                depth = depth,
                isExpanded = isExpanded,
                isLeaf = treeNode.children.isEmpty(),
                rules = treeNode.rules
            )
        )
        
        if (isExpanded) {
            result.addAll(treeNode.children.toUiProjection(depth + 1, expandedNodes))
        }
    }
    
    return result
}
