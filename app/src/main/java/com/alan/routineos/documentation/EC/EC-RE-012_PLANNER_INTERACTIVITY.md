---
id: EC-RE-012
title: Planificador (Planner) MVP
phase: 5
priority: High
effort: Large
owner: AI Agent
status: CLOSED
depends_on: [EC-RE-008, EC-RE-011]
branch: feature/planner-mvp
audit: Approved
created: 2026-09-05
updated: 2026-09-06
---

# EC-RE-012: Planificador (Planner) MVP

## Objetivo
Transformar el módulo **Planificador** de un prototipo visual estático a un espacio de trabajo funcional que permita gestionar la intención temporal diaria (Mover, Omitir, Crear espontáneos) sin afectar las reglas globales.

## Contexto
RoutineOS separa la intención (Planning) de la ejecución (Today). El Planificador es el lugar donde el usuario diseña su día, ya sea hoy o en el futuro, realizando intervenciones puntuales sobre sus rutinas recurrentes.

## Problema
- La pestaña "PLANIFICADOR" en `PlanningWorkspace` es de solo lectura.
- No hay forma de reprogramar o cancelar actividades para días futuros desde la UI.
- No se muestra la jerarquía de sub-pasos (Essential Hierarchy) en la vista de planificación.
- El FAB de Planning está vacío.

## Alcance (MUST)
- [x] **Navegación Temporal**: Cambio de día/semana funcional.
- [x] **Intervenciones Puntuales**: 
    - [x] Mover ocurrencia (crea `DailyInstance` modificada).
    - [x] Omitir ocurrencia (crea `DailyInstance` omitida).
    - [x] Revertir intervención (vuelve a `PLANNED`).
- [x] **Jerarquía Esencial**: Mostrar y expandir sub-pasos informativamente (usando `GetHierarchicalTimelineUseCase`).
- [x] **Eventos Espontáneos**: Crear y editar (modelo V3) directamente en el timeline de planificación.
- [x] **Detección de Conflictos**: Visualización de solapamientos tras intervenciones.
- [x] **Catálogo**: Botón para añadir actividades existentes al plan del día.

## Invariantes de Negocio
1. **Planning != Today**: Planning nunca completa actividades ni registra ejecuciones.
2. **Soberanía del Usuario**: Detectar conflictos pero nunca mover otras actividades automáticamente (no cascada).
3. **Persistencia**: Las intervenciones crean instancias/excepciones, NUNCA modifican la `ScheduleRule` base.
4. **Validación Temporal**: Aplicar regla `startTime < endTime`.

## Archivos Afectados
- `feature/planning/PlanningViewModel.kt` (MODIFY)
- `feature/planning/PlanningScreen.kt` (MODIFY)
- `feature/planning/components/PlanningTimeBlock.kt` (MODIFY)
- `domain/usecase/GetHierarchicalTimelineUseCase.kt` (REFACTOR para fechas arbitrarias)
- `domain/usecase/AddActivityToDayUseCase.kt` (NEW)

## Plan de Implementación
1. **Refactor de Dominio**: Asegurar que `GetHierarchicalTimelineUseCase` resuelva correctamente para cualquier fecha.
2. **Interactividad en ViewModel**: Implementar lógica de Move, Skip y Reset en `PlanningViewModel`.
3. **Unificación de UI**: Actualizar `PlanningTimeBlock` para que use la misma base jerárquica que Today, pero con acciones de planificación.
4. **Catálogo Integrado**: Implementar el diálogo/sheet para añadir desde el catálogo.
5. **Sincronización**: Verificar que los cambios en Planning impactan a Today (y viceversa) mediante la persistencia compartida.

## Definition of Done
- [x] Operaciones de Mover/Omitir funcionales para días futuros.
- [x] Jerarquía visible y expandible en el timeline de Planning.
- [x] Cero interferencia con `ActivityExecution` (Planning no ejecuta).
- [x] Build exitoso y tests pasando.

## Auditoría
Ver informe detallado en: [AUDIT_EC-RE-012_INTERACTIVE_PLANNING.md](../AUDITS/AUDIT_EC-RE-012_INTERACTIVE_PLANNING.md)
