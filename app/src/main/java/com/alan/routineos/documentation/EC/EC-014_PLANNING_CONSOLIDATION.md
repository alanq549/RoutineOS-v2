---
id: EC-014
title: Planning Workspace Consolidation
phase: 6
priority: High
effort: Large
owner: AI Agent
status: READY
depends_on: [EC-RE-012, EC-RE-013]
branch: feature/ec-014-planning-consolidation
audit: Pending
created: 2026-09-06
updated: 2026-09-06
---

# EC-014: Planning Workspace Consolidation

## Objetivo
Eliminar la navegación por pestañas (`PlanningSegmentedSelector` + `NavHost` anidado de 3 rutas) dentro de `Planning`, unificando el módulo en una sola vista de trabajo que integre el catálogo de actividades como panel de soporte y saque la vista "Sistemas" del flujo de planificación diaria.

## Decisión de Diseño: Dashboard vs. Catalog
Se decide renombrar `Dashboard*` a `ActivityCatalog*` para resolver una ambigüedad semántica: el término "Dashboard" se reserva para la pantalla de inicio unificada (futura), mientras que la funcionalidad actual representa un inventario o "Catálogo" de actividades disponibles para ser planificadas.

## Problema
1. `PlanningWorkspace.kt` monta un `NavHost` interno con 3 rutas (`AppRoutes.Planner`, `.Activities`, `.Systems`) navegadas vía `PlanningSegmentedSelector` — tratamiento de "pantallas separadas" para lo que son tres lentes sobre el mismo dato.
2. La pestaña "ACTIVIDADES" renderiza `DashboardRoute` — el flujo conectado a `ActivityRepository` real — pero su nombre de clase (`Dashboard*`) no refleja su función real (catálogo de actividades).
3. La pestaña "SISTEMAS" (`SystemRoute`/`SystemViewModel`) mezcla una vista de reflexión/balance con una herramienta de acción, al mismo nivel jerárquico que "asignar tiempo".
4. `PlanningScreen` tiene `FloatingActionButton(onClick = { })` desacoplado.

## Alcance
- [ ] Eliminar `PlanningSegmentedSelector` y el `NavHost` anidado de `PlanningWorkspace.kt`. `Planning` pasa a ser una única pantalla (`PlanningScreen`) sin sub-navegación por pestañas.
- [ ] Completar la sección "SIN HORARIO" de `PlanningScreen` para que sea el único punto de acceso directo al catálogo de actividades.
- [ ] Exponer el catálogo completo vía `ModalBottomSheet` accionado desde el FAB en la pantalla de planificación.
- [ ] Renombrar `DashboardRoute`/`DashboardViewModel` a `ActivityCatalog*` para eliminar la confusión semántica de identidad.
- [ ] Retirar la vista "Sistemas" del flujo interno de `Planning`.

## Archivos Afectados
- [ ] `feature/planning/PlanningWorkspace.kt` (MODIFY — quitar NavHost/selector)
- [ ] `feature/planning/PlanningScreen.kt` (MODIFY — FAB real + bottom sheet catálogo)
- [ ] `feature/planning/PlanningViewModel.kt` (MODIFY — exponer estado de catálogo inline)
- [ ] `feature/dashboard/DashboardRoute.kt` (RENAME -> `ActivityCatalogRoute.kt`)
- [ ] `feature/dashboard/DashboardViewModel.kt` (RENAME -> `ActivityCatalogViewModel.kt`)
- [ ] `core/navigation/AppRoutes.kt` (MODIFY — retirar sub-rutas de Planning)

## Validaciones
- [ ] Verificación manual: agregar una actividad al plan sin navegar fuera de `PlanningScreen`.
- [ ] Confirmar que "Sistemas" ya no es accesible desde `Planning`.
- [ ] Confirmar que no queda ningún `onClick = { }` en los archivos tocados.
- [ ] Compilación limpia (`assembleDebug`) y tests unitarios ejecutados.
