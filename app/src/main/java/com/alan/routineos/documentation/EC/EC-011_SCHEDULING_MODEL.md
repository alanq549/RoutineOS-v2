---
id: EC-011
title: Scheduling Model (ScheduleRule / ScheduleException / TimelineInstance)
phase: 5
priority: Critical
effort: Large
owner: AI Agent
status: IN_PROGRESS
depends_on: [EC-009]
branch: feature/scheduling-model
audit: Pending
created: 2026-08-09
updated: 2026-08-12
---

# EC-011: Scheduling Model

## Objetivo
Implementar `ScheduleRule`, `ScheduleException` y `TimelineInstance` — las tres
entidades ya declaradas como núcleo válido en
[08_ARCHITECTURE_INVARIANTS.md](../08_ARCHITECTURE_INVARIANTS.md) pero nunca
construidas — para que un `ActivityNode` pueda resolver a qué fecha(s)
concretas corresponde, de forma agnóstica de dominio.

## Contexto
El roadmap actual asume que la vista "Today" puede preguntar "¿qué nodos
corresponden a hoy?". Hoy esa pregunta no tiene respuesta posible: `ActivityNode`
no tiene ningún campo temporal (ver `domain/model/ActivityNode.kt`). EC-009 ya
implementó el motor de ejecución (`ActivityExecution`) asumiendo un nodo
no-recurrente; esta EC extiende el modelo para soportar recurrencia sin romper
lo ya auditado en EC-009.

## Problemas Identificados
1. `ActivityNode(id, activityDefinitionId, title)` no puede expresar "todos los
   lunes/miércoles/viernes" ni "3 veces por semana, el usuario elige cuándo"
   (horario flexible vs. fijo, mencionado como requisito por el usuario).
2. `ActivityExecutionEntity` actual vincula la ejecución solo a `nodeId`. Con un
   nodo recurrente, `executions.isNotEmpty()` (usado hoy en
   `ActivityDetailViewModel`) marcaría el nodo como completado para siempre
   después de la primera ejecución, sin distinguir el día. Este defecto nace en
   EC-009 y se corrige acá antes de que Today dependa de él.
3. No existe mecanismo para excepciones puntuales (mover una instancia a otro
   día, saltarla una vez sin romper la regla general).

## Alcance
- [ ] Entidad `ScheduleRule`: define la recurrencia de un `ActivityNode`
      (tipo: `FIXED_DAYS` con set de días de semana, o `FLEXIBLE_FREQUENCY` con
      N ocurrencias por periodo). Sin enums de dominio — el "significado" de la
      regla lo interpreta la UI, no el motor.
- [ ] Entidad `ScheduleException`: override puntual para una fecha (`SKIPPED`,
      `RESCHEDULED` con nueva fecha/hora). Persistida, referencia
      `scheduleRuleId` + fecha original.
- [ ] `TimelineInstance` (data class, **compute-only**, no `@Entity`, no tabla):
      resultado de resolver `ActivityNode` + `ScheduleRule` + `ScheduleException`
      para un rango de fechas. Se calcula en el repositorio, nunca se persiste.
- [ ] Caso de uso `ResolveTimelineForDateRange(nodeId, dateRange)` en
      `domain/usecase/`, puro, testeable sin Room.
- [ ] Corrección de `ActivityExecutionEntity`: agregar `scheduledDate: Long`
      (epoch day) como parte de la clave natural de una ejecución. El estado
      "completado" pasa a resolverse por `(nodeId, scheduledDate)`, no por
      `nodeId` solo. Migración de DB v3 → v4 (destructiva, ya aceptada como
      deuda registrada en 09_MOCK_DATA_STATUS.md).
- [ ] Actualizar `ActivityDetailViewModel` para que `isCompleted` se calcule
      por instancia de fecha, no por existencia global de ejecuciones.
- [ ] Exclusión explícita: **no** se toca `TodayViewModel` ni
      `FakeTodayRepository` en esta EC. Conectar Today al scheduling model real
      es EC-012.
- [ ] Exclusión explícita: no se implementa UI para crear/editar
      `ScheduleRule` todavía (eso depende de EC-010, bottom sheet de edición).
      Esta EC es solo modelo de dominio + persistencia + resolución.

## Archivos Afectados
- [ ] `domain/model/ScheduleRule.kt` (NEW)
- [ ] `domain/model/ScheduleException.kt` (NEW)
- [ ] `domain/model/TimelineInstance.kt` (NEW, sin anotación `@Entity`)
- [ ] `domain/usecase/ResolveTimelineForDateRange.kt` (NEW)
- [ ] `data/local/entities/ScheduleRuleEntity.kt` (NEW)
- [ ] `data/local/entities/ScheduleExceptionEntity.kt` (NEW)
- [ ] `data/local/entities/ActivityExecutionEntity.kt` (MODIFY — agregar `scheduledDate`)
- [ ] `data/local/dao/ScheduleRuleDao.kt` (NEW)
- [ ] `data/local/dao/ScheduleExceptionDao.kt` (NEW)
- [ ] `data/local/RoutineOSDatabase.kt` (MODIFY — version 3 → 4)
- [ ] `data/repository/OfflineActivityRepository.kt` (MODIFY — exponer resolución de timeline)
- [ ] `feature/dashboard/ActivityDetailViewModel.kt` (MODIFY — `isCompleted` por fecha)

## Plan de Implementación
1. Definir `ScheduleRule` y `ScheduleException` en `domain/model` (sin lógica,
   solo forma de datos).
2. Definir `TimelineInstance` como data class pura de dominio.
3. Implementar `ResolveTimelineForDateRange` con tests unitarios cubriendo:
   regla de días fijos, regla de frecuencia flexible, excepción de tipo
   `SKIPPED`, excepción de tipo `RESCHEDULED`.
4. Crear entidades Room + DAOs para `ScheduleRule`/`ScheduleException`.
5. Migrar `ActivityExecutionEntity` agregando `scheduledDate`; subir versión de
   DB a 4 con `fallbackToDestructiveMigration()` (ya aceptado como deuda).
6. Actualizar `OfflineActivityRepository` para exponer el nuevo caso de uso.
7. Actualizar `ActivityDetailViewModel` para que `isCompleted` compare por
   `(nodeId, scheduledDate)` en vez de existencia global.
8. Ejecutar checklist Anti-Remanente (grep de términos de dominio) sobre todos
   los archivos nuevos.

## Validaciones
- [ ] Tests unitarios de `ResolveTimelineForDateRange` (mínimo 4 casos: fijo,
      flexible, skip, reschedule) — sin dependencia de Room/Android.
- [ ] Test de regresión: una ejecución registrada para el lunes NO marca como
      completada la instancia del miércoles del mismo nodo recurrente.
- [ ] Verificación manual: crear un nodo con regla "lunes/miércoles/viernes",
      completar la instancia de un día, confirmar en DB (via App Inspector)
      que las otras fechas no quedan marcadas.
- [ ] `git diff` real adjunto al reporte de cierre (no resumen parafraseado).

## Resultado Esperado
Un `ActivityNode` recurrente puede resolver, para cualquier rango de fechas,
la lista de instancias que le corresponden, respetando excepciones puntuales.
El estado de completado por fecha es correcto y verificable en DB. Today sigue
sin usar nada de esto todavía (EC-012 lo conecta).

## Auditoría
Consultar guía en [AUDITS/README.md](../AUDITS/README.md).
- [ ] ¿Cumple con la arquitectura MVVM/Clean?
- [ ] ¿Las funciones son < 30 líneas?
- [ ] ¿Los archivos son < 300 líneas?
- [ ] ¿`TimelineInstance` quedó realmente sin anotación `@Entity` / sin tabla?
- [ ] ¿`ScheduleRule` evita codificar significado de dominio (sin enum tipo
      `WORKOUT_FREQUENCY`, por ejemplo)?

## Lecciones Aprendidas
(Espacio para documentar hallazgos, dificultades o mejoras descubiertas durante
el desarrollo).

## Definition of Done (Obligatorio)
Antes de marcar como APPROVED, verificar:
- [ ] Compila sin warnings nuevos
- [ ] Tests existentes pasan (unitarios + los que aplique)
- [ ] Checklist de [ARCHITECTURE_INVARIANTS.md](../08_ARCHITECTURE_INVARIANTS.md) revisado y sin violaciones
- [ ] Migración v3→v4 registrada en [MOCK_DATA_STATUS.md](../09_MOCK_DATA_STATUS.md)
- [ ] Lecciones aprendidas documentadas arriba
- [ ] `git diff` real (no resumen) adjunto al reporte de cierre

## Checklist de Validación de Usuario
(El agente auditor completa esta sección SOLO cuando el estado pasa a
USER_REVIEW_PENDING. Lenguaje llano, sin jerga técnica.)
- [ ] En la pantalla de **Detalle de Actividad**, al completar un paso, el estado
      es independiente para cada día (verificable cambiando la fecha del sistema).
- [ ] Los tests unitar
- ios confirman que las reglas de "Lunes/Miércoles/Viernes"
      y las excepciones de "Saltar día" funcionan lógicamente antes de tener UI.

> [!NOTE]
> La validación visual de recurrencias en la pantalla principal ("Today") es
> el objetivo de la **EC-012**, ya que esta EC (011) no incluye cambios en
> la interfaz de Today.


REGLA CRÍTICA: ningún agente puede marcar estos checkboxes ni cambiar el
estado a APPROVED. Solo el usuario lo hace manualmente.

## Estado
El estado vigente de esta EC es el declarado en el campo `status` del
frontmatter (arriba de este documento). No dupliques el valor aquí.
