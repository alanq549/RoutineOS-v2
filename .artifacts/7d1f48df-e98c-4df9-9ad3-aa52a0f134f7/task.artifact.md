# Tasks - Layout Audit: Recover Vertical Screen Space

- `[x]` **1. Core Layout Refactoring**
    - `[x]` Simplify `RoutineScaffold.kt` (remove internal padding Box)
    - `[x]` Audit `MainActivity.kt` root structure
- `[x]` **2. Feature Space Recovery**
    - `[x]` Refactor `TodayScreen.kt` (remove 100dp spacer, fix double padding)
    - `[x]` Refactor `PlanningWorkspace.kt` (tighten header, fix nested NavHost padding)
    - `[x]` Refactor `PlanningScreen.kt`, `RoutineLibraryScreen.kt`, `SystemScreen.kt` (remove bottom gaps)
    - `[x]` Refactor `StatsScreen.kt` (remove 120dp spacer, fix double padding)
    - `[x]` Refactor `AccountScreen.kt` (remove 120dp spacer, fix double padding)
- `[x]` **3. Verification & Cleanup**
    - `[x]` Run `./gradlew app:assembleDebug`
    - `[x]` Visual audit of all screens
    - `[x]` Document removed paddings
