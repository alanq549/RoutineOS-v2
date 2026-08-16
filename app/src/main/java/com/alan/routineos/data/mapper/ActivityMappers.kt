package com.alan.routineos.data.mapper

import com.alan.routineos.data.local.entities.ActivityDefinitionEntity
import com.alan.routineos.data.local.entities.ActivityExecutionEntity
import com.alan.routineos.data.local.entities.ActivityNodeEntity
import com.alan.routineos.data.local.entities.DailyInstanceEntity
import com.alan.routineos.data.local.entities.ScheduleExceptionEntity
import com.alan.routineos.data.local.entities.ScheduleRuleEntity
import com.alan.routineos.domain.model.ActivityDefinition
import com.alan.routineos.domain.model.ActivityExecution
import com.alan.routineos.domain.model.ActivityNode
import com.alan.routineos.domain.model.DailyInstance
import com.alan.routineos.domain.model.DailyInstanceStatus
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
    val target = when (targetType) {
        "DEFINITION" -> ScheduleTarget.Definition(targetId)
        "NODE" -> ScheduleTarget.Node(targetId)
        else -> throw IllegalStateException("Unknown targetType: $targetType")
    }
    return ScheduleRule(
        id = id,
        target = target,
        type = ScheduleRuleType.valueOf(type),
        daysOfWeek = if (daysOfWeek.isBlank()) emptySet() else daysOfWeek.split(",").map { it.toInt() }.toSet(),
        frequencyPerPeriod = frequencyPerPeriod,
        startTime = startTime,
        endTime = endTime,
        durationMinutes = durationMinutes,
        metadataJson = metadataJson
    )
}

fun ScheduleRule.toEntity(): ScheduleRuleEntity {
    val (targetId, targetType) = when (target) {
        is ScheduleTarget.Definition -> target.id to "DEFINITION"
        is ScheduleTarget.Node -> target.id to "NODE"
    }
    return ScheduleRuleEntity(
        id = id,
        targetId = targetId,
        targetType = targetType,
        type = type.name,
        daysOfWeek = daysOfWeek.sorted().joinToString(","),
        frequencyPerPeriod = frequencyPerPeriod,
        startTime = startTime,
        endTime = endTime,
        durationMinutes = durationMinutes,
        metadataJson = metadataJson
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

fun DailyInstanceEntity.toDomain(): DailyInstance {
    val target = when (targetType) {
        "DEFINITION" -> targetId?.let { ScheduleTarget.Definition(it) }
        "NODE" -> targetId?.let { ScheduleTarget.Node(it) }
        else -> null
    }
    return DailyInstance(
        id = id,
        target = target,
        scheduledDate = scheduledDate,
        titleSnapshot = titleSnapshot,
        descriptionSnapshot = descriptionSnapshot,
        plannedStartTime = plannedStartTime,
        plannedEndTime = plannedEndTime,
        plannedDurationMinutes = plannedDurationMinutes,
        status = DailyInstanceStatus.valueOf(status),
        isAdHoc = targetType == "AD_HOC"
    )
}

fun DailyInstance.toEntity(): DailyInstanceEntity {
    val targetType = when {
        isAdHoc -> "AD_HOC"
        target is ScheduleTarget.Definition -> "DEFINITION"
        target is ScheduleTarget.Node -> "NODE"
        else -> "AD_HOC"
    }
    return DailyInstanceEntity(
        id = id,
        targetId = when (target) {
            is ScheduleTarget.Definition -> target.id
            is ScheduleTarget.Node -> target.id
            else -> null
        },
        targetType = targetType,
        scheduledDate = scheduledDate,
        titleSnapshot = titleSnapshot,
        descriptionSnapshot = descriptionSnapshot,
        plannedStartTime = plannedStartTime,
        plannedEndTime = plannedEndTime,
        plannedDurationMinutes = plannedDurationMinutes,
        status = status.name,
        sourceRuleId = sourceRuleId
    )
}
