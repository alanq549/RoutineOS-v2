# Estado de Datos Simulados y Persistencia (Room DB)

Este documento rastrea el estado real de la infraestructura de datos y persistencia en RoutineOS v2.

## Estado de Repositorios

| Feature | Repositorio Actual | Persistencia Real | Estado |
|---|---|---|---|
| Activities / Catalog | ActivityRepositoryImpl | Room Database (Local) | 🟢 Real DB |
| Today Workspace | ActivityRepositoryImpl | Room Database (Local) | 🟢 Real DB |
| Planning Workspace | ActivityRepositoryImpl | Room Database (Local) | 🟢 Real DB |
| Systems / Domains | ActivityRepositoryImpl | Room Database (Local) | 🟢 Real DB |
| Stats Engine | ActivityRepositoryImpl | Room Database (Local) | 🟢 Real DB |

> [!NOTE]
> Todos los antiguos repositorios simulados (`FakePlanningRepository`, `FakeTodayRepository`, `FakeStatsRepository`, `FakeSystemRepository`) han sido completamente eliminados del proyecto.

## Datos Semilla (Development Seed)

| Entidad | Origen | Propósito | Estado |
|---|---|---|---|
| Universidad (Demo Jerárquica) | DatabaseSeed.kt | Validar jerarquía de 3 niveles en Dashboard/Detalle. | 🟢 Activo (Dev only) |

## Infraestructura y Migraciones de Base de Datos (Room v10)

La versión actual del esquema de base de datos es **Room DB v10**. Todas las migraciones son no destructivas y están activas en `DatabaseModule.kt`:

| Migración | Versiones | Propósito | Estado |
|---|---|---|---|
| `MIGRATION_5_6` | v5 ➔ v6 | Creación de backlog_items, refactor de daily_instances y executions | 🟢 Operativo |
| `MIGRATION_6_7` | v6 ➔ v7 | Incorporación de `actionProtocol` en `daily_instances` | 🟢 Operativo |
| `MIGRATION_7_8` | v7 ➔ v8 | Creación de snapshots en `notes` | 🟢 Operativo |
| `MIGRATION_8_9` | v8 ➔ v9 | Incorporación de `associatedInstanceId` en `daily_instances` | 🟢 Operativo |
| `MIGRATION_9_10` | v9 ➔ v10 | Incorporación de `role` (`ACTIVITY`, `TASK`, `REMINDER`) en `daily_instances` | 🟢 Operativo |
