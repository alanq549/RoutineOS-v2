package com.alan.routineos.data.di

import com.alan.routineos.data.repository.OfflineRoutineRepository
import com.alan.routineos.domain.repository.RoutineRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindRoutineRepository(
        offlineRoutineRepository: OfflineRoutineRepository
    ): RoutineRepository
}
