package com.alan.routineos.domain.model

data class ActivityNodeTree(
    val node: ActivityNode,
    val children: List<ActivityNodeTree> = emptyList(),
    val status: NodeStatus = NodeStatus.PENDING,
    val rules: List<ScheduleRule> = emptyList()
)
