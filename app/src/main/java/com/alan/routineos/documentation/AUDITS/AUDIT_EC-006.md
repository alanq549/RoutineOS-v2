---
ec_id: EC-006
ronda: 1
fecha: 2026-07-26
resultado: CHANGES_REQUESTED
---
## Hallazgos
Tras la revisión de la implementación de la EC-006, se han detectado múltiples violaciones a las invariantes de arquitectura y a los estándares de consistencia del proyecto.

1.  **Violación de Invariantes de Dominio (08_ARCHITECTURE_INVARIANTS.md)**:
    - Se han detectado múltiples strings hardcodeadas en la lógica de negocio (ViewModels) y en los repositorios Fake que utilizan términos de dominio prohibidos:
        - `DashboardViewModel.kt`: Categorías hardcodeadas: "Estudio", "Salud", "Trabajo", "Descanso".
        - `FakeTodayRepository.kt`, `FakePlanningRepository.kt`, etc.: Títulos y descripciones como "Universidad", "Gym", "Dormir", "Programación".
    - El principio central exige que el motor sea **domain-agnostic**. Aunque sean datos de ejemplo, no deben estar hardcodeados en el código fuente de la lógica/data layer. Deben ser inyectados desde recursos o persistidos en DB.

2.  **Inconsistencia en Inyección de Dependencias (Hilt)**:
    - Mientras que `DashboardViewModel` utiliza correctamente `@HiltViewModel` e inyecta `ActivityRepository`, otros ViewModels actualizados en esta sesión (`TodayViewModel`, `PlanningViewModel`, `StatsViewModel`, `SystemViewModel`) instancian sus repositorios manualmente:
      ```kotlin
      class TodayViewModel(private val repository: FakeTodayRepository = FakeTodayRepository()) : ViewModel()
      ```
    - Esto viola el estándar de arquitectura del proyecto donde todo componente de negocio debe ser provisto por Hilt.

3.  **Incumplimiento de Registro de Mocks (MOCK_DATA_STATUS.md)**:
    - Solo se ha registrado `FakePlanningRepository`.
    - No se han registrado `FakeTodayRepository`, `FakeStatsRepository`, ni `FakeSystemRepository`, los cuales están activos en el código fuente. Esto oculta deuda técnica.

4.  **Checklist de Validación de Usuario Engañoso**:
    - El punto "Al abrir la aplicación, se visualiza el Dashboard..." es incorrecto. La aplicación abre en `TodayScreen` (start destination), el cual sigue siendo 100% Fake. El Dashboard real está en una sub-pestaña de "Planificar".

## Plan de corrección
El agente implementador debe realizar las siguientes acciones:

1.  **Saneamiento de Invariantes**: 
    - Mover todas las strings de dominio específicas ("Universidad", "Gym", etc.) a `strings.xml` marcándolas como `translatable="false"` o, preferiblemente, asegurar que los ViewModels no asuman categorías predefinidas si no existen en la base de datos.
    - Los repositorios Fake deben usar nombres genéricos (ej. "Actividad A", "Nodo 1") o cargar sus strings desde recursos para no contaminar el código fuente con conceptos de dominio.
2.  **Estandarización de Hilt**: Refactorizar todos los ViewModels de la capa `feature` para usar `@HiltViewModel` y `@Inject`. Crear módulos de Hilt necesarios para proveer las implementaciones (incluso si son Fakes por ahora).
3.  **Actualización de MOCK_DATA_STATUS.md**: Registrar TODOS los repositorios Fake activos con sus fechas límite de reemplazo.
4.  **Corrección de Checklist**: Ajustar el spec de la EC para que el proceso de validación refleje la navegación real de la app.

## Ronda 2
**Fecha:** 2026-07-27
**Resultado:** CHANGES_REQUESTED

### Evaluación de Reversión de Veredicto
Se ha re-evaluado la EC-006 limitando el juicio estrictamente a su alcance declarado (feature/dashboard).

1. **Violación en Alcance:** Se verificó que `feature/dashboard/` (específicamente en `DashboardScreen.kt`, `DashboardViewModel.kt` y `ActivityModels.kt`) **SÍ contiene strings hardcodeadas de dominio específico** ("Estudio", "Salud", "Trabajo", "Descanso", "Developer Deep Work", "Higiene del Sueño"). Esto contraviene el principio agnóstico de `08_ARCHITECTURE_INVARIANTS.md` y debe corregirse (ej. moviendo a strings.xml o recursos dinámicos).
2. **Hallazgos Fuera de Alcance:** Se confirma que las violaciones encontradas en `TodayViewModel`, `PlanningViewModel`, `StatsViewModel` y `SystemViewModel` son **pre-existentes** y están fuera del alcance de EC-006. Se recomienda una nueva Épica (EC-007) para su saneamiento.
3. **Conexión Técnica:** La inyección de `ActivityRepository` vía Hilt en `DashboardViewModel` es correcta.
