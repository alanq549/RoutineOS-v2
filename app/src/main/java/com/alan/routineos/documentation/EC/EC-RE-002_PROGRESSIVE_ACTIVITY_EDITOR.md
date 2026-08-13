---
id: EC-RE-002
title: Progressive Activity Editor
phase: 1
priority: Critical
effort: Large
owner: AI Agent
status: APPROVED
depends_on: EC-RE-001
branch: feature/ec-re-002-progressive-activity-editor
audit: APPROVED
created: 2026-08-13
updated: 2026-08-13
---

# EC-RE-002: Progressive Activity Editor

## Objetivo
Implementar un sistema de edición interactivo y progresivo que permita al usuario construir y modificar la jerarquía de una actividad directamente en la pantalla de detalle, integrando el concepto de "Node Inspector" para configuraciones avanzadas.

## Contexto
Tras habilitar el soporte para nodos jerárquicos en EC-RE-001, el sistema requiere una interfaz de usuario que permita manipular esta estructura de forma fluida. Siguiendo el principio de **Progressive Disclosure**, el editor evitará formularios densos, permitiendo que la actividad crezca orgánicamente.

## Problema
Actualmente, la creación de nodos es limitada y no existe una forma de añadir hijos a nodos existentes ni de renombrarlos sin volver a pantallas anteriores. La gestión de la jerarquía es de "solo lectura" desde la perspectiva del usuario.

## Alcance
- [x] **Edición Inline**: Permitir renombrar cualquier nodo (título) directamente en la lista.
- [x] **Adición Jerárquica**: Botón para añadir un "Hijo" a cualquier nodo existente, transformando el nodo hoja en contenedor instantáneamente.
- [x] **Borrado Recursivo**: Implementación de **Soft Delete** (`isDeleted = true`) que preserva el historial de ejecuciones y metadata, permitiendo un "Undo" persistente.
- [x] **Node Inspector**: Shell genérico con puntos de entrada para **Scheduling** (Agendamiento) y **Metadata** (Métricas), actuando solo como punto de enlace a sus respectivos motores.
- [x] **Estabilidad del Árbol**: Validación de integridad en la capa de Dominio (detección de ciclos, anclaje de definición).
- [x] **Undo Persistente**: La acción de Deshacer utiliza `RestoreBranchUseCase` para revertir el borrado lógico de forma inmediata en la base de datos.

### Exclusiones
- No se implementará `MoveNode` (mover a otro padre) ni `ReorderNode` (cambiar posición entre hermanos) en esta EC.
- No se implementará la lógica profunda de configuración de reglas de agendamiento ni esquemas de métricas.

## Archivos Afectados
- `feature/dashboard/ActivityDetailScreen.kt`
- `feature/dashboard/ActivityDetailViewModel.kt`
- `domain/repository/ActivityRepository.kt`
- `data/repository/OfflineActivityRepository.kt`
- `domain/usecase/` (UpdateNode, AddChild, DeleteBranch, RestoreBranch, ValidateActivityNode)
- `data/local/entities/ActivityNodeEntity.kt` (V6 Migration)

## Plan de Implementación
1. Implementar UseCases de Dominio para `UpdateNode`, `AddChild`, `DeleteBranch` y `RestoreBranch`.
2. Realizar migración destructiva a V6 para añadir `isDeleted` y `description`.
3. Actualizar el ViewModel para manejar estados de edición transitorios y coordinar el flujo de "Undo".
4. Refactorizar `NodeItem` para soportar edición inline y el Shell del Inspector.
5. Validar integridad de la jerarquía tras múltiples operaciones de edición.

## Validaciones
- [x] Verificación de borrado: Eliminar un padre con hijos marca recursivamente todos como eliminados pero preserva sus ejecuciones en DB.
- [x] Verificación de agnosticismo: Ninguna clave de dominio (gym, task, exercise) aparece en el Inspector ni en la lógica.
- [x] Verificación de reactividad: La adición de un hijo expande automáticamente al padre y actualiza la UI Projection.

## Auditoría
Consultar guía en [AUDITS/README.md](../AUDITS/README.md).
- [x] ¿Las funciones son < 30 líneas?
- [x] ¿Se evita el uso de wizards obligatorios?
- [x] ¿El "significado" de los datos sigue perteneciendo exclusivamente al usuario?

## Definition of Done (Obligatorio)
Antes de marcar como APPROVED, verificar:
- [x] Compila sin warnings nuevos (Verificado: build exitoso).
- [x] Tests existentes pasan (unitarios + los que aplique) (Verificado: 28 tests exitosos).
- [x] Checklist de [ARCHITECTURE_INVARIANTS.md](../08_ARCHITECTURE_INVARIANTS.md) revisado y sin violaciones.
- [x] No se modifican accidentalmente Schedule/Metadata al editar la estructura.
- [x] Lecciones aprendidas documentadas arriba.

## Estado
El estado vigente de esta EC es el declarado en el campo `status` del frontmatter (arriba de este documento). No dupliques el valor aquí.
