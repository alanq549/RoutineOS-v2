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
status: IMPLEMENTED
audit: Pending
created: 2026-08-13
updated: 2026-08-15
---

# EC-RE-003: Scheduling Engine & Daily Instances

## Objetivo
Implementar el motor de agendamiento y la entidad `DailyInstance` para desacoplar la **Planificación (Intención)** de la **Realidad (Hoy)**, permitiendo modificaciones diarias sin alterar la definición original.

## Alcance
- [x] **Baseline V1**: Reinicio de la versión de base de datos a 1 con todos los campos temporales (`startTime`, `endTime`, `durationMinutes`) integrados.
- [x] **Motor de Proyección Virtual**: Lógica en la capa de Dominio para calcular el timeline resolviendo reglas y excepciones.
- [x] **Detector de Conflictos**: Implementación de `ConflictDetectorUseCase` para identificar solapamientos en el timeline.
- [x] **Materialización Selectiva**: Persistencia de `DailyInstance` con snapshots de integridad visual.
- [x] **Desacoplamiento**: Las instancias materializadas son inmutables ante cambios en las reglas base.
- [x] **Validación Jerárquica**: Verificación de reglas independientes en diferentes niveles del árbol.

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
