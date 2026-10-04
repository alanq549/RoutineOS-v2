---
ec_id: EC-RE-019
ronda: 2
fecha: 2026-09-06
resultado: PASS
---

# Auditoría Técnica: EC-RE-019 - Catalog Semantics & Occurrence Materialization Correction

**Fecha:** 2026-09-06  
**Estado:** CLOSED (Aprobado y Verificado Manualmente en Runtime)  
**Criterio de Evaluación:** Corrección de Materialización por `(sourceRuleId, scheduledDate)`, Inversión Semántica de `ActivityCard.kt`, Cero Migraciones Room DB v10 y Suite de Pruebas.

## 1. Archivos Modificados / Creados

### Capa Data / Repositorio
- [x] **[DailyInstanceDao.kt](file:///C:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/data/local/dao/DailyInstanceDao.kt)**: Agregada la consulta `@Query("SELECT * FROM daily_instances WHERE sourceRuleId = :sourceRuleId AND scheduledDate = :date LIMIT 1")`.
- [x] **[ActivityRepository.kt](file:///C:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/domain/repository/ActivityRepository.kt)**: Expuesta la función `getDailyInstanceBySourceRule(sourceRuleId, date)`.
- [x] **[OfflineActivityRepository.kt](file:///C:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/data/repository/OfflineActivityRepository.kt)**: Implementada la consulta de regla en el repositorio offline.

### Capa Dominio / Casos de Uso
- [x] **[MaterializeInstanceUseCase.kt](file:///C:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/domain/usecase/MaterializeInstanceUseCase.kt)**: Refactorizado para usar `(sourceRuleId, scheduledDate)` en ocurrencias recurrentes. Para `sourceRuleId == null`, no realiza lookup por ScheduleRule, trata la instancia como puntual, normaliza/persiste mediante su ID y conserva `backlogId` y demás datos de la instancia.

### Capa Presentación / UI
- [x] **[ActivityCard.kt](file:///C:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/feature/dashboard/components/ActivityCard.kt)**: Tap principal abre `ActivityDetailScreen`. Acción secundaria "PLANIFICAR OCURRENCIA" removida y reemplazada por botón de "ELIMINAR" (Soft Delete).
- [x] **[ActivityCatalogScreen.kt](file:///C:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/feature/dashboard/ActivityCatalogScreen.kt)** & **[ActivityCatalogRoute.kt](file:///C:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/feature/dashboard/ActivityCatalogRoute.kt)**: Propagados los callbacks de apertura de detalle y eliminación.
- [x] **[PlanningScreen.kt](file:///C:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/feature/planning/PlanningScreen.kt)**: Cierra el sheet de catálogo antes de navegar a `ActivityDetailScreen`.

### Pruebas Unitarias
- [x] **[MaterializationTest.kt](file:///C:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/test/java/com/alan/routineos/domain/usecase/MaterializationTest.kt)**: Cobertura completa de Casos A, B, C, D y E (82/82 tests pasando).

---

## 2. Resultados de Verificación de Criterios

| # | Criterio Canónico de Aceptación | Estado | Evidencia / Observación |
|:---:|:---|:---:|:---|
| **AC-01** | `ActivityDefinition` + `ScheduleRule` aplicable se proyecta automáticamente. | **PASS** | Proyectado automáticamente en memoria vía `TimelineResolutionEngine`. |
| **AC-02** | Ocurrencia virtual no se duplica en DB al cargarse. | **PASS** | `TimelineResolutionEngine.resolve()` opera 100% en memoria. |
| **AC-03** | `MaterializeInstanceUseCase` busca por `(sourceRuleId, scheduledDate)` cuando `sourceRuleId != null`. | **PASS** | Verificado en `MaterializeInstanceUseCase.kt:23`. |
| **AC-04** | Ocurrencias sin `sourceRuleId` no realizan consulta por regla. | **PASS** | Verificado en bloque `else` de `MaterializeInstanceUseCase.kt:29`. |
| **AC-05** | Dos `ScheduleRules` distintas pueden coexistir virtualmente. | **PASS** | Proyectadas independientemente por `TimelineResolutionEngine`. |
| **AC-06** | Ambas `ScheduleRules` pueden materializarse independientemente. | **PASS** | Verificado en `MaterializationTest.kt > Caso E`. |
| **AC-07** | Materializar A nunca secuestra la materialización de B. | **PASS** | Verificado en `MaterializationTest.kt > Caso E`. |
| **AC-08** | Unicidad recurrente = `(sourceRuleId, scheduledDate)`. | **PASS** | Verificado en DAO, Repositorio e índice UNIQUE de Room DB v10. |
| **AC-09** | Cero migraciones DB / Room DB v10 intacto. | **PASS** | Cero migraciones creadas. |
| **AC-10** | No alterar la semántica inmutable de `isAdHoc` (`target == null` ➔ `isAdHoc = true`, `target != null` ➔ `isAdHoc = false`). | **PASS** | `AddActivityToDayUseCase.kt` asigna `isAdHoc = false` para tarjetas estructurales. |
| **AC-11** | Ocurrencia materializada conserva `sourceRuleId`. | **PASS** | Verificado en `MaterializeInstanceUseCase.kt`. |
| **AC-12** | A y B permanecen independientes tras la materialización. | **PASS** | Verificado en `MaterializationTest.kt > Caso E`. |
| **AC-13** | Dos `ScheduleRules` pueden materializarse sin violar unicidad. | **PASS** | `Caso E` valida la lógica en `MaterializeInstanceUseCase`. El índice `UNIQUE(sourceRuleId, scheduledDate)` en Room DB valida la restricción a nivel DB. |
| **AC-14** | Tap principal nunca ejecuta `AddActivityToDayUseCase`. | **PASS** | Verificado en `ActivityCard.kt` y `PlanningScreen.kt`. |
| **AC-15** | `ActivityDetailScreen` no crea/modifica `DailyInstance`. | **PASS** | Carga sólo la definición y sus nodos. |
| **AC-16** | Regresar desde `ActivityDetailScreen` conserva `selectedDate`. | **PASS** | `NavController` desapila hacia `PlanningWorkspace` preservando `selectedDate`. |
| **AC-17** | Cambios en `ScheduleRule` desde `ActivityDetailScreen` actualizan `Planning` en runtime. | **PASS** | Validado mediante prueba manual/runtime realizada en la aplicación. |

---

## 3. Dictamen Final

> [!NOTE]
> La implementación técnica, pruebas unitarias y validación manual runtime fueron superadas exitosamente al 100%. Todos los criterios canónicos (AC-01 a AC-17) están satisfechos.

**Auditoría Técnica:** `PASS` (Código y Tests Unitarios 82/82)  
**Validación Runtime (AC-17):** `PASS` (Validado manualmente en la aplicación)  
**Estado Formal EC:** `CLOSED`

---

## 4. Notas Fuera de Alcance

> **Post-validation finding**:
> Stats presenta una desincronización al revertir ejecuciones. No corresponde a EC-RE-019. Pendiente de análisis e incidencia independiente.
