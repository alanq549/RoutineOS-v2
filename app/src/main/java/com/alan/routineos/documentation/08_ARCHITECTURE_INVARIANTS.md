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

## Política de Metadatos: Definición vs. Ejecución
Se establece una separación estricta entre la estructura de datos y los valores capturados:
1.  **MetadataSchema**: Define la "plantilla" de captura (nombres, tipos y reglas). Es una entidad de configuración vinculada al Nodo o Definición.
2.  **ActivityExecution.metadataJson**: Contiene los valores reales capturados mapeados por `field.id` (ej: `{"schemaVersion": 1, "values": {"f1": 80}}`). Es inmutable frente a cambios posteriores en el `MetadataSchema`.
3.  **Identidad Persistente**: Los valores se vinculan por ID, permitiendo renombrar campos en el esquema sin perder el vínculo histórico con los datos.
4.  **Agnosticismo**: El motor de metadatos solo conoce tipos base (NUMBER, TEXT, etc.), nunca conceptos de dominio (Peso, Reps).

## Entidades núcleo válidas
ActivityDefinition, ActivityNode, MetadataSchema, ScheduleRule, ScheduleException,
TimelineInstance (compute-only, no persistida), ActivityExecution.

## Política Temporal y Timezones
RoutineOS opera bajo la política de **Local Wall Clock Time**. Todas las reglas de
horario (`ScheduleRule.startTime`) se guardan como minutos desde medianoche en el
tiempo local del dispositivo.
*   **Razón**: El usuario ageda "Despertar a las 07:00 AM". Si viaja a otro país,
    la intención sigue siendo despertar a las 07:00 AM en ese país.
*   **UTC**: Solo se utiliza para marcas de tiempo de ejecución real (`completedAt`),
    pero nunca para definir la intención de agendamiento recurrente.

## Checklist obligatorio antes de cerrar cualquier EC
- [ ] ¿Alguna clase, enum o campo nuevo codifica un concepto de dominio específico
      (gym, universidad, hábito, etc.) en vez de ser genérico?
- [ ] ¿Los ejemplos de dominio (gimnasio, universidad) se usaron solo como casos
      ilustrativos en el spec, y NO como restricciones del modelo?
- [ ] ¿El agente de código citó este documento antes de generar el modelo de datos?

Si la respuesta a la primera pregunta es SÍ, el EC no puede cerrarse hasta corregirlo.
