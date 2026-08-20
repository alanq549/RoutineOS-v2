package com.alan.routineos.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.alan.routineos.data.local.dao.ActivityDefinitionDao
import com.alan.routineos.data.local.dao.ActivityExecutionDao
import com.alan.routineos.data.local.dao.ActivityNodeDao
import com.alan.routineos.data.local.dao.DailyInstanceDao
import com.alan.routineos.data.local.dao.MetadataSchemaDao
import com.alan.routineos.data.local.dao.ScheduleExceptionDao
import com.alan.routineos.data.local.dao.ScheduleRuleDao
import com.alan.routineos.data.local.dao.SystemDao
import com.alan.routineos.data.local.entities.*

@Database(
    entities = [
        ActivityDefinitionEntity::class,
        ActivityNodeEntity::class,
        ActivityExecutionEntity::class,
        ScheduleRuleEntity::class,
        ScheduleExceptionEntity::class,
        DailyInstanceEntity::class,
        MetadataSchemaEntity::class,
        SystemEntity::class,
    ],
    version = 4,
    exportSchema = true
)
abstract class RoutineOSDatabase : RoomDatabase() {
    abstract fun activityDefinitionDao(): ActivityDefinitionDao
    abstract fun activityNodeDao(): ActivityNodeDao
    abstract fun activityExecutionDao(): ActivityExecutionDao
    abstract fun scheduleRuleDao(): ScheduleRuleDao
    abstract fun scheduleExceptionDao(): ScheduleExceptionDao
    abstract fun dailyInstanceDao(): DailyInstanceDao
    abstract fun metadataSchemaDao(): MetadataSchemaDao
    abstract fun systemDao(): SystemDao
}
