---
id: EC-013
title: Planning Workspace Consolidation
phase: 5
priority: High
effort: Large
owner: AI Agent
status: DRAFT
depends_on: [EC-010, EC-012]
branch: feature/planning-consolidation
audit: Pending
created: 2026-08-09
updated: 2026-08-09
---

# EC-013: Planning Workspace Consolidation

## Objetivo
Eliminar la navegación por pestañas (`PlanningSegmentedSelector` + `NavHost`
anidado de 3 rutas) dentro de `Planning`, unificando el módulo en una sola
vista de trabajo que integre el catálogo de actividades como panel de soporte
y saque la vista "Sistemas" del flujo de planificación diaria.

## Decisión de Diseño: Dashboard vs. Catalog
Se decide renombrar `Dashboard*` a `ActivityCatalog*` para resolver una
ambigüedad semántica: el término "Dashboard" se reserva para la pantalla de
inicio unificada (futura), mientras que la funcionalidad actual representa un
inventario o "Catálogo" de actividades disponibles para ser planificadas.

## Problema
1. `PlanningWorkspace.kt` monta un `NavHost` interno con 3 rutas
   (`AppRoutes.Planner`, `.Activities`, `.Systems`) navegadas vía
   `PlanningSegmentedSelector` — tratamiento de "pantallas separadas" para lo
   que son tres lentes sobre el mismo dato.
2. La pestaña "ACTIVIDADES" renderiza `DashboardRoute` — el único flujo ya
   conectado a `ActivityRepository` real — pero su nombre de clase
   (`Dashboard*`) no refleja su función real (catálogo de actividades), lo
   que genera confusión de ahora en adelante.
3. `PlanningScreen.kt` ya tiene una sección `"SIN HORARIO"` con
   `PlanningUnscheduledCard` — el panel de soporte que el análisis propone ya
   está parcialmente construido, pero corre en paralelo a la pestaña
   "ACTIVIDADES" completa en vez de reemplazarla.
3. La pestaña "SISTEMAS" (`SystemRoute`/`SystemViewModel`, sobre
   `FakeSystemRepository`) mezcla una vista de reflexión/balance con una
   herramienta de acción, al mismo nivel jerárquico que "asignar tiempo".
4. `PlanningScreen` tiene `FloatingActionButton(onClick = { })` — mismo
   defecto que `10_INTERACTION_INVARIANTS.md` ya prohíbe.

## Alcance
- [ ] Eliminar `PlanningSegmentedSelector` y el `NavHost` anidado de
      `PlanningWorkspace.kt`. `Planning` pasa a ser una única pantalla
      (`PlanningScreen`) sin sub-navegación por pestañas.
- [ ] Completar la sección "SIN HORARIO" de `PlanningScreen` para que sea el
      único punto de acceso al catálogo de actividades — reemplaza a la
      pestaña "ACTIVIDADES" completa, no coexiste con ella.
- [ ] Exponer el catálogo completo (cuando "SIN HORARIO" no alcance, p. ej.
      demasiadas actividades para una fila) vía `ModalBottomSheet` accionado
      desde el FAB, según `10_INTERACTION_INVARIANTS.md` — no vía navegación
      de pantalla completa.
- [ ] Renombrar `DashboardRoute`/`DashboardViewModel` a un nombre que refleje
      su función real (p. ej. `ActivityCatalog*`) para eliminar la
      confusión de identidad señalada en el Problema #2. Este es un rename
      mecánico (sin cambios de lógica) — aislarlo en su propio commit dentro
      de la misma rama para que el diff de renombre no oculte cambios de
      comportamiento.
- [ ] Retirar la vista "Sistemas" de `Planning`. Decisión de producto
      pendiente de confirmación del usuario: moverla a `Stats` (ya existe
      como top-level route) o a una futura pantalla "Inicio". Hasta que se
      confirme, dejar `SystemRoute` fuera de `PlanningWorkspace` sin eliminar
      el código (puede quedar sin ruta que lo monte, documentado como tal en
      `05_ROADMAP.md` — a diferencia del caso de Dashboard, acá si es
      intencional dejarlo temporalmente sin ruta).
- [ ] Implementar el FAB de `PlanningScreen` (abre el bottom sheet del
      catálogo) o retirarlo si el punto anterior lo vuelve redundante — no
      puede quedar `onClick = { }`.
- [ ] Exclusión explícita: no se resuelve en esta EC dónde termina viviendo
      "Sistemas" definitivamente — solo se saca de `Planning`. La ubicación
      final es una EC de UX de Home/Stats aparte.
- [ ] Exclusión explícita: no se implementa drag-to-reschedule de bloques en
      el timeline — sigue siendo trabajo de una EC de interacción posterior
      específica de `PlanningTimeBlock`.

## Archivos Afectados
- [ ] `feature/planning/PlanningWorkspace.kt` (MODIFY — quitar NavHost/selector)
- [ ] `feature/planning/PlanningScreen.kt` (MODIFY — FAB real + bottom sheet catálogo)
- [ ] `feature/planning/PlanningViewModel.kt` (MODIFY — exponer estado de catálogo inline)
- [ ] `feature/dashboard/DashboardRoute.kt` (RENAME)
- [ ] `feature/dashboard/DashboardViewModel.kt` (RENAME)
- [ ] `feature/system/SystemRoute.kt` (MODIFY — desmontar de Planning, sin eliminar)
- [ ] `core/navigation/AppRoutes.kt` (MODIFY — retirar `Activities`/`Systems` como sub-rutas de Planning)
- [ ] `documentation/05_ROADMAP.md` (MODIFY — registrar decisión sobre Dashboard)

## Plan de Implementación
1. Completar "SIN HORARIO" en `PlanningScreen` conectado a datos reales
   (mismo `ActivityRepository` que ya usa `DashboardViewModel`).
2. Implementar el bottom sheet de catálogo completo, accesible desde el FAB.
3. Quitar `PlanningSegmentedSelector` y el `NavHost` anidado de
   `PlanningWorkspace`.
4. Desmontar `SystemRoute` de `Planning` (commit separado).
5. Renombrar `Dashboard*` → `ActivityCatalog*` (commit separado, sin lógica).
6. Verificación manual: crear y asignar una actividad sin salir de la
   pantalla de Planning.

## Validaciones
- [ ] Verificación manual: agregar una actividad al plan sin navegar fuera de
      `PlanningScreen` (ni pantalla nueva ni pestaña).
- [ ] Confirmar que "Sistemas" ya no es accesible desde `Planning`.
- [ ] Confirmar que no queda ningún `onClick = { }` en los archivos tocados.
- [ ] El rename de `Dashboard*` no introduce cambios de comportamiento
      (verificable revisando que su commit es diff de solo renombre).
- [ ] `git diff` real adjunto al reporte de cierre.

## Resultado Esperado
`Planning` es una única superficie de trabajo: asignar tiempo y elegir qué
actividad asignar ocurren sin cambiar de pantalla. "Sistemas" deja de competir
por atención con la tarea de planificar. El componente que antes se llamaba
"Dashboard" tiene un nombre que refleja lo que hace.

## Auditoría
Consultar guía en [AUDITS/README.md](../AUDITS/README.md).
- [ ] ¿Cumple con la arquitectura MVVM/Clean?
- [ ] ¿Las funciones son < 30 líneas?
- [ ] ¿Los archivos son < 300 líneas?
- [ ] ¿Se verificó el checklist completo de
      [10_INTERACTION_INVARIANTS.md](../10_INTERACTION_INVARIANTS.md)?
- [ ] ¿El rename de Dashboard quedó aislado en un commit sin lógica mezclada?

## Lecciones Aprendidas
(Espacio para documentar hallazgos, dificultades o mejoras descubiertas durante
el desarrollo).

## Definition of Done (Obligatorio)
Antes de marcar como APPROVED, verificar:
- [ ] Compila sin warnings nuevos
- [ ] Tests existentes pasan (unitarios + los que aplique)
- [ ] Checklist de [ARCHITECTURE_INVARIANTS.md](../08_ARCHITECTURE_INVARIANTS.md) revisado y sin violaciones
- [ ] Checklist de [10_INTERACTION_INVARIANTS.md](../10_INTERACTION_INVARIANTS.md) revisado y sin violaciones
- [ ] Decisión pendiente sobre destino final de "Sistemas" registrada en
      [05_ROADMAP.md](../05_ROADMAP.md)
- [ ] Lecciones aprendidas documentadas arriba
- [ ] `git diff` real (no resumen) adjunto al reporte de cierre

## Checklist de Validación de Usuario
(El agente auditor completa esta sección SOLO cuando el estado pasa a
USER_REVIEW_PENDING. Lenguaje llano, sin jerga técnica.)
- [ ] Abrir Planning y no ver pestañas arriba (PLANIFICADOR/ACTIVIDADES/SISTEMAS).
- [ ] Poder elegir una actividad del catálogo y asignarla a un día sin salir
      de la pantalla de planificación.
- [ ] No encontrar la vista de "Sistemas" dentro de Planning.

REGLA CRÍTICA: ningún agente puede marcar estos checkboxes ni cambiar el
estado a APPROVED. Solo el usuario lo hace manualmente.

## Estado
El estado vigente de esta EC es el declarado en el campo `status` del
frontmatter (arriba de este documento). No dupliques el valor aquí.
