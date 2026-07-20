# 01_DEVELOPMENT_WORKFLOW.md — RoutineOS v2

## Contexto
Este documento define el ciclo de vida del desarrollo en RoutineOS v2, asegurando que cada cambio sea trazable, auditado y alineado con la visión del proyecto definida en [00_PROJECT_SCOPE.md](./00_PROJECT_SCOPE.md).

## Ciclo de Desarrollo
El flujo estándar sigue estos pasos obligatorios:

1. **Definición en Roadmap**: Toda funcionalidad nace en el Roadmap de [04_PROJECT_STATUS.md](./04_PROJECT_STATUS.md).
2. **Creación de Engineering Card (EC)**: Se crea un archivo en `EC/` usando la [TEMPLATE.md](./EC/TEMPLATE.md). El estado inicial es `DRAFT`.
3. **Análisis y Preparación**: Se completa la EC hasta que pase a estado `READY`.
4. **Implementación**: El desarrollo se realiza en una rama específica siguiendo la [02_BRANCH_STRATEGY.md](./02_BRANCH_STRATEGY.md). El estado es `IN_PROGRESS`.
4. **Auditoría**: Una vez terminada la lógica, se realiza una auditoría técnica documentada en `AUDITS/` según [AUDITS/README.md](./AUDITS/README.md).
5. **Aprobación y Merge**: Tras la auditoría exitosa, se procede al merge a la rama protegida correspondiente.

## Roadmap
El Roadmap es la fuente de verdad sobre el futuro del proyecto a corto y mediano plazo. Se gestiona exclusivamente en [04_PROJECT_STATUS.md](./04_PROJECT_STATUS.md).

## Engineering Cards (EC)
Las EC son unidades de trabajo atómicas. Ningún cambio significativo de código debe ocurrir sin una EC asociada.
- Ver detalles en [EC/README.md](./EC/README.md).

## Auditorías
Las auditorías aseguran la calidad y el cumplimiento de las reglas de arquitectura (MVVM, Clean Architecture) antes de integrar cambios.
- Ver detalles en [AUDITS/README.md](./AUDITS/README.md).

## Flujo de Aprobación
1. **Developer Self-Audit**: El autor verifica su trabajo contra la EC (`IMPLEMENTED`).
2. **Technical Audit**: Revisión de arquitectura y consistencia (`AUDIT_PENDING`).
3. **Status Update**: Cambio de estado a `APPROVED` en el tablero de estado tras verificación exitosa.

## Continuidad Automática
Para que un agente IA o un desarrollador pueda continuar el proyecto sin fricción:
1. Leer [03_AGENT_RULES.md](./03_AGENT_RULES.md).
2. Consultar el primer elemento `IN_PROGRESS` o `READY` en [04_PROJECT_STATUS.md](./04_PROJECT_STATUS.md).
3. Seguir las instrucciones de la EC vinculada.
