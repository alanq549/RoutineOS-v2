---
ec_id: EC-009
ronda: 1
fecha: 2026-08-09
resultado: CHANGES_REQUESTED
---
## Hallazgos
El auditor ha revisado la implementación del Motor de Ejecución de Actividades (EC-009).

1.  **Cumplimiento de Alcance**:
    - Se creó la entidad `ActivityExecutionEntity` con el campo `metadataJson` y valor por defecto `{}`.
    - Se implementó el DAO, modelo de dominio y mappers correspondientes.
    - El repositorio `OfflineActivityRepository` expone los métodos para registrar y obtener ejecuciones.
    - El `ActivityDetailViewModel` carga reactivamente los nodos con su estado de ejecución.
    - La UI de `ActivityDetailScreen` permite marcar nodos como completados y muestra el indicador visual.
    - **Aclaración de Alcance:** Se confirma que `TodayViewModel` y su repositorio siguen usando datos simulados, lo cual es coherente con el retraso de la integración real para futuras ECs (EC-012).

2.  **Invariantes de Arquitectura (08_ARCHITECTURE_INVARIANTS.md)**:
    - **PASS**: El `metadataJson` es opaco y no contiene claves de dominio hardcodeadas en el código Kotlin.
    - **PASS**: No se detectó terminología prohibida ("peso", "series", "monto", "gym", etc.) en los archivos modificados.

3.  **Calidad de Código (Límites de Líneas)**:
    - **FALLO**: La función `loadActivity` en `ActivityDetailViewModel.kt` tiene **33 líneas**, excediendo el límite de < 30 líneas por función.
    - **PASS**: `ActivityDetailScreen.kt` cumple con los límites tras el refactor previo.

4.  **Hilt & DI**:
    - **PASS**: Se utiliza correctamente `@HiltViewModel` y `@Inject`. El `DatabaseModule` provee el nuevo DAO.

5.  **Deuda Técnica**:
    - **PASS**: Se registró correctamente la migración destructiva a DB v3 en `09_MOCK_DATA_STATUS.md`.

## Plan de corrección
1.  **Refactorizar `ActivityDetailViewModel.kt`**:
    - Extraer la lógica de transformación de flows dentro de `loadActivity` a una función privada auxiliar (ej. `getNodesWithExecutionFlow()`).
    - Asegurar que `loadActivity` quede por debajo de las 30 líneas.

## Ronda 2
**Fecha:** 2026-08-09
**Resultado:** USER_REVIEW_PENDING

### Evaluación de Correcciones
Se ha verificado la refactorización de `ActivityDetailViewModel.kt`.

1.  **Límite de Líneas por Función**:
    - Se extrajo la lógica de transformación de flows a la función privada `getNodesWithExecutionsFlow`.
    - La función `loadActivity` se redujo a **19 líneas**.
    - La nueva función `getNodesWithExecutionsFlow` tiene **17 líneas**.
    - Todas las funciones del archivo cumplen ahora con el límite de < 30 líneas.

### Conclusión
La EC-009 cumple con la totalidad de los requisitos técnicos, de arquitectura y de calidad de código. El motor de ejecución es funcional, agnóstico y reactivo.
