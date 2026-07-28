# 07_CURRENT_CONTEXT.md — RoutineOS v2

## Live Operational Context
Este documento es la única fuente de verdad sobre lo que está ocurriendo en el repositorio en este preciso momento.

| Key | Value |
| :--- | :--- |
| **Current Branch** | `develop` |
| **Current Phase** | Fase 4: Routine Management |
| **Current Goal** | Implementar Routine Dashboard Screen |
| **Current EC** | [EC-006_ROUTINE_DASHBOARD](./EC/EC-006_ROUTINE_DASHBOARD.md) |
| **Next EC** | [EC-007_ACTIVITY_CREATION_FLOW](./EC/EC-007_ACTIVITY_CREATION_FLOW.md) |
| **Blocked By** | Ninguna |
| **Working Directory** | `feature/dashboard/` |
| **Current Sprint** | Sprint 2: Feature Layer |
| **Last Updated** | 2026-07-26 |

---

## Notas Inmediatas
- EC-006 (Routine Dashboard) en estado CHANGES_REQUESTED.
- Se detectaron violaciones a invariantes de dominio en ViewModels y Fakes (strings hardcodeadas).
- Deuda técnica detectada: ViewModels de feature layer sin inyección de dependencias Hilt.
- Registro de Mocks actualizado en MOCK_DATA_STATUS.md.
