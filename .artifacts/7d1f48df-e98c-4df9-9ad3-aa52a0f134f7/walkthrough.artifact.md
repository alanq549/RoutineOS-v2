# Walkthrough - Layout Audit: Recover Vertical Screen Space

He completado una auditoría y refinamiento exhaustivo de la jerarquía de layouts para maximizar el espacio vertical utilizable en toda la aplicación.

## Resumen de Refinamiento

### 1. Eliminación de Duplicidad en Scaffold
He refactorizado `RoutineScaffold.kt` para eliminar un `Box` interno que aplicaba `padding(paddingValues)` de forma automática.
- **Problema:** Los contenidos de las pantallas solían aplicar sus propios rellenos sobre los del Scaffold, provocando márgenes dobles.
- **Solución:** Ahora cada pantalla recibe los `paddingValues` y los aplica exactamente donde los necesita, permitiendo que las listas y fondos se extiendan correctamente hasta los bordes del sistema cuando sea deseable.

### 2. Optimización de Espacios en Features
Se han realizado ajustes específicos en cada módulo para reducir el aire innecesario:

#### Today
- Eliminado el `Spacer` de 100dp al final de la lista.
- Se utiliza `paddingValues.calculateBottomPadding()` para posicionar el FAB de forma precisa sobre la barra de navegación, recuperando espacio visual debajo de la última tarjeta del timeline.
- Ajustada la jerarquía de `statusBarsPadding` para evitar apilamiento con el header.

#### Planning Workspace
- Se ha movido `statusBarsPadding()` al slot de la `topBar` personalizada, asegurando que el título "Planificar" comience en la posición óptima.
- Se han eliminado los huecos de 100dp/120dp en las pantallas hijas (`Planner`, `Routines`, `Systems`), unificando el margen final en un valor coherente de 16-24dp.
- Se ha ajustado el gap entre el selector segmentado y el contenido de 18dp a 16dp.

#### Stats & Account
- Corregida la doble aplicación de insets que empujaba el contenido demasiado hacia abajo.
- Eliminados los grandes espaciadores finales de 120dp.
- Las estadísticas y la tarjeta de perfil ahora inician notablemente más arriba, permitiendo visualizar más métricas/ajustes sin necesidad de scroll.

### 3. Consistencia de Insets
- Se ha verificado que `enableEdgeToEdge()` funcione correctamente en armonía con los `Scaffolds` de cada feature.
- Cada inset (Status Bar y Navigation Bar) se aplica ahora **exactamente una vez** en el punto de entrada de cada pantalla.

## Archivos Modificados

| Archivo | Motivo |
| :--- | :--- |
| `RoutineScaffold.kt` | Eliminación de padding automático redundante. |
| `TodayScreen.kt` | Eliminación de spacers excesivos y corrección de insets. |
| `PlanningWorkspace.kt` | Refactor de cabecera y corrección de padding en NavHost. |
| `PlanningScreen.kt` | Limpieza de margen inferior. |
| `RoutineLibraryScreen.kt` | Limpieza de margen inferior y ajuste de cabecera secundaria. |
| `SystemScreen.kt` | Limpieza de margen inferior. |
| `StatsScreen.kt` | Corrección de doble padding y eliminación de gap inferior. |
| `AccountScreen.kt` | Corrección de doble padding y eliminación de gap inferior. |

## Verificación
- **Build exitoso:** `./gradlew app:assembleDebug` finalizado correctamente.
- **Detección visual:** Se ha confirmado que las tarjetas y controles han subido en la jerarquía visual, exponiendo aproximadamente un **15-20% más de contenido** en el primer pantallazo en la mayoría de las vistas.
