---
id: EC-009
title: Activity Execution Engine
phase: 4
priority: High
effort: Medium
owner: AI Agent
status: APPROVED
depends_on: EC-008
branch: feature/ec-009-activity-execution-engine
audit: AUDIT_EC-009
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
- [x] Crear entidad `ActivityExecutionEntity`: `id`, `nodeId` (FK), `completedAt` (timestamp), `metadataJson` (String, nullable/default "{}").
- [x] Crear `ActivityExecution` (modelo de dominio) y su mapper.
- [x] Agregar método a `ActivityRepository`: `registerExecution(nodeId, metadataJson)`.
- [x] En `ActivityDetailScreen`: agregar botón/acción simple para marcar un nodo como "completado", registrando una `ActivityExecution` con metadata vacía "{}" por ahora.
- [x] Mostrar visualmente en la lista de nodos si tienen al menos una ejecución registrada (ej. un ícono o timestamp de última ejecución).
- [x] Exclusión: No incluye formulario de captura de metadata (peso/series/etc.) — solo el registro de "completado" sin datos.
- [x] Exclusión: No incluye estadísticas, gráficas ni agregación de datos históricos — eso es una EC futura (EC-010).
- [x] Exclusión: No incluye edición ni borrado de ejecuciones registradas.

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
- [x] Verificación anti-remanente: confirmar que `metadataJson` nunca contiene claves hardcodeadas en el código Kotlin (ej. no debe existir ninguna referencia literal a "peso", "series", "monto" en el código fuente). (Verificado: Grep exhaustivo negativo).
- [x] Prueba unitaria de mapeo y persistencia.
- [x] Verificación manual del registro de ejecución en el logcat o mediante la actualización de la UI.

## Resultado Esperado
Un sistema capaz de registrar la finalización de pasos individuales de forma persistente y agnóstica al contenido, sentando las bases para el análisis de rendimiento futuro.

## Auditoría
Consultar guía en [AUDITS/README.md](../AUDITS/README.md).
- [x] ¿Cumple con la arquitectura MVVM/Clean?
- [x] ¿Las funciones son < 30 líneas? (Verificado: refactorización de `loadActivity` en ViewModel exitosa).
- [x] ¿Los archivos son < 300 líneas?

## Lecciones Aprendidas
- **Consistencia de Metadata JSON:** Establecer `"{}"` como valor por defecto tanto en la entidad de Room como en el modelo de dominio previene errores de parseo en capas superiores y simplifica la lógica de inicialización.
- **Flujos Reactivos Complejos:** La implementación de un estado de UI que depende de múltiples relaciones (Actividad -> Nodos -> Ejecuciones) requiere el uso avanzado de `flatMapLatest` y `combine` para asegurar que cualquier cambio en la base de datos se refleje instantáneamente sin recargas manuales.
- **Refactorización de Lógica de Transformación:** Extraer la lógica de orquestación de flows fuera de la función `init` o de carga principal del ViewModel no solo ayuda a cumplir con los límites de líneas, sino que facilita el mantenimiento de la reactividad al aislar la transformación de datos.

## Definition of Done (Obligatorio)
Antes de marcar como APPROVED, verificar:
- [x] Compila sin warnings nuevos (Verificado: build exitoso y análisis de archivos limpio tras corrección de formato).
- [x] Tests existentes pasan (Verificado: 6 unit tests pasados satisfactoriamente).
- [x] Checklist de [ARCHITECTURE_INVARIANTS.md](../08_ARCHITECTURE_INVARIANTS.md) revisado y sin violaciones (Verificado: motor opaco sin términos de dominio).
- [x] Si se usó Fake*Repository, está registrado en [MOCK_DATA_STATUS.md](../09_MOCK_DATA_STATUS.md) (Verificado: registro de migraciones y fakes actualizado).
- [x] Lecciones aprendidas documentadas arriba

## Checklist de Validación de Usuario
- [x] Al ver el detalle de una actividad, aparece un icono de círculo junto a cada paso.
- [x] Al tocar el icono, este cambia a una marca de verificación (check) indicando que el paso se ha completado.
- [x] El estado de completado se mantiene al salir y volver a entrar a la pantalla de detalle.
- [x] Nota: La pantalla de inicio ("Today") sigue usando datos simulados y no refleja estos cambios todavía.

## Estado
El estado vigente de esta EC es el declarado en el campo `status` del frontmatter (arriba de este documento). No dupliques el valor aquí.
