# Implementation Plan - Layout Audit: Recover Vertical Screen Space

This plan focuses on auditing and refining the layout hierarchy to eliminate redundant padding, duplicated WindowInsets handling, and excessive spacers, maximizing usable vertical space across all screens.

## User Review Required

> [!IMPORTANT]
> I will refactor `RoutineScaffold` to stop applying `paddingValues` automatically to an internal `Box`. Instead, all screens will be responsible for using the provided `paddingValues`. This prevents "double-padding" bugs and gives more control over how content interacts with system bars.

## Proposed Changes

### 1. Core Design System

#### [MODIFY] [RoutineScaffold.kt](file:///C:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/core/designsystem/component/RoutineScaffold.kt)
- Remove the internal `Box` that applies `padding(paddingValues)`.
- Pass `paddingValues` directly to the `content` slot.
- Ensure `Scaffold` uses `contentWindowInsets = WindowInsets(0, 0, 0, 0)` or handles them explicitly to avoid unintended stacking.

---

### 2. Feature Refinements (Top & Bottom Spacing)

#### [MODIFY] [TodayScreen.kt](file:///C:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/feature/today/TodayScreen.kt)
- Use `paddingValues` from `RoutineScaffold` correctly.
- Remove redundant `Spacer` at the bottom (100dp).
- Ensure `TodayHeader` doesn't have excessive vertical padding.

#### [MODIFY] [PlanningWorkspace.kt](file:///C:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/feature/planning/PlanningWorkspace.kt)
- Remove double application of `paddingValues`.
- Align "Planificar" title more tightly to the top (status bar).
- Reduce gap between segmented selector and content.

#### [MODIFY] [PlanningScreen.kt](file:///C:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/feature/planning/PlanningScreen.kt), [RoutineLibraryScreen.kt](file:///C:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/feature/routines/RoutineLibraryScreen.kt), [SystemScreen.kt](file:///C:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/feature/system/SystemScreen.kt)
- Remove redundant bottom padding/spacers.
- Use consistent `16.dp` bottom margin.

#### [MODIFY] [StatsScreen.kt](file:///C:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/feature/stats/StatsScreen.kt)
- Fix double padding.
- Remove 120dp bottom spacer.
- Tighten header-to-period-selector spacing.

#### [MODIFY] [AccountScreen.kt](file:///C:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/feature/account/AccountScreen.kt)
- Fix double padding.
- Remove 120dp bottom spacer.
- Align "Cuenta" title correctly.

---

### 3. Navigation & Main Activity

#### [MODIFY] [MainActivity.kt](file:///C:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/MainActivity.kt)
- Ensure the root `RoutineTheme` and any top-level layout components don't introduce global padding that interferes with screen-level insets.

## Verification Plan

### Automated Tests
- `./gradlew app:assembleDebug` to verify no compilation errors.
- `./gradlew app:lintDebug` to check for UI performance warnings.

### Manual Verification
- **Side-by-side Visual Audit:** Compare screenshots to confirm titles start higher and more cards are visible in the timeline.
- **Overlap Check:** Verify that content does not get hidden under the Bottom Bar or status bar.
- **Scroll Check:** Ensure scrollable areas reach the very edge of the bottom bar without empty "dead zones".
