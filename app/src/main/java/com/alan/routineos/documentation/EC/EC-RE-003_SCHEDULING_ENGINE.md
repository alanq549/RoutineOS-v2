---
id: EC-RE-003
title: Scheduling Engine & Daily Instances
phase: 2
priority: Critical
effort: Large
owner: AI Agent
status: IN_PROGRESS
depends_on: EC-RE-002
branch: feature/ec-re-003-scheduling-engine
audit: Pending
created: 2026-08-13
updated: 2026-08-13
---

# EC-RE-003: Scheduling Engine & Daily Instances

## Objetivo
Implementar el motor de agendamiento y la entidad `DailyInstance` para desacoplar la **Planificación (Intención)** de la **Realidad (Hoy)**, permitiendo modificaciones diarias sin alterar la definición original.

## Contexto
RoutineOS requiere que el usuario pueda modificar su plan del día (mover horarios, omitir actividades) sin destruir la regla de recurrencia base. Esto se logra mediante la materialización selectiva de instancias virtuales en la base de datos.

## Problema
Actualmente, no existe una entidad que represente una ocurrencia concreta en el tiempo. Si se quisiera modificar el horario de hoy, se tendría que modificar la `ScheduleRule` global, lo cual afectaría a todos los días futuros.

## Alcance
- [ ] **Migración V7**: Introducción de `DailyInstanceEntity` con snapshots de título/descripción y un índice único compuesto para evitar duplicados.
- [ ] **Motor de Proyección Virtual**: Lógica en la capa de Dominio para calcular el timeline resolviendo reglas y excepciones sin persistir nada automáticamente.
- [ ] **Materialización Selectiva**: Implementar la lógica que persiste una `DailyInstance` solo cuando hay una interacción (modificación, ejecución o creación ad-hoc).
- [ ] **Snapshot de Presentación**: Asegurar que cada instancia materializada capture el estado visual (título/desc) del nodo en ese momento.
- [ ] **Desacoplamiento**: Una vez materializada, la instancia ignora cambios posteriores en la regla original.

### Exclusiones
- No incluye la UI final de "Today Workspace" (EC-RE-006).
- No incluye el motor de duraciones y rangos complejos (EC-RE-004).

## Archivos Afectados
- `data/local/entities/DailyInstanceEntity.kt` (NEW)
- `data/local/RoutineOSDatabase.kt` (V7)
- `domain/model/DailyInstance.kt` (NEW)
- `domain/usecase/ResolveTimelineUseCase.kt` (NEW)
- `domain/usecase/MaterializeInstanceUseCase.kt` (NEW)
- `data/repository/OfflineActivityRepository.kt`

## Plan de Implementación
1. Crear la infraestructura de datos (Entity, DAO, Migración v7).
2. Implementar la lógica de resolución virtual de reglas.
3. Desarrollar el UseCase de materialización con snapshots de integridad.
4. Actualizar el motor de ejecución para anclarse a las instancias diarias.
5. Validar la deduplicación mediante el índice compuesto.

## Validaciones
- [ ] `TimelineResolutionTest`: Verificación de reglas + excepciones virtuales.
- [ ] `MaterializationTest`: Verificación de snapshots y desacoplamiento.
- [ ] `DeduplicationTest`: El índice `(targetType, targetId, scheduledDate)` rechaza duplicados.

## Auditoría
Consultar guía en [AUDITS/README.md](../AUDITS/README.md).
- [ ] ¿La migración v7 fue realmente no destructiva?
- [ ] ¿Se evita la materialización automática al abrir la app?
- [ ] ¿Los nodos sin regla propia se mantienen como contexto y no generan instancias?

## Definition of Done (Obligatorio)
Antes de marcar como APPROVED, verificar:
- [ ] Compila sin warnings nuevos.
- [ ] Tests de resolución y materialización pasan.
- [ ] Checklist de [ARCHITECTURE_INVARIANTS.md](../08_ARCHITECTURE_INVARIANTS.md) revisado.
- [ ] Lecciones aprendidas documentadas arriba.

## Estado
El estado vigente de esta EC es el declarado en el campo `status` del frontmatter.
