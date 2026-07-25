# 04_PROJECT_STATUS.md — RoutineOS v2

## Tablero de Engineering Cards (EC)
Este es el panel de control de todas las tareas del proyecto. El contexto vivo de ejecución se encuentra en **[07_CURRENT_CONTEXT.md](./07_CURRENT_CONTEXT.md)**.

| ID | Title | Priority | Effort | Owner | Dependencies | Status | Notes |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| EC-000 | Project Setup & Philosophy | High | Small | AI Agent | None | CLOSED | Confirmado manualmente por el usuario. Ver AUDITS/AUDIT_EC-000.md |
| EC-001 | Data Foundation Implementation | High | Medium | AI Agent | None | CHANGES_REQUESTED | Violación de invariante de dominio detectada en auditoría retroactiva. Ver AUDITS/AUDIT_EC-001.md. Corrección consolidada en EC-005. |
| EC-002 | Core Architecture & Base Repositories | High | Medium | AI Agent | EC-001 | CHANGES_REQUESTED | Violación de invariante de dominio detectada en auditoría retroactiva. Ver AUDITS/AUDIT_EC-002.md. Corrección consolidada en EC-005. |
| EC-003 | Design System Foundations & Refinement | High | Medium | AI Agent | EC-000 | CLOSED | Fonts, Atomic Components & Catalog |
| EC-005 | Domain Model Agnostic Refactor | High | Medium | AI Agent | EC-002 | AUDIT_PENDING | Domain & Database Agnostic Refactor |
| EC-006 | Routine Dashboard Screen | High | Medium | AI Agent | EC-005 | PENDING | Reactive Dashboard Screen |

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
