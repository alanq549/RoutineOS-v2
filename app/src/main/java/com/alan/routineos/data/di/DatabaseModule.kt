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
import com.alan.routineos.data.local.dao.MetadataSchemaDao
import com.alan.routineos.data.local.dao.ScheduleExceptionDao
import com.alan.routineos.data.local.dao.ScheduleRuleDao
import com.alan.routineos.data.local.dao.SystemDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): RoutineOSDatabase {
        return Room.databaseBuilder(
            context,
            RoutineOSDatabase::class.java,
            "routine_db"
        )
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

    @Provides
    @Singleton
    fun provideMetadataSchemaDao(db: RoutineOSDatabase): MetadataSchemaDao = db.metadataSchemaDao()

    @Provides
    @Singleton
    fun provideSystemDao(db: RoutineOSDatabase): SystemDao = db.systemDao()
}
