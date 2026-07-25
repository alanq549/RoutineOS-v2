---
id: EC-000
title: Project Setup & Philosophy
phase: 1
priority: High
effort: Small
owner: AI Agent
status: CLOSED
depends_on: None
branch: chore/development-documentation
audit: AUDIT-001
created: 2026-07-19
updated: 2026-07-24
---

# EC-000: Project Setup & Philosophy

## Objetivo
Documentar la base técnica, arquitectónica y filosófica del proyecto RoutineOS v2 para servir como referencia histórica y técnica.

## Contexto
RoutineOS v2 no es solo una aplicación, es un ejercicio de ingeniería modular y asistida por IA. Esta EC registra las decisiones fundamentales tomadas durante la creación del framework inicial.

## Problema
Los proyectos suelen perder su esencia técnica y sus convenciones a medida que crecen. Se necesita una "Engineering Manifesto" que explique el *por qué* de la arquitectura.

## Alcance
- [x] Definición de la estructura de paquetes.
- [x] Elección del stack tecnológico (Compose, Hilt, Room, Navigation).
- [x] Establecimiento de la regla de responsabilidad única y límites de tamaño de archivos.
- [x] Creación del sistema de Engineering Cards.
- [x] Exclusión: Esta EC no contiene implementación de código.

## Archivos Afectados
- `app/src/main/java/com/alan/routineos/documentation/` (Todo el contenido).

## Plan de Implementación
1. Definir la visión en [00_PROJECT_SCOPE.md](../00_PROJECT_SCOPE.md).
2. Crear el flujo de trabajo en [01_DEVELOPMENT_WORKFLOW.md](../01_DEVELOPMENT_WORKFLOW.md).
3. Establecer reglas para agentes en [03_AGENT_RULES.md](../03_AGENT_RULES.md) y [06_AGENT_BOOTSTRAP.md](../06_AGENT_BOOTSTRAP.md).

## Validaciones
- [x] Los documentos son consistentes entre sí.
- [x] El sistema de auditoría es funcional.

## Resultado Esperado
Un framework de ingeniería listo para soportar el desarrollo de producción de forma ordenada.

## Auditoría
- [x] ¿El framework es autocontenido?
- [x] ¿Un nuevo desarrollador puede entender el proyecto solo con la documentación?

## Lecciones Aprendidas
La inversión inicial en documentación estructurada reduce drásticamente la alucinación de los agentes de IA y la fricción en el onboarding de humanos.

## Estado
El estado vigente de esta EC es el declarado en el campo `status` del frontmatter (arriba de este documento). No dupliques el valor aquí.
