# Informe de Auditoría General de Saneamiento y Consistencia — RoutineOS-v2

**Fecha**: 2026-09-22  
**Estado General**: `CHANGES_REQUESTED`  
**Compilación (`assembleDebug`)**: `PASS`  
**Pruebas (`testDebugUnitTest`)**: `PASS` (100% exitosas)  

---

## 1. Executive Summary

Se ha realizado una auditoría exhaustiva y no destructiva sobre la totalidad del proyecto `RoutineOS-v2`. El objetivo consistió en evaluar la coherencia de la arquitectura, la ausencia de código huérfano o muerto, la consistencia de la navegación, el cumplimiento de las invariantes de dominio, el comportamiento de la base de datos y migraciones, la vigencia de los datos mock y la fidelidad de la documentación existente.

### Resumen de Resultados
1. **Compilación y Tests Unitarios**: **`PASS`**. La construcción con Gradle (`assembleDebug`) concluyó exitosamente. La suite completa de pruebas unitarias (`testDebugUnitTest`) se ejecutó sin fallos.
2. **Coherencia de Dominio e Invariantes**: **`PASS`**. El modelo agnóstico de dominio, la política de tiempo local (*Local Wall Clock Time*), la separación jerárquica (`ActivityDefinition` → `ActivityNode`), la gestión de ocurrencias (`DailyInstance`), reglas de recurrencia (`ScheduleRule`) e historial (`ActivityExecution`) se encuentran intactos y rigurosamente respetados.
3. **Código Huérfano y Muerto**: **`CHANGES_REQUESTED`**. Se identificaron archivos y módulos huérfanos residuales de refactors anteriores:
   - El paquete `feature/system` completo (`FakeSystemRepository.kt` y `SystemModels.kt`) carece de consumidores.
   - El caso de uso `ResolveTimelineForDateRange.kt` está obsoleto y no posee inyección de dependencias ni llamadores.
   - El caso de uso `GetSystemsWithStatsUseCase.kt` está marcado como `@Deprecated` y no tiene consumidores en UI/ViewModels, aunque sigue registrado en `UseCaseModule.kt`.
4. **Residuos de Navegación**: **`CHANGES_REQUESTED`**. La barra de navegación inferior (*BottomBar*) muestra correctamente 4 pestañas (`Today`, `Planning`, `Stats`, `Account`), y la pestaña antigua `Systems` ha sido eliminada por completo de la interfaz de usuario. Sin embargo, en `AppRoutes.kt` persisten objetos auxiliares obsoletos (`AppRoutes.Planner` y `AppRoutes.Activities`) que ya no corresponden a destinos navegables independientes en `AppNavHost.kt`.
5. **Documentación Desfasada (*Documentation Drift*)**: **`CHANGES_REQUESTED`**. `09_MOCK_DATA_STATUS.md` mantiene registrados como "🟡 Activo" repositorios fake que ya fueron eliminados o sustituidos por Room real (`FakePlanningRepository`, `FakeTodayRepository`, `FakeStatsRepository`), y señala migraciones como pendientes cuando la base de datos actual ya se encuentra en la versión 10 con migraciones reales implementadas (`MIGRATION_5_6` a `MIGRATION_9_10`).

---

## 2. Current Architecture Snapshot

### Estructura de Navegación Principal
La aplicación implementa una estructura de 4 destinos de primer nivel en `MainActivity.kt`:
- **`Today`**: Ejecución, captura ágil, temporizador e historial inmediato.
- **`Planning`**: Workspace de planificación integral que unifica el organizador futuro, editor de reglas y el catálogo de actividades.
- **`Stats`**: Análisis histórico, KPIs y métricas de adhesión alimentados por `GetHistoryAnalyticsUseCase`.
- **`Account`**: Configuración y preferencias de cuenta.

### Flujo de Entidades de Dominio
```text
ActivityDefinition (Molde / Catálogo)
        │
        ├── systemId (Asociación organizativa a LifeSystem)
        └── ActivityNode (Estructura jerárquica del molde)
                │
                └── ScheduleRule (Intención de recurrencia)
                        │
                        └── DailyInstance (Ocurrencia temporal concreta)
                                │
                                └── ActivityExecution (Registro histórico de ejecución real)
```

---

## 3. Dead / Orphaned Code

### Archivos y Paquetes Huérfanos
1. **[FakeSystemRepository.kt](file:///c:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/feature/system/data/FakeSystemRepository.kt)**
   - **Problema**: El archivo pertenece a la feature desmantelada `feature/system`. No posee ningún consumidor en toda la base de código.
   - **Consumidores**: 0.
   - **Estado recomendado**: `DELETE` en la siguiente EC de saneamiento.

2. **[SystemModels.kt](file:///c:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/feature/system/model/SystemModels.kt)**
   - **Problema**: Define las estructuras `LifeArea`, `LifeAreaStatus` y `SystemSummary` asociadas a la antigua vista independiente de sistemas.
   - **Consumidores**: 0 (únicamente `FakeSystemRepository.kt`).
   - **Estado recomendado**: `DELETE` en la siguiente EC de saneamiento.

3. **[ResolveTimelineForDateRange.kt](file:///c:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/domain/usecase/ResolveTimelineForDateRange.kt)**
   - **Problema**: Caso de uso desactualizado para la resolución temporal. Fue reemplazado por `TimelineResolutionEngine` y `ResolveTimelineUseCase`. No tiene inyección con `@Inject` ni registro en `UseCaseModule.kt`.
   - **Consumidores**: 0.
   - **Estado recomendado**: `DELETE`.

4. **[GetSystemsWithStatsUseCase.kt](file:///c:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/domain/usecase/GetSystemsWithStatsUseCase.kt)**
   - **Problema**: Marcado con `@Deprecated("To be integrated into Stats module in future phases. Currently unused in consolidated Activities view.")`. No tiene llamadas en ViewModels ni componentes UI. Sin embargo, continúa inyectado en [UseCaseModule.kt](file:///c:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/data/di/UseCaseModule.kt#L121).
   - **Consumidores**: Ningún consumidor funcional (solo `UseCaseModule.kt`).
   - **Estado recomendado**: Retirar del módulo DI y evaluar su eliminación o migración definitiva al módulo `Stats`.

---

## 4. Navigation Residues

1. **Estado de la Navegación Principal**:
   - `Today`: Navegable.
   - `Planning`: Navegable (`PlanningWorkspace`).
   - `Stats`: Navegable.
   - `Account`: Navegable.
   - `Systems`: **100% Inexistente** como pestaña, ruta en `AppNavHost`, botón o deep link.

2. **Objetos Obsoletos en [AppRoutes.kt](file:///c:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/core/navigation/AppRoutes.kt)**:
   - `AppRoutes.Planner` (`"planner"`)
   - `AppRoutes.Activities` (`"activities"`)
   - **Problema**: En [MainActivity.kt](file:///c:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/MainActivity.kt#L116) la función `isPlanningRoute()` consulta estas rutas. Sin embargo, en [AppNavHost.kt](file:///c:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/core/navigation/AppNavHost.kt#L30) solo se registra `AppRoutes.Planning.route`. `Planner` y `Activities` son residuos de cuando existían pestañas anidadas dentro de Planning.
   - **Estado recomendado**: Depurar `AppRoutes.kt` e `isPlanningRoute()` durante la consolidación de `EC-014`.

---

## 5. Domain Consistency

### Verificación de Invariantes de Dominio
- **`ActivityDefinition`**: Mantiene la identidad y reutilización del molde. Campo `systemId` vincula opcionalmente la actividad a un `LifeSystem`.
- **`ActivityNode`**: Define la estructura jerárquica de la actividad.
- **`ScheduleRule`**: Representa la intención de recurrencia.
- **`DailyInstance`**: Representa la ocurrencia temporal concreta.
- **`ActivityExecution`**: Representa el historial de ejecución real.

### Verificación de Roles y Protocolos
- **`DailyInstanceRole`**: `ACTIVITY`, `TASK`, `REMINDER`.
- **`ActionProtocol`**: `TIMER`, `CHECK`.
- **Protocolo `NOTIFY`**: Eliminado. No existen referencias a `NOTIFY` como protocolo de ejecución.
- **Asignación del Protocolo**:
  - `ACTIVITY` = `TIMER`
  - `TASK` = `CHECK`
  - `REMINDER` = `CHECK`
- **Separación de Tareas y Recordatorios**: `Task ≠ ActivityNode`, `Reminder ≠ ActivityNode`, `Note ≠ ActivityNode`. Las tareas y recordatorios vinculados a una `ActivityDefinition` **no heredan sus hijos estructurales**.

---

## 6. Planning / Today Separation

Se auditó la separación conceptual entre las pantallas principales:
1. **Planning Workspace**:
   - Se enfoca exclusivamente en organizar el futuro, consultar el catálogo, editar reglas de agendamiento y simular conflictos (`SimulateMoveUseCase`).
   - No registra `ActivityExecution` ni actúa como interfaz de temporizador.
2. **Today Workspace**:
   - Se encarga de la ejecución del día actual (`RegisterDailyActionUseCase`), captura rápida y registro de eventos espontáneos.
   - No permite la modificación de plantillas ni actúa como editor de estructuras compuestas.

---

## 7. Activities / LifeSystem Consistency

1. **Activities**: Opera como catálogo y gestor de jerarquías de `ActivityDefinition` y `ActivityNode`.
2. **LifeSystem**: Se implementa como agrupación organizativa a través de `ActivityDefinition.systemId` apuntando a la tabla Room `life_systems` (`SystemEntity`).
3. **Módulo Independiente de Sistemas**: No existen pantallas, ViewModels ni DAOs independientes para la navegación de sistemas. La tabla `life_systems` sirve únicamente como catálogo de categorías/sistemas para clasificar definiciones de actividades.

---

## 8. UI / Mock Residues

### Semántica de Color Visual
Se verificó el cumplimiento en [RoutineColors.kt](file:///c:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/core/designsystem/color/RoutineColors.kt):
- **`Activity`**: Emerald (`roleEvent` = `#34D399`)
- **`Task`**: Indigo (`roleTask` = `#818CF8`)
- **`Reminder`**: Amber (`roleReminder` = `#FBBF24`)

El color representa la semántica del tipo de elemento y no su estado de ejecución.

### Mocks y Carga de Datos en Producción
- Las pantallas `Today`, `Planning` y `Stats` consumen repositorios e historiales Room reales.
- El sembrado de datos de prueba (`DatabaseSeed.kt`) se encuentra restringido al inicio de la aplicación en modo desarrollo (`RoutineApp.kt`).

---

## 9. Database / Migration Review

### Estado de la Base de Datos Room
- **Versión Actual**: 10
- **Tabla `life_systems`**: Registrada en `@Database` como `SystemEntity`.
- **Relaciones y Claves Foráneas**:
  - `ActivityDefinitionEntity.systemId` → `SystemEntity.id` (`ON DELETE SET NULL`)
  - `DailyInstanceEntity.parentInstanceId` → `DailyInstanceEntity.id` (`ON DELETE CASCADE`)
  - `DailyInstanceEntity.associatedInstanceId` → `DailyInstanceEntity.id` (`ON DELETE SET NULL`)
  - `DailyInstanceEntity.sourceRuleId` → `ScheduleRuleEntity.id` (`ON DELETE SET NULL`)
  - `DailyInstanceEntity.backlogId` → `BacklogItemEntity.id` (`ON DELETE SET NULL`)

### Historial de Migraciones
Room posee migraciones reales y continuas configuradas en [DatabaseModule.kt](file:///c:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/data/di/DatabaseModule.kt#L172):
- `MIGRATION_5_6`: Creación de backlog, refactor de daily_instances y executions.
- `MIGRATION_6_7`: Incorporación de `actionProtocol` en `daily_instances`.
- `MIGRATION_7_8`: Creación de snapshots en `notes`.
- `MIGRATION_8_9`: Incorporación de `associatedInstanceId`.
- `MIGRATION_9_10`: Incorporación de `role` (`ACTIVITY`, `TASK`, `REMINDER`).

---

## 10. Test Health

### Ejecución de Comandos
1. **Pruebas Unitarias (`.\gradlew.bat testDebugUnitTest`)**:
   - **Resultado**: `BUILD SUCCESSFUL` (Código de salida 0).
   - **Detalle**: 100% de los tests pasaron exitosamente.
2. **Compilación de Debug (`.\gradlew.bat assembleDebug`)**:
   - **Resultado**: `BUILD SUCCESSFUL` (Código de salida 0).
   - **Detalle**: La compilación se realiza sin errores de sintaxis o resolución de símbolos.

---

## 11. Documentation Drift

Se detectó desfasaje en los siguientes documentos de arquitectura y seguimiento:

1. **[09_MOCK_DATA_STATUS.md](file:///c:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/documentation/09_MOCK_DATA_STATUS.md)**:
   - Indica que `FakePlanningRepository`, `FakeTodayRepository` y `FakeStatsRepository` están "🟡 Activo". **Inconsistencia**: `FakePlanningRepository` y `FakeTodayRepository` ya fueron eliminados del código fuente en las ECs correspondientes, y `StatsViewModel` consume `GetHistoryAnalyticsUseCase` real.
   - Indica que `FakeSystemRepository` está "🟡 Activo". **Inconsistencia**: El archivo existe pero está 100% desvinculado (código huérfano).
   - Señala migraciones destructivas pendientes a v3 y v4. **Inconsistencia**: La base de datos está en v10 con migraciones 5 a 10 completamente operativas.

2. **[07_CURRENT_CONTEXT.md](file:///c:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/documentation/07_CURRENT_CONTEXT.md)**:
   - Presenta una discrepancia menor en la numeración entre el cuerpo ("EC-013") y la tabla de contexto ("EC-014").

---

## 12. Findings by Severity

| ID | Clasificación | Tipo | Archivo / Componente | Descripción | Estado Recomendado |
|---|---|---|---|---|---|
| **F-01** | `MEDIUM` | `ORPHAN_FILE` | [FakeSystemRepository.kt](file:///c:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/feature/system/data/FakeSystemRepository.kt) | Archivo de repositorio fake huérfano sin consumidores. | `DELETE` |
| **F-02** | `MEDIUM` | `ORPHAN_FILE` | [SystemModels.kt](file:///c:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/feature/system/model/SystemModels.kt) | Modelos `LifeArea` y `SystemSummary` huérfanos sin consumidores. | `DELETE` |
| **F-03** | `MEDIUM` | `ORPHAN_FILE` | [ResolveTimelineForDateRange.kt](file:///c:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/domain/usecase/ResolveTimelineForDateRange.kt) | Caso de uso obsoleto sin inyección ni uso. | `DELETE` |
| **F-04** | `LOW` | `DEAD_CODE` | [GetSystemsWithStatsUseCase.kt](file:///c:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/domain/usecase/GetSystemsWithStatsUseCase.kt) | Caso de uso `@Deprecated` sin consumidores en UI, pero mantenido en `UseCaseModule.kt`. | `CLEANUP_DI` / `DELETE` |
| **F-05** | `LOW` | `NAVIGATION_RESIDUE` | [AppRoutes.kt](file:///c:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/core/navigation/AppRoutes.kt) | Rutas `Planner` y `Activities` no mapeadas en `AppNavHost.kt`. | `CLEANUP` en EC-014 |
| **F-06** | `MEDIUM` | `DOCUMENTATION_DRIFT` | [09_MOCK_DATA_STATUS.md](file:///c:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/documentation/09_MOCK_DATA_STATUS.md) | Mantiene repositorios fakes y migraciones destructivas como activos cuando el código ya los eliminó/reemplazó. | `UPDATE_DOCS` |
| **F-07** | `LOW` | `DOCUMENTATION_DRIFT` | [07_CURRENT_CONTEXT.md](file:///c:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/documentation/07_CURRENT_CONTEXT.md) | Discrepancia menor en referencia entre EC-013 y EC-014. | `UPDATE_DOCS` |

---

## 13. Recommended Cleanup Order

Para abordar los hallazgos sin afectar el desarrollo de `EC-014` ni romper contratos existentes, se recomienda la siguiente secuencia de saneamiento:

1. **Paso 1: Saneamiento de Archivos Huérfanos**
   - Eliminar el paquete `feature/system/` (`FakeSystemRepository.kt`, `SystemModels.kt`).
   - Eliminar `domain/usecase/ResolveTimelineForDateRange.kt`.
2. **Paso 2: Limpieza de DI y Casos de Uso Obsoletos**
   - Remover `provideGetSystemsWithStatsUseCase` de [UseCaseModule.kt](file:///c:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/data/di/UseCaseModule.kt) y eliminar o reubicar [GetSystemsWithStatsUseCase.kt](file:///c:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/domain/usecase/GetSystemsWithStatsUseCase.kt).
3. **Paso 3: Depuración de Rutas de Navegación (Durante EC-014)**
   - Limpiar `AppRoutes.Planner` y `AppRoutes.Activities` de [AppRoutes.kt](file:///c:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/core/navigation/AppRoutes.kt) y [MainActivity.kt](file:///c:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/MainActivity.kt) al consolidar el workspace de Planning.
4. **Paso 4: Actualización de Documentación**
   - Actualizar [09_MOCK_DATA_STATUS.md](file:///c:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/documentation/09_MOCK_DATA_STATUS.md) reflejando que los repositorios Fake han sido eliminados y las migraciones de DB están al día (v10).
   - Sincronizar [07_CURRENT_CONTEXT.md](file:///c:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/documentation/07_CURRENT_CONTEXT.md).

---

## 14. Final Status

```text
CHANGES_REQUESTED
```

**Motivo**: Aunque el motor principal, la base de datos, la compilación y los tests unitarios están en un estado **100% operacional y consistente con el dominio**, la presencia de 4 archivos huérfanos/muertos (`FakeSystemRepository.kt`, `SystemModels.kt`, `ResolveTimelineForDateRange.kt`, `GetSystemsWithStatsUseCase.kt`), residuos de rutas obsoletas y la falta de actualización en `09_MOCK_DATA_STATUS.md` requieren un pase de saneamiento explícito antes de dar por cerrado el estado de limpieza previo a la nueva EC.
