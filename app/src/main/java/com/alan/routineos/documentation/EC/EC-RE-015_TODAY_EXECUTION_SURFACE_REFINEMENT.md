---
id: EC-RE-015
title: Today Execution Surface Refinement
phase: 6
priority: High
effort: Medium
owner: AI Agent
status: USER_REVIEW_PENDING
depends_on: [EC-RE-014]
branch: feature/ec-re-015-today-refinement
audit: Approved
created: 2026-09-06
updated: 2026-09-06
---

# EC-RE-015: Today Execution Surface Refinement

## 1. Objective
Refinar la superficie visual y estructural de la pantalla `Today` (Workspace de Ejecución Diaria) para resolver la deuda visual generada por la incorporación de Tareas (`TASK`), Recordatorios (`REMINDER`), eventos espontáneos y contexto asociado. El objetivo es lograr una interfaz técnica, compacta, unificada y coherente con el lenguaje de diseño "Technical Premium" de RoutineOS v2 sin alterar la lógica ni el modelo de dominio.

## 2. Current State Audit (Matriz de Componentes)

| Componente | Responsabilidad Actual | Problema Detectado | Cambio Propuesto | Reutilizable / Conservar |
|---|---|---|---|---|
| `TodayTimeline.kt` | Contenedor LazyColumn del timeline diario. | Dibuja la espina principal, pero convive con ejes locales dibujados en tarjetas individuales. | Unificar la espina temporal en un único eje global continuo sobre el que se alinean todos los `TimelineNode`. | Conservar y refactorizar. |
| `TimelineNode.kt` | Indicador gráfico circular en el eje del timeline. | Varía en alineación dependiendo de los márgenes locales de la tarjeta adyacente. | Garantizar que `TimelineNode.centerX == GlobalSpine.centerX` para `ACTIVITY`, `TASK` y `REMINDER`. | Conservar. |
| `TimelineConnector.kt` | Línea de conexión entre nodos. | Sin uso consistente a lo largo del timeline. | Integrar dentro del dibujado del riel continuo en `TodayTimeline`. | Conservar / simplificar. |
| `NormalTimelineCard.kt` | Despachador de tarjetas (`FullActivityCard`, `CompactTaskCard`, `LightweightReminderCard`). | `CompactTaskCard` dibuja su propia espina dorsal (`x = 22dp`), `LightweightReminderCard` usa colores hardcodeados (`Color(0xFF141B25)`), y `ContextFooter` agiganta las tarjetas. | Eliminar la espina propia de `CompactTaskCard`, unificar uso de tokens de tema, sintetizar `ContextFooter` con despliegue bajo demanda. | Conservar y refactorizar intensivamente. |
| `TimelineItemCard.kt` | Wrapper despachador entre intercepciones y tarjetas normales. | Funcionamiento correcto, pasa llamadas a `NormalTimelineCard` e `InterceptionContainer`. | Mantener contrato y delegación intactos. | Conservar. |
| `TimelineItemComponents.kt` | Badges de relación, iconos de estado y conectores de hilo. | Uso de estilos auxiliares con colores fijos y alphas inconsistentes. | Normalizar a tokens de `RoutineTheme.colors`. | Conservar y refactorizar. |
| `AssociatedItemsUiModels.kt` | Modelos de presentación para ítems del contexto asociado. | Correctos, transportan tareas, notas y recordatorios. | Mantener sin cambios en los datos. | Conservar. |
| `TodayTimelineUiModel.kt` | Modelo visual formateado para el timeline. | Correcto, contiene el estado del ítem y su contexto. | Mantener intacto. | Conservar. |

## 3. Visual Problems (Problemas Identificados)
1. **Línea de Tiempo Desalineada**:
   - `CompactTaskCard` dibuja una espina local en `x = 22.dp` con sangría de `40.dp`, mientras que `FullActivityCard` y `LightweightReminderCard` usan la columna general del timeline. La línea vertical resultante se corta e interrumpe entre ítems de distinto tipo.
2. **Sobrecarga por `ContextFooter` ("Tarjetas Monstruo")**:
   - La sección `ContextFooter` expande de forma plana todas las tareas, notas y recordatorios asociados a una rutina, agigantando la tarjeta y opacando las actividades contiguas.
3. **Colores Hardcodeados**:
   - Múltiples componentes en `feature/today/` contienen valores Hex locales (`Color(0xFFB894E6)`, `Color(0xFFFDBA74)`, `Color(0xFF141B25)`, `Color(0xFF070A0F)`) en lugar de `RoutineTheme.colors.roleEvent`, `roleTask`, `roleReminder`, `surface1`, `surface2`, `border`, etc.
4. **Heterogeneidad de Densidad y Tipografía**:
   - Disparidad de estilos, mayúsculas y tamaños diminutos (`8sp`, `9sp`) con alphas inconsistentes.

## 4. Proposed Refactor (Propuesta de Refactorización)
1. **Eje Global Unificado (Unified Spine)**:
   - Una única columna de riel continuo en `TodayTimeline.kt` donde la espina neutra pasa de forma ininterrumpida y todos los `TimelineNode` se ubican en el centro del riel.
   - Eliminación total del dibujado `drawBehind` de la espina en `CompactTaskCard`.
2. **Acordeón Sintético de Contexto (`ContextFooter`)**:
   - Por defecto, si una actividad posee contexto asociado, muestra una barra/píldora resumida (ej. `+ 3 tareas · + 1 nota`) expandible con un toque.
   - El despliegue completo de tareas asociadas o notas ocurre únicamente cuando el usuario expande el acordeón.
3. **Gramática Visual Unificada para Tarjetas**:
   - **`Activity`**: Ficha técnica prominente con color semántico `roleEvent` (Emerald).
   - **`Task`**: Unidad compacta (~44dp) con checkbox técnico y acento `roleTask` (Indigo).
   - **`Reminder`**: Ficha ligera de atención (~36dp) con acento `roleReminder` (Amber).
   - **Sustitución de colores Hex**: Reemplazar todo `Color(0x...)` por tokens del `RoutineTheme`.

## 5. Component Mapping (Mapeo de Componentes)
- **`TodayTimeline.kt`**: Garantiza el eje temporal global unificado (`TimelineRow`) y maneja el canvas del riel continuo.
- **`TimelineNode.kt`**: Renderiza el nodo centrado para cualquier `DailyInstanceStatus`.
- **`NormalTimelineCard.kt`**:
  - `FullActivityCard`: Renderiza la actividad con resumen de contexto sintetizado.
  - `CompactTaskCard`: Fila compacta de tarea alineada al área de contenido sin espina duplicada.
  - `LightweightReminderCard`: Banner ligero de recordatorio.
  - `ContextFooter`: Componente sintetizado (resumen compacto por defecto + vista expandida opcional).

## 6. Domain Invariants Preserved (Invariantes de Dominio Preservadas)
- **Cero cambios en modelos de dominio**: No se modifican `ActivityDefinition`, `ActivityNode`, `ScheduleRule`, `DailyInstance`, `ActivityExecution`, `BacklogItem`, `Deadline`, `Note`, `Task`, `Reminder`, `DailyInstanceRole`, ni `ActionProtocol`.
- **Relaciones contextuales intactas**: `associatedInstanceId` se conserva intacto (`associatedInstanceId != parentInstanceId`). Las tareas y notas asociadas son contexto, no nodos jerárquicos.
- **Lógica de ejecución intacta**: Se mantienen todas las acciones registradas (`COMPLETE`, `RESET`, `SKIP`, `MOVE_REQUEST`, `DELETE_INSTANCE`, `EDIT_SPONTANEOUS`).

## 7. Non-Goals (Exclusiones Explicitas)
- No se implementa ninguna funcionalidad del módulo `Body` (fisiología) ni se crean modelos como `BodyZone` o `BodyLoadUiModel`.
- No se altera la base de datos Room, migraciones, DAOs ni repositorios.
- No se modifica la pantalla de `Planning` ni la de `Stats`.

## 8. Implementation Steps (Pasos de Implementación)
1. **Auditoría previa y verificación de build**: Confirmar que `testDebugUnitTest` pasa previo a modificaciones.
2. **Refactor de `TodayTimeline.kt`**: Consolidar el eje de la espina temporal en la columna izquierda y asegurar que la línea del riel sea continua de inicio a fin de la lista.
3. **Refactor de `NormalTimelineCard.kt`**:
   - Limpiar `CompactTaskCard`: Remover `drawBehind` de la espina secundaria y padding excesivo de 40dp.
   - Limpiar `LightweightReminderCard`: Reemplazar `Color(0xFF141B25)` y hardcodes por `RoutineTheme.colors`.
   - Rediseñar `ContextFooter`: Crear la vista sintetizada (píldora resumida `+ N tareas`) con toggle de expansión.
   - Reemplazar constantes de color locales (`AdHocAccent`, `OverdueAccent`) por tokens semánticos del tema.
4. **Normalización de `TimelineItemComponents.kt`**: Reemplazar estilos y colores fijos por tokens de `RoutineTheme`.
5. **Verificación final**: Ejecutar `testDebugUnitTest` y `assembleDebug`.

## 9. Verification Plan (Plan de Verificación)
- **Pruebas Automatizadas**: `.\gradlew.bat testDebugUnitTest` debe dar 100% de éxito.
- **Compilación de Producción**: `.\gradlew.bat assembleDebug` debe finalizar sin errores.
- **Verificación Visual**:
  - Un solo eje vertical continuo en `TodayTimeline`.
  - Nodos alineados al centro del riel.
  - Tarjeta de Tarea compacta y sin espina duplicada.
  - `ContextFooter` sintetizado con visualización resumida por defecto.
  - Cero colores `Color(0x...)` hardcodeados en `feature/today/components/`.

## 10. Acceptance Criteria (Criterios de Aceptación)
- [x] Una sola espina temporal global alineada.
- [x] `TimelineNode.centerX == GlobalSpine.centerX` para todos los tipos.
- [x] Tarjeta de Actividad, Tarea y Recordatorio visualmente coherentes.
- [x] Tarea compacta sin jerarquía de `ActivityNode`.
- [x] Recordatorio ligero sin `ActivityExecution`.
- [x] `ContextFooter` sintetizado con despliegue opcional.
- [x] Invariante `associatedInstanceId != parentInstanceId` respetada.
- [x] Colores derivados de `RoutineTheme.colors` y `RoutineColors`.
- [x] Cero mocks visuales ni telemetría ficticia.
- [x] Cero modificaciones a modelos de dominio o esquemas DB.
- [x] `assembleDebug` PASS.
- [x] `testDebugUnitTest` PASS.
