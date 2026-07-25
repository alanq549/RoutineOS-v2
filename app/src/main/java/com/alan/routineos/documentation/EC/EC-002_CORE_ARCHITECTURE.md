---
id: EC-002
title: Core Architecture & Base Repositories
phase: 2
priority: High
effort: Medium
owner: AI Agent
status: CHANGES_REQUESTED
depends_on: EC-001
branch: feature/ec-002-core-architecture
audit: Pending
created: 2026-07-21
updated: 2026-07-24
---

# EC-002: Core Architecture & Base Repositories

## Objetivo
Establecer la capa de dominio y la implementación de repositorios reales para conectar la base de datos Room con la lógica de negocio de la aplicación.

## Contexto
Tras completar la persistencia (EC-001), el proyecto requiere una capa intermedia que abstraiga el origen de los datos. Esto permite que las features consuman modelos de dominio limpios en lugar de entidades de Room directamente.

## Problema
Actualmente, las features utilizan `FakeRepositories` con modelos locales. Se necesita una estructura de Repositorios que realice el mapeo entre `RoutineEntity` y modelos de dominio, facilitando la transición hacia datos reales.

## Alcance
- [x] Definición de modelos de dominio (`Routine`, `Task`) en el paquete `domain`.
- [x] Creación de interfaces de repositorio en `domain/repository`.
- [x] Implementación de `OfflineRoutineRepository` en `data/repository`.
- [x] Lógica de mapeo (Mappers) entre entidades y dominio.
- [x] Configuración de Hilt para la provisión de repositorios.
- [x] Exclusión: No se eliminarán los `FakeRepositories` todavía para no romper las previews de UI existentes.

## Archivos Afectados
- `app/src/main/java/com/alan/routineos/domain/model/Routine.kt`
- `app/src/main/java/com/alan/routineos/domain/model/Task.kt`
- `app/src/main/java/com/alan/routineos/domain/repository/RoutineRepository.kt`
- `app/src/main/java/com/alan/routineos/data/repository/OfflineRoutineRepository.kt`
- `app/src/main/java/com/alan/routineos/data/di/RepositoryModule.kt`

## Plan de Implementación
1. Crear el árbol de paquetes `com.alan.routineos.domain` (model, repository).
2. Definir los modelos de dominio limpios.
3. Definir la interfaz `RoutineRepository` con operaciones CRUD básicas y flujos reactivos.
4. Implementar `OfflineRoutineRepository` inyectando `RoutineDao` y `TaskDao`.
5. Crear funciones de extensión para el mapeo bi-direccional.
6. Proveer el repositorio mediante un nuevo `RepositoryModule` en Hilt.

## Validaciones
- [x] El proyecto compila correctamente con la nueva inyección.
- [x] Tests unitarios para el mapeo (Entity -> Domain).
- [x] Test de integración del Repositorio con base de datos en memoria.

## Resultado Esperado
Un puente funcional entre la base de datos y la lógica de negocio, permitiendo que futuras ECs implementen casos de uso o ViewModels conectados a datos reales.

## Auditoría
- [ ] ¿Los modelos de dominio son independientes de Room?
- [ ] ¿Se inyectan interfaces en lugar de implementaciones?
- [ ] ¿El mapeo está centralizado y es testeable?

## Lecciones Aprendidas
(A completar tras la implementación).

## Estado
El estado vigente de esta EC es el declarado en el campo `status` del frontmatter (arriba de este documento). No dupliques el valor aquí.
