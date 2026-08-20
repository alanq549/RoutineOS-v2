---
id: EC-RE-008
title: Visual Planning & Today Refinement
phase: 5
priority: High
effort: Large
owner: AI Agent
status: APPROVED
depends_on: EC-RE-007
branch: feature/ec-re-008-planning-refine
audit: AUDIT_EC-RE-008
created: 2026-08-19
updated: 2026-08-19
---

# EC-RE-008: Visual Planning & Today Refinement

## Objetivo
Eliminar la dependencia de datos falsos en la pestaña de **Planificar** (Planning) y realizar un refinamiento visual profundo del espacio de trabajo **Today**, asegurando que ambos se sientan como un producto final de alta calidad.

## Contexto
RoutineOS ya tiene los motores (Jerarquía, Agendamiento, Metadatos) y la organización (Sistemas). Sin embargo, la pestaña de Planificar sigue siendo un prototipo estático y el diseño de Today necesita pulirse para que la jerarquía de sub-pasos sea intuitiva.

## Problema
- `PlanningViewModel` usa `FakePlanningRepository`.
- La visualización de sub-pasos en Today es funcional pero visualmente densa/desordenada.
- No hay una transición fluida entre la planificación de una regla y su aparición en el calendario semanal.

## Alcance
- [x] **Proyección Real en Planning**: Sustituir el repositorio falso por `ResolveTimelineUseCase` para proyectar instancias reales en la vista semanal.
- [x] **Refinamiento de Tarjetas en Today**: Pulir la indentación, "líneas de hilo" (threads) e iconos de estado para una lectura clara de la jerarquía.
- [x] **Optimización de Captura**: Mejorar el flujo de entrada de datos (metadatos) para que sea más ligero y contextual.
- [x] **Saneamiento de Código**: Eliminar todos los archivos de "Fake Data" de Planning y Today.

### Exclusiones
- No incluye gráficas de análisis histórico (EC-RE-009).
- No incluye reordenamiento mediante Drag & Drop.

## Archivos Afectados
- `feature/planning/PlanningViewModel.kt` (REFACTOR - Connect to Real Data)
- `feature/planning/PlanningScreen.kt` (REFACTOR)
- `feature/today/components/TimelineItemCard.kt` (REFINE - UX/UI)
- `feature/today/components/TodayTimeline.kt` (REFINE)
- `feature/planning/data/FakePlanningRepository.kt` (DELETE)

## Invariantes de Ejecución (Congeladas)
1. **Unificación Temporal**: Planning y Today deben usar `ResolveTimelineUseCase` como única fuente de verdad. No hay duplicación de lógica.
2. **Precedencia de Realidad**: Una `DailyInstance` materializada siempre anula la proyección virtual de una `ScheduleRule`.
3. **Persistencia de Intención**: Mover o saltar en Planning/Today crea una instancia, pero NUNCA modifica la `ScheduleRule` base.
4. **Integridad de Metadata**: La captura respeta estrictamente la separación `MetadataSchema` → `ActivityExecution`.
5. **Calidad de Código**: Se mantiene el límite de 30 líneas por función y el agnosticismo de dominio.

## Plan de Implementación
1. **Unificación de Dominio**: Actualizar `ResolveTimelineUseCase` para soportar rangos y prioridad de materialización.
2. **Migración de Planning**: Conectar `PlanningViewModel` a datos reales mediante Hilt y eliminar el repositorio falso.
3. **Sincronización de Estados**: Asegurar que una acción en Today (ej: Move) se refleje instantáneamente en la vista de Planning.
4. **Refinamiento de Today**: Corregir errores de jerarquía y mejorar la densidad del formulario de captura.
5. **Test de Oro**: Validar el flujo: *Regla -> Visibilidad Dual -> Movimiento/Materialización -> Consistencia en ambas pantallas*.

## Validaciones
- [ ] La pestaña de Planificar muestra actividades reales configuradas en el Dashboard.
- [ ] Cambiar de día en el selector semanal de Planning actualiza correctamente la lista de instancias.
- [ ] Las actividades con sub-pasos en Today muestran una jerarquía visual clara y profesional.

## Definition of Done (Obligatorio)
- [ ] Cero datos falsos (Mocks) en Planning y Today.
- [ ] Build exitoso y tests pasando.
- [ ] Cumplimiento estricto de < 30 líneas por función.
