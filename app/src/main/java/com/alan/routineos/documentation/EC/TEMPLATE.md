---
id: EC-XXX
title: [Nombre de la Tarea]
phase: [1-6]
priority: [Low / Medium / High / Critical]
effort: [Small / Medium / Large]
owner: [User / AI Agent]
status: [DRAFT / PENDING / READY / IN_PROGRESS / IMPLEMENTED / AUDIT_PENDING / CHANGES_REQUESTED / USER_REVIEW_PENDING / APPROVED / MERGED / CLOSED]
depends_on: [EC-ID, None]
branch: [feature/name, fix/name, etc.]
audit: [AUDIT-ID, Pending]
created: YYYY-MM-DD
updated: YYYY-MM-DD
---

# EC-XXX: [Title]

## Objetivo
Descripción clara y concisa de lo que se busca lograr con esta tarea.

## Contexto
Por qué es necesaria esta tarea y cómo encaja en el roadmap actual definido en [05_ROADMAP.md](../05_ROADMAP.md).

## Problema
Descripción detallada del problema técnico o funcional que se busca resolver.

## Alcance
- [ ] Requerimiento 1
- [ ] Requerimiento 2
- [ ] Exclusión 1 (Lo que no cubre esta EC)

## Archivos Afectados
- [ ] `path/to/file1.kt`
- [ ] `path/to/file2.kt`

## Plan de Implementación
1. Paso 1
2. Paso 2

## Validaciones
- [ ] Prueba unitaria A
- [ ] Verificación manual B

## Resultado Esperado
Descripción del estado final tras la ejecución exitosa de la EC.

## Auditoría
Consultar guía en [AUDITS/README.md](../AUDITS/README.md).
- [ ] ¿Cumple con la arquitectura MVVM/Clean?
- [ ] ¿Las funciones son < 30 líneas?
- [ ] ¿Los archivos son < 300 líneas?

## Lecciones Aprendidas
(Espacio para documentar hallazgos, dificultades o mejoras descubiertas durante el desarrollo).

## Definition of Done (Obligatorio)
Antes de marcar como COMPLETED, verificar:
- [ ] Compila sin warnings nuevos
- [ ] Tests existentes pasan (unitarios + los que aplique)
- [ ] Checklist de [ARCHITECTURE_INVARIANTS.md](../08_ARCHITECTURE_INVARIANTS.md) revisado y sin violaciones
- [ ] Si se usó Fake*Repository, está registrado en [MOCK_DATA_STATUS.md](../09_MOCK_DATA_STATUS.md)
- [ ] Lecciones aprendidas documentadas arriba

## Checklist de Validación de Usuario
(El agente auditor completa esta sección SOLO cuando el estado pasa a
USER_REVIEW_PENDING. Lenguaje llano, sin jerga técnica, describiendo
comportamiento observable en la app — no líneas de código.)
- [ ] [Qué debe ver/probar el usuario en la app]

REGLA CRÍTICA: ningún agente puede marcar estos checkboxes ni cambiar el
estado a APPROVED. Solo el usuario lo hace manualmente.

## Estado
**ESTADO ACTUAL:** [DRAFT / PENDING / READY / IN_PROGRESS / IMPLEMENTED / AUDIT_PENDING / CHANGES_REQUESTED / USER_REVIEW_PENDING / APPROVED / MERGED / CLOSED]
