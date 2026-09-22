---
ec_id: EC-RE-013
ronda: 1
fecha: 2026-09-06
resultado: CHANGES_REQUESTED
---

# Auditoría Técnica: EC-RE-013 - Refinamiento de Catálogo y Constructor de Actividades

**Fecha:** 2026-09-06
**Estado:** CHANGES_REQUESTED
**Criterio de Evaluación:** Código Fuente Real, Interfaz de Usuario, Invariantes de Arquitectura y Suite de Tests

## 1. Archivos Revisados

### Interfaz de Usuario y ViewModels
- [ActivityCreationScreen.kt](file:///C:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/feature/dashboard/ActivityCreationScreen.kt)
- [ActivityCreationViewModel.kt](file:///C:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/feature/dashboard/ActivityCreationViewModel.kt)
- [DashboardViewModel.kt](file:///C:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/feature/dashboard/DashboardViewModel.kt)
- [ActivityCard.kt](file:///C:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/feature/dashboard/components/ActivityCard.kt)
- [ActivityModels.kt](file:///C:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/feature/dashboard/model/ActivityModels.kt)

## 2. Resultados de Verificación (Checklist Funcional y de Arquitectura)

| # | Punto de Control | Resultado | Evidencia / Observación |
|---|---|---|---|
| 1 | Conexión Dinámica de Sistemas | **PASS** | `ActivityCreationViewModel` consume `repository.getAllSystems()` reactivamente. |
| 2 | Selección e Interacción de Sistemas | **PASS** | `SystemTile` interactivo con alternancia de selección y `selectedSystemId`. |
| 3 | Persistencia del `systemId` | **PASS** | `ActivityDefinition` se guarda con `systemId = currentState.selectedSystemId`. |
| 4 | Mensaje de Respaldo Sin Sistemas | **PASS** | Muestra "No hay sistemas configurados." cuando la lista está vacía. |
| 5 | Compactación de Días en Catálogo | **PASS** | `DashboardViewModel` aplica `.take(2)` y genera `moreDaysCount`. |
| 6 | Invariante Domain-Agnostic en Catálogo | **FAIL** | String "UNIVERSIDAD" hardcodeado como fallback en `ActivityCard.kt` (`if (activity.iconName == "account_tree") "SIN SISTEMA" else "UNIVERSIDAD"`). |
| 7 | Eliminación de Mocks Estáticos en UI | **FAIL** | Mocks de texto estáticos hardcodeados en `ActivityCard.kt` ("05", "15", "27.5h", "5.5 hrs", "07:00 - 10:00"). |
| 8 | Suite de Pruebas Unitarias | **FAIL** | La suite `testDebugUnitTest` reporta 4 fallos en tests unitarios. |

## 3. Resultados de Ejecución

- **Build (`assembleDebug`):** EXITOSO.
- **Tests (`testDebugUnitTest`):** FALLIDO (76 pasaron, 4 fallaron).

## 4. Hallazgos y Observaciones

### Hallazgo 1: Violación de Invariante Domain-Agnostic en `ActivityCard.kt` [CRÍTICO]
- En `ActivityCard.kt` se hardcodeó la lógica:
  `if (activity.iconName == "account_tree") "SIN SISTEMA" else "UNIVERSIDAD"`
- Violación directa de `08_ARCHITECTURE_INVARIANTS.md`: ningún concepto de dominio específico ("UNIVERSIDAD") puede codificarse en tiempo de compilación.
- **Causa**: `ActivityCardModel` carece del campo `systemTitle: String?`. `DashboardViewModel.mapToCardModel` ya busca `linkedSystem = systems.find { it.id == def.systemId }`, pero no transfiere `linkedSystem?.title` al modelo visual.

### Hallazgo 2: Presencia de Valores Mock Hardcodeados en `ActivityCard.kt` [CRÍTICO]
- En `ActivityCard.kt` se dejaron textos estáticos de prototipo con comentarios `// Mocking from img`:
  - Tarjetas de estadísticas: `CardStatBadge("SESIONES", "05")`, `CardStatBadge("BLOQUES", "15")`, `CardStatBadge("CARGA SEM", "27.5h")`.
  - Duración de resumen: `Text("5.5 hrs")`.
  - Horario de resumen: `Text("07:00 - 10:00")`.
- **Incapacidad de Reflejar Datos Reales**: Los valores presentados al usuario no se calculan a partir de los datos reales del modelo de la actividad.

### Hallazgo 3: Fallos en Suite de Tests Unitarios [CRÍTICO]
- `gradle_build("app:testDebugUnitTest")` falló con 4 errores en:
  1. `com.alan.routineos.domain.usecase.SystemStatsCalculationTest > calculates system stats correctly`
  2. `com.alan.routineos.feature.planning.PlanningEventFlowTest > HIERARCHY - Ad-hoc item with no links should be strictly independent`
  3. `com.alan.routineos.feature.planning.PlanningEventFlowTest > HIERARCHY - Task linked to Activity should NOT inherit its structural children`
  4. `com.alan.routineos.feature.planning.PlanningEventFlowTest > EDIT - Reconstructs both semantic target and contextual association correctly`

## 5. Plan de Corrección (Paso a Paso para el Implementador)

1. **Añadir `systemTitle` a `ActivityCardModel`**:
   - Agregar `val systemTitle: String? = null` a `ActivityCardModel` en `ActivityModels.kt`.
   - En `DashboardViewModel.kt`, mapear `systemTitle = linkedSystem?.title?.uppercase()`.

2. **Refactorizar `ActivityCard.kt` para Eliminar Hardcodes de Dominio y Mocks**:
   - Reemplazar `if (activity.iconName == "account_tree") "SIN SISTEMA" else "UNIVERSIDAD"` por `activity.systemTitle ?: "SIN SISTEMA"`.
   - Eliminar o dinamizar los bloques de estadísticas estáticas ("05", "15", "27.5h") y los horarios hardcodeados ("5.5 hrs", "07:00 - 10:00"). Si las reglas u orígenes de datos no proporcionan horas formateadas en el `summaryItems`, se debe mostrar únicamente el título de la actividad y sus nodos sin inventar datos de hora ficticios.

3. **Reparar Suite de Tests Unitarios**:
   - Investigar y corregir las aserciones / datos de prueba en `SystemStatsCalculationTest` y `PlanningEventFlowTest` para que `testDebugUnitTest` complete con 100% de éxito.

---
**Firma:** AI Auditor Agent
