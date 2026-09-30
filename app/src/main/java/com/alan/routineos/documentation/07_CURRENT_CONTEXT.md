# 07_CURRENT_CONTEXT.md — RoutineOS v2

## Live Operational Context
Este documento es la única fuente de verdad sobre lo que está ocurriendo en el repositorio en este preciso momento.

| **Current Branch** | `feature/ec-re-017-planning-consolidation` |
| **Current Phase** | Fase 6: Personalización y Refinamiento |
| **Current Goal** | Auditar la consolidación de PlanningWorkspace e integración del catálogo |
| **Current EC** | [EC-RE-017: Planning Workspace Consolidation](../EC/EC-RE-017_PLANNING_WORKSPACE_CONSOLIDATION.md) |
| **Status** | `AUDIT_PENDING` |
| **Next EC** | [EC-RE-018: Backlog & Unscheduled Items Panel] |
| **Blocked By** | Ninguna |
| **Working Directory** | `feature/ec-re-017-planning-consolidation` |
| **Current Sprint** | Sprint 5: Experience & Refinement |
| **Last Updated** | 2026-09-06 |

---

## Notas Inmediatas
- **EC-RE-016 CERRADA**: Análisis de dominio de elemento planificable finalizado y verificado (`PASS`).
- **EC-RE-017 IMPLEMENTADA (`AUDIT_PENDING`)**: Finalizada la consolidación de `PlanningWorkspace`:
  - Eliminados el `NavHost` anidado y el selector de pestañas `PlanningSegmentedSelector` en `PlanningWorkspace.kt`.
  - Integrado el Catálogo de Actividades (`ActivityCatalogScreen`) como `ModalBottomSheet` contextual desde el FAB de `PlanningScreen.kt`.
  - Implementada la materialización directa al seleccionar una rutina desde el sheet hacia el `selectedDate` del planificador vía `AddActivityToDayUseCase`.
  - Preservada la creación de eventos espontáneos/ad-hoc mediante menú Speed Dial en el FAB.
  - Renombrado semántico completo: `Dashboard*` ➔ `ActivityCatalog*` (0 referencias activas al nombre antiguo).
  - Cero cambios en la capa de dominio, esquemas de base de datos Room o migraciones.
  - Creado informe de auditoría `AUDITS/AUDIT_EC-RE-017_PLANNING_WORKSPACE_CONSOLIDATION.md` en estado `AUDIT_PENDING`.
