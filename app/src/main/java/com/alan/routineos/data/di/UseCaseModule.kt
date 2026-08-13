package com.alan.routineos.data.di

import com.alan.routineos.domain.repository.ActivityRepository
import com.alan.routineos.domain.usecase.GetActivityTreeUseCase
import com.alan.routineos.domain.usecase.ValidateActivityNodeUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideGetActivityTreeUseCase(repository: ActivityRepository): GetActivityTreeUseCase {
        return GetActivityTreeUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideValidateActivityNodeUseCase(): ValidateActivityNodeUseCase {
        return ValidateActivityNodeUseCase()
    }
}
