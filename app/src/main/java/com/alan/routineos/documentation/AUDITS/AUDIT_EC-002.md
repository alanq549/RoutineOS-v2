---
ec_id: EC-002
tipo: retroactiva
ronda: 1
fecha: 2026-07-23
resultado: CHANGES_REQUESTED
---
## Hallazgos
El auditor revisó el alcance y el código actual de la EC-002:
1. **Verificación de Hallazgos Previos**:
   - **Inconsistencia de Tests en Walkthrough**: Resuelto. El total de pruebas unitarias e instrumentadas en la capa de datos y repositorios está alineado con la realidad.
   - **Cobertura Incompleta en OfflineRoutineRepositoryTest**: Resuelto. Se implementaron pruebas instrumentadas para `upsertTask` y `deleteTask` en [OfflineRoutineRepositoryTest.kt](file:///c:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/androidTest/java/com/alan/routineos/data/repository/OfflineRoutineRepositoryTest.kt).
   - **Verificación de Cascada**: Resuelto. Se añadió la prueba `deleteRoutineCascadesToTasks` en el test de integración del repositorio.
   - **Refinamiento de Mapper Test**: Resuelto. Se agregó el 5º test unitario de mappers en [RoutineMapperTest.kt](file:///c:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/test/java/com/alan/routineos/data/mapper/RoutineMapperTest.kt).
2. **Violación Crítica de 08_ARCHITECTURE_INVARIANTS.md**:
   - La EC-002 define los modelos de dominio [Routine.kt](file:///c:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/domain/model/Routine.kt) y [Task.kt](file:///c:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/domain/model/Task.kt).
   - Al igual que en la persistencia, el uso del tipo `Task` representa una traducción literal del concepto prohibido **"tarea"** (task).
   - El uso de clases específicas de dominio (`Routine`, `Task`) vulnera la regla de diseño domain-agnostic definida en [08_ARCHITECTURE_INVARIANTS.md](file:///c:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/documentation/08_ARCHITECTURE_INVARIANTS.md). Los modelos válidos en la capa de dominio son `ActivityDefinition` y `ActivityNode`.
   - La interfaz [RoutineRepository.kt](file:///c:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/domain/repository/RoutineRepository.kt) y su implementación expone y depende directamente de estas clases no válidas.

## Plan de corrección
Dado que esta auditoría es **retroactiva** y el código ya fue mergeado, se propone una **nueva EC correctiva** en formato borrador:

### Borrador de EC Correctiva: EC-006_DOMAIN_REFACTOR_AGNOSTIC
* **Título**: Refactorear Modelos de Dominio y Repositorios a Estructuras Domain-Agnostic
* **Fase**: 2 (Core & Data Layer)
* **Objetivo**: Migrar la capa de dominio, mapeadores y la abstracción de repositorios hacia los tipos definidos en la gobernanza arquitectónica (`ActivityDefinition` y `ActivityNode`), eliminando los tipos específicos `Routine` y `Task`.
* **Alcance**:
  - Reemplazar el modelo `Routine` por `ActivityDefinition`.
  - Reemplazar el modelo `Task` por `ActivityNode`.
  - Reemplazar la interfaz `RoutineRepository` por `ActivityRepository` (o `ActivityDefinitionRepository`).
  - Reemplazar `OfflineRoutineRepository` por `OfflineActivityRepository`.
  - Adaptar los mappers bidireccionales en `RoutineMappers.kt` (o renombrarlo a `ActivityMappers.kt`).
  - Refactorear `RoutineMapperTest` y `OfflineRoutineRepositoryTest` para verificar el correcto funcionamiento con los nuevos modelos domain-agnostic.
  - Actualizar `RepositoryModule` en Hilt para proveer `ActivityRepository`.
* **Archivos Afectados**:
  - [Routine.kt](file:///c:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/domain/model/Routine.kt) (Eliminar y crear `ActivityDefinition.kt`)
  - [Task.kt](file:///c:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/domain/model/Task.kt) (Eliminar y crear `ActivityNode.kt`)
  - [RoutineRepository.kt](file:///c:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/domain/repository/RoutineRepository.kt) (Eliminar y crear `ActivityRepository.kt`)
  - [OfflineRoutineRepository.kt](file:///c:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/data/repository/OfflineRoutineRepository.kt) (Eliminar y crear `OfflineActivityRepository.kt`)
  - [RepositoryModule.kt](file:///c:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/data/di/RepositoryModule.kt)
  - [RoutineMappers.kt](file:///c:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/data/mapper/RoutineMappers.kt) (Renombrar/Actualizar)
  - [RoutineMapperTest.kt](file:///c:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/test/java/com/alan/routineos/data/mapper/RoutineMapperTest.kt)
  - [OfflineRoutineRepositoryTest.kt](file:///c:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/androidTest/java/com/alan/routineos/data/repository/OfflineRoutineRepositoryTest.kt)
