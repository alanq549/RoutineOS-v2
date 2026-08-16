package com.alan.routineos.data.di

import com.alan.routineos.domain.repository.ActivityRepository
import com.alan.routineos.domain.usecase.AddChildUseCase
import com.alan.routineos.domain.usecase.DeleteBranchUseCase
import com.alan.routineos.domain.usecase.GetActivityTreeUseCase
import com.alan.routineos.domain.usecase.MaterializeInstanceUseCase
import com.alan.routineos.domain.usecase.ResolveTimelineUseCase
import com.alan.routineos.domain.usecase.RestoreBranchUseCase
import com.alan.routineos.domain.usecase.UpdateNodeUseCase
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

    @Provides
    @Singleton
    fun provideUpdateNodeUseCase(repository: ActivityRepository): UpdateNodeUseCase {
        return UpdateNodeUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideAddChildUseCase(
        repository: ActivityRepository,
        validateActivityNodeUseCase: ValidateActivityNodeUseCase
    ): AddChildUseCase {
        return AddChildUseCase(repository, validateActivityNodeUseCase)
    }

    @Provides
    @Singleton
    fun provideDeleteBranchUseCase(repository: ActivityRepository): DeleteBranchUseCase {
        return DeleteBranchUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideRestoreBranchUseCase(repository: ActivityRepository): RestoreBranchUseCase {
        return RestoreBranchUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideResolveTimelineUseCase(repository: ActivityRepository): ResolveTimelineUseCase {
        return ResolveTimelineUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideMaterializeInstanceUseCase(repository: ActivityRepository): MaterializeInstanceUseCase {
        return MaterializeInstanceUseCase(repository)
    }
}
