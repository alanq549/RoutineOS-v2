# EC-RE-019: Corrección de Semántica del Catálogo en Planning

## Estado
**DRAFT** - Pendiente de aprobación

## Contexto

### Antecedentes
- **EC-RE-017**: Planning Workspace Consolidation introdujo el flujo "AÑADIR DE CATÁLOGO" que permite seleccionar una Activity desde el catálogo en Planning.
- **EC-RE-012**: Planner Interactivity introdujo `AddActivityToDayUseCase` para materializar actividades en días específicos.

### Problema Identificado
El flujo actual del catálogo en Planning tiene una **semántica incorrecta**:

1. **Confusión de modelos**: Al tocar una Activity en el catálogo, se crea una `DailyInstance` materializada mediante `AddActivityToDayUseCase`, aunque la Activity ya debería aparecer automáticamente en Planning si tiene una `ScheduleRule` que aplica al día seleccionado.

2. **Duplicación**: No existe deduplicación. El usuario puede tocar la misma Activity múltiples veces, creando múltiples `DailyInstance` para el mismo día.

3. **Semántica ambigua de `isAdHoc`**: `AddActivityToDayUseCase` crea instancias con `isAdHoc = false` y `sourceRuleId = null`, lo cual es semánticamente inconsistente (no es ad-hoc, pero tampoco viene de una regla).

4. **UX confusa**: El usuario espera que tocar una Activity en el catálogo la "agregue" a Planning, pero el modelo correcto es que las Activities con ScheduleRules ya "están" en Planning automáticamente.

## Objetivo

Corregir la semántica del catálogo en Planning para que:

1. **Clarifique el modelo mental**: El usuario entienda que Planning muestra automáticamente las Activities con ScheduleRules aplicables.

2. **Separe las operaciones**:
   - **Ver/Editar definición**: Navegar al detalle de la Activity.
   - **Crear excepción puntual**: Acción explícita para forzar una ocurrencia cuando la regla no aplica.

3. **Prevenga duplicación**: Implementar deduplicación apropiada basada en la identidad semántica de las ocurrencias.

4. **Corrija la semántica de `isAdHoc`**: Las ocurrencias puntuales sin regla deben tener `isAdHoc = true`.

## Análisis de Dominio

### Modelo Correcto de RoutineOS

```
┌─────────────────────────────────────────────────────────────────┐
│                        ACTIVITIES                               │
│                                                                 │
│  ActivityDefinition                                             │
│    ├── ActivityNode (pasos/jerarquía)                          │
│    ├── ScheduleRule (cuándo ocurre recurrentemente)           │
│    └── LifeSystem (categoría)                                   │
│                                                                 │
│  Propósito: Definir qué es la rutina, sus pasos y cuándo       │
│            debe ocurrir.                                         │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                        PLANNING                                 │
│                                                                 │
│  selectedDate                                                   │
│      ↓                                                          │
│  TimelineResolutionEngine                                       │
│      ↓                                                          │
│  1. Proyección automática desde ScheduleRules aplicables      │
│     → Instancias VIRTUALES (isMaterialized = false)             │
│  2. Instancias MATERIALIZADAS persistidas                     │
│     → Ediciones, excepciones, ad-hoc                           │
│      ↓                                                          │
│  Timeline combinado                                             │
│      ↓                                                          │
│  Intervenciones permitidas:                                     │
│  ├── Move (mover horario)                                      │
│  ├── Skip (omitir ocurrencia)                                  │
│  ├── Reset (deshacer intervención)                             │
│  ├── Edit time (cambiar horario puntual)                       │
│  ├── Create Task/Reminder/Spontaneous Event                  │
│  └── Add Exception (FORZAR ocurrencia cuando regla no aplica)   │
│                                                                 │
│  Propósito: Ver el plan automático del día y realizar           │
│            ajustes puntuales.                                    │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                         TODAY                                   │
│                                                                 │
│  Hoy                                                            │
│      ↓                                                          │
│  Ocurrencias actuales (desde Planning)                         │
│      ↓                                                          │
│  Ejecución                                                      │
│      ↓                                                          │
│  ActivityExecution (registro de completitud)                   │
│                                                                 │
│  Propósito: Ejecutar y registrar el día actual.                │
└─────────────────────────────────────────────────────────────────┘
```

### Análisis de `isAdHoc`

#### Usos Actuales en Código

| Archivo | Uso | Significado en contexto |
|---------|-----|------------------------|
| `AddActivityToDayUseCase.kt:36` | `isAdHoc = false` | "Materialización de definición estructural" (pero sin `sourceRuleId`) |
| `AssignBacklogItemToDayUseCase.kt:48` | `isAdHoc = (item.definitionId == null)` | Es ad-hoc si el backlog item no tiene definición vinculada |
| `ConflictDetectorUseCase.kt:69` | `current.isAdHoc && other.isAdHoc` | Para determinar si una interrupción es legítima entre dos instancias ad-hoc |
| `PlanningViewModel.kt:398, 555, 711` | `isAdHoc = true` | Evento espontáneo creado sin vinculación semántica previa |
| `ActivityMappers.kt:160, 173` | Mapeo a/desde entidad | `isAdHoc = targetType == "AD_HOC"` |

#### Definición Semántica Real

`isAdHoc` representa: **"Origen de creación sin vinculación estructural previa"**

| Valor | Significado |
|-------|-------------|
| `true` | La instancia fue creada sin apuntar a un `ScheduleRule` existente. No es proyección de una regla recurrente. |
| `false` | La instancia fue creada como proyección de un `ScheduleRule` (aunque `sourceRuleId` pueda ser null en casos borde). |

**IMPORTANTE:** `isAdHoc` **NO significa**:
- "Es excepción" → No, las excepciones usan `ScheduleException`
- "Es puntual" → No, todas las instancias son puntuales para una fecha
- "No es recurrente" → No, la recurrencia la define `ScheduleRule`, no la instancia

#### Inconsistencia en `AddActivityToDayUseCase`

El comentario en el código dice:
```kotlin
isAdHoc = false // It's an occurrence of a structural target
```

Pero esto es **semánticamente incorrecto** porque:
1. No es una ocurrencia de un `ScheduleRule` (`sourceRuleId = null`)
2. No es proyección de regla recurrente
3. Es una creación manual puntual, que por definición debería ser `isAdHoc = true`

### Análisis de Deduplicación

#### La Regla `(target, date)` es Insuficiente

**Contraejemplo válido:**
```
Gym 08:00–09:00 (mañana)
Gym 18:00–19:00 (tarde)
```

¿Son dos ocurrencias de la misma Activity? **Sí.**
¿Deberían ser dos `DailyInstance` distintas? **Depende del modelo.**

#### Opciones de Identidad para Deduplicación

| Opción | Clave de unicidad | Permite dos Gym en mismo día | Complejidad | Evaluación |
|--------|-------------------|------------------------------|-------------|------------|
| **A** | `(target, date)` | ❌ No | Baja | **Incorrecta** - No permite escenario válido de dos horarios |
| **B** | `(target, date, startTime)` | ✅ Sí | Media | **Parcial** - Permite dos con diferente hora, pero ¿qué si no tienen hora? |
| **C** | `(target, date, id)` | ✅ Sí (sin límite) | Baja pero caótica | **Incorrecta** - Permite duplicados infinitos |
| **D** | `(sourceRuleId, date)` para virtuales, `(id)` para materializadas | ✅ Sí con reglas | Alta | **Compleja** - Diferente lógica según origen |
| **E** | `(target, date, sourceRuleId, isAdHoc)` | ✅ Sí según contexto | Media-Alta | **Recomendada** - Considera origen y naturaleza |

#### Recomendación: Opción E con Lógica Diferenciada

```kotlin
fun getIdentityKey(instance: DailyInstance): String {
    return when {
        // Instancia de regla (virtual o materializada de virtual)
        instance.sourceRuleId != null -> {
            "rule:${instance.sourceRuleId}:${instance.scheduledDate}"
        }
        // Instancia ad-hoc (puntual, sin regla)
        instance.isAdHoc -> {
            // Las ad-hoc se identifican por su ID único
            // No se deduplican entre sí (permiten múltiples)
            "adhoc:${instance.id}"
        }
        // Materialización forzada (el caso problemático actual)
        else -> {
            // No debería existir: ni de regla, ni ad-hoc
            // Si existe, es error de modelo
            "forced:${instance.target}:${instance.scheduledDate}"
        }
    }
}
```

#### Reglas de Deduplicación por Tipo de Operación

| Operación | Deduplicación | Lógica |
|-----------|---------------|--------|
| **Proyección virtual** | Por `(sourceRuleId, date)` | Una por regla por día |
| **Materialización de virtual** | Mantiene ID de virtual | Transición, no duplicado |
| **Creación ad-hoc** | Por `(target, date, startTime)` opcional | Previene mismo horario, permite diferente |
| **Materialización forzada** (actual) | **NINGUNA** (problema) | Crea duplicados ilimitados |

### Escenarios Obligatorios - Respuestas

#### Escenario A: Activity con ScheduleRule, Hoy = Aplica

```
Activity: Gym
ScheduleRule: Lunes, Miércoles, Viernes a las 08:00
Hoy: Lunes
```

**Comportamiento Esperado:**

| Momento | Estado Esperado | Implementación Correcta |
|---------|-----------------|------------------------|
| Planning carga | Gym 08:00 aparece automáticamente (virtual) | `TimelineResolutionEngine` proyecta instancia virtual |
| Usuario abre catálogo | Ve "Gym" en lista | `ActivityCatalog` muestra todas las Activities |
| Usuario toca "Gym" | **Abre `ActivityDetail`** | Navegación normal, sin crear instancia |
| En `ActivityDetail` | Ve nodos, ScheduleRules, sistema | Edición de definición, no de ocurrencia |
| Vuelve a Planning | Gym sigue apareciendo (como antes) | Sin cambios, no se creó duplicado |

**Comportamiento Actual (Incorrecto):**

| Momento | Estado Actual (Problema) |
|---------|------------------------|
| Usuario toca "Gym" | Crea NUEVA `DailyInstance` materializada via `AddActivityToDayUseCase` |
| Resultado | **DOS instancias de Gym**: una virtual (proyectada) + una materializada (creada) |
| Planificación | Muestra duplicado en el timeline |

#### Escenario B: Activity con ScheduleRule, Hoy = No Aplica

```
Activity: Gym
ScheduleRule: Lunes, Miércoles, Viernes
Hoy: Martes
```

**Situación:**
- Gym NO aparece automáticamente en Planning (la regla no aplica)
- El usuario quiere hacer Gym hoy como **excepción puntual**

**Opciones de Implementación:**

| Opción | Descripción | UX | Implementación |
|--------|-------------|-----|----------------|
| **A** | Crear `DailyInstance` ad-hoc | Usuario abre catálogo, ve Gym con indicador "No programado hoy", aprieta "Forzar hoy", confirma | `AddActivityToDayUseCase` con `isAdHoc = true`, deduplicación por `(target, date)` para ad-hoc |
| **B** | Modificar ScheduleRule temporalmente | Usuario edita la regla para añadir Martes, luego la revierte | Complejo, no recomendado |
| **C** | Crear nueva ScheduleRule puntual | Usuario crea regla "Martes" solo para esta semana | Crea recurrencia no deseada |

**Recomendación:** Opción A - Crear `DailyInstance` ad-hoc con flujo UX explícito.

#### Escenario C: Activity Sin ScheduleRule

```
Activity: Meditación
ScheduleRule: Ninguna
```

**Situación:**
- Activity existe en el sistema
- NUNCA aparece automáticamente en Planning
- El usuario puede querer "planificarla" puntualmente

**Pregunta Clave:** ¿Debería el catálogo en Planning mostrar Activities sin ScheduleRule?

| Opción | Argumentos a favor | Argumentos en contra | Recomendación |
|--------|-------------------|---------------------|---------------|
| **Sí, mostrar** | El usuario puede querer crear ocurrencias puntuales; el catálogo es el lugar natural para encontrar Activities | Puede confundir: "¿Por qué veo esto si no está planificado?" | ⚠️ Aceptable con indicador claro |
| **No, ocultar** | Mantiene claridad: Planning solo muestra Activities que "pueden" estar en Planning (tienen reglas) | Dificulta crear ocurrencias puntuales para Activities sin regla | ❌ Limita funcionalidad |

**Recomendación:** Mostrar con indicador visual claro: "Sin horario regular" + botón "Planificar puntualmente".

#### Escenario D: Dos Ocurrencias Legítimas Mismo Día

```
Activity: Gym
Ocurrencia 1: 08:00–09:00 (mañana)
Ocurrencia 2: 18:00–19:00 (tarde)
```

**¿Cómo se representan?**

| Opción | Implementación | Pros | Contras |
|--------|---------------|------|---------|
| **Dos ScheduleRules** | Regla 1: Lunes 08:00, Regla 2: Lunes 18:00 | Clara separación semántica; cada regla tiene su identidad | Más entities; más complejidad |
| **Una ScheduleRule con múltiples horarios** | No soportado actualmente | Menos entities | Requeriría cambio de modelo |
| **Una regla + una ad-hoc** | Mañana: regla, Tarde: ad-hoc | Flexible | Inconsistencia semántica (una es regla, otra no) |

**Recomendación:** Opción 1 (Dos ScheduleRules) - Mantiene coherencia semántica del modelo.

**Implicación para deduplicación:**
- Clave de unicidad: `(sourceRuleId, date)` para instancias de regla
- Dos reglas = dos ocurrencias legítimas permitidas

#### Escenario E: Instancia Virtual Modificada

```
Activity: Gym proyectada virtualmente 08:00
Usuario: Mueve a 09:00
```

**Flujo Correcto:**

| Paso | Estado | Acción |
|------|--------|--------|
| 1 | Virtual existe | `isMaterialized = false`, `id = "virtual_rule123_20240115"` |
| 2 | Usuario edita | Selecciona nueva hora 09:00 |
| 3 | Sistema materializa | Convierte virtual en persistida:
- Mismo ID (ahora persistente)
- `isMaterialized = true`
- `plannedStartTime = 09:00`
- `status = MODIFIED` |
| 4 | Resultado | **Una sola instancia**, transición de virtual a materializada |

**¿Es duplicado?** No, es la **misma instancia** con cambio de estado.

#### Escenario F: Activity Omitida por Excepción

```
Activity: Gym proyectada para hoy
Excepción: OMITTED para hoy
```

**Flujo:**

```kotlin
// TimelineResolutionEngine.shouldProjectVirtual()
ruleExceptions = exceptions.filter { it.scheduleRuleId == rule.id }
if (ruleExceptions.any { it.originalDate == epochDay }):
    return false  // HOY está bloqueado por excepción OMITTED
```

**Resultado:** Gym **NO aparece** en Planning.

**¿Puede el usuario crear manualmente otra ocurrencia?**

| Opción | Implicación | Recomendación |
|--------|-------------|---------------|
| **Sí, sin restricciones** | Anula el propósito de la excepción | ❌ No recomendado |
| **Sí, con advertencia** | "Has omitido esta actividad hoy. ¿Crear de todas formas?" | ⚠️ Aceptable |
| **No** | Respeta la intención de la excepción | ⚠️ Puede ser frustrante si fue error |

**Recomendación:** Opción 2 - Permitir con advertencia explícita.

---

## Decisiones de Diseño

### DEC-01: Semántica de `isAdHoc`

**Decisión:** `isAdHoc` significa **"creado sin vinculación a ScheduleRule existente"**.

**Implicaciones:**
- `isAdHoc = true`: Instancia creada puntualmente, no proyección de regla
- `isAdHoc = false`: Instancia proveniente de (o materialización de) un ScheduleRule

**Corrección a aplicar:**
- `AddActivityToDayUseCase` debe crear con `isAdHoc = true` (es creación puntual sin regla)

### DEC-02: Operación al tocar Activity en catálogo

**Decisión:** Tocar una Activity en el catálogo **abre su detalle** (`ActivityDetail`), no crea una ocurrencia.

**Justificación:**
- Separa claramente "ver/editar definición" de "crear ocurrencia"
- Permite al usuario inspeccionar la Activity antes de decidir crear una excepción
- Evita creación accidental de duplicados

### DEC-03: Creación de excepción puntual

**Decisión:** Crear una excepción puntual es una **acción explícita separada**, no el comportamiento por defecto.

**Flujo:**
1. Usuario toca Activity en catálogo
2. Se abre `ActivityDetail`
3. En `ActivityDetail`, botón "Crear excepción para [fecha]"
4. Sistema verifica si ya existe ocurrencia (virtual o materializada)
5. Si no existe, crea `DailyInstance` con:
   - `isAdHoc = true`
   - `sourceRuleId = null`
   - `status = PLANNED` (inicialmente)
   - Deduplicación: verifica `(target, date, isAdHoc=true)`

### DEC-04: Deduplicación

**Decisión:** La deduplicación considera el **origen y naturaleza** de la instancia.

**Reglas:**

| Tipo de instancia | Clave de unicidad | Permite múltiples |
|-------------------|-------------------|-------------------|
| Virtual (proyectada) | `(sourceRuleId, date)` | ❌ Una por regla por día |
| Materializada de virtual | `id` (mantiene ID de virtual) | ❌ Es la misma instancia |
| Ad-hoc (puntual) | `(target, date, plannedStartTime)` | ✅ Sí, si horario diferente |
| Materialización forzada (actual) | **NINGUNA** (problema) | ✅ Ilimitadas (bug) |

**Corrección:**
- `AddActivityToDayUseCase` (o su sucesor) debe verificar:
  - ¿Existe ya ad-hoc del mismo `target` en misma `date` con mismo `plannedStartTime`?
  - Si sí: retornar existente o mostrar error según contexto
  - Si no: crear nueva

### DEC-05: Corrección de EC-RE-017

**Decisión:** No reabrir EC-RE-017. Crear **EC-RE-019** (este documento) como corrección formal.

**Justificación:**
- EC-RE-017 fue auditada y marcada como "PASS" con el flujo problemático
- Reabrirla invalidaría el trabajo de auditoría previo
- Una nueva EC mantiene trazabilidad y permite documentar la corrección formalmente

---

## Especificación Técnica

### Cambios en Componentes

#### 1. `ActivityCatalogScreen` / `ActivityCard`

**Cambio:** Modificar comportamiento de `onClick` en modo sheet.

**Actual:**
```kotlin
onActivityClick = { activityId ->
    onAddActivityFromCatalog(activityId)  // Crea instancia
    showCatalogSheet = false
}
```

**Nuevo:**
```kotlin
onActivityClick = { activityId ->
    onNavigateToActivityDetail(activityId)  // Abre detalle
    showCatalogSheet = false
}
```

**Nuevo botón en `ActivityDetail`:**
```kotlin
// En ActivityDetailScreen, visible solo si viene de Planning
Button("Crear excepción para [fecha]") {
    onCreateException(activityId, selectedDate)
}
```

#### 2. `AddActivityToDayUseCase` (o sucesor)

**Cambio:** Corregir semántica y agregar deduplicación.

**Nombre propuesto:** `AddExceptionActivityUseCase`

**Implementación:**
```kotlin
class AddExceptionActivityUseCase @Inject constructor(
    private val repository: ActivityRepository
) {
    suspend operator fun invoke(
        activity: ActivityDefinition,
        date: LocalDate,
        startTimeMinutes: Int? = null,
        endTimeMinutes: Int? = null
    ): Result<DailyInstance> {
        val epochDay = date.toEpochDay()
        
        // 1. Verificar si ya existe proyección virtual
        val projectedInstances = repository.getProjectedInstancesForDate(epochDay)
            .filter { it.target == ScheduleTarget.Definition(activity.id) }
        
        if (projectedInstances.isNotEmpty()) {
            // La Activity ya está proyectada automáticamente
            // No deberíamos crear excepción, deberíamos editar la existente
            return Result.failure(
                ActivityAlreadyProjectedException(
                    "Activity already projected for this date. Edit the existing occurrence instead."
                )
            )
        }
        
        // 2. Verificar si ya existe ad-hoc del mismo target en misma fecha
        val existingAdHoc = repository.getDailyInstancesForDate(epochDay)
            .filter { 
                it.target == ScheduleTarget.Definition(activity.id) &&
                it.isAdHoc &&
                it.plannedStartTime == startTimeMinutes
            }
            .firstOrNull()
        
        if (existingAdHoc != null) {
            return Result.success(existingAdHoc) // Retornar existente
        }
        
        // 3. Crear nueva instancia ad-hoc
        val instance = DailyInstance(
            id = UUID.randomUUID().toString(),
            target = ScheduleTarget.Definition(activity.id),
            scheduledDate = epochDay,
            titleSnapshot = activity.title,
            descriptionSnapshot = activity.description,
            plannedStartTime = startTimeMinutes,
            plannedEndTime = endTimeMinutes,
            plannedDurationMinutes = if (startTimeMinutes != null && endTimeMinutes != null) 
                endTimeMinutes - startTimeMinutes else null,
            status = DailyInstanceStatus.PLANNED, // Inicia como PLANNED, no MODIFIED
            isAdHoc = true, // ← CORRECCIÓN: Es ad-hoc porque no viene de regla
            sourceRuleId = null
        )
        
        repository.upsertDailyInstance(instance)
        return Result.success(instance)
    }
}
```

#### 3. Navegación

**Cambio:** Agregar ruta de navegación de Planning a ActivityDetail.

**Actual:**
```kotlin
// PlanningWorkspace solo recibe onActivityClick (para creación)
fun PlanningWorkspace(
    onAddActivity: () -> Unit,
    onActivityClick: (String) -> Unit, // ← Usado para crear instancia
)
```

**Nuevo:**
```kotlin
// PlanningWorkspace recibe onNavigateToActivityDetail
fun PlanningWorkspace(
    onAddActivity: () -> Unit,
    onNavigateToActivityDetail: (String) -> Unit, // ← Para abrir detalle
    onCreateException: (String, LocalDate) -> Unit, // ← Para excepción explícita
)
```

**En `ActivityDetail`:**
```kotlin
@Composable
fun ActivityDetailScreen(
    // ... parámetros existentes ...
    showCreateExceptionButton: Boolean = false,
    exceptionDate: LocalDate? = null,
    onCreateException: ((String, LocalDate) -> Unit)? = null
) {
    // ... contenido existente ...
    
    if (showCreateExceptionButton && exceptionDate != null && onCreateException != null) {
        Button(
            onClick = { onCreateException(activityId, exceptionDate) }
        ) {
            Text("Crear excepción para ${exceptionDate.format()}")
        }
    }
}
```

---

## Impacto en ECs Relacionadas

### EC-RE-017: Planning Workspace Consolidation

**Estado:** Mantener CLOSED.

**Nota histórica:** EC-RE-017 introdujo el flujo problemático del catálogo. Esta EC (RE-019) corrige ese flujo sin invalidar el trabajo de EC-RE-017.

### EC-RE-012: Planner Interactivity

**Estado:** Revisar documentación.

**Nota:** `AddActivityToDayUseCase` fue introducido en EC-RE-012. Esta EC (RE-019) propone modificarlo o reemplazarlo con semántica corregida.

### EC-RE-018: Backlog Planning Integration

**Estado:** Sin conflicto.

EC-RE-018 es independiente; trata sobre Backlog, no sobre el catálogo de Activities.

---

## Criterios de Aceptación

### Funcionales

1. **Navegación correcta:** Al tocar una Activity en el catálogo de Planning, se abre `ActivityDetail`, no se crea instancia.

2. **Creación de excepción explícita:** En `ActivityDetail`, existe botón/acción para "Crear excepción para [fecha]" que crea una `DailyInstance` ad-hoc.

3. **Semántica correcta de `isAdHoc`:** Las instancias creadas como excepción puntual tienen `isAdHoc = true`.

4. **Deduplicación:** No se pueden crear dos instancias ad-hoc del mismo `target` en la misma `date` con el mismo `plannedStartTime`.

5. **Prevención de duplicado con virtual:** Si ya existe una instancia virtual proyectada para la Activity en esa fecha, no se permite crear excepción (se debe editar la existente).

### No Funcionales

1. **Performance:** La verificación de duplicados no debe agregar latencia perceptible (>100ms).

2. **Transaccionalidad:** La creación de excepción y verificación de duplicados deben ser atómicas.

3. **UX:** El usuario debe entender claramente la diferencia entre "ver definición" y "crear excepción".

---

## Riesgos

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|-------------|---------|------------|
| Confusión del usuario al cambiar comportamiento del catálogo | Media | Alto | Proporcionar onboarding/tooltips explicando el nuevo flujo |
| Dependencias ocultas en `AddActivityToDayUseCase` | Baja | Alto | Búsqueda exhaustiva de usos antes de modificar; mantener deprecación temporal |
| Performance de deduplicación en grandes volúmenes | Baja | Medio | Implementar índices apropiados en consultas; test de carga |
| Inconsistencia temporal entre virtual y materializada | Baja | Alto | Usar transacciones de base de datos; evitar race conditions |

---

## Historial de Revisiones

| Versión | Fecha | Autor | Cambios |
|---------|-------|-------|---------|
| 0.1 | 2024-01-15 | Sistema de Auditoría | Creación inicial del documento de análisis |
| 0.2 | 2024-01-15 | Sistema de Auditoría | Completado análisis de `isAdHoc` y deduplicación |
| 0.3 | 2024-01-15 | Sistema de Auditoría | Añadidos escenarios obligatorios y recomendaciones |

---

## Apéndice: Alternativas Consideradas

### Alternativa A: Mantener flujo actual, agregar deduplicación simple

**Descripción:** Mantener que tocar en catálogo cree instancia, pero verificar si ya existe una para ese `(target, date)`.

**Pros:** Mínimo cambio de UX.

**Contras:** No resuelve la confusión conceptual; el usuario sigue sin entender por qué la Activity ya no aparecía si no la "agregó".

**Descartado:** No aborda el problema raíz de semántica.

### Alternativa B: Eliminar catálogo de Planning, solo accesible desde Activities

**Descripción:** El catálogo solo está en la sección Activities, no en Planning.

**Pros:** Elimina la confusión completamente.

**Contras:** Dificulta crear excepciones puntuales; el usuario debe salir de Planning, ir a Activities, volver a Planning.

**Descartado:** Demasiado restrictivo para el caso de uso válido de excepciones.

### Alternativa C: Smart Suggestions en lugar de catálogo

**Descripción:** En lugar de catálogo, Planning muestra "Sugerencias" de Activities que podrían interesar basadas en historial, frecuencia, etc.

**Pros:** Más inteligente; reduce carga cognitiva de elegir entre todas las Activities.

**Contras:** Complejo de implementar; requiere algoritmos de recomendación; puede ser impredecible para el usuario.

**Descartado:** Fuera del alcance de esta corrección; podría considerarse en EC futura.

---

**Fin del Documento**
