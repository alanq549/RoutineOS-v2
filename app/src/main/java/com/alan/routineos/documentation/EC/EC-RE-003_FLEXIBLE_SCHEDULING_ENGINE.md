---
id: EC-RE-003
title: Flexible Scheduling Engine
phase: 2
priority: Critical
effort: Large
owner: AI Agent
status: DRAFT
depends_on: EC-RE-002
branch: feature/ec-re-003-scheduling-engine
audit: Pending
created: 2026-08-13
updated: 2026-08-13
---

# EC-RE-003: Flexible Scheduling Engine

## Objetivo
Implementar el motor de agendamiento capaz de transformar las definiciones jerárquicas en ocurrencias temporales concretas (`DailyInstance`), soportando reglas recurrentes, ventanas de tiempo y manejo de conflictos visuales.

## Contexto
Una vez consolidada la estructura jerárquica (EC-RE-001) y su edición (EC-RE-002), el siguiente paso es dotar al sistema de "inteligencia temporal". El motor debe permitir proyectar estas actividades en el calendario (Planning) y permitir su ejecución (Today) sin acoplar la definición original con las instancias diarias.

## Problema
El modelo de scheduling actual es rudimentario. No soporta horas específicas, duraciones, ni la complejidad de reglas aplicadas a diferentes niveles de un árbol de actividad. Además, no existe la entidad `DailyInstance`, lo que impide modificar un plan para hoy sin alterar la regla de recurrencia general.

## Alcance
- [ ] **Resolución de Árboles**: Algoritmo para generar instancias basadas en reglas de padres e hijos simultáneamente.
- [ ] **Nuevos Tipos de Regla**: Soporte para Hora Fija, Ventana de Tiempo (Inicio/Fin) y Duración.
- [ ] **Entidad DailyInstance**: Persistencia de la "instancia del día" como un snapshot del plan, permitiendo overrides sin afectar la `ScheduleRule`.
- [ ] **Manejo de Excepciones**: Refinar `ScheduleException` para integrarse con el nuevo flujo de resolución.
- [ ] **Detección de Conflictos**: Lógica agnóstica para identificar solapamientos temporales entre instancias.
- [ ] **Agnosticismo de Dominio**: El motor no conoce conceptos como "hábito", "clase" o "entrenamiento". Solo entiende de nodos y ventanas de tiempo.

### Exclusiones
- No se implementará la UI avanzada de Planning o Today en esta EC (solo la lógica del motor y persistencia).
- No se implementará la resolución automática de conflictos (solo detección y aviso).

## Archivos Afectados
- `data/local/entities/ScheduleRuleEntity.kt` (V7 Migration)
- `data/local/entities/DailyInstanceEntity.kt` (NEW)
- `domain/model/DailyInstance.kt` (NEW)
- `domain/usecase/ResolveDailyInstancesUseCase.kt` (NEW)
- `domain/usecase/ConflictDetectorUseCase.kt` (NEW)
- `data/repository/OfflineActivityRepository.kt`

## Plan de Implementación (Resumen)
1. Extender `ScheduleRuleEntity` para soportar campos temporales (V7).
2. Crear `DailyInstanceEntity` para persistir snapshots del plan.
3. Desarrollar el UseCase de resolución jerárquica (Rule Independence & Parent Propagation).
4. Implementar el detector de conflictos basado en intersección de conjuntos.
5. Actualizar el repositorio para orquestar la materialización de instancias.

## Validaciones
- [ ] Test: Un padre a las 08:00 y un hijo a las 10:00 generan dos `DailyInstances` independientes y correctas.
- [ ] Test: Modificar la hora de una `DailyInstance` no altera su `ScheduleRule` de origen.
- [ ] Test: Dos instancias en la misma hora disparan un flag de conflicto.

## Auditoría
Consultar guía en [AUDITS/README.md](../AUDITS/README.md).
- [ ] ¿El motor respeta la separación entre Intención (Rule) y Realidad (Execution)?
- [ ] ¿Se evita cualquier lógica de dominio hardcodeada?

## Definition of Done (Obligatorio)
- [ ] Lógica de resolución jerárquica verificada con tests unitarios.
- [ ] Soporte para Overrides de plan (DailyInstance) funcional.
- [ ] Detección de conflictos visuales habilitada.
- [ ] Migración V7 registrada y validada.

## Estado
El estado vigente de esta EC es el declarado en el campo `status` del frontmatter (arriba de este documento).
