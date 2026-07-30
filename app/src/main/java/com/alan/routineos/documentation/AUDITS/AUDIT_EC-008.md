---
ec_id: EC-008
ronda: 1
fecha: 2026-07-29
resultado: CHANGES_REQUESTED
---
## Hallazgos
El auditor ha revisado la implementación de la pantalla de detalle de actividad y gestión de nodos (EC-008).

1.  **Cumplimiento de Alcance**:
    - Se implementó la navegación desde `DashboardScreen` a `ActivityDetailScreen` pasando el `activityId`.
    - `ActivityDetailViewModel` carga correctamente la definición y sus nodos de forma reactiva.
    - Se incluyó un formulario de creación rápida para `ActivityNode` en la pantalla de detalle.
    - La persistencia de nodos funciona correctamente y la lista se actualiza automáticamente.

2.  **Calidad de Código (Límites de Líneas)**:
    - **FALLO**: `ActivityDetailScreen` (función composable principal) tiene **80 líneas**, excediendo significativamente el límite de < 30 líneas.
    - **FALLO**: `QuickAddNodeForm` tiene **35 líneas**, excediendo el límite de < 30 líneas.
    - Los archivos en sí cumplen con el límite de < 300 líneas.

3.  **Invariantes de Arquitectura (08_ARCHITECTURE_INVARIANTS.md)**:
    - **OBSERVACIÓN**: En `ActivityCard.kt`, se detectó un mapeo de iconos hardcodeado con términos de dominio: `"school"`, `"fitness_center"`, `"wb_sunny"`. Aunque es capa de UI, el uso de estos strings como identificadores de "tipo" o "estilo" roza la violación del principio agnóstico si se pretenden usar como categorías de negocio encubiertas. Sin embargo, el veredicto de fallo se centra principalmente en los límites de líneas por función.

4.  **Hilt & DI**:
    - Se utiliza correctamente `@HiltViewModel` y `@Inject` en `ActivityDetailViewModel`.

## Plan de corrección
El agente implementador debe realizar las siguientes acciones:

1.  **Refactorización de `ActivityDetailScreen.kt`**:
    - Extraer la lógica de carga (`if (uiState.isLoading) ...`) y el `LazyColumn` a sub-composables.
    - Reducir `ActivityDetailScreen` a < 30 líneas orquestando los nuevos componentes.
    - Refactorizar `QuickAddNodeForm` para estar bajo las 30 líneas (ej. extrayendo el `OutlinedTextField` o simplificando modificadores).

2.  **Limpieza de Invariantes**:
    - Revisar el mapeo de iconos en `ActivityCard.kt` para asegurar que no se estén introduciendo categorías de dominio "por la puerta de atrás". Si se permiten iconos, deben ser puramente visuales y no tipos de entidad.

## Ronda 3
**Fecha:** 2026-07-29
**Resultado:** USER_REVIEW_PENDING

### Evaluación de Correcciones (Ronda 2)
Se ha completado la refactorización de `NodesList` en `ActivityDetailScreen.kt` para cumplir con el estándar de calidad de código.

1.  **Clarificación de Discrepancia**: 
    - Se verificó el archivo original (Ronda 2) mediante comandos de conteo precisos.
    - El cuerpo de la función `NodesList` (desde `{` hasta `}`) tenía exactamente **34 líneas** (rango 87-120). 
    - Incluyendo la firma y la anotación `@Composable`, la función completa tenía **40 líneas** (rango 81-120). 
    - Esto confirma que el hallazgo del auditor era correcto y excedía el límite de 30 líneas.

2.  **Refactorización Exitosa**:
    - Se extrajeron los bloques `item { ... }` a funciones auxiliares: `DetailHeader`, `NodesSectionTitle`, `NodesContent` (usando `LazyListScope`) y `ListBottomSpacer`.
    - La nueva función `NodesList` tiene ahora **25 líneas totales** (rango 92-116), con un cuerpo de **19 líneas**. Está significativamente por debajo del límite de 30 líneas.

3.  **Integridad de Datos**:
    - Se verificó que la relación de clave foránea (`activityDefinitionId`) se respeta y que las listas de nodos son independientes entre diferentes actividades.

4.  **Estabilidad de Features**:
    - Se confirma que las features `Today` y `Planning` siguen compilando y funcionando correctamente tras el renombrado masivo a terminología agnóstica.
