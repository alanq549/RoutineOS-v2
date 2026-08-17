---
id: EC-RE-006
title: Today Workspace (Actions & Capture)
phase: 3
priority: Critical
effort: Large
owner: AI Agent
status: APPROVED
depends_on: EC-RE-005
branch: feature/ec-re-006-today-workspace
audit: APPROVED
created: 2026-08-16
updated: 2026-08-16
---

# EC-RE-006: Today Workspace (Actions & Capture)

## Objetivo
Transformar la pantalla principal (`Today`) en un espacio de trabajo operativo real. Se integrarán el motor de agendamiento (Fase 2) y el motor de metadatos (Fase 3) para permitir ejecutar, mover o saltar actividades capturando datos estructurados.

## Contexto
Hasta ahora, `Today` era un mockup estático con datos fijos. Con las bases de datos V1 y los motores de resolución listos, es el momento de que el usuario interactúe con su sistema de vida real.

## Problema
El usuario no puede realizar acciones reales desde la pantalla principal. No se resuelven los horarios configurados, no se pueden capturar métricas al completar tareas, y no existe soporte para cambios espontáneos (Ad-hoc).

## Alcance
- [ ] **Migración a Datos Reales**: Conectar `TodayViewModel` al `ActivityRepository` y `ResolveTimelineUseCase`.
- [ ] **Formulario Dinámico de Captura**: Generar automáticamente una interfaz de entrada basada en el `MetadataSchema` de la actividad al marcarla como completada.
- [ ] **Acciones de Instancia**:
    - **Completar**: Registro de ejecución + materialización.
    - **Saltar (Skip)**: Marcado como `OMITTED` para el registro histórico.
    - **Mover (Move)**: Reprogramación puntual para el día de hoy (cambia `plannedStartTime` de la instancia).
- [ ] **Soporte Ad-hoc**: Habilitar el FAB para crear actividades que solo existan en el timeline de hoy.
- [ ] **Resumen de Progreso**: El indicador de salud del header debe calcularse en base al estado real de las `DailyInstances`.

### Exclusiones
- No incluye gráficas históricas avanzadas (EC-RE-008).
- No incluye drag-and-drop para reordenar el timeline (Diferido a Phase 4).

## Archivos Afectados
- `feature/today/TodayViewModel.kt`
- `feature/today/TodayScreen.kt`
- `feature/today/components/CaptureMetadataSheet.kt` (NEW)
- `domain/usecase/RegisterDailyActionUseCase.kt` (NEW)
- `feature/today/model/TodayTimelineItem.kt` (Refactor)

## Plan de Implementación
1. Migrar `TodayViewModel` a Hilt e inyectar UseCases reales.
2. Implementar el motor de renderizado dinámico para la captura de metadatos.
3. Desarrollar la lógica de materialización de acciones (Complete/Skip/Move).
4. Activar el FAB para la creación de entradas Ad-hoc.
5. Validar que la ejecución desde Today se refleje correctamente en la base de datos (JOIN entre Instance y Execution).

## Validaciones
- [ ] Al completar una actividad con métricas "Peso" y "Reps", el formulario debe pedirlas obligatoriamente.
- [ ] Mover una actividad de las 08:00 a las 10:00 debe crear una `DailyInstance` con el nuevo horario sin tocar la regla base.
- [ ] El histórico debe preservar los valores capturados incluso tras borrar la actividad original.

## Auditoría
- [ ] ¿Se respeta la separación entre Intención y Realidad (Materialización)?
- [ ] ¿El formulario dinámico es agnóstico a los nombres de los campos?

## Definition of Done (Obligatorio)
- [ ] Compila sin errores.
- [ ] Flujo completo: Resolución -> Acción -> Captura -> Persistencia.
- [ ] Tests de integración de acciones de Today aprobados.
