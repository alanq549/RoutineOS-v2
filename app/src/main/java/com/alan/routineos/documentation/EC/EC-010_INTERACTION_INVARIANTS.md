---
id: EC-010
title: Interaction Invariants Refactor
phase: 4
priority: High
effort: Medium
owner: AI Agent
status: READY
depends_on: EC-009
branch: feature/ec-010-interaction-invariants
audit: Pending
created: 2026-08-09
updated: 2026-08-09
---

# EC-010: Interaction Invariants Refactor

## Objetivo
Refactorizar las vistas de ejecución (`TodayScreen`, `TimelineItemCard`, `NodeRow`, `PlanningTimeBlock`) para cumplir con los **Invariantes de Interacción** definidos en [10_INTERACTION_INVARIANTS.md](../10_INTERACTION_INVARIANTS.md), eliminando placeholders, navegación innecesaria y confirmaciones bloqueantes.

## Contexto
Tras una auditoría técnica, se detectó que la UI heredó patrones de navegación de los mockups de Stitch que no son óptimos para una aplicación de uso diario. Acciones frecuentes como "completar nodo" requieren navegar múltiples pantallas, lo cual viola el principio de baja carga cognitiva para acciones frecuentes.

## Problema
- `TodayScreen` tiene un FAB con `onClick = {}`.
- `PlanningTimeBlock` tiene un menú "..." con `onClick = {}`.
- `TimelineItemCard` y `NodeRow` (dentro de `TimelineItemCard`) no son interactivos (faltan `clickable`).
- La acción de "completar nodo" solo es accesible desde `ActivityDetailScreen`, obligando al usuario a navegar fuera de su contexto de ejecución.

## Decisión de Arquitectura
Aplicar el patrón de **Tap directo** y **Swipe/Actions in-situ**. El ViewModel de la pantalla de ejecución debe exponer los casos de uso del repositorio directamente. Se usará `Snackbar` con acción "Deshacer" para reemplazar los `AlertDialog`.

## Alcance
- [ ] Implementar acción de "completar nodo" vía tap directo en `TimelineItemCard` y `NodeRow` dentro de pantallas de ejecución.
- [ ] Refactorizar `TodayScreen`: el FAB debe disparar la creación rápida o la acción primaria sin navegación si es posible (o abrir un Bottom Sheet).
- [ ] Implementar menús contextuales o acciones de swipe para "saltar" y "posponer" en items de la línea de tiempo.
- [ ] Sustituir cualquier diálogo de confirmación por ejecución inmediata + Snackbar con deshacer.
- [ ] Eliminar o deshabilitar todos los `onClick = {}` y placeholders `TODO()`.
- [ ] Integrar `ActivityDetailViewModel.completeNode()` o equivalente en los ViewModels de ejecución correspondientes.

## Archivos Afectados
- `app/src/main/java/com/alan/routineos/feature/today/TodayScreen.kt`
- `app/src/main/java/com/alan/routineos/feature/today/components/TimelineItemCard.kt`
- `app/src/main/java/com/alan/routineos/feature/planning/components/PlanningTimeBlock.kt`
- `app/src/main/java/com/alan/routineos/feature/today/TodayViewModel.kt`
- `app/src/main/java/com/alan/routineos/documentation/10_INTERACTION_INVARIANTS.md` (Referencia)

## Plan de Implementación
1. Auditar todos los tap targets en `TodayScreen` y `PlanningView`.
2. Habilitar la acción de completar nodo en la vista de lista/timeline.
3. Implementar el feedback visual inmediato y el Snackbar de deshacer.
4. Limpiar los callbacks vacíos heredados de los mockups.
5. Verificar cumplimiento del checklist de [10_INTERACTION_INVARIANTS.md](../10_INTERACTION_INVARIANTS.md).

## Validaciones
- [ ] La acción primaria (completar) ocurre en 1 tap sin cambiar de pantalla.
- [ ] No existen diálogos de confirmación para completar/saltar.
- [ ] Todos los botones visibles son funcionales o están explícitamente deshabilitados.

## Resultado Esperado
Una experiencia de usuario fluida donde las acciones diarias ocurren con fricción cero, cumpliendo con los estándares de diseño de RoutineOS.

## Definition of Done
- [ ] Sin `onClick = {}` en componentes de ejecución.
- [ ] Snackbar con deshacer funcionando para acciones reversibles.
- [ ] Checklist de [10_INTERACTION_INVARIANTS.md](../10_INTERACTION_INVARIANTS.md) aprobado.
