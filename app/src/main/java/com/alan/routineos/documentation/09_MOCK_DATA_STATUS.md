# Estado de datos simulados (Fake Repositories)

Este documento rastrea qué pantallas/features usan repositorios falsos (Fake*Repository)
en vez de la capa Room real, para evitar que queden como deuda técnica invisible.

| Feature | Archivo | Repositorio Fake | EC que lo introdujo | Fecha límite de reemplazo | Estado |
|---|---|---|---|---|---|
| Planning | FakePlanningRepository | Sí | EC-00X | Pendiente | 🟡 Activo |

Regla: ningún EC puede marcarse como COMPLETED si introduce o mantiene un Fake*Repository
sin una entrada en esta tabla con fecha límite de reemplazo.
