package com.alan.routineos.data.di

import android.content.Context
import androidx.room.Room
import com.alan.routineos.data.local.RoutineDatabase
import com.alan.routineos.data.local.dao.RoutineDao
import com.alan.routineos.data.local.dao.TaskDao
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
    fun provideDatabase(@ApplicationContext context: Context): RoutineDatabase {
        return Room.databaseBuilder(
            context,
            RoutineDatabase::class.java,
            "routine_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideRoutineDao(db: RoutineDatabase): RoutineDao = db.routineDao()

    @Provides
    @Singleton
    fun provideTaskDao(db: RoutineDatabase): TaskDao = db.taskDao()
}
