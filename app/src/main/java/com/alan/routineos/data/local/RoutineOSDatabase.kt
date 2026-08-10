package com.alan.routineos.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.alan.routineos.data.local.dao.ActivityDefinitionDao
import com.alan.routineos.data.local.dao.ActivityExecutionDao
import com.alan.routineos.data.local.dao.ActivityNodeDao
import com.alan.routineos.data.local.entities.ActivityDefinitionEntity
import com.alan.routineos.data.local.entities.ActivityExecutionEntity
import com.alan.routineos.data.local.entities.ActivityNodeEntity

@Database(
    entities = [
        ActivityDefinitionEntity::class,
        ActivityNodeEntity::class,
        ActivityExecutionEntity::class,
    ],
    version = 3,
    exportSchema = true
)
abstract class RoutineOSDatabase : RoomDatabase() {
    abstract fun activityDefinitionDao(): ActivityDefinitionDao
    abstract fun activityNodeDao(): ActivityNodeDao
    abstract fun activityExecutionDao(): ActivityExecutionDao
}
