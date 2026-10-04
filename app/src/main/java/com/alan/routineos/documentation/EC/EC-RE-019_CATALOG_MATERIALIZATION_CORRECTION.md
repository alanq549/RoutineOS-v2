---
id: EC-RE-019
title: Catalog Semantics & Occurrence Materialization Correction
phase: 6
priority: High
effort: Medium
owner: AI Agent
status: CLOSED
depends_on: [ EC-RE-017 ]
branch: feature/ec-re-019-catalog-materialization-fix
audit: Approved
created: 2026-09-06
updated: 2026-09-06
---

# EC-RE-019: Catalog Semantics & Occurrence Materialization Correction

> **REGLA DE ORO DE ESTA EC**: Corrección técnica puntual del motor de materialización de reglas de
> programación y alineación semántica de interfaz en el catálogo de actividades. No se realizan
> migraciones de base de datos ni modificaciones al esquema de Room DB v10 (
`index_daily_instances_sourceRuleId_scheduledDate`).

---

## 1. Objective (Objetivo)

Corregir dos desviaciones en la experiencia de planificación e infraestructura de dominio:

1. **Identidad y Materialización de Ocurrencias Recurrentes**: Sustituir la búsqueda errónea
   `getDailyInstanceByTarget(targetId, date)` en `MaterializeInstanceUseCase` por la búsqueda
   unívoca `getDailyInstanceBySourceRule(sourceRuleId, date)`. Esto permite que múltiples reglas de
   programación para una misma rutina en el mismo día coexistan y se materialicen de forma
   independiente.
2. **Alineación Semántica UX del Catálogo**: Invertir la prioridad de interacción en
   `ActivityCard.kt` para que el tap principal abra la pantalla de detalle/configuración de la
   rutina (`ActivityDetailScreen`) y la acción de crear una ocurrencia puntual sea una operación
   secundaria e explícita ("PLANIFICAR OCURRENCIA").

---

## 2. Current Architecture & Diagnosis (Diagnóstico Técnico)

- **[HECHO - Bug Previo]**: `MaterializeInstanceUseCase.kt` buscaba instancias previas usando
  `getDailyInstanceByTarget(targetId, scheduledDate)`. Si una actividad ("Gym") tenía la `Rule A` (
  08:00) y la `Rule B` (18:00) para el mismo día, al materializarse A, la posterior invocación de B
  encontraba la instancia de A en la base de datos por compartir el `targetId`, retornando
  erróneamente A y bloqueando la materialización independiente de B.
- **[HECHO - Corrección]**: La identidad unívoca de una ocurrencia recurrente es
  `(sourceRuleId, scheduledDate)`. Se agregó la consulta
  `@Query("SELECT * FROM daily_instances WHERE sourceRuleId = :sourceRuleId AND scheduledDate = :date LIMIT 1")`
  en `DailyInstanceDao.kt` y se vinculó en `MaterializeInstanceUseCase.kt`.
- **[HECHO - Tap del Catálogo]**: El toque principal en la tarjeta del catálogo llamaba a
  `AddActivityToDayUseCase`. Se corrigió en `ActivityCard.kt` para que el tap principal invoque
  `onActivityClick` (navegación a `ActivityDetailScreen`) y la acción de planificar ocurrencia sea
  el botón secundario "PLANIFICAR OCURRENCIA".

---

## 3. Domain Model Invariants Preserved (Invariantes de Dominio Preservadas)

- **`ActivityDefinition`**: Plantilla/molde abstracto de la rutina.
- **`ScheduleRule`**: Intención recurrente que proyecta la rutina en fechas específicas.
- **`DailyInstance`**: Ocurrencia concreta. Si `sourceRuleId != null`, pertenece a una regla
  recurrente. Si `sourceRuleId == null`, es una ocurrencia puntual o ad-hoc.
- **`isAdHoc`**: Fiel al esquema Room v10 (`target == null` ➔ `isAdHoc = true`; `target != null` ➔
  `isAdHoc = false`).
- **No se crearon**: `TaskEntity`, `ReminderEntity`, `PlannableElement`, ni campos `isException` /
  `isOneShot`.
- **No se realizaron**: Migraciones DB ni cambios al índice único de Room DB v10.

---

## 4. Implementation Plan (Pasos Ejecutados)

1. **`DailyInstanceDao.kt`**: Agregado `getInstanceBySourceRule(sourceRuleId, date)`.
2. **`ActivityRepository.kt` & `OfflineActivityRepository.kt`**: Expuesto e implementado
   `getDailyInstanceBySourceRule(sourceRuleId, date)`.
3. **`MaterializeInstanceUseCase.kt`**:
    - `virtualInstance.sourceRuleId != null` ➔ Busca por `(sourceRuleId, scheduledDate)` y
      materializa si no existe.
    - `virtualInstance.sourceRuleId == null` ➔ Instancia puntual/ad-hoc; realiza pass-through
      directo.
4. **`ActivityCard.kt`**:
    - Tap principal en el cuerpo o botón "DETALLE" ➔ NAVEGA a `ActivityDetailScreen`.
    - Botón secundario "PLANIFICAR OCURRENCIA" ➔ Invoca `AddActivityToDayUseCase`.
5. **`PlanningScreen.kt`**: Cierra el sheet de catálogo (`showCatalogSheet = false`) antes de
   navegar a `ActivityDetailScreen`.

---

## 5. Verification Plan & Test Results (Plan de Verificación y Pruebas)

- **Pruebas Unitarias Exitosas (`81/81 PASS`)**:
    - `Caso E - Regression Test`: `Rule A` y `Rule B` para la misma actividad en el mismo día se
      materializan independientemente sin colisión.
    - `Caso B`: Materialización idempotente de la misma regla no crea registros duplicados.
    - `Caso C`: Instancias con `sourceRuleId == null` se procesan como puntuales sin consulta de
      regla.
- **Compilación Limpia**:
    - `./gradlew assembleDebug`: `BUILD SUCCESSFUL`.
