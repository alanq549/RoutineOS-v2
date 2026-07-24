---
ec_id: EC-001
tipo: retroactiva
ronda: 1
fecha: 2026-07-23
resultado: CHANGES_REQUESTED
---
## Hallazgos
El auditor revisó el alcance y el código actual de la EC-001:
1. **Verificación de Hallazgos Previos**:
   - **Schema Export**: Resuelto. `exportSchema = true` está configurado en [RoutineDatabase.kt](file:///c:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/data/local/RoutineDatabase.kt) y la ruta de exportación está configurada en `build.gradle.kts`.
   - **Métrica de Tests**: Resuelto. Se implementó el quinto test en [RoomDatabaseTest.kt](file:///c:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/androidTest/java/com/alan/routineos/data/local/RoomDatabaseTest.kt) (`readNonExistentRoutineReturnsNull`), alcanzando una cobertura de 5/5 tests instrumentados para la base de datos.
2. **Violación Crítica de 08_ARCHITECTURE_INVARIANTS.md**:
   - La EC define las tablas/entidades `RoutineEntity` y `TaskEntity`.
   - El archivo [08_ARCHITECTURE_INVARIANTS.md](file:///c:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/documentation/08_ARCHITECTURE_INVARIANTS.md) prohíbe explícitamente en su principio central que existan conceptos de dominio específicos como tipo, enum o campo hardcodeado. Lista explícitamente **"tarea"** (task) como concepto prohibido.
   - Además, define que las únicas entidades núcleo válidas son: `ActivityDefinition`, `ActivityNode`, `MetadataSchema`, `ScheduleRule`, `ScheduleException`, `TimelineInstance`, `ActivityExecution`.
   - Por ende, modelar la persistencia sobre los tipos específicos `RoutineEntity` y `TaskEntity` es una violación directa del invariante arquitectónico de diseño domain-agnostic.

## Plan de corrección
Dado que esta auditoría es **retroactiva** y el código ya fue mergeado, se propone una **nueva EC correctiva** en formato borrador:

### Borrador de EC Correctiva: EC-005_PERSISTENCE_REFACTOR_AGNOSTIC
* **Título**: Refactorear Capa de Persistencia a Entidades Domain-Agnostic
* **Fase**: 2 (Core & Data Layer)
* **Objetivo**: Reemplazar las entidades y DAOs específicos de Routine/Task por las entidades del core temporal (`ActivityDefinition` y `ActivityNode`) para respetar los invariantes del diseño domain-agnostic.
* **Alcance**:
  - Renombrar/Reemplazar `RoutineEntity` por `ActivityDefinitionEntity`.
  - Renombrar/Reemplazar `TaskEntity` por `ActivityNodeEntity`.
  - Renombrar/Reemplazar `RoutineDao` por `ActivityDefinitionDao`.
  - Renombrar/Reemplazar `TaskDao` por `ActivityNodeDao`.
  - Actualizar `RoutineDatabase` para usar las nuevas entidades y DAOs.
  - Refactorear los tests instrumentados de base de datos (`RoomDatabaseTest`).
* **Archivos Afectados**:
  - [RoutineDatabase.kt](file:///c:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/data/local/RoutineDatabase.kt)
  - [RoutineEntity.kt](file:///c:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/data/local/entities/RoutineEntity.kt) (Eliminar y crear `ActivityDefinitionEntity.kt`)
  - [TaskEntity.kt](file:///c:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/data/local/entities/TaskEntity.kt) (Eliminar y crear `ActivityNodeEntity.kt`)
  - [RoutineDao.kt](file:///c:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/data/local/dao/RoutineDao.kt) (Eliminar y crear `ActivityDefinitionDao.kt`)
  - [TaskDao.kt](file:///c:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/data/local/dao/TaskDao.kt) (Eliminar y crear `ActivityNodeDao.kt`)
  - [DatabaseModule.kt](file:///c:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/data/di/DatabaseModule.kt)
  - [RoomDatabaseTest.kt](file:///c:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/androidTest/java/com/alan/routineos/data/local/RoomDatabaseTest.kt)
