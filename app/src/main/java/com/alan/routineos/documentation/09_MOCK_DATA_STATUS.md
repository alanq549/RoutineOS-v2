# Estado de datos simulados (Fake Repositories)

Este documento rastrea qué pantallas/features usan repositorios falsos (Fake*Repository)
en vez de la capa Room real, para evitar que queden como deuda técnica invisible.

| Feature | Archivo | Repositorio Fake | EC que lo introdujo | Fecha límite de reemplazo | Estado |
|---|---|---|---|---|---|
| Planning | FakePlanningRepository | Sí | Pre-existente (commit 7525a2a, 2026-07-19) — detectado en auditoría de EC-006 | Pendiente | 🟡 Activo |
| Today | FakeTodayRepository | Sí | Pre-existente (commit 7525a2a, 2026-07-19) — detectado en auditoría de EC-006 | Pendiente | 🟡 Activo |
| Stats | FakeStatsRepository | Sí | Pre-existente (commit 7525a2a, 2026-07-19) — detectado en auditoría de EC-006 | Pendiente | 🟡 Activo |
| System | FakeSystemRepository | Sí | Pre-existente (commit 7525a2a, 2026-07-19) — detectado en auditoría de EC-006 | Pendiente | 🟡 Activo |

Regla: ningún EC puede marcarse como COMPLETED si introduce o mantiene un Fake*Repository
sin una entrada en esta tabla con fecha límite de reemplazo.
