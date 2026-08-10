package com.alan.routineos.data.mapper

import com.alan.routineos.data.local.entities.ActivityDefinitionEntity
import com.alan.routineos.data.local.entities.ActivityExecutionEntity
import com.alan.routineos.data.local.entities.ActivityNodeEntity
import com.alan.routineos.domain.model.ActivityDefinition
import com.alan.routineos.domain.model.ActivityExecution
import com.alan.routineos.domain.model.ActivityNode

fun ActivityDefinitionEntity.toDomain(): ActivityDefinition {
    return ActivityDefinition(
        id = id,
        title = title,
        description = description
    )
}

fun ActivityDefinition.toEntity(): ActivityDefinitionEntity {
    return ActivityDefinitionEntity(
        id = id,
        title = title,
        description = description
    )
}

fun ActivityNodeEntity.toDomain(): ActivityNode {
    return ActivityNode(
        id = id,
        activityDefinitionId = activityDefinitionId,
        title = title
    )
}

fun ActivityNode.toEntity(): ActivityNodeEntity {
    return ActivityNodeEntity(
        id = id,
        activityDefinitionId = activityDefinitionId,
        title = title
    )
}

fun ActivityExecutionEntity.toDomain(): ActivityExecution {
    return ActivityExecution(
        id = id,
        nodeId = nodeId,
        completedAt = completedAt,
        metadataJson = metadataJson
    )
}

fun ActivityExecution.toEntity(): ActivityExecutionEntity {
    return ActivityExecutionEntity(
        id = id,
        nodeId = nodeId,
        completedAt = completedAt,
        metadataJson = metadataJson
    )
}
