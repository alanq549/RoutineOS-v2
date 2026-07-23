package com.alan.routineos.data.repository

import com.alan.routineos.data.local.dao.RoutineDao
import com.alan.routineos.data.local.dao.TaskDao
import com.alan.routineos.data.mapper.toDomain
import com.alan.routineos.data.mapper.toEntity
import com.alan.routineos.domain.model.Routine
import com.alan.routineos.domain.model.Task
import com.alan.routineos.domain.repository.RoutineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OfflineRoutineRepository @Inject constructor(
    private val routineDao: RoutineDao,
    private val taskDao: TaskDao
) : RoutineRepository {

    override fun getRoutines(): Flow<List<Routine>> {
        return routineDao.getAllRoutines().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getRoutineById(id: String): Routine? {
        return routineDao.getRoutineById(id)?.toDomain()
    }

    override suspend fun upsertRoutine(routine: Routine) {
        routineDao.insertRoutine(routine.toEntity())
    }

    override suspend fun deleteRoutine(routine: Routine) {
        routineDao.deleteRoutine(routine.toEntity())
    }

    override fun getTasksForRoutine(routineId: String): Flow<List<Task>> {
        return taskDao.getTasksForRoutine(routineId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun upsertTask(task: Task) {
        taskDao.insertTask(task.toEntity())
    }

    override suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task.toEntity())
    }
}
