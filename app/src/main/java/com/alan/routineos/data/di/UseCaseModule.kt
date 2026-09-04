package com.alan.routineos.data.di

import com.alan.routineos.domain.repository.ActivityRepository
import com.alan.routineos.domain.usecase.*
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
    fun provideTimelineResolutionEngine(): TimelineResolutionEngine {
        return TimelineResolutionEngine()
    }

    @Provides
    @Singleton
    fun provideResolveTimelineUseCase(
        repository: ActivityRepository,
        resolutionEngine: TimelineResolutionEngine,
        conflictDetector: ConflictDetectorUseCase,
        suggestionEngine: SuggestionEngine
    ): ResolveTimelineUseCase {
        return ResolveTimelineUseCase(repository, resolutionEngine, conflictDetector, suggestionEngine)
    }

    @Provides
    @Singleton
    fun provideSuggestionEngine(conflictDetector: ConflictDetectorUseCase): SuggestionEngine {
        return SuggestionEngine(conflictDetector)
    }

    @Provides
    @Singleton
    fun provideMaterializeInstanceUseCase(repository: ActivityRepository): MaterializeInstanceUseCase {
        return MaterializeInstanceUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideRegisterDailyActionUseCase(
        repository: ActivityRepository,
        materializeInstanceUseCase: MaterializeInstanceUseCase
    ): RegisterDailyActionUseCase {
        return RegisterDailyActionUseCase(repository, materializeInstanceUseCase)
    }

    @Provides
    @Singleton
    fun provideConflictDetectorUseCase(): ConflictDetectorUseCase {
        return ConflictDetectorUseCase()
    }

    @Provides
    @Singleton
    fun provideGetHierarchicalTimelineUseCase(
        repository: ActivityRepository,
        resolveTimelineUseCase: ResolveTimelineUseCase
    ): GetHierarchicalTimelineUseCase {
        return GetHierarchicalTimelineUseCase(repository, resolveTimelineUseCase)
    }

    @Provides
    @Singleton
    fun provideValidateScheduleRuleUseCase(): ValidateScheduleRuleUseCase {
        return ValidateScheduleRuleUseCase()
    }

    @Provides
    @Singleton
    fun provideValidateMetadataSchemaUseCase(): ValidateMetadataSchemaUseCase {
        return ValidateMetadataSchemaUseCase()
    }

    @Provides
    @Singleton
    fun provideGetSystemsWithStatsUseCase(repository: ActivityRepository): GetSystemsWithStatsUseCase {
        return GetSystemsWithStatsUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideAssignActivityToSystemUseCase(repository: ActivityRepository): AssignActivityToSystemUseCase {
        return AssignActivityToSystemUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideUnassignActivityFromSystemUseCase(repository: ActivityRepository): UnassignActivityFromSystemUseCase {
        return UnassignActivityFromSystemUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideHistoricalOccurrenceResolver(
        repository: ActivityRepository,
        resolutionEngine: TimelineResolutionEngine
    ): HistoricalOccurrenceResolver {
        return HistoricalOccurrenceResolver(repository, resolutionEngine)
    }

    @Provides
    @Singleton
    fun provideGetHistoryAnalyticsUseCase(
        repository: ActivityRepository,
        occurrenceResolver: HistoricalOccurrenceResolver
    ): GetHistoryAnalyticsUseCase {
        return GetHistoryAnalyticsUseCase(repository, occurrenceResolver)
    }
}
