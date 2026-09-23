---
id: EC-RE-014
title: Project Cleanup & Consistency
phase: 6
priority: Medium
effort: Small
owner: AI Agent
status: CLOSED
depends_on: [EC-RE-013]
branch: feature/ec-re-014-project-cleanup
audit: Approved
created: 2026-09-06
updated: 2026-09-06
---

# EC-RE-014: Project Cleanup & Consistency

## Objetivo
Eliminar residuos huerfanos confirmados por la auditoría y dejar el repositorio limpio, consistente y libre de artefactos obsoletos antes del siguiente análisis de dominio.

## Alcance (MUST)
- [ ] **Eliminar feature/system huérfana**:
  - Eliminar `feature/system/data/FakeSystemRepository.kt` y `feature/system/model/SystemModels.kt`.
  - Eliminar otros archivos huérfanos en `feature/system/` (como `SystemRoute.kt`, `SystemViewModel.kt`, `SystemScreen.kt`) si no tienen consumidores.
  - Conservar intactos: `SystemEntity`, `LifeSystem`, `life_systems` (base de datos y modelo organizativo).
- [ ] **Eliminar `ResolveTimelineForDateRange`**:
  - Eliminar `domain/usecase/ResolveTimelineForDateRange.kt` tras verificar ausencia de consumidores.
  - Mantener intactos: `TimelineResolutionEngine` y `ResolveTimelineUseCase`.
- [ ] **Limpiar `GetSystemsWithStatsUseCase`**:
  - Eliminar `provideGetSystemsWithStatsUseCase` de `UseCaseModule.kt`.
  - Eliminar `domain/usecase/GetSystemsWithStatsUseCase.kt` y su test asociado en `SystemStatsCalculationTest.kt` (o limpiarlo si queda obsoleto).
- [ ] **Limpiar residuos de navegación**:
  - En `AppRoutes.kt` y `MainActivity.kt`, eliminar rutas obsoletas (`AppRoutes.Planner`, `AppRoutes.Activities`) y lógica auxiliar obsoleta (`isPlanningRoute()`).
  - Confirmar navegación top-level final: `Today`, `Planning`, `Stats`, `Account`.
- [ ] **Actualizar documentación**:
  - Actualizar `09_MOCK_DATA_STATUS.md` reflejando estado real de fakes y Room DB v10 con sus migraciones (`MIGRATION_5_6` a `MIGRATION_9_10`).
  - Actualizar `07_CURRENT_CONTEXT.md` y `04_PROJECT_STATUS.md` alineando las referencias de EC-RE-014.

## Exclusiones (NO TOCAR)
- No modificar entidades de dominio ni esquemas de base de datos (`ActivityDefinition`, `ActivityNode`, `ScheduleRule`, `DailyInstance`, `ActivityExecution`, `BacklogItem`, `Deadline`, `Note`, `Task`, `Reminder`, etc.).
- No introducciones de nueva navegación ni refactors no solicitados.
