# EC-RE-016 – Plannable Element Domain Analysis

---

## 1. Executive Summary

This document analyses the core concepts of RoutineOS **without** proposing concrete design changes. It classifies each concept according to the existing model (definitions, occurrences, context, constraints, backlog/intention, execution/history) and evaluates whether they can be represented in the current domain, where `DailyInstance` and `DailyInstanceRole` fit, and which relationships (`parentInstanceId`, `associatedInstanceId`, `targetId/targetType`) are applicable.

All conclusions are expressed as **HECHO**, **HIPÓTESIS**, **DECISIÓN**, or **OPEN QUESTION** so that the next design step can be taken based on a vetted analysis.

---

## 2. Core Terminology (Current Model)

| Term | Meaning in RoutineOS |
|------|-----------------------|
| **ActivityDefinition** | Immutable template that describes *what* an activity can be. It owns a tree of **ActivityNode** objects that model the internal structure of the definition.
| **ActivityNode** | Structural element of an `ActivityDefinition`. It is **not** an occurrence; it is part of the definition only.
| **DailyInstance** | Concrete occurrence for a particular day. It references a definition (via `targetId/targetType`) and carries a **DailyInstanceRole** (`ACTIVITY`, `TASK`, `REMINDER`).
| **DailyInstanceRole** | Enumerates the semantic role of a `DailyInstance` – it tells the UI how to treat the occurrence (e.g., show as activity, as task, as reminder).
| **ActionProtocol** | Execution semantics attached to a node/instance (`TIMER`, `CHECK`).
| **ScheduleRule** | Temporal rule that drives recurrence or fixed‑time scheduling for a definition.
| **ActivityExecution** | Runtime record produced when an activity (or a task that is also an activity) is performed.
| **BacklogItem** | Intentional work item that lives **outside** any temporal schedule; it has no `DailyInstance`.
| **Note** | Free‑form information attached to other entities; no temporal dimension.
| **Deadline** | Temporal constraint that belongs to another entity (e.g., a task) and is stored separately.

---

## 3. Concept‑by‑Concept Analysis

### 3.1 Activity

- **Definition**: `ActivityDefinition` + its `ActivityNode` tree.
- **Occurrence**: Represented by a `DailyInstance` with role **ACTIVITY**.
- **Context / Information**: May have associated notes, deadlines via `associatedInstanceId`.
- **Constraints**: Uses `ScheduleRule` for recurrence or fixed time.
- **Backlog / Intention**: Not a backlog item; it is an executable definition.
- **Execution / History**: Generates `ActivityExecution` when the occurrence is performed.

**HECHO** – The current model already separates definition (`ActivityDefinition`) from occurrence (`DailyInstance`).

---

### 3.2 Spontaneous Event

- **Nature**: An ad‑hoc occurrence that is **not** part of any `ActivityDefinition` tree. It is created directly as a `DailyInstance` (or a similar lightweight occurrence) without an underlying `ActivityNode`.
- **Definition**: No persistent definition; the event is defined at creation time.
- **Occurrence**: Stored as a `DailyInstance` with a special flag (e.g., a `ScheduleRule` of type *unscheduled* or a distinct `DailyInstanceRole` if later introduced).
- **Context**: May be linked to other entities via `associatedInstanceId`.
- **Constraints**: Usually **UNSCHEDULED**; no `ScheduleRule` is required.
- **Execution / History**: May generate a minimal execution record for audit, but not an `ActivityExecution` because there is no activity template.

**HECHO** – `Spontaneous Event` is not a specialization of `ActivityNode`.
**HIPÓTESIS** – `Spontaneous Event` can be modelled as a `DailyInstance` with no associated `ActivityDefinition` (i.e., `targetId` empty) and a role that indicates its ad‑hoc nature.
**OPEN QUESTION** – Should a dedicated `DailyInstanceRole` be added for spontaneous events, or can the existing roles suffice with a flag?

---

### 3.3 Task

- **Definition vs Occurrence**: The current model treats *Task* as a **role** of a `DailyInstance` (`DailyInstanceRole.TASK`). There is **no separate Task entity**.
- **Temporal Semantics**: May have a `ScheduleRule` (for recurring tasks) or be a one‑off occurrence.
- **Hierarchy**: Can have child tasks via `parentInstanceId` on the `DailyInstance`.
- **Context**: Can be associated with notes, deadlines via `associatedInstanceId`.
- **Execution**: When a task is performed, it generates an `ActivityExecution` because tasks are a type of activity in the runtime.

**HECHO** – `Task` is already represented by `DailyInstanceRole.TASK`.
**HIPÓTESIS** – The existing `DailyInstance` + role model is conceptually sufficient to capture both reusable (recurring) and punctual tasks.
**OPEN QUESTION** – Is there a need for a separate *Task definition* entity for reusable task templates, or is the current `ActivityDefinition`‑driven approach enough?

---

### 3.4 Reminder

- **Definition vs Occurrence**: Like `Task`, a Reminder lives as a `DailyInstance` with role **REMINDER**.
- **Temporal Semantics**: Usually a single point in time (`POINT`); may have a `ScheduleRule` for recurring reminders.
- **Execution**: Does **NOT** generate an `ActivityExecution`; it only triggers a notification.
- **Hierarchy / Context**: Typically a leaf; can be linked to other entities via `associatedInstanceId`.

**HECHO** – `Reminder` is already expressed by `DailyInstanceRole.REMINDER`.
**HIPÓTESIS** – The current model covers both punctual and recurring reminders without extra entities.
**OPEN QUESTION** – Should reminders ever need a parent‑child relationship (e.g., reminder groups), or is the flat leaf model sufficient?

---

### 3.5 Note

- **Nature**: Pure information / context. It does not have a temporal schedule.
- **Storage**: Stand‑alone entity (`Note`) linked to other entities via `targetId/targetType`.
- **Relations**: Can be attached to any definition or occurrence using `associatedInstanceId`.
- **Execution / History**: No execution semantics.

**HECHO** – `Note` is correctly modelled as an independent entity.

---

### 3.6 Deadline

- **Nature**: Temporal constraint that belongs to another entity (commonly a Task or Activity).
- **Storage**: Independent entity (`Deadline`) with a **POINT** time value.
- **Relation**: Linked to its owner via `targetId/targetType` (or `associatedInstanceId`).
- **Execution**: Does not generate `ActivityExecution`; it only influences scheduling/validation.

**HECHO** – `Deadline` is a separate entity and not a generic reminder.

---

### 3.7 Pending Item (BacklogItem)

- **Nature**: Intentional work that has **no** scheduled time.
- **Storage**: `BacklogItem` entity, representing a *backlog* entry.
- **Temporal Semantics**: **UNSCHEDULED / PENDING**.
- **Relation**: May be associated with other entities via `associatedInstanceId`.
- **Execution**: Becomes a `DailyInstance` only when it is promoted to a scheduled item.

**HECHO** – `BacklogItem` already exists and maps to the *pending* concept.

---

## 4. Temporal Semantics Recap

| Semantics | Where it lives | Applies to |
|-----------|----------------|-----------|
| **BLOCK** | Field on `DailyInstance` (or on a definition’s `ScheduleRule`) | Activities / Tasks that span a time range.
| **POINT** | Field on `DailyInstance` or on `Deadline` | Reminders, single‑point Tasks, Deadlines.
| **UNSCHEDULED** | Implicit when no `ScheduleRule` / no `DailyInstance` | Spontaneous Events, Notes, BacklogItem, ad‑hoc reminders.

**HECHO** – The three temporal tokens remain the core temporal vocabulary.
**HIPÓTESIS** – The place that stores the token (definition vs occurrence) depends on the concept: definitions store recurrence rules, occurrences store the concrete time for the day.

---

## 5. Planning vs Today (Current Separation)

- **Planning**: Logical view that composes *future* intent. It includes all definitions (`ActivityDefinition`), unscheduled items (`BacklogItem`, `Note`), and scheduled items that have not yet materialised into a concrete day (i.e., those with a `ScheduleRule`).
- **Today**: Concrete view of **DailyInstance** objects for the current date. It shows only occurrences that are materialised for *today*.
- An entity can appear in Planning without having a `DailyInstance` for today (e.g., a weekly task that does not occur today).
- Conversely, a `DailyInstance` may appear in Today without a visible entry in Planning if it originated from a spontaneous event.

**HECHO** – This separation already exists in the code base.
**OPEN QUESTION** – Should Planning also surface *context* items (notes, deadlines) that are attached to a future occurrence, or keep them purely in their own sections?

---

## 6. Relationship Invariants Review

| Relationship | Purpose | Concepts that use it |
|--------------|---------|----------------------|
| `parentInstanceId` | Structural hierarchy of **occurrences** (e.g., sub‑tasks) | `DailyInstance` (ACTIVITY, TASK) |
| `associatedInstanceId` | Contextual link between an occurrence/definition and auxiliary information | `DailyInstance` ↔ `Note`, `Deadline`, `BacklogItem` |
| `targetId + targetType` | Semantic relation to another entity (e.g., a Reminder attached to a Task) | Mostly used by `DailyInstance` to point to its definition; also used by `Note`, `Deadline` to point to the owner |

**HECHO** – These invariants are already enforced.
**HIPÓTESIS** – No additional relationship fields are required for the concepts analyzed.

---

## 7. Answers to Requested Questions

1. **¿Qué es una definición?**
   - **HECHO** – En RoutineOS, una *definición* es un objeto persistente que describe **qué** puede suceder. El principal tipo es `ActivityDefinition`, que contiene una jerarquía de `ActivityNode` y opcionalmente un `ScheduleRule`.

2. **¿Qué es una ocurrencia?**
   - **HECHO** – Una *ocurrencia* es una instancia temporal concreta de una definición. Se modela con `DailyInstance` y su `DailyInstanceRole` (ACTIVITY, TASK, REMINDER). Las *Spontaneous Events* también son ocurrencias, pero sin definición asociada.

3. **¿Qué es contexto?**
   - **HECHO** – Información adicional que no tiene propia temporalidad pero está vinculada a una definición u ocurrencia. Se modela con `Note`, `Deadline` y mediante `associatedInstanceId`.

4. **¿Qué es una restricción?**
   - **HECHO** – Un límite temporal que afecta a otra entidad, representado por `Deadline` (punctual) o por reglas de recurrencia en `ScheduleRule`.

5. **¿Qué es backlog?**
   - **HECHO** – Conjunto de intenciones sin tiempo asignado, representado por `BacklogItem`.

6. **¿Qué es ejecución/historial?**
   - **HECHO** – Registro de que una ocurrencia fue realizada. Implementado como `ActivityExecution` (para actividades/tareas) y como auditorías de cambios para notas, deadlines, etc.

7. **¿Qué conceptos pueden planificarse?**
   - **HECHO** – Todo lo que aparece en **Planning**: `ActivityDefinition` (con `ScheduleRule`), `Task` (via `DailyInstanceRole.TASK`), `Reminder` (via `DailyInstanceRole.REMINDER`), `Spontaneous Event` (ad‑hoc), `BacklogItem`, `Note`, `Deadline`.
   - **HIPÓTESIS** – Solo aquellas que tienen o pueden obtener un `ScheduleRule` o que el usuario crea explícitamente desde la UI pueden materializarse como `DailyInstance` para un día concreto.

8. **¿Qué conceptos pueden ejecutarse?**
   - **HECHO** – `Activity` (y sus tareas asociadas) generan `ActivityExecution`. `Reminder` y `Deadline` **no** generan ejecución, sólo disparan notificaciones o validaciones.

9. **¿Cuándo usamos DailyInstance?**
   - **HECHO** – Cuando una entidad necesita una representación **temporal concreta** para un día específico (actividades, tareas, recordatorios, eventos espontáneos). También cuando una tarea recurrente se materializa para el día actual.

10. **¿Cuándo NO usamos DailyInstance?**
    - **HECHO** – Para conceptos que carecen de temporalidad directa: `Note`, `Deadline` (solo es una restricción), `BacklogItem` (intención sin tiempo), y para la definición misma (`ActivityDefinition`).

11. **¿Existe realmente una abstracción común?**
    - **OPEN QUESTION** – Aunque many concepts share the `parentInstanceId` / `associatedInstanceId` relationships, they belong to different layers (definition vs occurrence). Introducing a universal `PlannableElement` interface may blur these layers. The analysis must first determine whether a common abstraction adds value beyond the existing role‑based classification.

---

## 8. Summary of Findings

| Category | Status |
|----------|--------|
| **Definition** | Fully satisfied by `ActivityDefinition` + `ActivityNode`.
| **Occurrence** | Covered by `DailyInstance` with roles; hypothesis remains that it can also host ad‑hoc events.
| **Context / Information** | Handled by `Note` (free text) and `Deadline` (constraint) linked via `associatedInstanceId`.
| **Constraint** | Represented by `Deadline` and `ScheduleRule`.
| **Backlog / Intention** | Represented by `BacklogItem`.
| **Execution / History** | Implemented via `ActivityExecution` and audit logs.
| **Common Abstraction** | Open – further evaluation needed.

---

## 9. Next Steps

1. Validate the **HIPÓTESIS** about spontaneous events being modelled as plain `DailyInstance` objects without a definition.
2. Assess whether any additional `DailyInstanceRole` is required for ad‑hoc occurrences.
3. Keep the current `DailyInstance` as the sole temporal occurrence model until the above hypotheses are confirmed.
4. Re‑evaluate the need for a shared interface only after the concept map stabilises.

---

*End of Document*
