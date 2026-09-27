---
id: EC-RE-016
title: Plannable Element Domain Analysis
phase: 6
priority: High
effort: Medium
owner: AI Agent
status: AUDIT_PENDING
depends_on: [EC-RE-015]
branch: feature/ec-re-016-domain-analysis
audit: Pending
created: 2026-09-06
updated: 2026-09-06
---

# EC-RE-016: Plannable Element Domain Analysis

> **REGLA DE ORO DE ESTA EC**: Esta tarjeta es **exclusivamente de análisis conceptual y formalización de dominio**. No se modifica código fuente Kotlin, base de datos Room, migraciones, DAOs, ViewModels, UI, navegación ni suite de pruebas.

---

## 1. Executive Summary

El propósito fundamental de esta análisis es responder a la pregunta central de arquitectura: **¿Qué es realmente un elemento en RoutineOS?**

A lo largo de la evolución de RoutineOS v2, se incorporaron conceptos como `Activity`, `Spontaneous Event`, `Task`, `Reminder`, `Note`, `Deadline` y `BacklogItem`. La coexistencia de estos conceptos generó la necesidad de definir si cada uno requiere una entidad de base de datos independiente, o si responden a roles y comportamientos sobre un núcleo operativo común.

### **Conclusión Principal del Análisis**
1. **`DailyInstance` es el Núcleo Operativo Universal**: Toda unidad de intención operativa que ocupa o solicita un lugar en el tiempo (fecha, bloque horario o lista sin horario para un día) se representa de forma unificada mediante `DailyInstance`.
2. **Diferenciación por Rol y Protocolo**: `DailyInstance` no se duplica en múltiples entidades para eventos, tareas o recordatorios. En su lugar, utiliza dos dimensiones ortogonales:
   - **`role`** (`ACTIVITY`, `TASK`, `REMINDER`): Define la naturaleza funcional del elemento.
   - **`actionProtocol`** (`TIMER`, `CHECK`): Define el mecanismo de interacción/ejecución.
3. **Entidades Externas Especializadas**: Permanecen fuera de `DailyInstance` los conceptos que representan moldes/reutilización (`ActivityDefinition`), contenedores sin fecha (`BacklogItem`), restricciones temporales absolutas (`Deadline`), y anotaciones textuales (`Note`).

---

## 2. Definition of Plannable Element (Elemento Planificable)

- **[HECHO]**: RoutineOS v2 separa conceptual y físicamente el espacio de intención e intervención futura (**Planning**) del espacio de ejecución y registro en tiempo real (**Today**).
- **[DECISIÓN - DEC-01]**: Se define como **Elemento Planificable (*Plannable Element*)** a cualquier unidad de intención que pueda ser asignada, proyectada, intervenida o creada para una fecha determinada (`scheduledDate`).

### **Atributos Fundamentales de un Elemento Planificable**
Un elemento es planificable si posee:
1. **Anclaje Temporal Diario**: Un `scheduledDate` (Epoch Day).
2. **Formato de Ocupación Temporal**:
   - **`BLOCK`**: Rango explícito con inicio y fin/duración (`plannedStartTime` + `plannedEndTime` / `plannedDurationMinutes`).
   - **`POINT`**: Momento puntual en el día (`plannedStartTime` sin duración).
   - **`UNSCHEDULED`**: Asignado al día pero sin hora fija (`plannedStartTime == null`).
3. **Movilidad Temporal (`mobility`)**: `FLEXIBLE` (reprogramable) o `IMMOBILE` (evento rígido/interrupción).
4. **Semántica Funcional (`role`)**: `ACTIVITY` (rutina/bloque estructural), `TASK` (unidad de acción puntual), `REMINDER` (alerta de atención).
5. **Protocolo de Acción (`actionProtocol`)**: `TIMER` (temporización/ejecución) o `CHECK` (completado binario).

---

## 3. Analysis by Concept (Análisis Concepto por Concepto)

Analizamos individualmente los 7 conceptos contra las dimensiones clave del sistema:

### **3.1 Activity (Actividad Recurrente / Rutina)**
- **[HECHO]**: Se define como plantilla en `ActivityDefinition` + `ActivityNode` y se materializa en días específicos como `DailyInstance`.
- **¿Necesita entidad propia?**: Sí para el molde (`ActivityDefinition` + `ActivityNode`), pero su ocurrencia diaria es una `DailyInstance` (`role = ACTIVITY`).
- **¿Reutilizable?**: Sí, vía `ActivityDefinition`.
- **¿Necesita `ScheduleRule`?**: Sí para la generación recurrente.
- **¿Puede tener hijos?**: Sí. Tiene estructura interna (`ActivityNode`) y puede alojar contexto asociado (`associatedInstanceId`).
- **¿Aparece en Planning y Today?**: Sí.
- **¿Genera `ActivityExecution` e historial?**: Sí, la ejecución registra los metadatos y tiempos reales.
- **Temporalidad**: Típicamente `BLOCK` o `UNSCHEDULED`.

### **3.2 Spontaneous Event (Evento Espontáneo / Ad-Hoc)**
- **[HECHO]**: Es un evento creado sobre la marcha en Today o Planning sin depender de una `ActivityDefinition` ni de una `ScheduleRule`.
- **¿Necesita entidad propia?**: No. Se representa como `DailyInstance` con `isAdHoc = true` y `target = null`.
- **¿Reutilizable?**: No (ocurrencia única).
- **¿Necesita `ScheduleRule`?**: No.
- **¿Puede tener hijos?**: Puede tener ítems asociados contextualmente (`associatedInstanceId`) y notas.
- **¿Aparece en Planning y Today?**: Sí.
- **¿Genera `ActivityExecution` e historial?**: Sí, al completarse registra su ejecución.
- **Temporalidad**: `BLOCK`, `POINT` o `UNSCHEDULED`.

### **3.3 Task (Tarea Puntual)**
- **[HECHO]**: Acción de completado binario. Puede existir de forma independiente o estar vinculada a una Actividad o a un `BacklogItem`.
- **¿Necesita entidad propia?**: No. Se representa como `DailyInstance` con `role = TASK` y `actionProtocol = CHECK`.
- **¿Reutilizable?**: No por sí misma (es una instancia única para un día).
- **¿Necesita `ScheduleRule`?**: No.
- **¿Puede tener hijos?**: No posee nodos estructurales (`Task != ActivityNode`).
- **¿Aparece en Planning y Today?**: Sí.
- **¿Genera `ActivityExecution` e historial?**: Sí, la acción de completado registra el evento de ejecución.
- **Temporalidad**: `POINT` o `UNSCHEDULED` (raramente `BLOCK`).

### **3.4 Reminder (Recordatorio / Alerta)**
- **[HECHO]**: Aviso puntual de atención para una hora fija o relativa.
- **¿Necesita entidad propia?**: No. Se representa como `DailyInstance` con `role = REMINDER`, `actionProtocol = CHECK` y campos `reminderAbs`/`reminderRel`.
- **¿Reutilizable?**: No.
- **¿Necesita `ScheduleRule`?**: No.
- **¿Puede tener hijos?**: No.
- **¿Aparece en Planning y Today?**: Sí (como banner/alerta visual).
- **¿Genera `ActivityExecution` e historial?**: No requiere trazabilidad de ejecución compleja; cambia su estado en `DailyInstanceStatus`.
- **Temporalidad**: `POINT`.

### **3.5 Note (Nota / Anotación Textual)**
- **[HECHO]**: Texto libre adjunto a una definición, elemento de backlog, instancia u ocurrencia.
- **¿Necesita entidad propia?**: Sí (`Note` / `NoteEntity`).
- **¿Reutilizable?**: No, es una anotación contextual vinculada mediante llaves foráneas (`definitionId`, `backlogId`, `instanceId`, `executionId`).
- **¿Necesita `ScheduleRule`?**: No.
- **¿Puede tener hijos?**: No.
- **¿Aparece en Planning y Today?**: Aparece dentro de las tarjetas del elemento al que está adjunta.
- **¿Genera `ActivityExecution`?**: No.

### **3.6 Deadline (Fecha Límite)**
- **[HECHO]**: Restricción temporal objetiva (`dueAt` Epoch Ms) para una fecha u hora límite.
- **¿Necesita entidad propia?**: Sí (`Deadline` / `DeadlineEntity`).
- **¿Reutilizable?**: No, es un marcador de restricción asociado a un `definitionId`, `backlogId` o `instanceId`.
- **¿Necesita `ScheduleRule`?**: No.
- **¿Puede tener hijos?**: No.
- **¿Aparece en Planning y Today?**: Se renderiza como un indicador visual de límite / advertencia de tiempo restante.
- **¿Genera `ActivityExecution`?**: No.

### **3.7 Pending Item / BacklogItem (Bolsa de Pendientes)**
- **[HECHO]**: Ítem que no tiene fecha asignada aún. Es un estanque de intenciones sin anclaje temporal diario.
- **¿Necesita entidad propia?**: Sí (`BacklogItem` / `BacklogItemEntity`).
- **¿Reutilizable?**: Puede ser instanciado en un día determinado creando una `DailyInstance` vinculada vía `backlogId`.
- **¿Necesita `ScheduleRule`?**: No.
- **¿Puede tener hijos?**: No.
- **¿Aparece en Planning y Today?**: Aparece en el panel de catálogo / pendientes de Planning. Cuando se asigna a un día, pasa al timeline como `DailyInstance`.
- **¿Genera `ActivityExecution`?**: Al completarse la `DailyInstance` instanciada desde el backlog.

---

## 4. Behavioral Dimensions (Matriz Comparativa de Comportamiento)

| Concepto | Entidad DB Principal | Mold / Template | Rol (`DailyInstanceRole`) | Protocolo (`ActionProtocol`) | Riel Temporal | Hijos Estructurales |
|---|---|---|---|---|---|---|
| **Activity** | `DailyInstanceEntity` | `ActivityDefinition` | `ACTIVITY` | `TIMER` | `BLOCK` / `UNSCHEDULED` | Sí (`ActivityNode`) |
| **Spontaneous Event** | `DailyInstanceEntity` | Ninguno (Ad-hoc) | `ACTIVITY` / `TASK` | `TIMER` / `CHECK` | `BLOCK` / `POINT` | No |
| **Task** | `DailyInstanceEntity` | Ninguno / `BacklogItem` | `TASK` | `CHECK` | `POINT` / `UNSCHEDULED` | No |
| **Reminder** | `DailyInstanceEntity` | Ninguno | `REMINDER` | `CHECK` | `POINT` | No |
| **Note** | `NoteEntity` | N/A (Anotación) | N/A | N/A | N/A | No |
| **Deadline** | `DeadlineEntity` | N/A (Restricción) | N/A | N/A | N/A | No |
| **BacklogItem** | `BacklogItemEntity` | Contenedor sin fecha | N/A | N/A | N/A | No |

---

## 5. DailyInstance Analysis (Análisis de `DailyInstance` como Núcleo Universal)

- **[HECHO]**: En la versión actual de RoutineOS v2, la tabla `daily_instances` en Room contiene los siguientes campos:
  ```sql
  id TEXT PRIMARY KEY,
  targetId TEXT,
  targetType TEXT, -- DEFINITION, NODE, AD_HOC
  scheduledDate INTEGER,
  titleSnapshot TEXT,
  descriptionSnapshot TEXT,
  plannedStartTime INTEGER,
  plannedEndTime INTEGER,
  plannedDurationMinutes INTEGER,
  status TEXT, -- PLANNED, MODIFIED, COMPLETED, OMITTED
  mobility TEXT, -- FLEXIBLE, IMMOBILE
  sourceRuleId TEXT,
  parentInstanceId TEXT,
  backlogId TEXT,
  actionProtocol TEXT, -- TIMER, CHECK
  role TEXT, -- ACTIVITY, TASK, REMINDER
  reminderAbs INTEGER,
  reminderRel INTEGER,
  associatedInstanceId TEXT
  ```

### **[DECISIÓN - DEC-02]: Universatilidad de `DailyInstance`**
`DailyInstance` es el **único nodo operativo de agenda** para representar cualquier ocurrencia en un día determinado, abarcando actividades, eventos espontáneos, tareas y recordatorios.

### **[DECISIÓN - DEC-03]: Invariantes de Relación en `DailyInstance`**
Para evitar ambigüedades jerárquicas y mezclas estructurales, se preservan estrictamente las siguientes invariantes:

1. **`parentInstanceId` = Jerarquía Estructural Exclusiva de Ocurrencias**:
   - Define la relación padre-hijo entre instancias de la misma actividad/árbol.
   - **Regla Estricta**: `Task != ActivityNode`. Una Tarea NO es un sub-nodo de una Actividad.

2. **`associatedInstanceId` = Asociación Contextual Explicita**:
   - Permite vincular una Tarea, Recordatorio o Evento a una ocurrencia concreta sin alterar la jerarquía de la rutina.
   - **Regla Estricta**: `associatedInstanceId != parentInstanceId`.

3. **`targetId` + `targetType` = Orientación Semántica**:
   - Apunta a la `ActivityDefinition` o `ActivityNode` de origen. Permite saber a qué "molde" pertenece la instancia materializada.

---

## 6. ActivityDefinition vs DailyInstance

### **[HECHO]**:
- `ActivityDefinition` + `ActivityNode` representan el **molde / diseño abstracto** de una rutina.
- `DailyInstance` representa la **materialización / ocurrencia real** para un día específico.

### **Matriz de Diferenciación**:
```text
  [ ActivityDefinition ] (Molde Reutilizable)
          │
          ├── [ ScheduleRule ] (Regla de Recurrencia)
          │         │
          │         ▼
          └── [ DailyInstance ] (Ocurrencia en un Día Concreto)
                    │
                    ├── role = ACTIVITY / TASK / REMINDER
                    ├── actionProtocol = TIMER / CHECK
                    └── status = PLANNED / COMPLETED / OMITTED
```

---

## 7. BacklogItem (Bolsa de Pendientes)

- **[HECHO]**: `BacklogItem` representa intenciones que el usuario desea realizar pero aún no ha decidido cuándo.
- **[DECISIÓN - DEC-04]**:
  - `BacklogItem` es una entidad fuera de `DailyInstance` porque no posee un `scheduledDate`.
  - Cuando el usuario arrastra o asigna un `BacklogItem` a un día en Planning o Today, el sistema **crea una `DailyInstance`** con `backlogId = backlogItem.id`.
  - Completar la `DailyInstance` en Today actualiza el estado del `BacklogItem` a `RESOLVED`.

---

## 8. Temporal Semantics (Semántica Temporal de Planificación)

- **[DECISIÓN - DEC-05]**: La temporalidad de un elemento planificable no requiere entidades diferentes, sino combinaciones de los atributos `plannedStartTime`, `plannedEndTime` y `plannedDurationMinutes` en `DailyInstance`:

1. **`BLOCK` (Bloque de Tiempo)**:
   - `plannedStartTime != null` AND (`plannedEndTime != null` OR `plannedDurationMinutes != null`).
   - Ejemplos: Clase de Universidad (10:00 - 12:00), Sesión de Gym (60 min).

2. **`POINT` (Punto Fijo en el Tiempo)**:
   - `plannedStartTime != null` AND `plannedEndTime == null` AND `plannedDurationMinutes == null`.
   - Ejemplos: Recordatorio de tomar medicina a las 08:00, Llamada rápida a las 15:00.

3. **`UNSCHEDULED` (Pendiente del Día / Sin Hora)**:
   - `plannedStartTime == null`.
   - Ejemplos: Tarea para hacer en el día sin horario específico, Actividad flexible.

---

## 9. Planning vs Today

### **[HECHO]**:
- **Planning** opera sobre la **intención futura**. Modifica `DailyInstance` en estado `PLANNED` o `MODIFIED`, crea excepciones (`ScheduleException`) y manipula el backlog. **Planning nunca genera `ActivityExecution`**.
- **Today** opera sobre la **ejecución en tiempo real**. Registra la realidad, completa instancias, captura metadatos cuantitativos (`metadataJson`) y escribe registros en `ActivityExecution`.

### **Tabla de Responsabilidades por Espacio**:

| Operación | Espacio Planning | Espacio Today | Impacto en DB |
|---|---|---|---|
| Reprogramar hora / día | SÍ | SÍ | Modifica / Materializa `DailyInstance` |
| Omitir ocurrencia (`SKIP`) | SÍ | SÍ | Estado `OMITTED` en `DailyInstance` |
| Completar (`COMPLETE`) | **NO** | SÍ | Estado `COMPLETED` + Crea `ActivityExecution` |
| Capturar metadatos (kgs, reps) | **NO** | SÍ | Escribe `ActivityExecution.metadataJson` |
| Crear evento ad-hoc / espontáneo | SÍ | SÍ | Crea `DailyInstance` (`isAdHoc = true`) |

---

## 10. Candidate Domain Models (Verificación del Modelo Actual)

### **[HECHO]**:
Tras evaluar los requerimientos del elemento planificable universal, se confirma que el modelo de datos actual de RoutineOS v2 (Room v10) es **100% suficiente** para soportar toda la semántica requerida sin necesidad de alterar tablas ni agregar nuevos campos.

```kotlin
// Dominio Universal Existente en RoutineOS v2
data class DailyInstance(
    val id: String,
    val target: ScheduleTarget?,             // Orientación Semántica
    val scheduledDate: Long,                 // Epoch Day (Anclaje Temporal)
    val titleSnapshot: String,
    val descriptionSnapshot: String,
    val plannedStartTime: Int? = null,       // BLOCK / POINT / UNSCHEDULED
    val plannedEndTime: Int? = null,
    val plannedDurationMinutes: Int? = null,
    val status: DailyInstanceStatus = DailyInstanceStatus.PLANNED,
    val mobility: TemporalMobility = TemporalMobility.FLEXIBLE,
    val sourceRuleId: String? = null,
    val isAdHoc: Boolean = false,
    val parentInstanceId: String? = null,    // Jerarquía Estructural Exclusiva
    val backlogId: String? = null,           // Vínculo con Backlog
    val actionProtocol: ActionProtocol = ActionProtocol.TIMER, // TIMER / CHECK
    val role: DailyInstanceRole = DailyInstanceRole.ACTIVITY,   // ACTIVITY / TASK / REMINDER
    val reminderAbs: Int? = null,
    val reminderRel: Int? = null,
    val associatedInstanceId: String? = null // Asociación Contextual Explicita
)
```

---

## 11. Open Questions (Preguntas Abiertas para Futuras Fases)

- **[OPEN QUESTION - Q1]**: ¿Debería existir un mecanismo de sincronización automática bidireccional donde desmarcar una `DailyInstance` desmarque también el `BacklogItem` si fue instanciada desde el backlog?
- **[OPEN QUESTION - Q2]**: En el caso de los `REMINDER` independientes, ¿debería registrarse una entrada en `ActivityExecution` para métricas de puntualidad en `Stats`, o mantenerse estrictamente como cambio de estado en `DailyInstance`?
- **[OPEN QUESTION - Q3]**: Cuando una `ActivityDefinition` con fecha límite (`Deadline`) genera instancias diarias, ¿debería heredarse la advertencia visual de `Deadline` en cada ocurrencia de `Today`?

---

## 12. Proposed Domain Contract (Contrato de Dominio Propuesto)

Se formaliza el siguiente contrato conceptual para el manejo de elementos planificables en RoutineOS v2:

```kotlin
/**
 * Representación unificada de cualquier unidad de intención asignable en Planning o Today.
 */
interface PlannableElement {
    val id: String
    val scheduledDate: Long
    val title: String
    val role: DailyInstanceRole
    val actionProtocol: ActionProtocol
    val temporalFormat: TemporalFormat
    val isAdHoc: Boolean
}

enum class TemporalFormat {
    BLOCK,       // Rango de tiempo explícito
    POINT,       // Momento puntual
    UNSCHEDULED  // Asignado al día sin hora fija
}
```

---

## 13. Decision Log (Registro Oficial de Decisiones)

| ID | Tema | Decisión Adoptada | Justificación |
|---|---|---|---|
| **DEC-01** | Definición de Elemento Planificable | Cualquier unidad de intención con `scheduledDate` asignado. | Unifica la vista de Planning y Today bajo un mismo contrato operativo. |
| **DEC-02** | Núcleo Operativo Universal | `DailyInstance` es el único nodo para actividades, eventos espontáneos, tareas y recordatorios. | Evita la proliferación de tablas duplicadas en Room y simplifica los UseCases. |
| **DEC-03** | Invariantes de Relaciones | Estricta separación: `parentInstanceId` (Estructura), `associatedInstanceId` (Contexto), `target` (Semántica). | Previene que tareas asociadas se expandan erróneamente como pasos de una rutina. |
| **DEC-04** | Integración de Backlog | `BacklogItem` vive fuera de `DailyInstance` hasta ser asignado a una fecha. | Mantiene limpia la tabla de agenda diaria sin contaminarla con pendientes sin fecha. |
| **DEC-05** | Formato Temporal | Se deduce de `plannedStartTime`, `plannedEndTime` y `plannedDurationMinutes`. | No requiere nuevos enums ni columnas en base de datos. |
| **DEC-06** | Rol vs Protocolo | `role` (`ACTIVITY`/`TASK`/`REMINDER`) es independiente de `actionProtocol` (`TIMER`/`CHECK`). | Permite tareas con temporizador o actividades con completado binario. |
| **DEC-07** | Notas y Límite | `Note` y `Deadline` permanecen como entidades externas relacionales. | Preserva la responsabilidad única de cada entidad. |
| **DEC-08** | Cero Cambios de Código en EC-16 | Esta EC no modifica ningún archivo de código fuente del proyecto. | Garantiza un análisis formal riguroso antes de cualquier implementación. |

---

## 14. Implementation Consequences (Consecuencias para Próximas ECs)

1. **`EC-RE-017` (Unified Planning Workspace)**: Consolidará la pantalla de `Planning` consumiendo `DailyInstance` de forma unificada sin sub-pestañas.
2. **`EC-RE-018` (Backlog & Unscheduled Panel)**: Implementará el panel desplegable de pendientes basado en `BacklogItem` e instancias `UNSCHEDULED`.
3. **Mantenibilidad de Código**: Los casos de uso (`GetHierarchicalTimelineUseCase`, `ResolveTimelineUseCase`, `RegisterDailyActionUseCase`) continuarán operando sobre la estructura limpia y validada de `DailyInstance`.
