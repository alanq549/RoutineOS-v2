package com.alan.routineos.domain.repository

import com.alan.routineos.domain.model.Routine
import com.alan.routineos.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface RoutineRepository {
    fun getRoutines(): Flow<List<Routine>>
    suspend fun getRoutineById(id: String): Routine?
    suspend fun upsertRoutine(routine: Routine)
    suspend fun deleteRoutine(routine: Routine)
    fun getTasksForRoutine(routineId: String): Flow<List<Task>>
    suspend fun upsertTask(task: Task)
    suspend fun deleteTask(task: Task)
}
