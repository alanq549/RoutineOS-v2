---
audit_id: AUDIT_ROADMAP_NEXT_STEP
fecha: 2026-09-06
estado: COMPLETE
---

# Auditoría Técnica de Roadmap y Estado Real del Proyecto — RoutineOS v2

**Fecha:** 2026-09-06  
**Objetivo:** Evaluar objetivamente el estado del código fuente, base de datos y documentación para determinar cuál debe ser el trabajo real pendiente y la siguiente Engineering Card (EC) recomendada.

---

## 1. Current Project State (Estado Actual del Proyecto)

- **[HECHO]**: RoutineOS v2 se encuentra en la **Fase 6 (Personalización y Refinamiento)**.
- **[HECHO]**: La arquitectura de datos utiliza **Room Database v10** con 5 migraciones no destructivas operativas (`MIGRATION_5_6` a `MIGRATION_9_10`).
- **[HECHO]**: Todos los antiguos repositorios ficticios (`FakePlanningRepository`, `FakeTodayRepository`, `FakeStatsRepository`, `FakeSystemRepository`) han sido completamente eliminados. Toda la aplicación opera sobre `ActivityRepositoryImpl` y base de datos real.
- **[HECHO]**: La suite de pruebas unitarias consta de **76 tests pasando al 100%** (`testDebugUnitTest`). Compilación limpia (`assembleDebug`).

---

## 2. EC History (Historial de Engineering Cards)

| EC ID | Título | Estado Documental | Implementada en Código | Auditada | Cerrada | Qué resolvió realmente |
|---|---|---|---|---|---|---|
| **EC-000** | Project Setup & Philosophy | CLOSED | SÍ | SÍ | SÍ | Configuración inicial y framework documental. |
| **EC-001** | Data Foundation | CLOSED | SÍ | SÍ | SÍ | Base de datos Room e infra. Refactorizada en EC-005. |
| **EC-002** | Core Architecture | CLOSED | SÍ | SÍ | SÍ | Repositorios base. Refactorizada en EC-005. |
| **EC-003** | Design System Foundations | CLOSED | SÍ | SÍ | SÍ | Tokens de diseño, colores y tipografía. |
| **EC-005** | Domain Model Agnostic Refactor | CLOSED | SÍ | SÍ | SÍ | Eliminación de conceptos hardcodeados de dominio. |
| **EC-006** | Routine Dashboard Screen | APPROVED | SÍ | SÍ | SÍ | Pantalla reactiva de catálogo/dashboard de rutinas. |
| **EC-007** | Activity Creation Flow | APPROVED | SÍ | SÍ | SÍ | Creador de `ActivityDefinition`. |
| **EC-008** | Activity Detail & Nodes | APPROVED | SÍ | SÍ | SÍ | Detalle de actividad e inspector de nodos. |
| **EC-009** | Activity Execution Engine | APPROVED | SÍ | SÍ | SÍ | Registro de ejecuciones y metadatos JSON. |
| **EC-010** | Interaction Cleanup | CLOSED | SÍ | SÍ | SÍ | Limpieza de tap targets muertos. |
| **EC-011** | Scheduling Model | CLOSED | SÍ | SÍ | SÍ | `ScheduleRule`, `ScheduleException`. |
| **EC-RE-001**| Hierarchical Activity Nodes | CLOSED | SÍ | SÍ | SÍ | Arbol jerárquico de nodos de actividad. |
| **EC-RE-002**| Progressive Activity Editor | CLOSED | SÍ | SÍ | SÍ | Edición inline con Undo persistente. |
| **EC-RE-003**| Scheduling Engine | CLOSED | SÍ | SÍ | SÍ | `DailyInstance` y resolución de instancias diarias. |
| **EC-RE-004**| Flexible Rules Editor | CLOSED | SÍ | SÍ | SÍ | Editor visual de reglas y días en inspector. |
| **EC-RE-005**| Metadata Schemas | CLOSED | SÍ | SÍ | SÍ | CRUD de esquemas cuantitativos de captura. |
| **EC-RE-006**| Today Workspace | CLOSED | SÍ | SÍ | SÍ | Layout inicial de Today y captura dinámica. |
| **EC-RE-007**| Contextual Organization | CLOSED | SÍ | SÍ | SÍ | Agrupación por `LifeSystem`. |
| **EC-RE-008**| Visual Planning & Today Refinement| CLOSED | SÍ | SÍ | SÍ | Datos reales en Planning y pulido visual de Today. |
| **EC-RE-009**| Interruption Logic | CLOSED | SÍ | SÍ | SÍ | Detección preventiva de conflictos de horario. |
| **EC-RE-010**| Historical Analysis & Trends | CLOSED | SÍ | SÍ | SÍ | Motor de estadísticas y KPIs (60 tests). |
| **EC-RE-011**| Intelligence Dashboard | CLOSED | SÍ | SÍ | SÍ | Gráficas visuales de ritmo semanal y adherencia. |
| **EC-RE-012**| Planificador (Planner) MVP | CLOSED | SÍ | SÍ | SÍ | Timeline interactivo con Move, Skip y Reset. |
| **EC-RE-013**| Systems & Activities Refinement | CLOSED | SÍ | SÍ | SÍ | Sincronización real de sistemas en creador y compactación. |
| **EC-RE-014**| Project Cleanup & Consistency | CLOSED | SÍ | SÍ | SÍ | Eliminación de fakes, rutas huérfanas y use-cases muertos. |
| **EC-RE-015**| Today Execution Surface Refinement| CLOSED | SÍ | SÍ | SÍ | Espina temporal unificada 24dp, acordeón sintetizado de contexto. |
| **EC-RE-016**| Plannable Element Domain Analysis| CLOSED | SÍ | SÍ | SÍ | Análisis conceptual confirmando `DailyInstance` universal. |
| **EC-014** (Ex EC-013)| Planning Workspace Consolidation| **READY** | **NO** | NO | NO | Unificar `PlanningWorkspace` quitando sub-pestañas y el `NavHost` anidado. |

---

## 3. Implemented vs Planned (Comparación Código Real vs Documentación)

- **[HECHO]**: Prácticamente todas las ECs de la serie `EC-RE-001` a `EC-RE-016` están **completamente implementadas en código de producción**.
- **[HECHO]**: La única tarea relevante que figura en la documentación como pendiente real y que **aún no se ha ejecutado en código** es la consolidación de la pantalla de planificación (**EC-014 / EC-013: Planning Workspace Consolidation**).

---

## 4. Domain Status (Estado del Modelo de Dominio)

| Entidad / Dominio | Estado Real en Código | Evidencia en Archivo |
|---|---|---|
| **`ActivityDefinition`** | **IMPLEMENTADO** | `domain/model/ActivityDefinition.kt` / `data/local/entities/ActivityDefinitionEntity.kt` |
| **`LifeSystem`** | **IMPLEMENTADO** | `domain/model/LifeSystem.kt` / `data/local/entities/SystemEntity.kt` |
| **`ActivityNode`** | **IMPLEMENTADO** | `domain/model/ActivityNode.kt` / `data/local/entities/ActivityNodeEntity.kt` |
| **`ScheduleRule`** | **IMPLEMENTADO** | `domain/model/ScheduleRule.kt` / `data/local/entities/ScheduleRuleEntity.kt` |
| **`DailyInstance`** | **IMPLEMENTADO** | `domain/model/DailyInstance.kt` / `data/local/entities/DailyInstanceEntity.kt` (v10) |
| **`Tasks`** | **IMPLEMENTADO** | `DailyInstance` con `role = TASK` y `actionProtocol = CHECK` |
| **`Reminders`** | **IMPLEMENTADO** | `DailyInstance` con `role = REMINDER` y `actionProtocol = CHECK` |
| **`Spontaneous Events`**| **IMPLEMENTADO** | `DailyInstance` con `isAdHoc = true` |
| **`BacklogItem`** | **PARCIAL** | Entidad `BacklogItem.kt` y tabla `backlog_items` existen, pero no hay panel UI ni UseCases de asignación desde panel a agenda. |
| **`Deadline`** | **PARCIAL** | Entidad `Deadline.kt` y tabla `deadlines` existen. No hay editor CRUD dedicado en UI. |
| **`Note`** | **IMPLEMENTADO** | `domain/model/Note.kt` / `data/local/entities/NoteEntity.kt` |
| **`ActivityExecution`**| **IMPLEMENTADO** | `domain/model/ActivityExecution.kt` / `data/local/entities/ActivityExecutionEntity.kt` |

---

## 5. Planning Status (Estado del Módulo Planning)

- **[HECHO]**: `PlanningScreen.kt` y `PlanningViewModel.kt` poseen funcionalidad completa para:
  - Navegación temporal de días y semanas.
  - Intervenciones puntuales: `Move` (mover ocurrencia), `Skip` (omitir), `Reset` (deshacer intervención).
  - Creación y edición de eventos espontáneos/ad-hoc vía `SpontaneousEditorSheet`.
  - Validación preventiva de conflictos vía `SimulateMoveUseCase`.
  - Búsqueda e inclusión de actividades del catálogo en el día vía `AddActivityToDayUseCase`.
- **[HECHO - PENDIENTE REAL]**: `PlanningWorkspace.kt` **aún monta un `NavHost` anidado** con 2 sub-rutas (`"planner"` y `"activities"`) y una barra de sub-pestañas arriba (`PlanningSegmentedSelector` con `"PLANIFICADOR"` y `"ACTIVIDADES"`).
- **[HECHO - PENDIENTE REAL]**: `DashboardRoute`/`DashboardViewModel` en `feature/dashboard/` conservan el nombre `Dashboard*` en lugar del nombre preciso `ActivityCatalog*`.

---

## 6. Today Status (Estado del Módulo Today)

- **[HECHO]**: Tras `EC-RE-015`, `Today` cuenta con:
  - Riel y espina temporal unificada de 24dp.
  - Gramática visual unificada de tarjetas (`Activity` Emerald, `Task` Indigo, `Reminder` Amber).
  - Acordeón sintetizado de contexto (`ContextFooter`) con resumen compacto (`+ N tareas`) expandible.
  - Tokens de tema centralizados sin colores Hex hardcodeados.
- **Conclusión para Today**: No existen pendientes funcionales ni visuales críticos. Toda iteración adicional en Today representaría refinamientos cosméticos menores.

---

## 7. Spontaneous Events Status & Temporal Resolution (Análisis Específico)

- **Pregunta del usuario**: ¿La resolución temporal de eventos espontáneos (`min(child.start)` ➔ `max(child.end)`, inmutabilidad de rango explícito y fallback temporal) está implementada?
- **Estado**: **`YA IMPLEMENTADO`**.
- **Evidencia en Código**: En `domain/usecase/GetHierarchicalTimelineUseCase.kt` (líneas 212–252):
  ```kotlin
  // Temporal Logic V3:
  val explicitStart = entry.instance.plannedStartTime
  val childrenStart = recursiveChildren.mapNotNull { it.effectiveStartTimeMinutes }.minOrNull()
  
  // Rule: Explicit start is immutable.
  val effectiveStart = explicitStart ?: childrenStart

  // Duration Logic V3:
  val explicitDuration = entry.instance.plannedDurationMinutes
  val explicitEnd = entry.instance.plannedEndTime
  
  // Derivation only if NOT explicit range
  val childrenDuration = if (recursiveChildren.any { it.effectiveStartTimeMinutes != null || it.totalDurationMinutes != null }) {
      val start = effectiveStart
      val maxEnd = recursiveChildren.mapNotNull { child ->
          val childStart = child.effectiveStartTimeMinutes
          val childDur = child.totalDurationMinutes ?: 0
          if (childStart != null) childStart + childDur else null
      }.maxOrNull()
      
      if (start != null && maxEnd != null && maxEnd > start) maxEnd - start else 0
  } else {
      0
  }
  ```
- **Conclusión**: El motor ya respeta la inmutabilidad de horarios explícitos y realiza la derivación `min/max` automáticamente cuando el padre no posee rango explícito. **No se requiere una EC para esto**.

---

## 8. Backlog Status (Estado de Pendientes sin Fecha)

- **Estado**: **`PARCIAL`**.
- **Detalle**: La base de datos Room posee la tabla `backlog_items` y `DailyInstance` tiene la clave `backlogId`. Sin embargo, no existe un panel en Planning para visualizar la lista de pendientes ni use-cases para arrastrar/convertir un `BacklogItem` en `DailyInstance`.
- **Dependencia**: Su implementación lógica depende de que `PlanningWorkspace` esté unificado primero (EC-014).

---

## 9. Stats Status (Estado del Módulo de Estadísticas)

- **Estado**: **`IMPLEMENTADO`**.
- **Detalle**: `StatsScreen` y `StatsViewModel` consumen ejecuciones reales e historial mediante `HistoricalAnalysisEngine` y muestran KPIs, tendencias y patrones semanales.
- **Conclusión**: Limpio de referencias a la antigua vista de Systems.

---

## 10. Body Status (Estado del Módulo Body / Carga Corporal)

- **Estado**: **`SOLO MODELOS Y PROTOTIPOS HTML`**.
- **Evidencia**: `BodyLoadModels.kt` contiene los enums `BodyZone` y `BodyLoadLevel`. Existen maquetas en `documentation/REDESIGN/STITCH_V3/stats/body/`.
- **Conclusión**: No hay lógica de cálculo ni renderizado Canvas/SVG en el código fuente de la app. El módulo `Body` fue diferido explícitamente para fases futuras de métricas fisiológicas cuando la captura de esquemas cuantitativos tenga suficiente volumen.

---

## 11. Open Questions (Preguntas Abiertas de Dominio)

- **[HECHO]**: La mayoría de dudas sobre la naturaleza de `DailyInstance`, `Task`, `Reminder`, `Note`, `Deadline` y `BacklogItem` quedaron cerradas y formalizadas en el mapa de decisiones de **`EC-RE-016`** (`DEC-01` a `DEC-08`).
- **Pregunta Abierta Restante**:
  - `Q1`: ¿Debería el panel de Backlog (futuro) permitir crear `BacklogItem`s directamente desde el catálogo de actividades?

---

## 12. Obsolete Roadmap Items (Residuos Documentales Obsoletos)

- **`EC-013_PLANNING_CONSOLIDATION.md`**: Existe duplicado con `EC-014_PLANNING_CONSOLIDATION.md`. Debe usarse la numeración `EC-014` para evitar colisión histórica.
- **`EC-RE-017 Spontaneous Event Temporal Resolution`**: Obsoleto/innecesario pues la derivación e inmutabilidad temporal ya están escritas en `GetHierarchicalTimelineUseCase.kt`.

---

## 13. Candidate Next ECs (Candidatos Evaluados)

1. **Opción A: EC-014 — Planning Workspace Consolidation**
   - **Objetivo**: Unificar `PlanningWorkspace.kt` en una sola pantalla (`PlanningScreen`) sin sub-pestañas (`PlanningSegmentedSelector` + `NavHost` anidado). Exponer el catálogo de actividades completo en un `ModalBottomSheet` desde el FAB. Renombrar `DashboardRoute`/`ViewModel` a `ActivityCatalogRoute`/`ViewModel`.
   - **Esfuerzo**: Pequeño / Mediano (Surgical UI refactor en `feature/planning/` y `feature/dashboard/`).
   - **Justificación**: Es el único pendiente estructural pendiente del MVP de Planning antes de agregar paneles accesorios.

2. **Opción B: EC-RE-018 — Backlog & Unscheduled Items Panel**
   - **Objetivo**: Crear el panel lateral/inferior de pendientes para listar `BacklogItem`s y asignarlos a la agenda diaria.
   - **Dependencia**: Requiere que `PlanningWorkspace` esté unificado primero (EC-014).

3. **Opción C: EC-RE-017 — Spontaneous Event Temporal Resolution**
   - **Evaluación**: **Descartado**. Ya se encuentra implementado en `GetHierarchicalTimelineUseCase.kt`.

---

## 14. Recommended Next EC (Siguiente Trabajo Recomendado)

### **RECOMENDACIÓN**: **`EC-014 — Planning Workspace Consolidation`**

- **¿Por qué este es el siguiente trabajo real?**:
  Porque la pantalla de `Planning` todavía presenta la fricción de sub-pestañas arriba (`PLANIFICADOR` y `ACTIVIDADES`), dividiendo la experiencia de planificar en dos pantallas separadas cuando debería ser una única superficie de trabajo donde el catálogo de actividades sirve como panel/sheet de soporte.
- **¿Ya existe en código?**: **NO**.
- **¿Requiere cambios de dominio o DB?**: **NO**. Es un refactor puramente de presentación y organización de rutas en Compose.

---

## 15. Summary & Next Step Directives

```text
NEXT STEP:
EC-014 — Planning Workspace Consolidation

REASON:
Es el único pendiente real documentado y de código en el módulo de Planning. Unificará la pantalla eliminando las sub-pestañas superiores ("PLANIFICADOR" / "ACTIVIDADES") y el NavHost anidado en PlanningWorkspace.kt, exponiendo el catálogo vía ModalBottomSheet desde el FAB.

ALREADY EXISTS?:
NO (PlanningWorkspace.kt aún conserva la sub-navegación por pestañas y el NavHost interno).

IMPLEMENTATION NEEDED?:
SÍ (Refactor quirúrgico de UI en feature/planning/ y renombrado semántico de Dashboard* -> ActivityCatalog*).
```
