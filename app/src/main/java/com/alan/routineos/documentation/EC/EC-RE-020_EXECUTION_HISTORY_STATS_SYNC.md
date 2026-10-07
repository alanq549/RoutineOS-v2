---
id: EC-RE-020
title: Execution History Lifecycle & Stats Synchronization
phase: 6
priority: High
effort: Medium
owner: AI Agent
status: CLOSED
result: PASS
depends_on: [EC-RE-019]
branch: feature/ec-re-020-execution-history-stats-sync
audit: Approved
created: 2026-09-06
updated: 2026-09-06
---

# EC-RE-020: Execution History Lifecycle & Stats Synchronization

> **REGLA DE ORO DE ESTA EC**: Tarjeta de **análisis formal, diagnóstico de causa raíz y especificación de contrato de datos**. En esta fase NO se modifica ningún archivo de código fuente Kotlin, interfaz Compose, DAO, repositorio, ViewModel ni esquema de base de datos Room.

---

## 1. Objective (Objetivo)

Diseñar y formalizar la sincronización limpia entre el ciclo de vida de **`DailyInstance`**, la facticidad histórica **`ActivityExecution`** y las métricas de **`Stats`** cuando el usuario revierte o desmarca explícitamente una ejecución mediante la acción `Reset`.

Actualmente existe una desincronización donde al revertir una ejecución (`Reset`):
1. La `DailyInstance` cambia su estado a `PLANNED` / `MODIFIED` (o se elimina de Room si proviene de una regla recurrente `sourceRuleId != null`).
2. El registro de facticidad `ActivityExecution` se conserva intacto en la tabla `activity_executions` (o su `dailyInstanceId` pasa a ser `NULL` por la clave foránea `ON DELETE SET NULL`).
3. La eliminación correcta de `ActivityExecution` corrige la fuente de datos. Además, `Stats` debe disponer de un mecanismo para volver a calcular el estado cuando cambie la información histórica, sin depender de refresh artificial.

Esta EC define la causa raíz formal, evalúa las alternativas de arquitectura y establece el contrato quirúrgico para garantizar la consistencia sin afectar las ejecuciones de otras reglas independientes para la misma actividad y fecha.

---

## 2. Current Architecture & Diagnostics (Causa Raíz Formal)

### 2.1 Cadena de Eventos al Completar (`Complete`)
```text
[ Acción Complete ]
        ↓
DailyInstance.status = COMPLETED
        ↓
Insertar ActivityExecution (dailyInstanceId = instance.id, scheduledDate = date)
        ↓
HistoricalOccurrenceResolver halla match (dailyInstanceId == occ.instance.id)
        ↓
Stats contabiliza la ocurrencia como EJECUTADA / ADHERIDA
```

### 2.2 Cadena de Eventos al Revertir (`Reset`) — Diagnóstico del Defecto
```text
[ Acción Reset ]
        ↓
Caso Recurrente (sourceRuleId != null):
  DailyInstance es eliminada de Room.
  FK dailyInstanceId en ActivityExecution pasa a NULL (ON DELETE SET NULL).
Caso Ad-hoc / Backlog (sourceRuleId == null):
  DailyInstance.status pasa a MODIFIED / PLANNED.
  ActivityExecution permanece con dailyInstanceId = instance.id.
        ↓
RegisterDailyActionUseCase NO elimina ni invalida el ActivityExecution.
        ↓
HistoricalOccurrenceResolver ejecuta resolveRange():
  Busca ejecuciones en esa fecha.
  Para el caso recurrente: matchBySnapshot(exec, occ.instance) asocia el exec huérfano (dailyInstanceId == null) con la nueva ocurrencia proyectada por el título/definición.
  Para el caso ad-hoc: exec.dailyInstanceId == occ.instance.id sigue coincidiendo con la instancia reseteada.
        ↓
Stats (vía GetHistoryAnalyticsUseCase.execute) realiza una consulta puntual estática.
Sin observación reactiva de los cambios de Room, la fuente de datos histórica conserva ejecuciones huérfanas.
```

---

## 3. Lifecycle Analysis (Ciclo de Vida Operacional)

| Operación | Estado de `DailyInstance` | Estado de `ActivityExecution` | Comportamiento Esperado en Historial/Stats |
|---|---|---|---|
| **`Complete`** | `COMPLETED` | Creado con `dailyInstanceId = instance.id` | Contabiliza como ejecución válida en historial y métricas de Stats. |
| **`Reset`** | `PLANNED` / `MODIFIED` (o eliminada si `sourceRuleId != null`) | **Eliminado quirúrgicamente por `dailyInstanceId = instance.id`** | Deja de contabilizar en Stats. La ocurrencia vuelve a contar como pendiente o no completada. |
| **`Skip`** | `OMITTED` | No se crea registro (o si existía uno previo, se elimina quirúrgicamente) | Contabiliza como omitida. No incrementa ejecuciones válidas. |
| **`Move`** | `MODIFIED` + `plannedStartTime` actualizado | Permanece vinculado si ya estaba completado, o actualiza `scheduledDate` si aplica | Preserva la facticidad si la ejecución ocurrió. |

---

## 4. Orden Obligatorio de Operaciones en `Reset`

Para garantizar que la clave foránea `ON DELETE SET NULL` no deje registros huérfanos con `dailyInstanceId == null`, el orden de operaciones en `RegisterDailyActionUseCase.kt` al ejecutar `Reset` debe ser estrictamente:

```text
1. materializeIfVirtual(entry.root)
        ↓
2. obtener instance.id concreto
        ↓
3. deleteExecutionsForDailyInstance(instance.id)  [DELETE FROM activity_executions WHERE dailyInstanceId = :instanceId]
        ↓
4. deleteDailyInstance(instance.id)  [o reset de status a PLANNED/MODIFIED]
```

> **REGLA INVARIANTE DE CLAVE**: NUNCA utilizar como clave de borrado `nodeId + scheduledDate`, `targetId + scheduledDate`, `activityId + scheduledDate` ni `titleSnapshot`. La eliminación se realiza **únicamente** por `dailyInstanceId = instance.id`.

---

## 5. Decision & Domain Contract (Decisión Adoptada)

- **[DECISIÓN - DEC-01]**: Se adopta la **Reversión Quirúrgica de Ejecución por `dailyInstanceId`**.
- **Fundamento**: En RoutineOS v2, `ActivityExecution.dailyInstanceId` identifica la `DailyInstance` a la que pertenece. Cuando el usuario hace clic explícito en `Reset` ("Deshacer"), el dominio interpreta que la ejecución fue revertida.
- **Contrato de Invariante Operativa**:
  La operación:
  `DELETE FROM activity_executions WHERE dailyInstanceId = :instanceId`
  elimina quirúrgicamente todas las ejecuciones asociadas a esa `DailyInstance`, sin afectar ejecuciones pertenecientes a otras instancias.
- **Mecanismo de Actualización de Stats**:
  - La eliminación correcta de `ActivityExecution` corrige la fuente de datos histórica.
  - Además, la arquitectura de `Stats` se integrará mediante observación reactiva de los flujos de datos (`Flow<List<ActivityExecution>>`, `Flow<List<DailyInstance>>`), permitiendo que `StatsViewModel` y `GetHistoryAnalyticsUseCase` vuelvan a calcular automáticamente el snapshot cuando cambie la información histórica en Room.
  - **Prohibición Expresa**: Se prohíbe el uso de parches de UI como `delay`, `manual invalidate`, `refresh button` o trucos de recomposición artificial.

---

## 6. Multi-Rule Isolation Analysis (Caso Crítico de Aislamiento de Reglas)

### Escenario de Prueba Obligatorio
```text
Configuración:
ActivityDefinition X ("Gimnasio")
Fecha: 2026-09-10

Reglas:
Rule A ➔ Gym 08:00 ➔ DailyInstance A (id = "inst_A") ➔ ActivityExecution A (dailyInstanceId = "inst_A")
Rule B ➔ Gym 18:00 ➔ DailyInstance B (id = "inst_B") ➔ ActivityExecution B (dailyInstanceId = "inst_B")
```

### Comportamiento con la Solución Quirúrgica
```text
Acción: Usuario ejecuta Reset sobre la instancia A (08:00)

Ejecución de Borrado:
DELETE FROM activity_executions WHERE dailyInstanceId = 'inst_A'

Resultado:
- ActivityExecution A (id = "inst_A") ➔ ELIMINADO
- DailyInstance B (id = "inst_B")     ➔ INTACTA (status = COMPLETED)
- ActivityExecution B (id = "inst_B") ➔ INTACTO (permanece en DB)
- Stats: Muestra 1 ejecución realizada (Rule B) y 0 para Rule A.
```

---

## 7. Materialization & Cascade Behavior Analysis

### Ocurrencia Recurrente Virtual
```text
Virtual Rule A
      ↓ (Complete)
MaterializeInstanceUseCase creates DailyInstance A (id = "UUID_A", sourceRuleId = "rule_A")
      ↓
RegisterDailyActionUseCase saves ActivityExecution A (dailyInstanceId = "UUID_A")
      ↓ (Reset)
1. deleteExecutionsForDailyInstance("UUID_A") deletes ActivityExecution A
2. deleteDailyInstance("UUID_A") deletes DailyInstance A (reverts to virtual projection)
      ↓
Stats re-resolves range: Virtual Rule A has status = PLANNED and execution = null.
Métricas en Stats de coherencia y adherencia retornan al estado real no completado.
```

---

## 8. HistoricalOccurrenceResolver Analysis

- **[HECHO]**: Se evaluó si `HistoricalOccurrenceResolver.kt` requiere cambios.
- **Conclusión**: El fallback `matchBySnapshot` dentro de `HistoricalOccurrenceResolver` es necesario para resolver el historial de nodos eliminados o ejecuciones históricas sin instancia. No debe eliminarse.
- **Mecanismo de Prevención**: Al eliminar quirúrgicamente el `ActivityExecution` mediante `deleteExecutionsForDailyInstance(instance.id)` **antes** de eliminar la `DailyInstance`, no quedan registros huérfanos con `dailyInstanceId == null` para esa fecha. Por lo tanto, `matchBySnapshot` no encuentra ningún falso positivo y `Stats` refleja fielmente el estado reseteado.

---

## 9. Domain Invariants (Invariantes de Dominio Preservadas)

1. `DailyInstance.id` es la clave primaria de asociación directa con `ActivityExecution.dailyInstanceId`.
2. `deleteExecutionsForDailyInstance(dailyInstanceId)` se ejecuta quirúrgicamente **antes** del reset o eliminación de la `DailyInstance`.
3. Cero afectación a ejecuciones de otras reglas (`Rule B`) para la misma fecha o actividad.
4. Cero cambios en Room schema v10, cero migraciones y cero agregación de entidades (`TaskEntity`, `ReminderEntity`, `PlannableElement`).

---

## 10. Acceptance Criteria (Criterios de Aceptación Futura)

- [ ] **AC-01**: Completar una instancia (`Complete`) crea una `DailyInstance` marcada como `COMPLETED` y un `ActivityExecution` asociado a su `dailyInstanceId`.
- [ ] **AC-02**: Las estadísticas en `Stats` contabilizan la ejecución completada inmediatamente.
- [ ] **AC-03**: Revertir la ejecución (`Reset`) elimina quirúrgicamente el `ActivityExecution` vinculado a ese `dailyInstanceId`.
- [ ] **AC-04**: Al ejecutar `Reset`:
  1. `ActivityExecution` es eliminado correctamente.
  2. El cálculo histórico deja de contabilizarlo.
  3. `Stats` refleja el nuevo cálculo sin intervención manual ni refresh artificial del usuario.
- [ ] **AC-05**: Cuando existen dos reglas independientes (`Rule A` y `Rule B`) para el mismo `ActivityDefinition` en el mismo día y ambas están completadas, ejecutar `Reset` en `Rule A` elimina solo el `ActivityExecution` de A, dejando intactos la `DailyInstance B` y el `ActivityExecution` de B.
- [ ] **AC-06**: Revertir (`Reset`) una ocurrencia recurrente materializada elimina el `ActivityExecution` y la `DailyInstance` materializada, retornando la proyección a su estado virtual no completado en `Stats`.
- [ ] **AC-07**: Cerrar y reabrir la aplicación tras un `Complete` seguido de un `Reset` mantiene la consistencia correcta de `Stats`.

---

## 11. Technical Implementation Scope (Archivos Potencialmente Modificables)

1. **`data/local/dao/ActivityExecutionDao.kt`**:
   - Agregar consulta quirúrgica:
     ```kotlin
     @Query("DELETE FROM activity_executions WHERE dailyInstanceId = :dailyInstanceId")
     suspend fun deleteExecutionsForDailyInstance(dailyInstanceId: String)
     ```
2. **`domain/repository/ActivityRepository.kt` & `OfflineActivityRepository.kt`**:
   - Agregar e implementar el contrato:
     ```kotlin
     suspend fun deleteExecutionsForDailyInstance(dailyInstanceId: String)
     ```
3. **`domain/usecase/RegisterDailyActionUseCase.kt`**:
   - En `handleResetRecursive`, invocar quirúrgicamente `repository.deleteExecutionsForDailyInstance(instance.id)` **antes** de eliminar o resetear la `DailyInstance`.
4. **`domain/usecase/GetHistoryAnalyticsUseCase.kt` & `feature/stats/StatsViewModel.kt`**:
   - Integración de observación reactiva mediante flujos de datos (`Flow`) para recalcular automáticamente el snapshot analítico ante emisiones de Room.
5. **Pruebas Unitarias de Dominio**:
   - Crear `app/src/test/java/com/alan/routineos/domain/usecase/ExecutionHistoryLifecycleTest.kt`.

---

## 12. Verification Plan (Plan de Verificación Futura)

- **Ejecución de Pruebas Unitarias**:
  ```powershell
  .\gradlew.bat :app:testDebugUnitTest
  ```
- **Verificación de Compilación**:
  ```powershell
  .\gradlew.bat :app:assembleDebug
  ```

---

## 13. Current Status Summary (Resumen de Estado)

```text
EC-RE-020
Status: ANALYSIS
Result: NOT_IMPLEMENTED

DOMAIN CHANGES:
NONE (Solo especificación conceptual y análisis de reactividad).

DB CHANGES:
NONE (El esquema Room DB v10 ya cuenta con la columna dailyInstanceId e índice en activity_executions).

CODE MODIFICATIONS:
NONE (0 líneas de código de producción o base de datos alteradas).
```
