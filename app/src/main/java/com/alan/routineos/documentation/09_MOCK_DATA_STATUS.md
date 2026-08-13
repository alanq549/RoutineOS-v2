# Estado de datos simulados (Fake Repositories)

Este documento rastrea qué pantallas/features usan repositorios falsos (Fake*Repository)
en vez de la capa Room real, para evitar que queden como deuda técnica invisible.

| Feature | Archivo | Repositorio Fake | EC que lo introdujo | Fecha límite de reemplazo | Estado |
|---|---|---|---|---|---|
| Planning | FakePlanningRepository | Sí | Pre-existente (commit 7525a2a, 2026-07-19) — detectado en auditoría de EC-006 | Pendiente | 🟡 Activo |
| Today | FakeTodayRepository | Sí | Pre-existente (commit 7525a2a, 2026-07-19) — detectado en auditoría de EC-006 | EC-012 | 🟡 Activo |
| Stats | FakeStatsRepository | Sí | Pre-existente (commit 7525a2a, 2026-07-19) — detectado en auditoría de EC-006 | Pendiente | 🟡 Activo |
| System | FakeSystemRepository | Sí | Pre-existente (commit 7525a2a, 2026-07-19) — detectado en auditoría de EC-006 | Pendiente | 🟡 Activo |

## Datos Semilla (Development Seed)

| Entidad | Origen | Propósito | Estado |
|---|---|---|---|
| Universidad (Demo Jerárquica) | DatabaseSeed.kt | Validar jerarquía de 3 niveles en Dashboard/Detalle. | 🟢 Activo (Dev only) |

## Deuda Técnica de Infraestructura

| Tarea | Origen | Impacto | Estado |
|---|---|---|---|
| Migración destructiva a DB v3 | EC-009 | Pérdida de datos locales existentes tras actualización. Requiere implementación de migraciones reales antes de producción. | 🔴 Pendiente |
| Migración destructiva a DB v4 | EC-011 | Agregado de `scheduledDate` a `ActivityExecutionEntity`. Rompe compatibilidad con v3. | 🔴 Pendiente |

Regla: ningún EC puede marcarse como COMPLETED si introduce o mantiene un Fake*Repository
sin una entrada en esta tabla con fecha límite de reemplazo.
