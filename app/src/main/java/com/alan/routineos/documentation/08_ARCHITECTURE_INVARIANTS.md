# Invariantes de Arquitectura — RoutineOS (NO NEGOCIABLES)

## Principio central
RoutineOS es un motor de planificación temporal domain-agnostic. NINGÚN concepto de
dominio (gym, universidad, hábito, entrenamiento, tarea) puede existir como tipo,
enum o campo hardcodeado en el modelo de datos ni en la lógica de negocio.

## Regla explícita
PROHIBIDO: enums como `ActivityType { WORKOUT, TASK, HABIT, EVENT, ROUTINE }`
o cualquier variante que codifique dominio en tiempo de compilación.

PERMITIDO: el usuario define tipos, categorías y campos de metadata en runtime,
persistidos como datos (JSON column / tablas de esquema), nunca como enums de Kotlin
ligados a un dominio específico.

## Entidades núcleo válidas
ActivityDefinition, ActivityNode, MetadataSchema, ScheduleRule, ScheduleException,
TimelineInstance (compute-only, no persistida), ActivityExecution.

## Checklist obligatorio antes de cerrar cualquier EC
- [ ] ¿Alguna clase, enum o campo nuevo codifica un concepto de dominio específico
      (gym, universidad, hábito, etc.) en vez de ser genérico?
- [ ] ¿Los ejemplos de dominio (gimnasio, universidad) se usaron solo como casos
      ilustrativos en el spec, y NO como restricciones del modelo?
- [ ] ¿El agente de código citó este documento antes de generar el modelo de datos?

Si la respuesta a la primera pregunta es SÍ, el EC no puede cerrarse hasta corregirlo.
