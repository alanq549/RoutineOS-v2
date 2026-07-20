# 03_AGENT_RULES.md — RoutineOS v2

## Reglas para Agentes IA
Este documento define el protocolo operativo para cualquier agente que interactúe con el repositorio de RoutineOS v2.

### Protocolo de Inicio
Antes de realizar cualquier modificación, el agente DEBE:
1. Leer [00_PROJECT_SCOPE.md](./00_PROJECT_SCOPE.md) para entender la constitución técnica.
2. Leer [04_PROJECT_STATUS.md](./04_PROJECT_STATUS.md) para identificar la tarea actual.
3. Localizar la Engineering Card (EC) correspondiente en la carpeta `EC/`.

### Priorización de Tareas
1. Continuar con la primera EC que tenga estado `IN_PROGRESS`.
2. Si no hay ninguna, tomar la primera EC en estado `READY` y cambiarla a `IN_PROGRESS`.
3. Nunca trabajar en dos ECs simultáneamente a menos que sea una instrucción explícita del usuario.

### Operaciones de Git
- **Creación de Ramas**: Crear una rama siguiendo [02_BRANCH_STRATEGY.md](./02_BRANCH_STRATEGY.md) antes de iniciar cambios de código.
- **Commits**: Realizar commits atómicos que correspondan a avances significativos de la EC.
- **Merge**: No realizar merges a `main` o `develop` sin haber documentado la auditoría previa.

### Actualización de Estado
El agente es responsable de mantener [04_PROJECT_STATUS.md](./04_PROJECT_STATUS.md) actualizado al:
- Iniciar una tarea (`READY` -> `IN_PROGRESS`).
- Terminar una implementación (`IN_PROGRESS` -> `IMPLEMENTED`).
- Completar una auditoría (`IMPLEMENTED` -> `AUDIT_PENDING` -> `APPROVED`).

### Restricciones Críticas (Lo que NUNCA debe hacer)
- **Modificar archivos no relacionados**: No tocar archivos fuera del scope de la EC actual.
- **Ignorar el Design System**: No crear estilos ad-hoc; usar los componentes base.
- **Omitir la Documentación**: No dar por cerrada una tarea si no hay registro de la implementación y auditoría.
- **Romper el Build**: Siempre verificar que el proyecto compile tras cambios significativos.
- **Ignorar la regla de 300 líneas**: Si un archivo supera las 300 líneas, el agente debe proponer un refactor.
