package com.alan.routineos.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.alan.routineos.data.local.dao.RoutineDao
import com.alan.routineos.data.local.dao.TaskDao
import com.alan.routineos.data.local.entities.RoutineEntity
import com.alan.routineos.data.local.entities.TaskEntity

@Database(
    entities = [RoutineEntity::class, TaskEntity::class],
    version = 1,
    exportSchema = true
)
abstract class RoutineDatabase : RoomDatabase() {
    abstract fun routineDao(): RoutineDao
    abstract fun taskDao(): TaskDao
}
