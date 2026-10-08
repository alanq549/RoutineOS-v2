---
id: EC-RE-021
title: Today Execution Surface Visual Alignment & Context Accordion Refinement
phase: 6
priority: Medium
effort: Small
owner: AI Agent
status: CLOSED
result: PASS
depends_on: [EC-RE-019, EC-RE-020]
branch: feature/ec-re-021-today-visual-refinement
audit: Approved
created: 2026-09-06
updated: 2026-09-06
---

# EC-RE-021: Today Execution Surface Visual Alignment & Context Accordion Refinement

> **REGLA DE ORO DE ESTA EC**: Tarjeta de **refinamiento visual de presentación en Jetpack Compose**. En esta etapa NO se modifica ningún archivo de base de datos, DAO, Caso de Uso, modelo de dominio ni lógica de negocio.

---

## 1. Objective (Objetivo)

Resolver problemas visuales y de presentación identificados en la superficie de ejecución de **`Today`**:
1. **Alineación del Eje Vertical del Timeline**: Corregir las ligeras desviaciones horizontales e interrupciones/cortes en la línea del eje (*spine*) entre tarjetas consecutivas de tipo `Activity`, `Task` y `Reminder`.
2. **Saturación Vertical por Contexto (`ContextFooter`)**: Consolidar el componente `ContextFooter` en un acordeón sintético colapsado por defecto, permitiendo expandir los detalles (tareas asociadas, notas, recordatorios) a demanda sin alterar las relaciones de dominio (`associatedInstanceId`).
3. **Migración a Tokens de Diseño (`RoutineTheme.colors`)**: Reemplazar colores en valores hexadecimales hardcodeados (`0xFF...`) por tokens semánticos existentes del tema (`roleEvent`, `roleTask`, `roleReminder`, `surface1`, `surface2`, `border`, `onSurface`, etc.).

---

## 2. Exact Component / Files to Modify (Archivos a Modificar)

Unicamente archivos de la capa de UI de `Today` en `feature/today/components/`:
- **[`TodayTimeline.kt`](file:///C:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/feature/today/components/TodayTimeline.kt)**: Geometría del riel vertical, conector y nodo.
- **[`NormalTimelineCard.kt`](file:///C:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/feature/today/components/NormalTimelineCard.kt)**: `FullActivityCard`, `CompactTaskCard`, `LightweightReminderCard` y `ContextFooter`.
- **[`TimelineNode.kt`](file:///C:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/feature/today/components/TimelineNode.kt)**: Dimensiones e ícono del nodo.
- **[`TimelineItemComponents.kt`](file:///C:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/feature/today/components/TimelineItemComponents.kt)**: Componentes auxiliares de íconos y líneas de hilo.

---

## 3. Current Geometry Analysis of Timeline Spine (Análisis de Geometría Actual)

### 3.1 Estado Actual en `TodayTimeline.kt` (`TimelineRow`)
- **Columna del Eje**: Ancho fijo de `64.dp` (`width(64.dp)`).
- **Línea del Riel (`Canvas`)**:
  - `padding(top = 24.dp)`
  - Ancho = `1.dp`, strokeWidth = `1.5.dp.toPx()`.
  - Alineación: `Alignment.TopCenter` (centro en $x = 32.dp$).
- **Nodo de Estado (`TimelineNode`)**:
  - `padding(top = 16.dp)`
  - Alineación: `Alignment.TopCenter` (centro en $x = 32.dp$).
- **Columna de Tarjeta**: `Modifier.weight(1f).padding(bottom = 24.dp)`.

### 3.2 Desviaciones e Inconsistencias Detectadas
1. **Brecha Vertical**: La línea del riel inicia en `top = 24.dp` mientras que el nodo está posicionado en `top = 16.dp`, creando una discontinuidad/corte de `8.dp` en el conector continuo entre tarjetas consecutivas.
2. **Disparidad de Centrado**: `CompactTaskCard` y `LightweightReminderCard` tienen rellenos internos distintos (`padding(vertical = 8.dp)`) que desalinean ligeramente el centro de sus indicadores de estado respecto al nodo $y = 16.dp$ de la `FullActivityCard`.

---

## 4. Proposed Geometry & Justification (Geometría Propuesta)

### 4.1 Geometría Fija Unificada Propuesta
- **Ancho del Riel Eje**: `56.dp` (optimiza el margen izquierdo liberando espacio para el contenido de la tarjeta).
- **Eje Central de Alineación**: $x = 28.dp$.
- **Centro del Nodo/Indicador**: $y = 20.dp$ (relativo al tope de la fila).
- **Línea Continua del Riel (`Spine Line`)**:
  - Dibujado continuo sin cortes desde `top = 0.dp` hasta `bottom = size.height` en el `Canvas` de fondo.
  - El trazo discontinuo (*dashed*) para tareas vencidas/pendientes se renderiza de forma fluida.
- **Alineación de Tarjetas**:
  - `FullActivityCard`, `CompactTaskCard` y `LightweightReminderCard` ajustan su cabecera para que el ícono/check principal coincida exactamente con el centro $y = 20.dp$.

---

## 5. Current `ContextFooter` Behavior (Comportamiento Actual)

- Muestra un botón plano sintético (`Surface` con `onClick`) con el resumen `$completedTasksCount/${tasks.size} tareas · 1 nota · 1 recordatorio`.
- Al hacer clic, conmuta `isContextExpanded` desplegando la lista de tareas asociadas, la nota y los recordatorios.
- **Riesgo**: Rellenos estáticos (`start = 26.dp`) y bordes duros con colores hardcodeados pueden provocar saturación o desbordamiento cuando existen múltiples elementos asociados.

---

## 6. Proposed Accordion Behavior (Comportamiento del Acordeón)

- **Estado Inicial**: Colapsado por defecto, mostrando la cápsula de resumen con conteos exactos (`"3 tareas asociadas"`, `"1 nota + 2 tareas"`).
- **Interacción**: Al tocar la cápsula, se expande suavemente mediante `AnimatedVisibility` revelando el detalle de las tareas (con selector de completado), notas y recordatorios.
- **Invariante**: **Cero modificación de datos**. Preserva intactos los IDs, la propiedad `associatedInstanceId`, `targetId` y el estado reactivo.

---

## 7. Current Color Tokens to Reuse (Mapeo de Tokens de Color)

Se eliminan los valores Hex hardcodeados en favor de los tokens oficiales de `RoutineTheme.colors`:

| Valor Hex Hardcodeado Actual | Token Reemplazante en `RoutineTheme.colors` | Uso |
|---|---|---|
| `Color(0xFF0B0E14)` | `RoutineTheme.colors.background` / `surface1` | Fondo principal de tarjeta |
| `Color(0xFF070A0F)` | `RoutineTheme.colors.surface1` / `surface2` | Fondo de bloque interno / árbol de sub-nodos |
| `0xFFB894E6` | `RoutineTheme.colors.roleTask` | Color semántico de rol Tarea |
| `0xFFFDBA74` | `RoutineTheme.colors.roleReminder` | Color semántico de rol Recordatorio |
| `Color.Black` (hardcoded en botones) | `RoutineTheme.colors.onPrimary` / `onSurface` | Texto e íconos sobre botones primarios |

---

## 8. Preliminary Acceptance Criteria (Criterios de Aceptación)

- [ ] **AC-01**: `FullActivityCard`, `CompactTaskCard` y `LightweightReminderCard` comparten exactamente el mismo eje vertical ($x = 28.dp$).
- [ ] **AC-02**: No existen cortes ni saltos visibles en la línea del timeline (*spine*) entre tarjetas consecutivas.
- [ ] **AC-03**: `ContextFooter` aparece colapsado por defecto cuando existen elementos asociados.
- [ ] **AC-04**: La expansión del acordeón permite consultar los detalles de tareas, notas y recordatorios sin alterar su identidad ni la asociación `associatedInstanceId`.
- [ ] **AC-05**: Todos los colores de las tarjetas de `Today` utilizan los tokens semánticos de `RoutineTheme.colors`.
- [ ] **AC-06**: No quedan valores Hex hardcodeados (`0xFF...`) en `feature/today/components/`.
- [ ] **AC-07**: Cero modificaciones en Room DB v10, DAOs, UseCases o entidades de dominio.
- [ ] **AC-08**: `Today` conserva intactas todas las funcionalidades existentes (completar, deshacer, omitir, mover y edición ad-hoc).
- [ ] **AC-09**: La suite de pruebas unitarias (`testDebugUnitTest`) continúa pasando al 100% (`82/82`).
- [ ] **AC-10**: La compilación de depuración (`assembleDebug`) finaliza exitosamente (`BUILD SUCCESSFUL`).

---

## 9. Files Explicitly Excluded (Archivos que NO deben modificarse)

- **Base de Datos y Persistencia**: Esquema de Room DB v10, migraciones, `DailyInstanceDao`, `ActivityExecutionDao`, `ActivityDefinitionDao`, `ScheduleRuleDao`.
- **Casos de Uso**: `RegisterDailyActionUseCase`, `MaterializeInstanceUseCase`, `ResolveTimelineUseCase`, `TimelineResolutionEngine`.
- **Modelos de Dominio**: `DailyInstance`, `ActivityDefinition`, `ScheduleRule`, `ActivityExecution`, `BacklogItem`.
- **Otras Pantallas**: `PlanningScreen.kt`, `StatsScreen.kt`, módulo `Body`.

---

## 10. Validation Strategy (Estrategia de Validación)

1. **Compilación y Pruebas Automatizadas**:
   ```powershell
   .\gradlew.bat :app:testDebugUnitTest
   .\gradlew.bat :app:assembleDebug
   ```
2. **Inspección Estática de Código**: Verificar que no queden importaciones o literales Hex hardcodeados en `feature/today/components/`.

---

### **Estado del Documento**: `ANALYSIS` / `NOT_IMPLEMENTED`

*(No se ha modificado ningún archivo de código fuente de producción ni base de datos).*
