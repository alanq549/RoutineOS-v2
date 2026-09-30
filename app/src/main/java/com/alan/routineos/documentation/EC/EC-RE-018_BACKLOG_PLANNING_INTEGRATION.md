---
id: EC-RE-018
title: Backlog Operativo e Integración con Planning
phase: 6
priority: High
effort: Medium
owner: AI Agent
status: READY
depends_on: [EC-RE-017]
branch: feature/ec-re-018-backlog-integration
audit: Pending
created: 2026-09-06
updated: 2026-09-06
---

# EC-RE-018: Backlog Operativo e Integración con Planning

> **REGLA DE ORO DE ESTA EC**: Esta tarjeta es **exclusivamente de análisis formal y diseño de plan de implementación**. No se realiza ninguna modificación a código fuente Kotlin, base de datos Room, migraciones, ViewModels ni suite de pruebas en esta etapa.

---

## 1. Objective (Objetivo)

Diseñar y formalizar la integración completa del concepto **`BacklogItem`** dentro de la experiencia de planificación (**Planning**) y ejecución (**Today**). 

Actualmente, el modelo de datos de RoutineOS v2 ya cuenta con la entidad `BacklogItemEntity`, la tabla `backlog_items` en Room v10, el DAO `BacklogItemDao` y la llave foránea `DailyInstance.backlogId`. Sin embargo, el usuario carece de un flujo de UI para gestionar una bolsa de pendientes sin fecha, asignarlos a días concretos o devolverlos al backlog.

El objetivo de esta EC es definir el contrato de flujo, ciclo de vida, transiciones de estado, integraciones en interfaz e invariantes de sincronización futura para hacer operativo el Backlog sin alterar el esquema de base de datos actual.

---

## 2. Current Backlog Architecture (Arquitectura Actual)

- **[HECHO]**: La entidad de dominio `BacklogItem` existe en `domain/model/BacklogItem.kt`:
  ```kotlin
  enum class BacklogItemStatus { OPEN, RESOLVED, ARCHIVED }
  data class BacklogItem(
      val id: String,
      val definitionId: String?,
      val title: String,
      val status: BacklogItemStatus
  )
  ```
- **[HECHO]**: La entidad Room `BacklogItemEntity` existe en `data/local/entities/BacklogItemEntity.kt` y la tabla `backlog_items` está activa en **Room DB v10**.
- **[HECHO]**: `BacklogItemDao` en `data/local/dao/BacklogItemDao.kt` incluye métodos para `getAllBacklogItems()`, `upsertBacklogItem()`, y `deleteBacklogItem()`.
- **[HECHO]**: `DailyInstance` y `DailyInstanceEntity` poseen la columna `backlogId` vinculada mediante clave foránea a `backlog_items.id` con estrategia `ON DELETE SET NULL`.
- **[HECHO]**: `ActivityRepository` y `ActivityRepositoryImpl` aún **no exponen** flujos ni UseCases para gestionar `BacklogItem` desde la capa de presentación.

---

## 3. Current User Experience Gap (Brecha Actual de Experiencia)

- **[HECHO]**: El usuario no tiene forma de crear tareas o intenciones pendientes "para algún día" sin asignarles una fecha inmediata.
- **[HECHO]**: `PlanningScreen` permite gestionar el cronograma con hora y la sección "SIN HORARIO" para el día seleccionado, pero no cuenta con un estanque/panel de pendientes globales (`BacklogItem` con `status == OPEN`).

---

## 4. Backlog ➔ DailyInstance Flow (Flujo de Materialización)

- **[DECISIÓN - DEC-01]**: El proceso de planificar un ítem del backlog sigue esta semántica:

```text
[ BacklogItem (status = OPEN) ]
          │
          │ (Usuario selecciona fecha en Planning)
          ▼
[ Crear DailyInstance ]
  ├── id = UUID
  ├── target = ScheduleTarget.Definition(definitionId) si aplica, o null
  ├── scheduledDate = date.toEpochDay()
  ├── titleSnapshot = backlogItem.title
  ├── descriptionSnapshot = ""
  ├── plannedStartTime = null (en sección UNSCHEDULED por defecto)
  ├── status = DailyInstanceStatus.PLANNED
  ├── role = DailyInstanceRole.TASK (o ACTIVITY si tiene definitionId)
  ├── actionProtocol = ActionProtocol.CHECK (o TIMER)
  ├── backlogId = backlogItem.id
  └── isAdHoc = (definitionId == null)
          │
          ▼
[ Persistir en Room ] ➔ Aparece en el timeline de Planning/Today
```

---

## 5. Lifecycle / State Semantics (Ciclo de Vida y Estados)

- **[DECISIÓN - DEC-02]**: Matriz de impacto sobre `BacklogItem` según las acciones realizadas en la `DailyInstance` instanciada:

| Acción en `DailyInstance` | Estado de `DailyInstance` | Estado de `BacklogItem` | Justificación |
|---|---|---|---|
| **Completar (`COMPLETE`)** | `COMPLETED` | `RESOLVED` | La intención fue ejecutada en la realidad. Se genera `ActivityExecution` y se resuelve el pendiente. |
| **Omitir (`SKIP`)** | `OMITTED` | `OPEN` | Se omitió para ese día específico, pero el pendiente general sigue abierto para días futuros. |
| **Mover / Reprogramar** | `PLANNED` (nueva fecha) | `OPEN` | Se cambió la fecha de la ocurrencia. El pendiente sigue abierto. |
| **Deshacer (`RESET`)** | `PLANNED` | `OPEN` | Se desmarcó la completación. El pendiente vuelve a estar abierto. |
| **Borrar Instancia Día** | Eliminada de agenda | `OPEN` | Se eliminó la asignación diaria. El ítem regresa a la lista de pendientes libres. |

---

## 6. Replanning / Reopening (Devolución al Backlog)

- **[DECISIÓN - DEC-03]**:
  - Si el usuario decide "Devolver al Backlog" una `DailyInstance` que proviene de un `BacklogItem` (`backlogId != null`), el sistema elimina la `DailyInstance` de la agenda del día y asegura que `BacklogItem.status == OPEN`.
  - Si un `BacklogItem` archivado o resuelto es reabierto manualmente desde la UI del backlog, su estado cambia a `OPEN`.

---

## 7. Duplicate / Multiple Instances (Reglas de Duplicidad)

- **[DECISIÓN - DEC-04]**:
  - **Ocurrencia Única Activa**: Un `BacklogItem` en estado `OPEN` solo debe tener **una `DailyInstance` activa no completada** (`status == PLANNED` o `MODIFIED`) en la agenda a la vez.
  - Si el usuario intenta asignar a otra fecha un `BacklogItem` que ya tiene una `DailyInstance` pendiente en otro día, el sistema actualiza la fecha (`scheduledDate`) de la `DailyInstance` existente en lugar de duplicar instancias.

---

## 8. Planning Integration (Integración en Planning)

- **[DECISIÓN - DEC-05]**:
  - En `PlanningScreen.kt`, el catálogo/panel de pendientes coexistirá contextualmente.
  - Se añadirá una opción en el menú del FAB o en la cabecera: **"PENDIENTES / BACKLOG"**.
  - Al abrir el panel de Backlog, se muestra la lista de `BacklogItem`s abiertos (`OPEN`).
  - Cada ítem ofrece un botón de un solo toque: **"Asignar a hoy / día seleccionado"**, invocado mediante `AssignBacklogItemToDayUseCase`.
  - Permite crear nuevos `BacklogItem`s directamente en la bolsa de pendientes sin asignar fecha.

---

## 9. Today Integration (Integración en Today)

- **[DECISIÓN - DEC-06]**:
  - `Today` renderiza cualquier `DailyInstance` cuyo `backlogId != null` exactamente igual que cualquier otra tarea o actividad.
  - Completar una tarea proveniente del backlog en Today marca la `DailyInstance` como `COMPLETED`, escribe la `ActivityExecution` y actualiza atómicamente el `BacklogItem.status` a `RESOLVED`.

---

## 10. Domain Invariants (Invariantes de Dominio Preservadas)

- **[HECHO - Preservado]**:
  - `parentInstanceId` = Exclusivo para jerarquía estructural de sub-pasos (`ActivityNode`).
  - `associatedInstanceId` = Exclusivo para asociación contextual (`Task != ActivityNode`).
  - `target` (`targetId` + `targetType`) = Orientación semántica a la plantilla original.
  - `backlogId` = Vínculo exclusivo de origen desde la bolsa de pendientes.
  - **No se sobrecarga ni reutiliza ningún campo para múltiples propósitos**.

---

## 11. Offline-First / Future Sync Considerations (Sincronización Futura)

- **[HIPÓTESIS DE SINCRONIZACIÓN]**:
  1. **UUIDs Cliente**: Todos los IDs (`BacklogItem.id`, `DailyInstance.id`) se generan como UUIDs en cliente (`UUID.randomUUID().toString()`), lo que garantiza tolerancia total a desconexión sin colisiones de clave primaria.
  2. **Relaciones en Cascada/Set Null**: La clave foránea `DailyInstance.backlogId` usa `ON DELETE SET NULL` en Room, previniendo fallos si un ítem de backlog se elimina remotamente.
  3. **Recomendaciones de Campos Futuros (Para Fases de Sync)**:
     - Añadir un campo `updatedAt: Long` (Epoch Ms) a `BacklogItemEntity` para resolución de conflictos *Last-Write-Wins* (LWW).
     - Añadir `isDeleted: Boolean` para borrado suave (*soft-delete*) en deltas de sincronización.
     - Operaciones idempotentes: La transición `OPEN` ➔ `RESOLVED` es idempotente por id de `BacklogItem`.

---

## 12. Performance / Loading Considerations (Rendimiento y Carga)

- **[HECHO]**: La tabla `backlog_items` en Room v10 cuenta con índice en `definitionId`, y `daily_instances` cuenta con índice en `backlogId`.
- **Carga Reactiva**: Los elementos abiertos se exponen mediante `Flow<List<BacklogItem>>` mediante `BacklogItemDao.getAllBacklogItems()`.
- **Estados Vacíos**: Interfaz limpia con mensajes descriptivos cuando la bolsa de pendientes esté vacía, evitando skeletons pesados.

---

## 13. Non-Goals (Exclusiones Explícitas)

- No se modifica ninguna entidad existente ni el número de versión del esquema Room v10.
- No se implementa motor ni infraestructura de sincronización remota en esta EC.
- No se altera la pantalla de `Stats` ni el módulo `Body`.
- No se crean editores de `Deadline` o `Account`.

---

## 14. Implementation Plan (Pasos de Implementación Futura)

1. **Capa Data / Repositorio**:
   - Exponer en `ActivityRepository` y `ActivityRepositoryImpl` los métodos:
     - `getOpenBacklogItems(): Flow<List<BacklogItem>>`
     - `upsertBacklogItem(item: BacklogItem)`
     - `deleteBacklogItem(id: String)`
2. **Casos de Uso de Dominio**:
   - Crear `AssignBacklogItemToDayUseCase(repository)` para materializar la `DailyInstance` desde un `BacklogItem`.
   - Actualizar `RegisterDailyActionUseCase` para que al completar o deshacer una `DailyInstance` con `backlogId != null`, se actualice el `BacklogItemStatus` (`RESOLVED` / `OPEN`).
3. **Capa Presentación / UI**:
   - Exponer el estado de backlog en `PlanningViewModel`.
   - Crear el sheet/panel `BacklogPanelSheet` en `PlanningScreen.kt`.
   - Agregar la acción "Añadir de Pendientes" en el menú Speed Dial del FAB.

---

## 15. Verification Plan (Plan de Verificación Futura)

- **Pruebas Unitarias**:
  - Test de `AssignBacklogItemToDayUseCase` verificando la creación de `DailyInstance` con `backlogId`.
  - Test de `RegisterDailyActionUseCase` verificando la transición `OPEN` ➔ `RESOLVED` al completar la instancia.
  - Test de reversión `RESET` verificando la transición `RESOLVED` ➔ `OPEN`.
- **Pruebas de Compilación**:
  ```powershell
  .\gradlew.bat assembleDebug
  .\gradlew.bat testDebugUnitTest
  ```

---

## 16. Acceptance Criteria (Criterios de Aceptación Futura)

- [ ] El usuario puede crear ítems en la bolsa de pendientes (`BacklogItem`) sin fecha asignada.
- [ ] En `PlanningScreen`, el usuario puede abrir el panel de pendientes y asignar un `BacklogItem` al día seleccionado.
- [ ] La asignación crea una `DailyInstance` con `backlogId` correctamente vinculado.
- [ ] Completar la tarea en Today marca la instancia como `COMPLETED`, registra la `ActivityExecution` y cambia el `BacklogItem` a `RESOLVED`.
- [ ] Deshacer la completación (`RESET`) devuelve el `BacklogItem` a estado `OPEN`.
- [ ] Omitir (`SKIP`) la tarea en Today deja el `BacklogItem` en estado `OPEN`.
- [ ] Cero modificaciones al esquema Room DB v10 o migraciones.
- [ ] `assembleDebug` PASS y `testDebugUnitTest` PASS.

---

## 17. Open Questions (Preguntas Abiertas)

- **[OPEN QUESTION - Q1]**: ¿Debería el panel de Backlog permitir asignar prioridades visuales (`ALTA`, `MEDIA`, `BAJA`) a los ítems pendientes?
- **[OPEN QUESTION - Q2]**: ¿Debería permitirse asociar una `Note` a un `BacklogItem` antes de que este sea asignado a un día?

---

## 18. Decision Log (Registro de Decisiones de Diseño)

| ID | Tema | Decisión Adoptada | Justificación |
|---|---|---|---|
| **DEC-01** | Flujo de Materialización | Crear `DailyInstance` con `backlogId = backlogItem.id`. | Reutiliza el motor unificado de instancias de agenda diaria. |
| **DEC-02** | Omisión de Instancia | `SKIP` mantiene `BacklogItem.status = OPEN`. | Omitir para hoy no destruye la intención de realizar el pendiente en el futuro. |
| **DEC-03** | Reversión de Completación | `RESET` vuelve `BacklogItem.status = OPEN`. | Garantiza consistencia bidireccional si el usuario desmarca una tarea por error. |
| **DEC-04** | Control de Duplicación | Una sola `DailyInstance` no completada por `BacklogItem`. | Evita saturar la agenda con ocurrencias duplicadas del mismo pendiente. |
| **DEC-05** | Ubicación en Planning | Panel/Sheet de Pendientes desplegable desde el FAB de Planning. | Evita crear pestañas redundantes manteniendo Planning como superficie única. |
| **DEC-06** | Ejecución en Today | Misma ficha compacta de `TASK` en Today. | Mantener experiencia homogénea de ejecución en tiempo real. |
