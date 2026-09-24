# Informe Técnico & UX: Módulo Body (Carga Corporal) y Módulo Today (Workspace de Ejecución)

**Proyecto:** RoutineOS v2  
**Fecha:** 2026-09-06  
**Autor:** Agente Técnico RoutineOS  

---

## Parte 1: Módulo Body (Carga Corporal & Fisiología)

### 1.1 Visión & Estado Actual
El módulo **Body (Carga Corporal)** fue concebido dentro del rediseño de Stitch V3 como una lente fisiológica sobre las rutinas del usuario. Su objetivo es mapear el desgaste físico por zona muscular/corporal (pecho, piernas, espalda, etc.) a lo largo del tiempo (semana, mes, año) basándose en las ejecuciones y metadatos registrados.

#### **Archivos & Maquetas Disponibles:**
- **Modelos de Dominio:** [BodyLoadModels.kt](file:///C:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/feature/stats/model/BodyLoadModels.kt)
- **Diseños Stitch V3:**
  - [stats_week_body_refined.html](file:///C:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/documentation/REDESIGN/STITCH_V3/stats/body/stats_week_body_refined.html)
  - [stats_month_body_refined.html](file:///C:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/documentation/REDESIGN/STITCH_V3/stats/body/stats_month_body_refined.html)
  - [stats_year_body_refined.html](file:///C:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/documentation/REDESIGN/STITCH_V3/stats/body/stats_year_body_refined.html)

### 1.2 Estructura de Modelos (`BodyLoadModels.kt`)
- `BodyZone`: Enum con 10 zonas anatómicas (`HEAD`, `CHEST`, `ARMS_FRONT`, `ABS`, `LEGS_FRONT`, `BACK_UPPER`, `BACK_LOWER`, `ARMS_BACK`, `GLUTES`, `LEGS_BACK`).
- `BodyLoadLevel`: Niveles de carga fisiológica (`NONE`, `LOW`, `MEDIUM`, `HIGH`, `RECOVERY_REQUIRED`).
- `BodyLoadUiModel`: Encapsula la zona dominante, conteo de zonas activas, mapa destacado, ranking por porcentaje y alertas de recuperación requerida.

### 1.3 Por qué no está activo en la pantalla principal de Stats
1. **Invariante Domain-Agnostic ([08_ARCHITECTURE_INVARIANTS.md](file:///C:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/documentation/08_ARCHITECTURE_INVARIANTS.md))**: RoutineOS es un motor agnóstico de planificación. La carga corporal no puede hardcodearse como un tipo fijo en las ejecuciones; depende de que el usuario defina o capture `MetadataSchemas` de ejercicio en los nodos (ej. kgs, repeticiones, RPE).
2. **Proyección en Roadmap**: El renderizado interactivo SVG/Canvas del cuerpo humano está reservado para la EC de **Extensión de Métricas de Fisiología y Salud**, donde se cruzará el `metadataJson` capturado en `ActivityExecution` con el modelo `BodyLoadUiModel`.

---

## Parte 2: Módulo Today (Workspace de Ejecución Diaria)

### 2.1 Diagnóstico de UX/UI (Impacto de Tareas & Recordatorios)

Al incorporar la gestión de Tareas (`TASK`) y Recordatorios (`REMINDER`) dentro del flujo diario, la pantalla de `Today` acumuló deuda de diseño y fricción visual:

#### **A. Línea de Tiempo Desalineada (Eje Discordante)**
- `CompactTaskCard` dibuja una espina dorsal en `x = 22.dp` mediante `drawBehind`, con sangría de `40.dp`.
- `FullActivityCard` y `LightweightReminderCard` no respetan ese eje de 22dp, haciendo que la línea vertical se corte e interrumpa entre diferentes elementos.

#### **B. Sobrecarga por Tarjetas "Monstruo" (`ContextFooter`)**
- Cuando una rutina tiene tareas asociadas, notas o recordatorios, `FullActivityCard` expande un `ContextFooter` con separadores horizontales, ítems anidados y chips (`AssistChip`).
- La tarjeta de la rutina se vuelve desproporcionadamente alta y aplasta el resto del timeline diario.

#### **C. Heterogeneidad de Estilos e Iconografía**
- Cada tipo de tarjeta usa una estructura completamente distinta: `FullActivityCard` (grande con degradados), `CompactTaskCard` (con sangría profunda de 40dp), `LightweightReminderCard` (superficie plana con badge mini).
- Se observa saturación de textos diminutos en mayúsculas (`8sp`, `9sp`) con alphas de color inconsistentes.

#### **D. Colores Hardcodeados**
- Uso de valores Hex locales (`0xFFB894E6`, `0xFFFDBA74`, `0xFF141B25`, `0xFF070A0F`) en lugar de los tokens centralizados en `RoutineTheme.colors`.

### 2.2 Propuesta de Rediseño Stitch V2 (Technical Premium)

1. **Eje de Tiempo Unificado (24dp Spine)**: Unificar la espina vertical en `x = 24.dp` a lo largo de toda la `LazyColumn`. Todos los elementos se alinean al margen `start = 48.dp`.
2. **Acordeón Sintético de Contexto**: En lugar de expandir todo el `ContextFooter` por defecto, mostrar un indicador sintetizado (`"3 tareas asociadas"`) desplegable bajo demanda.
3. **Fichas Técnicas de Alta Densidad**:
   - **Tarea (`TASK`)**: Fila compacta de 44dp con checkbox técnico.
   - **Recordatorio (`REMINDER`)**: Banner limpio de 36dp en tono `roleReminder`.
4. **Tokens Unificados**: Reemplazar colores hex locales por `RoutineTheme.colors.roleEvent`, `roleTask` y `roleReminder`.

---

## Parte 3: Árbol de Archivos & Responsabilidades

### 3.1 Estructura del Módulo `feature/today`

```
feature/today/
├── TodayRoute.kt                  # Punto de entrada de navegación Hilt/Compose para Today
├── TodayScreen.kt                 # Pantalla contenedor principal, manejo de scroll, FAB y sheets
├── TodayUiState.kt                # Estado inmutable de la UI (timelineItems, focusId, progress, etc.)
├── TodayViewModel.kt              # ViewModel que coordina acciones (Complete, Move, Skip, Spontaneous)
├── components/
│   ├── CaptureMetadataSheet.kt    # Bottom sheet para captura de metadatos (JSON schema)
│   ├── ConflictWarningDialog.kt   # Diálogo de advertencia preventiva en conflictos de horario
│   ├── InterceptionContainer.kt   # Contenedor para interrupciones y eventos espontáneos
│   ├── NormalTimelineCard.kt      # Componente central que renderiza Activity, Task y Reminder Cards
│   ├── PastBoundaryItem.kt        # Indicador divisor entre actividades pasadas y futuras
│   ├── QuickAddDialog.kt          # Diálogo rápido para creación de eventos en tiempo real
│   ├── SpontaneousEditorSheet.kt  # Editor avanzado para eventos ad-hoc, tareas, notas y recordatorios
│   ├── SubNodeRow.kt              # Fila individual para renderizar sub-pasos/pasos jerárquicos
│   ├── TimePicker.kt              # Selector técnico de hora (Picker)
│   ├── TimelineConnector.kt       # Conectores gráficos entre nodos del timeline
│   ├── TimelineItemCard.kt        # Wrapper base para tarjetas del timeline
│   ├── TimelineItemComponents.kt  # Componentes auxiliares (Badges, etiquetas de estado)
│   ├── TimelineNode.kt            # Nodo gráfico del timeline
│   ├── TodayHeader.kt             # Cabecera de fecha y barra de progreso general
│   ├── TodayNextActivityCard.kt   # Tarjeta destacada de "Siguiente Actividad"
│   ├── TodayProgressCircle.kt     # Anillo/indicador circular de progreso diario
│   └── TodayTimeline.kt           # DSL LazyColumn builder para la lista de timeline
└── model/
    ├── AssociatedItemsUiModels.kt # Modelos UI para tareas, recordatorios y notas asociadas
    ├── TodayModels.kt             # Definición de tipos base para Today
    └── TodayTimelineUiModel.kt    # Modelo unificado de presentación UI para cada ítem del timeline
```

### 3.2 Estructura del Módulo `Body` (Fisiología en `feature/stats`)

```
feature/stats/
└── model/
    └── BodyLoadModels.kt          # Enums (BodyZone, BodyLoadLevel, BodyView) y modelo BodyLoadUiModel
documentation/REDESIGN/STITCH_V3/stats/body/
├── stats_week_body_refined.html   # Maqueta interactiva HTML/CSS para carga semanal del cuerpo
├── stats_week_body_refined.png    # Preview visual de carga semanal
├── stats_month_body_refined.html  # Maqueta interactiva HTML/CSS para carga mensual
├── stats_month_body_refined.png   # Preview visual de carga mensual
├── stats_year_body_refined.html   # Maqueta interactiva HTML/CSS para carga anual
└── stats_year_body_refined.png    # Preview visual de carga anual
```

---

### 3.3 Matriz de Funcionalidades y Responsabilidades por Archivo

| Archivo | Capa | Responsabilidad Principal |
| :--- | :--- | :--- |
| **`TodayRoute.kt`** | UI (Route) | Inyecta `TodayViewModel` con Hilt y vincula callbacks con la navegación. |
| **`TodayScreen.kt`** | UI (Screen) | Gestiona el layout principal, animaciones de mask/fade, autoscroll al "AHORA" y visualización de BottomSheets/Dialogs. |
| **`TodayViewModel.kt`** | ViewModel | Ejecuta acciones diarias (`Complete`, `Skip`, `Move`, `Reset`), re-calcula conflictos con `SimulateMoveUseCase` y expone `uiState`. |
| **`NormalTimelineCard.kt`** | UI Component | Renderiza las 3 variantes de tarjeta (`FullActivityCard`, `CompactTaskCard`, `LightweightReminderCard`) y su menú de acciones. |
| **`SpontaneousEditorSheet.kt`** | UI Component | Formulario modal para crear y editar eventos ad-hoc, vinculando tareas borradores, notas y recordatorios. |
| **`CaptureMetadataSheet.kt`** | UI Component | Formulario dinámico generado a partir del `MetadataSchema` para capturar valores cuantitativos. |
| **`ConflictWarningDialog.kt`** | UI Component | Diálogo preventivo que alerta al usuario cuando un movimiento genera solapamientos. |
| **`TodayNextActivityCard.kt`** | UI Component | Tarjeta de acceso rápido que destaca la siguiente actividad en foco. |
| **`TodayTimelineUiModel.kt`** | UI Model | Representa el estado formateado listo para renderizar en pantalla (títulos, rangos de hora, conflicto, sub-nodos). |
| **`AssociatedItemsUiModels.kt`** | UI Model | Modelos livianos para tareas, notas y recordatorios anidados dentro de una rutina. |
| **`BodyLoadModels.kt`** | Domain Model | Define los enums anatómicos y el contrato de datos para el cálculo de carga corporal por zona. |
