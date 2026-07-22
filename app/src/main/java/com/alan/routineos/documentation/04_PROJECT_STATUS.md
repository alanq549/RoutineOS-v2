# 04_PROJECT_STATUS.md — RoutineOS v2

## Tablero de Engineering Cards (EC)
Este es el panel de control de todas las tareas del proyecto. El contexto vivo de ejecución se encuentra en **[07_CURRENT_CONTEXT.md](./07_CURRENT_CONTEXT.md)**.

| ID | Title | Priority | Effort | Owner | Dependencies | Status | Notes |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| EC-000 | Project Setup & Philosophy | High | Small | AI Agent | None | APPROVED | Architecture and Manifesto |
| EC-001 | Data Foundation Implementation | High | Medium | AI Agent | None | APPROVED | Room & Hilt Setup |

---

## Estados Permitidos
- **DRAFT**: EC en redacción inicial, no lista para revisión.
- **PENDING**: Tarea en backlog, objetivos definidos pero no lista para iniciar.
- **READY**: Tarea analizada y lista para ejecución inmediata.
- **IN_PROGRESS**: Siendo trabajada actualmente por un desarrollador/agente.
- **IMPLEMENTED**: Código escrito y documentación de implementación finalizada.
- **AUDIT_PENDING**: En proceso de revisión técnica por un tercero o auto-auditoría.
- **APPROVED**: Auditoría técnica superada con éxito.
- **MERGED**: Código integrado en `develop` y rama de trabajo eliminada.
- **CLOSED**: Funcionalidad verificada y ciclo de vida finalizado.

---

## Notas de Tareas
- Las ECs se mueven de estado aquí, pero los detalles técnicos de cada una se gestionan en su archivo respectivo en la carpeta `EC/`.
