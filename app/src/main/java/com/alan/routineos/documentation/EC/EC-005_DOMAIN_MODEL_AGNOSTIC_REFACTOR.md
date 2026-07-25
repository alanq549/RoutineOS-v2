---
id: EC-005
title: Domain Model Agnostic Refactor
phase: 2
priority: High
effort: Medium
owner: AI Agent
status: IMPLEMENTED
depends_on: EC-002
branch: refactor/ec-005-domain-agnostic
audit: Pending
created: 2026-07-23
updated: 2026-07-24
---

# EC-005: Domain Model Agnostic Refactor

## Objetivo
Eliminar la violación de invariante de dominio detectada en la auditoría retroactiva de EC-001 y EC-002 (ver [AUDIT_EC-001.md](../AUDITS/AUDIT_EC-001.md) y [AUDIT_EC-002.md](../AUDITS/AUDIT_EC-002.md)), renombrando todas las clases, entidades de datos, interfaces y repositorios a un modelo genérico *domain-agnostic*.

## Contexto
El análisis de gobernanza en `develop` demostró que modelar el dominio y la persistencia alrededor de conceptos como "Routine" y "Task" viola el principio central de [08_ARCHITECTURE_INVARIANTS.md](../08_ARCHITECTURE_INVARIANTS.md). El motor de RoutineOS v2 debe ser agnóstico del dominio en tiempo de compilación y utilizar los tipos genéricos del core temporal (`ActivityDefinition` y `ActivityNode`).

## Problema
El acoplamiento actual de clases de datos y negocio con términos como `Task` (traducción de "tarea", prohibida explícitamente en las reglas) compromete la escalabilidad conceptual y el diseño agnóstico del motor.

## Alcance
- [x] Renombrar `RoutineEntity` a `ActivityDefinitionEntity`.
- [x] Renombrar `TaskEntity` a `ActivityNodeEntity`.
- [x] Renombrar `Routine` a `ActivityDefinition` en los modelos de dominio.
- [x] Renombrar `Task` a `ActivityNode` en los modelos de dominio.
- [x] Renombrar `RoutineRepository` a `ActivityRepository`.
- [x] Renombrar `OfflineRoutineRepository` a `OfflineActivityRepository`.
- [x] Renombrar `RoutineDao` a `ActivityDefinitionDao` y `TaskDao` a `ActivityNodeDao`.
- [x] Actualizar la base de datos `RoutineDatabase` para usar las nuevas entidades, renombrando las tablas internas a `activity_definitions` y `activity_nodes`.
- [x] Renombrar y actualizar la lógica de mappers en `RoutineMappers.kt` a `ActivityMappers.kt`.
- [x] Ajustar la inyección de dependencias en `DatabaseModule` y `RepositoryModule`.
- [x] Refactorear y verificar todas las pruebas locales e instrumentadas afectadas por el cambio.
- [x] Exclusión: No se crearán pantallas ni se alterará el flujo de usuario visual en esta EC.

## Archivos Afectados
- `app/src/main/java/com/alan/routineos/data/local/RoutineOSDatabase.kt`
- `app/src/main/java/com/alan/routineos/data/local/entities/ActivityDefinitionEntity.kt`
- `app/src/main/java/com/alan/routineos/data/local/entities/ActivityNodeEntity.kt`
- `app/src/main/java/com/alan/routineos/data/local/dao/ActivityDefinitionDao.kt`
- `app/src/main/java/com/alan/routineos/data/local/dao/ActivityNodeDao.kt`
- `app/src/main/java/com/alan/routineos/data/mapper/ActivityMappers.kt`
- `app/src/main/java/com/alan/routineos/domain/model/ActivityDefinition.kt`
- `app/src/main/java/com/alan/routineos/domain/model/ActivityNode.kt`
- `app/src/main/java/com/alan/routineos/domain/repository/ActivityRepository.kt`
- `app/src/main/java/com/alan/routineos/data/repository/OfflineActivityRepository.kt`
- `app/src/main/java/com/alan/routineos/data/di/DatabaseModule.kt`
- `app/src/main/java/com/alan/routineos/data/di/RepositoryModule.kt`
- `app/src/test/java/com/alan/routineos/data/mapper/ActivityMapperTest.kt`
- `app/src/androidTest/java/com/alan/routineos/data/local/RoomDatabaseTest.kt`
- `app/src/androidTest/java/com/alan/routineos/data/repository/OfflineActivityRepositoryTest.kt`

## Plan de Implementación
1. Renombrar clases y archivos en la capa de persistencia (`entities` y `dao`), modificando el nombre de las tablas en Room `@Entity` a `activity_definitions` y `activity_nodes`.
2. Actualizar las relaciones de claves foráneas y queries de Room en los DAOs con el nuevo modelo.
3. Actualizar `RoutineDatabase` para registrar las nuevas clases de entidad y métodos abstractos de DAO.
4. Renombrar las clases de dominio puro (`Routine` -> `ActivityDefinition`, `Task` -> `ActivityNode`).
5. Renombrar y adaptar el repositorio y su implementación inyectada.
6. Ajustar los mappers de datos de base de datos a dominio y viceversa.
7. Corregir y renombrar los archivos de tests unitarios e instrumentados, garantizando la cobertura de borrado en cascada y aserciones correspondientes.

## Validaciones
- [ ] Compilación del proyecto exitosa.
- [ ] Pruebas unitarias de mappers corriendo con éxito.
- [ ] Pruebas instrumentadas de base de datos y repositorios en memoria pasando satisfactoriamente.
- [ ] Nueva exportación del esquema de Room generada bajo `app/schemas` reflejando las nuevas tablas.

## Resultado Esperado
Un motor de base de datos e infraestructura de dominio completamente desacoplados de nombres de dominio específicos, respetando las invariantes arquitectónicas del proyecto.

## Auditoría
Consultar guía en [AUDITS/README.md](../AUDITS/README.md).
- [ ] ¿Se eliminaron todas las referencias a clases del tipo `Routine` y `Task` en persistencia y dominio?
- [ ] ¿Los tests unitarios e instrumentados pasan al 100%?
- [ ] ¿Los nombres de las tablas de base de datos son genéricos (`activity_definitions`, `activity_nodes`)?

## Lecciones Aprendidas
(A completar tras la implementación).

## Definition of Done (Obligatorio)
Antes de marcar como COMPLETED, verificar:
- [ ] Compila sin warnings nuevos
- [ ] Tests existentes pasan (unitarios + los que aplique)
- [ ] Checklist de [ARCHITECTURE_INVARIANTS.md](../08_ARCHITECTURE_INVARIANTS.md) revisado y sin violaciones
- [ ] Si se usó Fake*Repository, está registrado en [MOCK_DATA_STATUS.md](../09_MOCK_DATA_STATUS.md)
- [ ] Lecciones aprendidas documentadas arriba

## Checklist de Validación de Usuario
- [ ] N/A (Esta EC representa una refactorización de arquitectura interna de persistencia y dominio, sin pantalla asociada).

## Estado
El estado vigente de esta EC es el declarado en el campo `status` del frontmatter (arriba de este documento). No dupliques el valor aquí.
