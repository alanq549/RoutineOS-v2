package com.alan.routineos.data.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.alan.routineos.data.local.RoutineOSDatabase
import com.alan.routineos.data.local.dao.ActivityDefinitionDao
import com.alan.routineos.data.local.dao.ActivityExecutionDao
import com.alan.routineos.data.local.dao.ActivityNodeDao
import com.alan.routineos.data.local.dao.DailyInstanceDao
import com.alan.routineos.data.local.dao.ScheduleExceptionDao
import com.alan.routineos.data.local.dao.ScheduleRuleDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // 1. Create daily_instances table
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `daily_instances` (
                    `id` TEXT NOT NULL, 
                    `targetId` TEXT, 
                    `targetType` TEXT NOT NULL, 
                    `scheduledDate` INTEGER NOT NULL, 
                    `titleSnapshot` TEXT NOT NULL, 
                    `descriptionSnapshot` TEXT NOT NULL, 
                    `plannedStartTime` INTEGER, 
                    `status` TEXT NOT NULL, 
                    `sourceRuleId` TEXT, 
                    PRIMARY KEY(`id`)
                )
            """)
            
            // 2. Create unique index
            db.execSQL("""
                CREATE UNIQUE INDEX IF NOT EXISTS `index_daily_instances_targetType_targetId_scheduledDate` 
                ON `daily_instances` (`targetType`, `targetId`, `scheduledDate`)
            """)

            // 3. Add dailyInstanceId column to activity_executions
            db.execSQL("ALTER TABLE `activity_executions` ADD COLUMN `dailyInstanceId` TEXT DEFAULT NULL")
            
            // 4. Add index for FK
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_activity_executions_dailyInstanceId` ON `activity_executions` (`dailyInstanceId`)")
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): RoutineOSDatabase {
        return Room.databaseBuilder(
            context,
            RoutineOSDatabase::class.java,
            "routine_db"
        )
            .addMigrations(MIGRATION_6_7)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideActivityDefinitionDao(db: RoutineOSDatabase): ActivityDefinitionDao = db.activityDefinitionDao()

    @Provides
    @Singleton
    fun provideActivityNodeDao(db: RoutineOSDatabase): ActivityNodeDao = db.activityNodeDao()

    @Provides
    @Singleton
    fun provideActivityExecutionDao(db: RoutineOSDatabase): ActivityExecutionDao = db.activityExecutionDao()

    @Provides
    @Singleton
    fun provideScheduleRuleDao(db: RoutineOSDatabase): ScheduleRuleDao = db.scheduleRuleDao()

    @Provides
    @Singleton
    fun provideScheduleExceptionDao(db: RoutineOSDatabase): ScheduleExceptionDao = db.scheduleExceptionDao()

    @Provides
    @Singleton
    fun provideDailyInstanceDao(db: RoutineOSDatabase): DailyInstanceDao = db.dailyInstanceDao()
}
