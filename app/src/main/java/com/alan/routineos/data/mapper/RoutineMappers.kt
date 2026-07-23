package com.alan.routineos.data.mapper

import com.alan.routineos.data.local.entities.RoutineEntity
import com.alan.routineos.data.local.entities.TaskEntity
import com.alan.routineos.domain.model.Routine
import com.alan.routineos.domain.model.Task

fun RoutineEntity.toDomain(): Routine {
    return Routine(
        id = id,
        title = title,
        description = description
    )
}

fun Routine.toEntity(): RoutineEntity {
    return RoutineEntity(
        id = id,
        title = title,
        description = description
    )
}

fun TaskEntity.toDomain(): Task {
    return Task(
        id = id,
        routineId = routineId,
        title = title
    )
}

fun Task.toEntity(): TaskEntity {
    return TaskEntity(
        id = id,
        routineId = routineId,
        title = title
    )
}
