---
id: EC-009
title: Activity Execution Engine
phase: 4
priority: High
effort: Medium
owner: AI Agent
status: READY
depends_on: EC-008
branch: feature/ec-009-activity-execution-engine
audit: Pending
created: 2026-07-29
updated: 2026-07-29
---

# EC-009: Activity Execution Engine

## Objetivo
Permitir marcar un `ActivityNode` como completado, registrando una `ActivityExecution` con metadata genérica en formato JSON, sin hardcodear ningún campo de dominio específico (peso, series, repeticiones, monto, etc.) en el modelo de datos.

## Contexto
EC-008 permite crear y listar nodos, pero no hay forma de registrar su ejecución/finalización. Esta EC introduce el concepto de "ejecución" como entidad separada de la "definición", preparando el terreno para que el motor sirva cualquier dominio (fitness, estudio, finanzas, hábitos) sin cambios futuros al core.

## Problema
Los usuarios pueden definir qué quieren hacer (actividades y nodos), pero no pueden registrar que lo han hecho. Sin una capa de ejecución, el sistema es solo un gestor de plantillas estáticas y no un motor de seguimiento real.

## Decisión de Arquitectura
`ActivityExecution` usa un campo `metadataJson: String` para datos específicos del dominio, en vez de columnas fijas. El usuario/UI decide qué claves usar; el motor nunca las interpreta ni las valida por tipo en la capa de persistencia core.

## Alcance
- [ ] Crear entidad `ActivityExecutionEntity`: `id`, `nodeId` (FK), `completedAt` (timestamp), `metadataJson` (String, nullable/default "{}").
- [ ] Crear `ActivityExecution` (modelo de dominio) y su mapper.
- [ ] Agregar método a `ActivityRepository`: `registerExecution(nodeId, metadataJson)`.
- [ ] En `ActivityDetailScreen`: agregar botón/acción simple para marcar un nodo como "completado", registrando una `ActivityExecution` con metadata vacía "{}" por ahora.
- [ ] Mostrar visualmente en la lista de nodos si tienen al menos una ejecución registrada (ej. un ícono o timestamp de última ejecución).
- [ ] Exclusión: No incluye formulario de captura de metadata (peso/series/etc.) — solo el registro de "completado" sin datos.
- [ ] Exclusión: No incluye estadísticas, gráficas ni agregación de datos históricos — eso es una EC futura (EC-010).
- [ ] Exclusión: No incluye edición ni borrado de ejecuciones registradas.

## Archivos Afectados
- `app/src/main/java/com/alan/routineos/data/local/entities/ActivityExecutionEntity.kt`
- `app/src/main/java/com/alan/routineos/domain/model/ActivityExecution.kt`
- `app/src/main/java/com/alan/routineos/data/local/RoutineOSDatabase.kt`
- `app/src/main/java/com/alan/routineos/data/local/dao/ActivityExecutionDao.kt`
- `app/src/main/java/com/alan/routineos/domain/repository/ActivityRepository.kt`
- `app/src/main/java/com/alan/routineos/data/repository/OfflineActivityRepository.kt`
- `app/src/main/java/com/alan/routineos/feature/dashboard/ActivityDetailScreen.kt`
- `app/src/main/java/com/alan/routineos/feature/dashboard/ActivityDetailViewModel.kt`

## Plan de Implementación
1. Crear la entidad `ActivityExecutionEntity` con relación a `ActivityNodeEntity`.
2. Definir `ActivityExecutionDao` con operaciones básicas de inserción y consulta.
3. Actualizar `RoutineOSDatabase` para incluir la nueva tabla.
4. Definir el modelo de dominio `ActivityExecution` y funciones de mapeo.
5. Exponer la funcionalidad en el repositorio.
6. Actualizar el ViewModel de detalle para manejar la acción de completar nodo.
7. Refinar la UI de `ActivityDetailScreen` para mostrar el estado de ejecución.

## Validaciones
- [ ] Verificación anti-remanente: confirmar que `metadataJson` nunca contiene claves hardcodeadas en el código Kotlin (ej. no debe existir ninguna referencia literal a "peso", "series", "monto" en el código fuente).
- [ ] Prueba unitaria de mapeo y persistencia.
- [ ] Verificación manual del registro de ejecución en el logcat o mediante la actualización de la UI.

## Resultado Esperado
Un sistema capaz de registrar la finalización de pasos individuales de forma persistente y agnóstica al contenido, sentando las bases para el análisis de rendimiento futuro.

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

## Checklist de Validación de Usuario
- [ ] Al ver el detalle de una actividad, aparece una opción para marcar cada paso como "completado".
- [ ] Tras marcar un paso como completado, aparece un indicador visual (ej. una marca de verificación) que persiste al cerrar y abrir la pantalla.

## Estado
El estado vigente de esta EC es el declarado en el campo `status` del frontmatter (arriba de este documento). No dupliques el valor aquí.
