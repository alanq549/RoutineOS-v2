---
id: EC-RE-004
title: Flexible Rules & Scheduling Editor
phase: 2
priority: High
effort: Medium
owner: AI Agent
status: APPROVED
audit: APPROVED
created: 2026-08-15
updated: 2026-08-15
---

# EC-RE-004: Flexible Rules & Scheduling Editor

## Objetivo
Implementar la interfaz de usuario ("El Grifo") para configurar horarios flexibles, rangos y duraciones en el Node Inspector, conectándola con la infraestructura temporal establecida en EC-RE-003.

## Contexto
Aunque el motor de agendamiento ya soporta campos de tiempo en la base de datos (startTime, endTime, durationMinutes), actualmente no hay forma de que el usuario los configure desde la app. Esta EC habilita la edición visual de estas reglas.

## Problema
El "Node Inspector" actual solo tiene botones de shell. Los usuarios no pueden definir cuándo ocurre una actividad, lo que impide que el motor de agendamiento genere proyecciones útiles en Today o Planning.

## Alcance
- [x] **Scheduling Editor Sheet**: Implementación de `ModalBottomSheet` con TimePicker y DaySelector.
- [x] **Multi-Rule Support**: Soporte para múltiples reglas por target (ej: Mañana y Tarde).
- [x] **Persistencia Reactiva**: Guardado inmediato en `ScheduleRuleEntity` con actualización instantánea de la UI.
- [x] **Resumen Visual**: Visualización de horarios activos directamente en el árbol de la actividad (ej: "LMV 08:00").
- [x] **Integración Jerárquica**: Los UseCases resuelven correctamente las reglas en todos los niveles del árbol.

## Validaciones
- [x] Configurar un horario a las 08:00 AM y verificar en App Inspector que se guarda como `480` en `schedule_rules`.
- [x] Añadir dos horarios distintos a la misma actividad y verificar que ambos persisten.
- [x] Cambiar un horario y verificar que las `DailyInstances` virtuales se actualizan inmediatamente en el motor de resolución.

## Auditoría
- [x] ¿Se utiliza el `TimePicker` estándar de Material 3?
- [x] ¿La lógica de conversión de horas (HH:mm -> minutos) está en el Domain?
- [x] ¿El editor es lo suficientemente simple para seguir el principio de Progressive Disclosure?

## Definition of Done (Obligatorio)
- [x] Compila sin warnings nuevos.
- [x] Tests de persistencia de reglas pasan.
- [x] La UI permite configurar horas y días sin crashes.
- [x] Lecciones aprendidas documentadas.
- 
## notas de usario (opcional)
- la creacin de horarios no limita a tener 2 veces el mismo horario exactamente igual, puedo crear n veces el mismo ejm: de lun a vier a las 7:00 "
- los horarios tienen validacion contra UTC ?


## Estado
El estado vigente de esta EC es el declarado en el campo `status` del frontmatter.
