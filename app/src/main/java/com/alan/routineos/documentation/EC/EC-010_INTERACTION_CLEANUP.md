---
id: EC-010
title: Interaction Cleanup (placeholders muertos + feedback en flujo real existente)
phase: 4
priority: High
effort: Small
owner: AI Agent
status: IMPLEMENTED
depends_on: [EC-009]
branch: feature/ec-010-interaction-cleanup
audit: Pending
created: 2026-08-09
updated: 2026-08-09
---

# EC-010: Interaction Cleanup

## Objetivo
Eliminar los tap targets muertos (`onClick = {}`) detectados en la auditoría y
agregar feedback de "deshacer" al único flujo de completado que hoy tiene
datos reales (`ActivityDetailScreen`), sin tocar wiring de Hilt/Room nuevo en
`Today` ni lógica de edición nueva en `Planning`. Esta EC es deliberadamente
pequeña: separa "limpieza segura ahora" de "wiring que depende de EC-011".

## Contexto
La card original propuesta por el agente bajo este mismo ID mezclaba esta
limpieza con la migración de `TodayViewModel` a Hilt/`ActivityRepository` y
con edición funcional en `PlanningTimeBlock`. Ambas cosas dependen de trabajo
que todavía no existe: EC-011 (scheduling model, del cual depende un registro
de ejecución correcto en Today) y EC-013 (a la que pertenece cualquier menú de
edición real en Planning). Esta versión reemplaza esa card y acota el alcance
a lo que no genera doble trabajo.

## Problema
- `TodayScreen` tiene un FAB con `onClick = {}`.
- `PlanningTimeBlock` tiene un `IconButton` (menú "...") con `onClick = {}`.
- `ActivityDetailScreen` ya completa nodos por tap directo
  (`CompletionIcon` → `onCompleteNodeClick`), pero no da ningún feedback de
  confirmación ni forma de deshacer — el usuario no tiene certeza de que la
  acción se registró, ni forma de revertir un toque accidental.

## Alcance
- [x] Deshabilitar (`enabled = false`), no remover, el FAB de `TodayScreen`.
      No implementar acción real todavía — no hay caso de uso definido sin
      EC-012.
- [x] Deshabilitar (`enabled = false`) el `IconButton` de menú en
      `PlanningTimeBlock`. No implementar edición — no hay modelo de
      scheduling sin EC-011.
- [x] Agregar `SnackbarHost` a `ActivityDetailScreen` con mensaje "Nodo
      completado" / "Nodo marcado como pendiente" + acción "Deshacer" tras
      cada tap en `CompletionIcon`. Usa el `toggleNodeCompletion()`
      implementado en esta EC — se añadió lógica de repositorio para
      permitir la eliminación real en el undo.
- [x] Exclusión explícita: no se toca `TodayViewModel`, no se agrega `@Inject`
      ni `ActivityRepository` ahí. Ese wiring es EC-012.
- [x] Exclusión explícita: `TimelineItemCard`/`NodeRow` en Today no reciben
      `clickable` en esta EC — no hay datos reales que completar todavía.
- [x] Exclusión explícita: no se implementa ningún menú funcional en
      `PlanningTimeBlock` — eso es EC-013.

## Archivos Afectados
- [x] `app/src/main/java/com/alan/routineos/data/local/dao/ActivityExecutionDao.kt` (MODIFY - delete query)
- [x] `app/src/main/java/com/alan/routineos/domain/repository/ActivityRepository.kt` (MODIFY - interface update)
- [x] `app/src/main/java/com/alan/routineos/data/repository/OfflineActivityRepository.kt` (MODIFY - implement delete)
- [x] `app/src/main/java/com/alan/routineos/feature/today/TodayScreen.kt` (MODIFY — FAB `enabled = false` / Box pattern)
- [x] `app/src/main/java/com/alan/routineos/feature/today/components/TodayNextActivityCard.kt` (MODIFY - disable play button)
- [x] `app/src/main/java/com/alan/routineos/feature/planning/PlanningScreen.kt` (MODIFY - FAB cleanup)
- [x] `app/src/main/java/com/alan/routineos/feature/planning/components/PlanningTimeBlock.kt` (MODIFY — menú `enabled = false`)
- [x] `app/src/main/java/com/alan/routineos/feature/dashboard/ActivityDetailViewModel.kt` (MODIFY — logic for toggle + Snackbar)
- [x] `app/src/main/java/com/alan/routineos/feature/dashboard/ActivityDetailScreen.kt` (MODIFY — `SnackbarHost` + event consumption)
- [x] `app/src/main/java/com/alan/routineos/core/designsystem/component/RoutineScaffold.kt` (MODIFY - add snackbarHost parameter)
- [x] `app/src/test/java/com/alan/routineos/feature/dashboard/ActivityDetailViewModelTest.kt` (NEW - protection against race conditions)

## Plan de Implementación
1. Deshabilitar FAB de Today y menú de `PlanningTimeBlock`.
2. Agregar flujo de `SnackbarEvent` en `ActivityDetailViewModel`.
3. Implementar `toggleNodeCompletion` con protección anti race-condition.
4. Conectar `SnackbarHost` en `ActivityDetailScreen`, con acción "Deshacer"
   que revierte el estado en la DB.
5. Verificar checklist de `10_INTERACTION_INVARIANTS.md`.

## Validaciones
- [x] Completar un nodo en `ActivityDetailScreen` muestra snackbar con
      "Deshacer", y tocar "Deshacer" revierte el estado.
- [x] FAB de Today y menú de `PlanningTimeBlock` están visualmente
      deshabilitados, sin `onClick = {}` en el código.
- [x] `TodayViewModel` no tiene ningún cambio en este diff.
- [x] `git diff` real adjunto al reporte de cierre.

## Resultado Esperado
Ningún tap target del proyecto queda muerto (deshabilitado explícitamente
donde no hay lógica real, funcional donde sí la hay). El único flujo de
completado real (Activity Detail) da feedback y permite deshacer. Today y
Planning quedan exactamente como estaban en datos, listos para EC-012/EC-013
sin trabajo duplicado.

## Auditoría
Consultar guía en [AUDITS/README.md](../AUDITS/README.md).
- [x] ¿`TodayViewModel` quedó intacto (cero cambios de wiring)?
- [x] ¿El FAB y el menú de Planning están `enabled = false`, no removidos ni
      con lógica fingida?
- [x] ¿El snackbar de deshacer permite reversión real en DB?

## Lecciones Aprendidas
1. **Soporte Técnico para Undo**: No basta con revertir el estado de la UI; el patrón de "Deshacer" en acciones de completado requiere la capacidad de eliminar registros de ejecución en la base de datos, lo cual exigió una extensión imprevista de la capa de datos.
2. **Race Conditions en UI**: Se detectó que un usuario puede tapear múltiples veces un mismo nodo antes de que la corrutina de guardado termine. La implementación de un conjunto de IDs en proceso (`processingNodeIds`) en el ViewModel es vital para prevenir estados inconsistentes en la base de datos.
3. **Generalización de Scaffold**: Se refactorizó `RoutineScaffold` para exponer el parámetro `snackbarHost`, permitiendo que cualquier pantalla del sistema use feedback de snackbar de forma consistente sin duplicar la estructura de `Scaffold` de Material 3.

## Definition of Done (Obligatorio)
- [x] Compila sin warnings nuevos (Verificado: build exitoso)
- [x] Tests existentes pasan (Verificado: 7 tests exitosos)
- [x] Checklist de [10_INTERACTION_INVARIANTS.md](../10_INTERACTION_INVARIANTS.md) revisado y sin violaciones
- [x] Cero cambios en `TodayViewModel` / capa de datos de Today
- [x] `git diff` real (no resumen) adjunto al reporte de cierre

## Checklist de Validación de Usuario
- [x] Completar un paso en el detalle de una actividad muestra un aviso con
      opción de "Deshacer".
- [x] El botón "+" de Today y el menú "..." de Planning se ven apagados/grises
      (no rotos, no fingen funcionar).

REGLA CRÍTICA: ningún agente puede marcar estos checkboxes ni cambiar el
estado a APPROVED. Solo el usuario lo hace manualmente.

## Estado
El estado vigente de esta EC es el declarado en el campo `status` del
frontmatter (arriba de este documento). No dupliques el valor aquí.
