---
id: EC-RE-017
title: Planning Workspace Consolidation
phase: 6
priority: High
effort: Medium
owner: AI Agent
status: CLOSED
depends_on: [EC-RE-016]
branch: feature/ec-re-017-planning-consolidation
audit: Approved
created: 2026-09-06
updated: 2026-09-06
---

# EC-RE-017: Planning Workspace Consolidation

> **Nota Histórica de Numeración**: Esta tarjeta corresponde al pendiente documental previamente registrado de forma inconsistente como `EC-013 / EC-014 — Planning Workspace Consolidation`. Se asigna la numeración oficial **`EC-RE-017`** para mantener la secuencia lineal tras `EC-RE-016`.

> **REGLA DE ORO DE ESTA EC**: Esta tarjeta se enfoca exclusivamente en la **consolidación de navegación y la integración del catálogo en UI**. No se modifica la capa de dominio, esquemas de Room DB, migraciones, DAOs, ni contratos de ejecución.

---

## 1. Objective (Objetivo)

Consolidar el módulo `PlanningWorkspace` en una única superficie de trabajo continua, eliminando la navegación por pestañas (`PlanningSegmentedSelector` + `NavHost` anidado de 2 rutas) dentro de `Planning`. El catálogo de actividades (`ActivityCatalog`) coexistirá de forma contextual como un panel desplegable (`ModalBottomSheet`) accesible desde la pantalla de planificación, sin convertir el catálogo en una ruta top-level independiente ni romper la navegación unificada del sistema.

---

## 2. Current Architecture (Arquitectura Actual)

- **[HECHO]**: La navegación principal en `AppNavHost.kt` monta `PlanningWorkspace` para la ruta top-level `AppRoutes.Planning.route` (`"planning_graph"`).
- **[HECHO]**: Dentro de `PlanningWorkspace.kt`, existe un `NavHost` interno secundario con dos sub-rutas de texto:
  - `"planner"`: Renderiza `PlanningRoute()` -> `PlanningScreen()`.
  - `"activities"`: Renderiza `DashboardRoute()` -> `DashboardScreen()`.
- **[HECHO]**: La barra superior de `PlanningWorkspace.kt` incluye un selector de segmentos (`PlanningSegmentedSelector`) con los botones `"PLANIFICADOR"` y `"ACTIVIDADES"`.
- **[HECHO]**: La pantalla de `PlanningScreen.kt` ya cuenta con el caso de uso `AddActivityToDayUseCase` e interfaz para buscar y vincular actividades a la agenda del día.

```text
ESTADO ACTUAL (Duplicación de Navegación):

AppNavHost ("planning_graph")
└── PlanningWorkspace.kt
    ├── TopBar: PlanningSegmentedSelector ("PLANIFICADOR" / "ACTIVIDADES")
    └── Nested NavHost
        ├── composable("planner") -> PlanningRoute()
        └── composable("activities") -> DashboardRoute()
```

---

## 3. Current PlanningWorkspace Audit (Auditoría del Código Real)

| Archivo | Responsabilidad Actual | Problema / Diagnóstico | Cambio Propuesto |
|---|---|---|---|
| **`PlanningWorkspace.kt`** | Monta el `NavHost` secundario y el `PlanningSegmentedSelector`. | Genera dos pantallas completas separadas para lo que es la misma tarea operativa (planificar). | **REFACTOR**: Eliminar el `NavHost` anidado y el `PlanningSegmentedSelector`. Delegar directamente la pantalla de `PlanningRoute()`. |
| **`PlanningScreen.kt`** | Timeline de planificación diaria, intervención de eventos e integración de hojas ad-hoc. | El FAB actual abre el editor de eventos ad-hoc, pero no da acceso directo al catálogo de actividades. | **MODIFY**: Agregar un botón/acción secundaria o sheet en el FAB para desplegar el catálogo de actividades como `ModalBottomSheet`. |
| **`DashboardRoute.kt`** | Punto de entrada del catálogo de actividades. | Utiliza el nombre ambiguo `Dashboard*` en lugar del concepto preciso `ActivityCatalog*`. | **RENAME**: Renombrar a `ActivityCatalogRoute.kt` (sin alterar lógica). |
| **`DashboardViewModel.kt`**| ViewModel del catálogo de actividades. | Nombre ambiguo `DashboardViewModel`. | **RENAME**: Renombrar a `ActivityCatalogViewModel.kt` (sin alterar lógica). |
| **`DashboardScreen.kt`** | UI del catálogo de actividades con tarjetas y filtro de sistemas. | Vive como pantalla completa en una sub-ruta del `NavHost` anidado. | **REFACTOR**: Permitir su renderizado dentro de un `ModalBottomSheet` para selección e inserción directa en Planning. |
| **`AppRoutes.kt`** | Rutas registradas en la aplicación. | Limpio tras la EC-RE-014 (ya no contiene `Planner` ni `Activities`). | **CONSERVAR**: Mantener intacto. |
| **`MainActivity.kt`** | Barra de navegación inferior (`RoutineBottomBar`). | Limpio tras la EC-RE-014. | **CONSERVAR**: Mantener intacto. |

---

## 4. Redundant Navigation Analysis (Análisis de Navegación Redundante)

- **[HECHO]**: Tratar el Catálogo de Actividades como una sub-pantalla completa navegable paralelamente a la planificación fuerza al usuario a cambiar de contexto continuamente (ir a "ACTIVIDADES", ver la rutina, regresar a "PLANIFICADOR", seleccionar el día y asignarla).
- **[DECISIÓN - DEC-01]**: La planificación de actividades es una acción sobre el día. El catálogo es un panel de soporte/herramienta. Por tanto, el catálogo debe desplegarse como un componente contextual (`ModalBottomSheet`) desde la misma pantalla de planificación.

---

## 5. Target UX Structure (Estructura UX Objetivo)

```text
ESTADO OBJETIVO (Superficie Única Consolidada):

AppNavHost ("planning_graph")
└── PlanningWorkspace.kt (o PlanningRoute)
    └── PlanningScreen.kt
        ├── TopBar Header ("Planificar")
        ├── Week Calendar Header (PlanningWeekHeader)
        ├── Cronograma del Día (PlanningTimeBlock)
        ├── Secciones de Ajustes y Excepciones
        ├── FAB de Acción -> Despliega Catálogo de Actividades (ActivityCatalogSheet)
        └── SpontaneousEditorSheet (Edición/Creación Ad-Hoc)
```

---

## 6. Component Mapping (Mapeo de Componentes Reutilizados)

1. **`PlanningScreen.kt`**: Pasa a ser la única superficie visible en la ruta de planificación.
2. **`PlanningViewModel.kt`**: Mantiene todas sus capacidades interactivas (`Move`, `Skip`, `Reset`, `onLinkToDefinition`, `AddActivityToDayUseCase`).
3. **`ActivityCatalogScreen`** (Ex `DashboardScreen`): Se reutiliza íntegramente dentro de un `ModalBottomSheet` (`ActivityCatalogSheet`) para que el usuario pueda explorar, filtrar por sistema y seleccionar una actividad para añadirla al plan del día.

---

## 7. Navigation Changes (Cambios en Navegación)

- **[HECHO]**: La navegación de nivel superior se mantiene estrictamente en 4 pestañas:
  1. `Today` (`"today"`)
  2. `Planning` (`"planning_graph"`)
  3. `Stats` (`"stats"`)
  4. `Account` (`"account"`)
- **[DECISIÓN - DEC-02]**: No se introducirá ninguna sub-ruta de navegación top-level para `Activities` ni para `Systems`. `Systems` permanece dentro del catálogo como filtro contextual.

---

## 8. Activity Catalog Integration (Integración Contextual del Catálogo)

- **[DECISIÓN - DEC-03]**: Al presionar la acción de agregar desde catálogo en `PlanningScreen`, se abre un `ModalBottomSheet` (`ActivityCatalogSheet`).
- Al hacer clic en una tarjeta de actividad dentro del sheet:
  1. Se invoca `AddActivityToDayUseCase` asignando la actividad al `selectedDate` del planificador.
  2. Se cierra el sheet automáticamente.
  3. La actividad aparece inmediatamente en la sección "SIN HORARIO" o en el timeline del día en `PlanningScreen`.

---

## 9. Domain Invariants Preserved (Invariantes de Dominio Preservadas)

- **[HECHO]**: Cero modificaciones a la base de datos Room v10 o migraciones.
- **[HECHO]**: Cero modificaciones a los modelos de dominio: `ActivityDefinition`, `ActivityNode`, `ScheduleRule`, `DailyInstance`, `ActivityExecution`, `BacklogItem`, `Deadline`, `Note`, `LifeSystem`, `DailyInstanceRole`, `ActionProtocol`.
- **[HECHO]**: Cero modificaciones a contratos de repositorios o DAOs.

---

## 10. Non-Goals (Exclusiones Explícitas)

- No se reconstruye la pantalla de `Planning` (su lógica y timeline de intervenciones ya pasaron auditoría en `EC-RE-012`).
- No se implementa reordenamiento por arrastre (*drag-to-reschedule*).
- No se modifica el módulo `Today` ni el módulo `Stats`.
- No se altera la estructura de `LifeSystem` ni el modelo de datos de `ActivityDefinition`.

---

## 11. Implementation Steps (Pasos de Implementación)

1. **Renombrado Semántico (Paso Ailado sin Cambios de Lógica)**:
   - Renombrar `DashboardRoute.kt` ➔ `ActivityCatalogRoute.kt`.
   - Renombrar `DashboardViewModel.kt` ➔ `ActivityCatalogViewModel.kt`.
   - Renombrar `DashboardScreen.kt` ➔ `ActivityCatalogScreen.kt`.
2. **Consolidación de `PlanningWorkspace.kt`**:
   - Eliminar `PlanningSegmentedSelector` y el `NavHost` anidado.
   - Conectar `PlanningWorkspace` directamente a `PlanningRoute()`.
3. **Integración de `ActivityCatalogSheet`**:
   - Crear un `ModalBottomSheet` en `PlanningScreen.kt` que renderice `ActivityCatalogScreen`.
   - Conectar el callback de selección de actividad para invocar `onLinkToDefinition` / `AddActivityToDayUseCase` e insertar la actividad en el día seleccionado.
4. **Verificación y Pruebas**:
   - Confirmar compilación sin advertencias (`assembleDebug`).
   - Ejecutar la suite de pruebas unitarias (`testDebugUnitTest`).

---

## 12. Verification Plan (Plan de Verificación)

- **Navegación Unificada**: Abrir la pestaña `Planning` en la app y verificar que se muestra directamente el timeline sin sub-pestañas superiores (`PLANIFICADOR` / `ACTIVIDADES`).
- **Inclusión Contextual desde Catálogo**: Abrir el FAB/Catálogo, seleccionar una actividad existente y confirmar que se agrega al plan del día seleccionado sin cambiar de pantalla.
- **Pruebas Automatizadas**:
  ```powershell
  .\gradlew.bat assembleDebug
  .\gradlew.bat testDebugUnitTest
  ```

---

## 13. Acceptance Criteria (Criterios de Aceptación)

- [ ] `PlanningWorkspace` es una única superficie sin `NavHost` anidado ni `PlanningSegmentedSelector`.
- [ ] No existen sub-rutas `"planner"` ni `"activities"` en la navegación.
- [ ] La navegación top-level se mantiene intacta (`Today`, `Planning`, `Stats`, `Account`).
- [ ] El catálogo de actividades se despliega contextualmente mediante `ModalBottomSheet` en `PlanningScreen`.
- [ ] Seleccionar una actividad la asigna inmediatamente al día seleccionado.
- [ ] Renombrado semántico de `Dashboard*` a `ActivityCatalog*` completado sin alterar lógica.
- [ ] Cero cambios en la capa de dominio, base de datos Room o migraciones.
- [ ] `assembleDebug` PASS.
- [ ] `testDebugUnitTest` PASS.

---

## 14. Migration & Naming Considerations

- **[DECISIÓN - DEC-04]**: El renombrado de `Dashboard*` a `ActivityCatalog*` es puramente cosmético a nivel de presentación en UI para eliminar la ambigüedad del término "Dashboard" (que se reserva para la pantalla de inicio global en fases futuras). No afecta la estructura de clases del dominio ni la persistencia de datos.
