# 04_PROJECT_STATUS.md — RoutineOS v2

## Tablero de Engineering Cards (EC)
Este es el panel de control de todas las tareas del proyecto. El contexto vivo de ejecución se encuentra en **[07_CURRENT_CONTEXT.md](./07_CURRENT_CONTEXT.md)**.

| ID | Title | Priority | Effort | Owner | Dependencies | Status | Notes |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| EC-000 | Project Setup & Philosophy | High | Small | AI Agent | None | CLOSED | Confirmado manualmente por el usuario. Ver AUDITS/AUDIT_EC-000.md |
| EC-001 | Data Foundation Implementation | High | Medium | AI Agent | None | CLOSED | Violación de invariante de dominio detectada en auditoría retroactiva. Ver AUDITS/AUDIT_EC-001.md. Corrección consolidada en EC-005. |
| EC-002 | Core Architecture & Base Repositories | High | Medium | AI Agent | EC-001 | CLOSED | Violación de invariante de dominio detectada en auditoría retroactiva. Ver AUDITS/AUDIT_EC-002.md. Corrección consolidada en EC-005. |
| EC-003 | Design System Foundations & Refinement | High | Medium | AI Agent | EC-000 | CLOSED | Fonts, Atomic Components & Catalog |
| EC-005 | Domain Model Agnostic Refactor | High | Medium | AI Agent | EC-002 | CLOSED | Domain & Database Agnostic Refactor |
| EC-006 | Routine Dashboard Screen | High | Medium | AI Agent | EC-005 | APPROVED | Reactive Dashboard Screen. Arquitectura agnóstica verificada. Fallos en Invariantes de Dominio y DI corregidos. |
| EC-007 | Activity Creation Flow | High | Medium | AI Agent | EC-006 | APPROVED | Creation flow for ActivityDefinitions. |
| EC-008 | Activity Detail & Nodes Management | High | Medium | AI Agent | EC-007 | APPROVED | Detail screen and node management. Calidad de código verificada. |
| EC-009 | Activity Execution Engine | High | Medium | AI Agent | EC-008 | APPROVED | Generic JSON-based execution tracking. Calidad de código verificada. |
| EC-010 | Interaction Cleanup | High | Small | AI Agent | EC-009 | CLOSED | Surgical cleanup of dead tap targets and snackbar feedback. |
| EC-011 | Scheduling Model | Critical | Large | AI Agent | EC-009 | CLOSED | ScheduleRule, ScheduleException & TimelineInstance. |
| EC-RE-001 | Hierarchical Activity Nodes | Critical | Large | AI Agent | EC-011 | CLOSED | Hierarchical tree refactor and validation implemented. |
| EC-RE-002 | Progressive Activity Editor | Critical | Large | AI Agent | EC-RE-001 | CLOSED | Inline hierarchical editing with persistent Undo support. |
| EC-RE-003 | Scheduling Engine & Daily Instances | Critical | Large | AI Agent | EC-RE-002 | CLOSED | Refined flexible logic with snapshot integrity. |
| EC-RE-004 | Flexible Rules & Scheduling Editor | High | Medium | AI Agent | EC-RE-003 | CLOSED | Visual time/day editors in Node Inspector. |
| EC-RE-005 | Metadata Schemas & Context | High | Medium | AI Agent | EC-RE-004 | CLOSED | CRUD and Context logic implemented. |
| EC-RE-006 | Today Workspace (Actions & Capture) | Critical | Large | AI Agent | EC-RE-005 | CLOSED | Refactored domain hierarchy and dynamic capture. |
| EC-RE-007 | Contextual Organization (Systems) | Medium | Medium | AI Agent | EC-RE-006 | CLOSED | Grouping activities into top-level Life Systems. |
| EC-RE-008 | Visual Planning & Today Refinement | High | Large | AI Agent | EC-RE-007 | APPROVED | Real data in Planning and visual polish in Today. |

---

## Estados Permitidos
- **DRAFT**: EC en redacción inicial, no lista para revisión.
- **PENDING**: Tarea en backlog, objetivos definidos pero no lista para iniciar.
- **READY**: Tarea analizada y lista para ejecución inmediata.
- **IN_PROGRESS**: Siendo trabajada actualmente por un desarrollador/agente.
- **IMPLEMENTED**: Código escrito y documentación de implementación finalizada.
- **AUDIT_PENDING**: En proceso de revisión técnica por un tercero o auto-auditoría.
- **CHANGES_REQUESTED**: El auditor detectó problemas y solicitó cambios (vuelve a IN_PROGRESS).
- **USER_REVIEW_PENDING**: Auditoría técnica aprobada, esperando validación final del usuario.
- **APPROVED**: Auditoría técnica y validación de usuario superadas con éxito.
- **MERGED**: Código integrado en `develop` y rama de trabajo eliminada.
- **CLOSED**: Funcionalidad verificada y ciclo de vida finalizado.

---

## Notas de Tareas
- Las ECs se mueven de estado aquí, pero los detalles técnicos de cada una se gestionan en su archivo respectivo en la carpeta `EC/`.
