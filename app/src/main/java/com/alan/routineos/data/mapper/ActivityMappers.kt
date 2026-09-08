package com.alan.routineos.data.mapper

import com.alan.routineos.data.local.entities.*
import com.alan.routineos.domain.model.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

fun ActivityDefinitionEntity.toDomain(): ActivityDefinition {
    return ActivityDefinition(
        id = id,
        title = title,
        description = description,
        systemId = systemId,
        isDeleted = isDeleted
    )
}

fun ActivityDefinition.toEntity(): ActivityDefinitionEntity {
    return ActivityDefinitionEntity(
        id = id,
        title = title,
        description = description,
        systemId = systemId,
        isDeleted = isDeleted
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
        dailyInstanceId = dailyInstanceId,
        scheduledDate = scheduledDate,
        completedAt = completedAt,
        metadataJson = metadataJson,
        activityIdSnapshot = activityIdSnapshot,
        systemIdSnapshot = systemIdSnapshot,
        titleSnapshot = titleSnapshot
    )
}

fun ActivityExecution.toEntity(): ActivityExecutionEntity {
    return ActivityExecutionEntity(
        id = id,
        nodeId = nodeId,
        dailyInstanceId = dailyInstanceId,
        scheduledDate = scheduledDate,
        completedAt = completedAt,
        metadataJson = metadataJson,
        activityIdSnapshot = activityIdSnapshot,
        systemIdSnapshot = systemIdSnapshot,
        titleSnapshot = titleSnapshot
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
        mobility = TemporalMobility.valueOf(mobility),
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
        mobility = mobility.name,
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
        mobility = TemporalMobility.valueOf(mobility),
        sourceRuleId = sourceRuleId,
        isAdHoc = targetType == "AD_HOC",
        parentInstanceId = parentInstanceId,
        backlogId = backlogId
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
        mobility = mobility.name,
        sourceRuleId = sourceRuleId,
        parentInstanceId = parentInstanceId,
        backlogId = backlogId
    )
}

fun MetadataSchemaEntity.toDomain(): MetadataSchema {
    val target = when (targetType) {
        "DEFINITION" -> ScheduleTarget.Definition(targetId)
        "NODE" -> ScheduleTarget.Node(targetId)
        else -> throw IllegalStateException("Unknown targetType: $targetType")
    }
    return MetadataSchema(
        id = id,
        target = target,
        fields = json.decodeFromString(fieldsJson),
        schemaVersion = schemaVersion
    )
}

fun MetadataSchema.toEntity(): MetadataSchemaEntity {
    val (targetId, targetType) = when (target) {
        is ScheduleTarget.Definition -> target.id to "DEFINITION"
        is ScheduleTarget.Node -> target.id to "NODE"
    }
    return MetadataSchemaEntity(
        id = id,
        targetId = targetId,
        targetType = targetType,
        fieldsJson = json.encodeToString(fields),
        schemaVersion = schemaVersion
    )
}

fun SystemEntity.toDomain(): LifeSystem {
    return LifeSystem(
        id = id,
        title = title,
        description = description,
        iconKey = iconKey,
        colorHex = colorHex,
        isArchived = isArchived
    )
}

fun LifeSystem.toEntity(): SystemEntity {
    return SystemEntity(
        id = id,
        title = title,
        description = description,
        iconKey = iconKey,
        colorHex = colorHex,
        isArchived = isArchived
    )
}

fun BacklogItemEntity.toDomain(): BacklogItem {
    return BacklogItem(
        id = id,
        definitionId = definitionId,
        title = title,
        status = BacklogItemStatus.valueOf(status)
    )
}

fun BacklogItem.toEntity(): BacklogItemEntity {
    return BacklogItemEntity(
        id = id,
        definitionId = definitionId,
        title = title,
        status = status.name
    )
}

fun DeadlineEntity.toDomain(): Deadline {
    return Deadline(
        id = id,
        dueAt = dueAt,
        definitionId = definitionId,
        backlogId = backlogId,
        instanceId = instanceId
    )
}

fun Deadline.toEntity(): DeadlineEntity {
    return DeadlineEntity(
        id = id,
        dueAt = dueAt,
        definitionId = definitionId,
        backlogId = backlogId,
        instanceId = instanceId
    )
}

fun NoteEntity.toDomain(): Note {
    return Note(
        id = id,
        content = content,
        definitionId = definitionId,
        backlogId = backlogId,
        instanceId = instanceId
    )
}

fun Note.toEntity(): NoteEntity {
    return NoteEntity(
        id = id,
        content = content,
        definitionId = definitionId,
        backlogId = backlogId,
        instanceId = instanceId
    )
}
