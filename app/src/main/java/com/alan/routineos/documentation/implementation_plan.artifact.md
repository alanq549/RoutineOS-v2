# Implementation Plan - EC-RE-010: Historical Analysis & Trends

This Engineering Card introduces the historical analysis layer to RoutineOS, transforming execution data into actionable metrics and trends without judging the user's behavior.

## User Review Required

> [!IMPORTANT]
> **Historical Data Immutability**: We will strictly follow the principle that historical data is immutable in meaning. Future changes to schemas or activity structures will not re-interpret historical metadata.

> [!WARNING]
> **Reset Logic Change**: The `RESET` action will no longer delete `ActivityExecution` records. Instead, it will update their state, preserving the history of what was once completed but then reverted.

## Proposed Changes

### Domain Layer

#### [MODIFY] [ActivityExecution.kt](file:///C:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/domain/model/ActivityExecution.kt)
- Add `executionState` field (COMPLETED, REVERTED/CANCELLED).
- Add `startedAt` field if possible (or keep it simple for now as per prompt's point 2).

#### [NEW] [HistoricalAnalysisModels.kt](file:///C:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/domain/model/HistoricalAnalysisModels.kt)
- Define `AnalysisPeriod` (DAY, WEEK, MONTH, CUSTOM).
- Define `HistoricalMetric` and `Trend` structures.
- Define `AnalysisResult` for Completion, Frequency, and Duration.

#### [NEW] [GetHistoricalAnalysisUseCase.kt](file:///C:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/domain/usecase/GetHistoricalAnalysisUseCase.kt)
- Implement logic to aggregate executions by definition, node, system, and period.
- Implement completion rate calculation (`completed / executableOccurrences`).
- Implement duration analysis (min, max, avg) using `completedAt - startedAt`.
- Implement frequency tracking.
- Implement trend comparison (Current vs Previous period).
- Implement metadata aggregation for `NUMBER`, `BOOLEAN`, and `SELECT` types.

#### [MODIFY] [RegisterDailyActionUseCase.kt](file:///C:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/domain/usecase/RegisterDailyActionUseCase.kt)
- Update `handleReset` to update the `ActivityExecution` state instead of deleting it.
- Ensure `handleComplete` creates an `ActivityExecution` with `COMPLETED` state.

### Data Layer

#### [MODIFY] [ActivityRepository.kt](file:///C:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/domain/repository/ActivityRepository.kt) & Implementation
- Update `registerExecution` to support `executionState`.
- Add/Update methods to query executions within a date range efficiently.
- Add method to update execution status (for RESET).

---

## Verification Plan

### Automated Tests
- `GetHistoricalAnalysisUseCaseTest`:
    - Verify completion rate calculation with hierarchical aggregation.
    - Verify trend detection (UP, DOWN, STABLE).
    - Verify metadata aggregation for different field types.
    - Verify that schema changes don't affect old metadata analysis (mocking schema versions).
    - Verify empty period handling.
- `RegisterDailyActionUseCaseTest`:
    - Verify `RESET` preserves `ActivityExecution` but changes its state.

### Manual Verification
- Deploy and verify that completing and then resetting an activity doesn't wipe the execution record in the database (via logs/inspection).
- Verify that the stats (once UI is implemented in phase 2) correctly reflect the data.
