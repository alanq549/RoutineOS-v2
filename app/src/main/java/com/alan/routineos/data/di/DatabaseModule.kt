package com.alan.routineos.data.di

import android.content.Context
import androidx.room.Room
import com.alan.routineos.data.local.RoutineOSDatabase
import com.alan.routineos.data.local.dao.ActivityDefinitionDao
import com.alan.routineos.data.local.dao.ActivityNodeDao
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
}
