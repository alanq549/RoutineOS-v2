---
id: EC-012
title: Today Real Integration (datos reales + interacción directa)
phase: 5
priority: Critical
effort: Large
owner: AI Agent
status: DRAFT
depends_on: [EC-010, EC-011]
branch: feature/today-real-integration
audit: Pending
created: 2026-08-09
updated: 2026-08-09
---

# EC-012: Today Real Integration

## Objetivo
Migrar `TodayViewModel` de `FakeTodayRepository` a datos reales resueltos por
el scheduling model (EC-011), e implementar la interacción de completar/saltar
un nodo siguiendo `10_INTERACTION_INVARIANTS.md` (EC-010) — tap directo, sin
navegación, sin diálogo de confirmación.

## Contexto
`Today` es el `startDestination` de la app (`AppNavHost.kt`) y la pantalla de
uso diario más frecuente, pero hoy es enteramente mock: `FakeTodayRepository`,
`onNodeToggled` vacío, FAB con `onClick = { }`, y `TimelineItemCard`/`NodeRow`
sin ningún `clickable`. La única acción de completar nodo que funciona hoy vive
en `ActivityDetailViewModel`, tres pantallas de navegación de distancia. Esta
EC cierra esa brecha usando lo que EC-011 (resolución de instancias por fecha)
y EC-010 (reglas de interacción) ya dejaron definido.

## Problema
1. `TodayViewModel` no está en el grafo de Hilt (se instancia con
   `FakeTodayRepository()` como default arg) — no puede consumir
   `ActivityRepository` sin refactor.
2. No hay forma de resolver "qué nodos corresponden a hoy" sin el caso de uso
   `ResolveTimelineForDateRange` de EC-011.
3. La UI de Today (`TimelineItemCard`, `NodeRow`) fue heredada del mockup de
   Stitch sin ningún tap target — hay que reconstruir la interacción, no solo
   reconectar datos.
4. `AppRoutes.Dashboard`/`DashboardViewModel` (el único flujo ya conectado a
   datos reales) no tiene ruta registrada en `AppNavHost` — queda código
   muerto en paralelo a este trabajo si no se resuelve.

## Alcance
- [ ] Convertir `TodayViewModel` a `@HiltViewModel` con `@Inject constructor`,
      recibiendo `ActivityRepository` real. Eliminar `FakeTodayRepository` del
      grafo de producción (puede quedar como fixture de test).
- [ ] Invocar `ResolveTimelineForDateRange(today)` (EC-011) para poblar el
      timeline del día — reemplaza los datos hardcodeados actuales.
- [ ] `onNodeToggled(nodeId, scheduledDate)` debe llamar al caso de uso real de
      registrar ejecución (el mismo que usa `ActivityDetailViewModel` desde
      EC-009, con `scheduledDate` de EC-011), no un no-op.
- [ ] Reescribir `TimelineItemCard`/`NodeRow` con `clickable` para completar
      por tap directo (regla de EC-010) — sin `AlertDialog` de confirmación.
- [ ] Agregar `Snackbar` con "Deshacer" tras completar/saltar, en vez de
      confirmación previa.
- [ ] Resolver el FAB de Today: implementar su acción real (crear nodo rápido
      para hoy) o retirarlo si no hay caso de uso definido — no puede quedar
      `onClick = { }`.
- [ ] Resolver la ruta huérfana de `Dashboard`: decidir explícitamente si su
      contenido se fusiona dentro de `Today` o si se elimina el código muerto
      de `AppRoutes`/`DashboardViewModel`. Documentar la decisión en
      `05_ROADMAP.md`.
- [ ] Exclusión explícita: **no** se implementa drag-to-reschedule ni bottom sheet
      de edición de horario en esta EC — eso es interacción de `Planning`, EC
      posterior.
- [ ] Exclusión explícita: no se crea UI nueva para `ScheduleRule` — esta EC
      solo consume lo que EC-011 ya expone vía el repositorio.

## Archivos Afectados
- [ ] `feature/today/TodayViewModel.kt` (MODIFY — Hilt real)
- [ ] `feature/today/TodayScreen.kt` (MODIFY — FAB real)
- [ ] `feature/today/TimelineItemCard.kt` (MODIFY — clickable + snackbar)
- [ ] `data/repository/FakeTodayRepository.kt` (MOVE a carpeta de test fixtures o DELETE)
- [ ] `di/RepositoryModule.kt` (MODIFY si aplica — confirmar binding único de `ActivityRepository`)
- [ ] `navigation/AppNavHost.kt` (MODIFY — resolver ruta huérfana de Dashboard)
- [ ] `navigation/AppRoutes.kt` (MODIFY si se elimina Dashboard)
- [ ] `documentation/05_ROADMAP.md` (MODIFY — registrar decisión sobre Dashboard)
- [ ] `documentation/09_MOCK_DATA_STATUS.md` (MODIFY — dar de baja el ítem de `FakeTodayRepository`)

## Plan de Implementación
1. Definir y resolver primero qué pasa con `Dashboard` (fusionar o eliminar)
   antes de tocar `Today`, para no construir sobre una ruta que va a cambiar.
2. Convertir `TodayViewModel` a Hilt real, inyectando `ActivityRepository`.
3. Conectar `ResolveTimelineForDateRange` para poblar el estado de UI.
4. Implementar tap directo + snackbar-deshacer en `TimelineItemCard`, según
   `10_INTERACTION_INVARIANTS.md`.
5. Implementar o retirar el FAB.
6. Eliminar/mover `FakeTodayRepository` y actualizar `09_MOCK_DATA_STATUS.md`.
7. Verificación manual end-to-end: completar un nodo desde Today sin salir de
   la pantalla, confirmar persistencia en DB.

## Validaciones
- [ ] Test unitario: `TodayViewModel` expone las instancias correctas para la
      fecha actual usando un `ActivityRepository` fake de test (no el fake de
      producción retirado).
- [ ] Verificación manual: completar un nodo desde Today, verificar que
      persiste (App Inspector) y que `ActivityDetailScreen` refleja el mismo
      estado para ese nodo/fecha.
- [ ] Verificación manual: la acción de completar no abre ninguna pantalla ni
      diálogo — ocurre inline con snackbar de deshacer.
- [ ] Confirmar que no queda ningún `onClick = { }` en los archivos tocados.
- [ ] `git diff` real adjunto al reporte de cierre.

## Resultado Esperado
Today deja de ser una maqueta: muestra instancias reales del día (vía
scheduling model) y permite completarlas con un toque, sin navegar. El único
camino de ejecución deja de ser `ActivityDetailScreen`. No queda código muerto
de `Dashboard` sin resolver.

## Auditoría
Consultar guía en [AUDITS/README.md](../AUDITS/README.md).
- [ ] ¿Cumple con la arquitectura MVVM/Clean?
- [ ] ¿Las funciones son < 30 líneas?
- [ ] ¿Los archivos son < 300 líneas?
- [ ] ¿Se verificó el checklist completo de
      [10_INTERACTION_INVARIANTS.md](../10_INTERACTION_INVARIANTS.md)?
- [ ] ¿`TodayViewModel` quedó correctamente en el grafo de Hilt (no
      instanciación manual)?

## Lecciones Aprendidas
(Espacio para documentar hallazgos, dificultades o mejoras descubiertas durante
el desarrollo).

## Definition of Done (Obligatorio)
Antes de marcar como APPROVED, verificar:
- [ ] Compila sin warnings nuevos
- [ ] Tests existentes pasan (unitarios + los que aplique)
- [ ] Checklist de [ARCHITECTURE_INVARIANTS.md](../08_ARCHITECTURE_INVARIANTS.md) revisado y sin violaciones
- [ ] Checklist de [10_INTERACTION_INVARIANTS.md](../10_INTERACTION_INVARIANTS.md) revisado y sin violaciones
- [ ] `FakeTodayRepository` dado de baja en [MOCK_DATA_STATUS.md](../09_MOCK_DATA_STATUS.md)
- [ ] Lecciones aprendidas documentadas arriba
- [ ] `git diff` real (no resumen) adjunto al reporte de cierre

## Checklist de Validación de Usuario
(El agente auditor completa esta sección SOLO cuando el estado pasa a
USER_REVIEW_PENDING. Lenguaje llano, sin jerga técnica.)
- [ ] Abrir la app y ver, en la pantalla principal, las actividades reales del
      día (no datos de ejemplo fijos).
- [ ] Tocar una actividad para marcarla como hecha, sin que se abra ninguna
      pantalla nueva ni aparezca un cartel de confirmación.
- [ ] Ver un aviso abajo con opción de "Deshacer" tras marcarla.

REGLA CRÍTICA: ningún agente puede marcar estos checkboxes ni cambiar el
estado a APPROVED. Solo el usuario lo hace manualmente.

## Estado
El estado vigente de esta EC es el declarado en el campo `status` del
frontmatter (arriba de este documento). No dupliques el valor aquí.
