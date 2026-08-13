---
id: EC-RE-001
title: Hierarchical Activity Nodes
phase: 1
priority: Critical
effort: Large
owner: AI Agent
status: IN_PROGRESS
depends_on: EC-011
branch: feature/ec-re-001-hierarchical-nodes
audit: Pending
created: 2026-08-13
updated: 2026-08-13
---

# EC-RE-001: Hierarchical Activity Nodes

## Objetivo
Refactorizar la estructura de `ActivityNode` de una lista plana a un árbol jerárquico arbitrario y permitir que `ScheduleRule` sea polimórfico (pertenecer a una definición o a un nodo).

## Contexto
El re-baseline estratégico definió que RoutineOS debe ser un "OS Adaptable". Esto requiere que las actividades no sean simples listas, sino estructuras compuestas que reflejen la complejidad de la organización humana (ej. Universidad -> Materia -> Laboratorio).

## Problema
El modelo actual (V4) solo soporta un nivel de profundidad y vincula horarios únicamente a nodos, impidiendo agendar una actividad completa o crear sub-pasos anidados.

## Alcance
- [ ] Refactor de `ActivityNodeEntity` para incluir `parentId` y `position`.
- [ ] Refactor de `ScheduleRuleEntity` para soportar `activityDefinitionId` como target.
- [ ] Implementación de lógica de dominio para reconstruir el árbol desde una lista plana.
- [ ] Actualización de `ActivityDetailScreen` para mostrar la jerarquía visualmente.
- [ ] Definición de la regla de completion: el estado de un padre se calcula en base a sus hijos (Opción C).

## Archivos Afectados
- `data/local/entities/ActivityNodeEntity.kt`
- `data/local/entities/ScheduleRuleEntity.kt`
- `domain/model/ActivityNode.kt`
- `domain/model/ScheduleRule.kt`
- `data/local/RoutineOSDatabase.kt` (V5)
- `feature/dashboard/ActivityDetailViewModel.kt`
- `feature/dashboard/ActivityDetailScreen.kt`

## Plan de Implementación
1. Actualizar Entidades Room y subir versión a V5 (Destructiva).
2. Actualizar Modelos de Dominio.
3. Implementar mapper de lista plana a árbol en la capa de Dominio.
4. Ajustar el ViewModel de detalle para manejar la jerarquía y estados de expansión.
5. Refactorizar la UI para soportar indentación y navegación del árbol.

## Validaciones
- [ ] `ActivityNodeTreeMapperTest`: Verificación de reconstrucción del árbol.
- [ ] Verificación manual de independencia de datos entre actividades.

## Resultado Esperado
Un núcleo de datos capaz de representar estructuras jerárquicas infinitas, listo para el editor progresivo y el motor de planificación flexible.

## Auditoría
Consultar guía en [AUDITS/README.md](../AUDITS/README.md).
- [ ] ¿Cumple con la arquitectura MVVM/Clean?
- [ ] ¿Las funciones son < 30 líneas?
- [ ] ¿Los archivos son < 300 líneas?

## Lecciones Aprendidas
(A completar tras la implementación).

## Definition of Done (Obligatorio)
Antes de marcar como APPROVED, verificar:
- [ ] Compila sin warnings nuevos
- [ ] Tests existentes pasan (unitarios + los que aplique)
- [ ] Checklist de [ARCHITECTURE_INVARIANTS.md](../08_ARCHITECTURE_INVARIANTS.md) revisado y sin violaciones
- [ ] Si se usó Fake*Repository, está registrado en [MOCK_DATA_STATUS.md](../09_MOCK_DATA_STATUS.md)
- [ ] Lecciones aprendidas documentadas arriba

## Estado
El estado vigente de esta EC es el declarado en el campo `status` del frontmatter (arriba de este documento). No dupliques el valor aquí.
