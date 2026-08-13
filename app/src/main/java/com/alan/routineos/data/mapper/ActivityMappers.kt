package com.alan.routineos.data.mapper

import com.alan.routineos.data.local.entities.ActivityDefinitionEntity
import com.alan.routineos.data.local.entities.ActivityExecutionEntity
import com.alan.routineos.data.local.entities.ActivityNodeEntity
import com.alan.routineos.data.local.entities.ScheduleExceptionEntity
import com.alan.routineos.data.local.entities.ScheduleRuleEntity
import com.alan.routineos.domain.model.ActivityDefinition
import com.alan.routineos.domain.model.ActivityExecution
import com.alan.routineos.domain.model.ActivityNode
import com.alan.routineos.domain.model.ScheduleException
import com.alan.routineos.domain.model.ScheduleExceptionType
import com.alan.routineos.domain.model.ScheduleRule
import com.alan.routineos.domain.model.ScheduleRuleType
import com.alan.routineos.domain.model.ScheduleTarget

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
        parentId = parentId,
        position = position,
        title = title,
        description = description,
        isDeleted = isDeleted
    )
}

fun ActivityNode.toEntity(): ActivityNodeEntity {
    return ActivityNodeEntity(
        id = id,
        activityDefinitionId = activityDefinitionId,
        parentId = parentId,
        position = position,
        title = title,
        description = description,
        isDeleted = isDeleted
    )
}

fun ActivityExecutionEntity.toDomain(): ActivityExecution {
    return ActivityExecution(
        id = id,
        nodeId = nodeId,
        scheduledDate = scheduledDate,
        completedAt = completedAt,
        metadataJson = metadataJson
    )
}

fun ActivityExecution.toEntity(): ActivityExecutionEntity {
    return ActivityExecutionEntity(
        id = id,
        nodeId = nodeId,
        scheduledDate = scheduledDate,
        completedAt = completedAt,
        metadataJson = metadataJson
    )
}

fun ScheduleRuleEntity.toDomain(): ScheduleRule {
    val target = when {
        activityDefinitionId != null -> ScheduleTarget.Definition(activityDefinitionId)
        activityNodeId != null -> ScheduleTarget.Node(activityNodeId)
        else -> throw IllegalStateException("ScheduleRuleEntity must have a target")
    }
    return ScheduleRule(
        id = id,
        target = target,
        type = ScheduleRuleType.valueOf(type),
        daysOfWeek = if (daysOfWeek.isBlank()) emptySet() else daysOfWeek.split(",").map { it.toInt() }.toSet(),
        frequencyPerPeriod = frequencyPerPeriod
    )
}

fun ScheduleRule.toEntity(): ScheduleRuleEntity {
    return ScheduleRuleEntity(
        id = id,
        activityDefinitionId = (target as? ScheduleTarget.Definition)?.id,
        activityNodeId = (target as? ScheduleTarget.Node)?.id,
        type = type.name,
        daysOfWeek = daysOfWeek.joinToString(","),
        frequencyPerPeriod = frequencyPerPeriod
    )
}

fun ScheduleExceptionEntity.toDomain(): ScheduleException {
    return ScheduleException(
        id = id,
        scheduleRuleId = scheduleRuleId,
        originalDate = originalDate,
        type = ScheduleExceptionType.valueOf(type),
        newDate = newDate
    )
}

fun ScheduleException.toEntity(): ScheduleExceptionEntity {
    return ScheduleExceptionEntity(
        id = id,
        scheduleRuleId = scheduleRuleId,
        originalDate = originalDate,
        type = type.name,
        newDate = newDate
    )
}
