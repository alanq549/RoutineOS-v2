# RoutineOS v2 — Engineering Framework

## Propósito
Este repositorio utiliza un Framework de Ingeniería estructurado para garantizar que el desarrollo sea escalable, mantenible y perfectamente operable tanto por desarrolladores humanos como por agentes de IA. La documentación aquí contenida es la única fuente de verdad y el "sistema operativo" del proyecto.

## Guía de Lectura Recomendada

### Para Desarrolladores Humanos
Si es tu primera vez en el proyecto, sigue este orden para entenderlo en 10 minutos:
1. **[00_PROJECT_SCOPE.md](./00_PROJECT_SCOPE.md)**: La constitución técnica y reglas de oro.
2. **[05_ROADMAP.md](./05_ROADMAP.md)**: Visión a largo plazo y fases del proyecto.
3. **[EC/EC-000_PROJECT_SETUP.md](./EC/EC-000_PROJECT_SETUP.md)**: Filosofía, arquitectura y stack tecnológico.
4. **[07_CURRENT_CONTEXT.md](./07_CURRENT_CONTEXT.md)**: ¿Qué está pasando justo ahora?
5. **[04_PROJECT_STATUS.md](./04_PROJECT_STATUS.md)**: El tablero de tareas (Engineering Cards).

### Para Agentes de IA
Los agentes DEBEN iniciar obligatoriamente en:
- **[06_AGENT_BOOTSTRAP.md](./06_AGENT_BOOTSTRAP.md)**: Protocolo de arranque y algoritmo de ejecución.

## Estructura Documental

| Documento | Responsabilidad |
| :--- | :--- |
| **[PROJECT_SCOPE](./00_PROJECT_SCOPE.md)** | Reglas innegociables, estándares de código y arquitectura base. |
| **[WORKFLOW](./01_DEVELOPMENT_WORKFLOW.md)** | El ciclo de vida de una funcionalidad (Idea -> EC -> Audit -> Merge). |
| **[BRANCH_STRATEGY](./02_BRANCH_STRATEGY.md)** | Convenciones de Git, commits y definiciones de Done/Ready. |
| **[AGENT_RULES](./03_AGENT_RULES.md)** | Principios de ingeniería y restricciones operativas para la IA. |
| **[PROJECT_STATUS](./04_PROJECT_STATUS.md)** | Tablero de control de las Engineering Cards (EC). |
| **[ROADMAP](./05_ROADMAP.md)** | Fases estables y dependencias del proyecto. |
| **[CURRENT_CONTEXT](./07_CURRENT_CONTEXT.md)** | Estado vivo: rama actual, tarea activa y bloqueos inmediatos. |

## Flujo General de Desarrollo
1. Identificar la siguiente fase en el **Roadmap**.
2. Seleccionar o crear una **Engineering Card (EC)** en el **Project Status**.
3. Consultar el **Current Context** para asegurar la alineación.
4. Implementar siguiendo las **Agent Rules** y la **Branch Strategy**.
5. Realizar una **Audit** antes del merge final.
