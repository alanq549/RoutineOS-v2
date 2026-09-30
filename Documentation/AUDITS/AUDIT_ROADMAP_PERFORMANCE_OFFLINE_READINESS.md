# Auditoría integral: roadmap, rendimiento y preparación offline-first

**Fecha:** 2026-09-30  
**Alcance:** diagnóstico estático del checkout. No se modificaron código de producto, tests ni migraciones; no se ejecutaron tests, builds, benchmarks ni navegación manual. Este informe es el único artefacto creado.

## 1. Executive Summary

- EC-RE-013…017 aparecen CLOSED en el status/contexto más reciente y sus cambios principales están reflejados en el código. No se propone reabrir Today, Planning consolidation, Activity Catalog, Systems removal ni Spontaneous Event temporal resolution.
- Backlog es el siguiente trabajo de producto mejor sustentado: su entidad/tabla/DAO y vínculo con DailyInstance existen, pero no se demuestra un flujo de usuario/panel. Requiere trabajo real. Candidato tentativo EC-RE-018; no se creó una EC.
- Se reporta latencia percibida al navegar, pero no hay dispositivo/perfilador/traza en esta sesión. No hay timings exactos. Código sugiere puntos que medir (Stats histórico, fan-out metadata en Today, consultas por definición del catálogo y estado inicial vacío), pero no prueba que Navigation sea la causa.
- Loading UX es parcial: Stats modela Loading/Empty/Error; Today, Planning y Catalog tienen campos `isLoading` sin ramas visibles equivalentes; Account se alimenta de fake local y no requiere skeleton.
- Room es source of truth local hoy, con repositorio/Flow; no existe infraestructura de sync. Faltan timestamps de cambio consistentes, tombstones, versionado, outbox, ids de servidor y resolución de conflictos.
- La documentación tiene deriva considerable. Las rutas pedidas `documentation/EC` y `documentation/AUDITS` no existen actualmente; documentos equivalentes viven bajo `app/src/main/java/com/alan/routineos/documentation/`. La auditoría de 2026-09-06 que recomendaba EC-014/Planning consolidation está obsoleta frente a EC-RE-017 cerrada.

## 2. Current Architecture Snapshot

```text
Compose Route → ViewModel → UseCase / ActivityRepository (contrato de dominio)
                                  ↓
                OfflineActivityRepository → DAO → Room v10
```

`RepositoryModule` enlaza `ActivityRepository` con `OfflineActivityRepository`; DAOs emiten varios `Flow` y escrituras suspend. No se encontró fuente remota, DTO de API ni motor de sync. Hay separación entre modelos de dominio y entidades Room, con mappers. Room permite operar sin red para las pantallas conectadas. `Account` es excepción: mantiene `FakeAccountRepository`.

Las carpetas reales de código son `app/src/main/java/com/alan/routineos/{feature,domain,data,core}`. Migraciones/schemas y tests existen, pero no se ejecutaron. Se inspeccionó solo el estado actual.

## 3. Roadmap Audit

No se encontraron las rutas exactas solicitadas en raíz. Equivalentes: `app/src/main/java/com/alan/routineos/documentation/EC/`, `.../AUDITS/`, `04_PROJECT_STATUS.md`, `07_CURRENT_CONTEXT.md`, `05_ROADMAP.md`, `08_ARCHITECTURE_INVARIANTS.md` y `09_MOCK_DATA_STATUS.md`. Las tablas siguientes contrastan status, fichas/audits disponibles y código; “implementada” no implica que se haya repetido validación manual en esta auditoría.

| EC | Título | Estado documental | Implementada | Auditada | Cerrada | Qué resolvió | Relevancia / obsolescencia |
|---|---|---|---|---|---|---|---|
| 000 | Project Setup & Philosophy | CLOSED | Sí, fundacional | Sí | Sí | Framework documental | Histórica |
| 001 | Data Foundation | CLOSED | Sí | Sí | Sí | Room/data base | Consolidada/corregida después por 005 |
| 002 | Core Architecture & Base Repositories | CLOSED | Sí | Sí | Sí | Repositorios base | Consolidada/corregida por 005 |
| 003 | Design System Foundations | CLOSED | Sí | Sí | Sí | Tokens/componentes | Base vigente |
| 004 | Sin ficha | No listada | — | — | — | Numeración saltada; EC-006 fue originalmente 004 | Histórico, no inventar pendiente |
| 005 | Domain Model Agnostic Refactor | CLOSED | Sí | Sí | Sí | Dominio agnóstico | Invariante vigente |
| 006 | Routine Dashboard / Activity Catalog | APPROVED en tabla | Sí | Sí | Audit general lo considera finalizado | Catálogo reactivo | No pendiente; estado APPROVED/CLOSED inconsistente |
| 007 | Activity Creation Flow | APPROVED | Sí | Sí | Audit general lo considera finalizado | Alta de definiciones | No pendiente |
| 008 | Activity Detail & Nodes | APPROVED | Sí | Sí | Audit general lo considera finalizado | Detalle y jerarquía | No pendiente |
| 009 | Activity Execution Engine | APPROVED | Sí | Sí | Audit general lo considera finalizado | Ejecuciones/metadatos | No pendiente |
| 010 | Interaction Cleanup | CLOSED | Sí | Sí | Sí | Tap targets/feedback | Cerrada |
| 011 | Scheduling Model | CLOSED | Sí | Sí | Sí | Reglas/excepciones | Cerrada |
| 012 | Today Real Integration | Ficha histórica | Sí | Sí indirecta | Sí por ECs siguientes | Today con datos reales | Numeración histórica, no duplicar |
| 013 | Planning Consolidation legacy | Ficha duplicada | Sí vía RE-017 | Sí vía RE-017 | Sí vía RE-017 | Consolidación de Planning | Obsoleta/duplicada |
| 014 | Planning Consolidation legacy | Ficha duplicada | Sí vía RE-017 | Sí vía RE-017 | Sí vía RE-017 | Mismo trabajo que EC-013 legacy | Obsoleta/duplicada |
| RE-001 | Hierarchical Activity Nodes | CLOSED | Sí | Sí | Sí | Árbol de nodos | Cerrada |
| RE-002 | Progressive Activity Editor | CLOSED | Sí | Sí | Sí | Edición jerárquica/undo | Cerrada |
| RE-003 | Scheduling Engine / Daily Instances | CLOSED | Sí | Sí | Sí | Materialización/resolución | Hay dos fichas RE-003; duplicidad documental |
| RE-004 | Flexible Rules Editor | CLOSED | Sí | Sí | Sí | Editor de reglas | Cerrada |
| RE-005 | Metadata Schemas | CLOSED | Sí | Sí | Sí | Esquemas/captura | Cerrada |
| RE-006 | Today Workspace | CLOSED | Sí | Sí | Sí | Ejecución/captura | Cerrada |
| RE-007 | Contextual Organization | CLOSED | Sí | Sí | Sí | LifeSystem como agrupación | No reabrir; LifeSystem sigue vigente como dato |
| RE-008 | Planning & Today Refinement | CLOSED | Sí | Sí | Sí | Datos reales/pulido | Cerrada |
| RE-009 | Interruption Logic | CLOSED | Sí | Sí | Sí | Conflictos/eventos espontáneos | Cerrada |
| RE-010 | Historical Analysis & Trends | CLOSED | Sí | Sí | Sí | Analítica histórica | Cerrada; optimizar es pregunta distinta |
| RE-011 | Intelligence Dashboard | CLOSED | Sí | Sí | Sí | Ritmo/adherencia | Cerrada |
| RE-012 | Planner Interactivity | CLOSED | Sí | Sí | Sí | Move/skip/reset/catálogo | Cerrada |
| RE-013 | Systems & Activities Refinement | CLOSED | Sí | Sí | Sí | Selección real y refinamiento | Cerrada; no reabrir Systems removal |
| RE-014 | Project Cleanup & Consistency | CLOSED | Sí | Sí | Sí | Limpieza de residuos | Cerrada; no impide medir latencia |
| RE-015 | Today Execution Surface Refinement | CLOSED | Sí | Sí | Sí | Espina/cards/contexto | Cerrada; más pulido sería cosmético sin evidencia |
| RE-016 | Plannable Element Domain Analysis | CLOSED (análisis) | Sí, decisión documentada | Sí | Sí | DailyInstance como ocurrencia operativa | Vigente como modelo; dudas puntuales por validar |
| RE-017 | Planning Workspace Consolidation | CLOSED / manual PASS en contexto | Sí | Sí | Sí | Ruta unificada, catálogo como ModalBottomSheet, renombre ActivityCatalog | Cerrada; no repetir |
| RE-018 | Backlog Panel | Solo referencia futura | Parcial | No se encontró ficha | No | Panel/ciclo de pendientes | Candidato futuro, no EC aprobada |

### Drift y duplicados

- EC-013 y EC-014 legacy duplican consolidación, reasignada a RE-017; existen dos fichas RE-003. Son residuos de numeración, no trabajos activos separados.
- `07_CURRENT_CONTEXT.md` tiene fecha 2026-09-06 y propone RE-018 Backlog. Es contexto heredado, no prueba de ficha aprobada.
- `AUDIT_ROADMAP_NEXT_STEP.md` (2026-09-06) dice que Planning consolidation aún falta y recomienda EC-014; contradice RE-017 y código posterior, por tanto está obsoleta.
- `04_PROJECT_STATUS.md` conserva EC-006…009 como APPROVED y RE-013…017 como CLOSED aunque auditorías tratan los primeros como ciclo completado. Estados no se usan uniformemente.
- `05_ROADMAP.md` tiene fases hasta Analytics y decisiones sobre rutas/subpestañas anteriores a RE-017; no refleja con precisión la topología vigente.
- `09_MOCK_DATA_STATUS.md` y auditorías antiguas deben contrastarse con bindings presentes. Los fakes Today/Planning/Stats ya no aparecen como consumidores; FakeAccountRepository sí se usa.
- Se encontraron rutas internas y `Documentation/EngineeringCards` (mayúscula) con auditorías parciales adicionales; no equivalen a las rutas raíz pedidas.

## 4. Completed vs Pending

| Área | Estado | Evidencia / conclusión |
|---|---|---|
| Activities / Catalog | IMPLEMENTADO | Catálogo, alta/detalle y sheet en Planning. |
| LifeSystem | IMPLEMENTADO | Entidad/DAO/repo y asociación a ActivityDefinition; no necesita ruta top-level. |
| ActivityDefinition / ActivityNode | IMPLEMENTADO | Modelos, Room, CRUD y árbol. |
| ScheduleRule / DailyInstance | IMPLEMENTADO | Resolución/materialización y Planning/Today. |
| Planning consolidation | IMPLEMENTADO | Un solo workspace; no NavHost anidado, ActivityCatalog como sheet. |
| Today surface | IMPLEMENTADO | Today y UI refinada según RE-015. |
| Tasks / Reminders / Spontaneous Events | IMPLEMENTADO | Roles/protocolos de DailyInstance, acciones/contexto; resolución temporal espontánea ya existe en GetHierarchicalTimelineUseCase. |
| BacklogItem | PARCIAL | Entidad/tabla/DAO y FK `DailyInstance.backlogId`; falta flujo/panel para el usuario y operaciones de producto demostradas. |
| Deadline | PARCIAL | Persistencia existe; no se demuestra UI CRUD. No bloquea uso actual. |
| Note | PARCIAL/IMPLEMENTADO | Persistencia contextual y uso asociado a instancia; no se demuestra experiencia global completa. |
| ActivityExecution / Stats | IMPLEMENTADO | Historial y agregaciones conectadas a datos Room. Costo de entrada no medido. |
| Body | FUTURO | Modelos/prototipos, no módulo funcional; no desplaza una necesidad operativa. |
| Account / Me | PARCIAL / DEMO | UI con `FakeAccountRepository`; logout vacío. |
| Navigation | IMPLEMENTADO con riesgo no medido | NavHost principal + bottom-bar save/restore. No evidencia de recreación excesiva. |
| Offline sync/backend | FUTURO / NO PREPARADO | Sin transporte/cola/protocolo remoto. |

## 5. Next Real Work

**NEXT REAL WORK:** hacer Backlog utilizable dentro de Planning: mostrar elementos abiertos, permitir asignarlos a fecha y vincular/materializar una `DailyInstance`, manteniendo estados coherentes. Antes de implementar deben decidirse creación desde catálogo y reglas para reabrir/desmarcar/reprogramar.

**WHY:** el concepto está persistido pero no accesible funcionalmente; es una brecha visible y el estado/contexto más nuevo ya lo apunta. Mantiene `DailyInstance` como nodo operativo, sin rediseñar Planning.

**ALREADY EXISTS?** PARTIAL  
**IMPLEMENTATION NEEDED?** YES  
**SUGGESTED EC:** `EC-RE-018 — Backlog operativo e integración con Planning` (tentativa, no creada).  
**CONFIDENCE:** MEDIUM-HIGH (medium por decisiones abiertas de producto).

## 6. Navigation Performance Audit

No hay dispositivo conectado, build instrumentada, Perfetto/Macrobenchmark ni Android Studio profiler accesible en esta sesión. No se realizó interacción manual. Por eso los ocho tiempos solicitados quedan **no medidos**, sin timings inventados:

| Transición | Medición |
|---|---|
| Today → Planning | No instrumentada |
| Planning → Today | No instrumentada |
| Today → Stats | No instrumentada |
| Stats → Today | No instrumentada |
| Today → Account | No instrumentada |
| Account → Today | No instrumentada |
| Planning → Stats | No instrumentada |
| Stats → Planning | No instrumentada |

Para diagnóstico defendible: registrar tap, evento de navegación, composición destino, primera UI significativa y primer frame presentado; medir tap-to-content y frame duration por separado; warm/cold, mismo dispositivo/build, varias repeticiones, mediana/p95. Esta falta de telemetría significa que optimizar Navigation ahora sería especulativo.

**PERFORMANCE STATUS: NEEDS OPTIMIZATION** en el sentido de que requiere medición/diagnóstico; severidad real **no confirmada** (no CRITICAL). El código no revela recreación obvia del NavHost: `MainActivity` mantiene un NavController y usa `popUpTo(start)`, `saveState`, `launchSingleTop`, `restoreState`; `AppNavHost` tiene un solo NavHost top-level. Estado de ViewModels debe verificarse en la traza/back stack real.

## 7. Root Cause Analysis

| Clasificación | Archivo / símbolo | Problema / evidencia | Impacto y causa probable | Solución conceptual | Prioridad |
|---|---|---|---|---|---|
| VIEWMODEL / DATABASE | `StatsViewModel.loadData`, `GetHistoryAnalyticsUseCase.execute`, `HistoricalOccurrenceResolver.resolveRange` | Carga analítica al iniciar, lee ocurrencias históricas, nodos/defs/sistemas y agrega métricas por rango. No se vio `withContext(Default)` alrededor de toda la agregación. | Stats puede retrasar primer contenido al crecer historial; queries suspend/Flow no implican que agregación CPU esté fuera del dispatcher de UI. No hay evidencia de bloqueo medido. | Perfilar; después cachear/separar cálculo CPU o SQL si es cuello real. | P1 condicionado |
| VIEWMODEL / DATABASE | `TodayViewModel.loadData`, `resolveMetadataForEntries` | Combina nodos, timeline, expanded ids y minute ticker; abre Flow de MetadataSchema por nodeId y rehace el combine al actualizar timeline. | N+1/fan-out potencial y recomputación cada minuto; puede retrasar estado significativo conforme crece árbol. No prueba bloqueo Main. | Instrumentar primera emisión; batch/caché o diferir datos no críticos si medido. | P1 |
| VIEWMODEL / COMPOSE | `PlanningViewModel.uiState` | Combina numerosos StateFlows con definiciones/nodos y mapea timeline/catálogo/contexto; `isLoading` no se usa en UI. | Puede mostrarse vacío antes de primera emisión y calcular listas en entradas/cambios. | Representar inicialización/empty con claridad y perfilar transformaciones. | P1 |
| DATABASE / VIEWMODEL | `ActivityCatalogViewModel.loadData` | Por cada definición combina consultas de nodos y reglas; obtiene tarjetas con `first()` antes de emitir el estado. | Fan-out que escala con catálogo y puede demorar sheet. Queries Room son asíncronas, por lo que no equivale a main-thread block. | Query agregada/emisión progresiva si trace confirma. | P1 |
| LOADING-STATE | Today, Planning, ActivityCatalog screens | UiState tiene `isLoading`; no se halló rama correspondiente en estas pantallas. Stats sí renderiza spinner/empty y el estado contiene error. | Loading puede parecer pantalla vacía/empty y contenido aparece de golpe. | Diferenciar loading/error/empty; instant cached state como primera opción local. | P1 UX |
| DI / VIEWMODEL | `AccountRoute`, `AccountViewModel` | Usa `viewModel()` y construye FakeAccountRepository; combine de valores estáticos. | Account no representa datos reales ni Hilt/data latency; carga local debería ser inmediata. | Resolver cuando exista alcance real de Account. | P2/deuda funcional |
| NAVIGATION | `MainActivity`, `AppNavHost` | Bottom bar usa restauración de estado; un único NavHost y rutas top-level. | Sin evidencia estática de navegación que reconstruya todo; comportamiento de destino debe medirse. | Contar ViewModel init/composiciones en trace antes de cambiar. | P2, no probado |
| ANIMATION / COMPOSE | `StatsScreen` | `AnimatedContent` cambia visualización interna. | Podría costar al cambiar periodo/contenido, no atribuye delay al cambio de top-level. | Ajustar solo si frame trace lo demuestra. | P2 |
| MAIN-THREAD | Features auditadas | No se encontró `runBlocking`, `Thread.sleep` o `Dispatchers.Main` explícito. Use cases de cálculo no declaran dispatcher CPU. | Sin evidencia de bloqueo explícito; falta perfilar trabajo CPU. | Perfetto/JankStats/trace métodos. | P1 verificación |
| RECOMPUTATION | Today, Stats, ActivityCatalog | Filtrados/find/map repetidos y recomputación a partir de listas; Today refresca con ticker. | Riesgo con datos grandes; quizá insignificante con DB actual desconocida. | Perfil dataset pequeño/grande antes de optimizar. | P2 condicionado |

No se observó carga de imágenes pesadas en esas superficies. No se puede atribuir causa a Hilt, Compose global o animación sin perfiles.

## 8. Loading / Skeleton Audit

| Superficie | Estado actual | Evaluación conceptual |
|---|---|---|
| Today | `isLoading=true` inicial pero UI no lo consume; timeline vacía puede ser carga o empty; no se vio error global. | Shell/header local instantáneo; placeholder breve solo si lectura cold realmente tarda. Separar empty/error. No skeleton persistente sobre Room rápida. |
| Planning | Campo `isLoading` default false, sin manejo visible; sí hay empty “Sin eventos programados”. | Evitar empty antes de primera emisión; skeleton solo si perfil muestra espera, de otro modo empty semántico e instant cache. |
| Stats | Spinner, `EmptyStatsView`, errorMessage; success con snapshot/gráficas. | Estados diferenciados. Cálculo histórico podría justificar mantener snapshot anterior/placeholder si se prueba demora; skeleton no arregla cálculo. |
| Account / Me | Loading declarado, datos fake locales; error no modelado. | Sin skeleton: datos estáticos deberían salir inmediatamente. Error pasa a importar con fuente real. |
| Activities / Catalog | Loading inicial true, pantalla empty si lista vacía; sin error dedicado. | Riesgo de empty accidental pre emisión. Placeholder de tarjetas solo si el retraso de Room es perceptible. |

**SKELETON STATUS: PARTIAL.** No se justifica un sistema transversal aún; primero diferenciar correctamente loading/empty y medir. Para fuente local, instant cached state suele ser más apropiado que skeleton.

## 9. Offline-first Readiness

**Source of truth:** arquitectura actual se aproxima a `UI → ViewModel → UseCase → Repository → Room`; Room es el almacenamiento conectado y las pantallas principales leen Flows locales sin depender de red. Esto es una base local-first/offline-capable, no sync-ready.

| Capacidad | Presente | Detalle |
|---|---|---|
| Room fuente local | Sí | Database v10, DAOs, migraciones 5→10. |
| Repository de dominio | Sí | `ActivityRepository` desacopla UI/UseCase de Room, aunque su contrato es amplio. |
| Flows | Sí | Muchas lecturas reactivas; no todas las entidades exponen igual CRUD. |
| UUID estable | Parcial/Sí | UUID en varios use cases y claves String; no se verificó política uniforme para cada entidad/dispositivo. |
| createdAt/updatedAt | Mayormente no | `completedAt`, `scheduledDate`, `dueAt` son fechas de negocio, no timestamps universales de sincronización. |
| deletedAt/tombstones | No como sync | `isDeleted` solo en ActivityDefinition/ActivityNode; otros borrados son delete/cascade/set-null. |
| version/serverId/localId | No encontrado | Sin campo de versión, identidad remota o estado sucio. |
| Outbox/pending operations | No encontrado | No hay cola durable. |
| Conflictos/idempotencia | No como protocolo | Índice único de materialización ayuda localmente, no resuelve duplicados distribuidos. |

## 10. Backend / Sync Readiness

**YA PREPARADO:** dominio separado de entidades Room, repositorio abstraído, mappers, IDs String/UUID en altas, relaciones por FK, snapshots históricos y operación local completa.

**PREPARACIÓN PARCIAL:** `isDeleted` solo en algunas entidades; UUID no documentado como estrategia universal; política temporal distingue `completedAt` instantáneo de `scheduledDate` y ScheduleRule en horario local; repositorio admite cambio de fuente pero está amplio/mezcla operaciones. Snapshots preservan parte de historia.

**BLOQUEADOR para sync real:** no hay DTO/API/auth, mappers remotos, engine, outbox, retry durable, `updatedAt` universal, versionado, tombstones uniformes, conflicto ni ownership multi-device. Delete cascades pueden eliminar relaciones sin evento exportable.

Flujo conceptual no definitivo:

```text
UI → ViewModel → UseCase → Repository → Room (source of truth)
                                              ↕ transacción
                                            Outbox
                                              ↕ retry + idempotency key
                                          Sync Engine ↔ API
                                              ↓ version/conflict/tombstone
                                       aplicar remoto en Room → Flow → UI
```

Probables entidades sync: ActivityDefinition, ActivityNode, LifeSystem, ScheduleRule/Exception, DailyInstance, BacklogItem, Deadline, Note, MetadataSchema y ActivityExecution. Preferencias UI/ticker se mantienen locales. Ejecuciones tienden a ser append-only; ScheduleRule necesita semántica local-time/DST; DailyInstance y jerarquía/orden de nodos requieren reglas de conflicto; Backlog debe resolver OPEN/RESOLVED/reapertura; asociaciones de contexto y notas requieren idempotencia. LWW puede ser candidato solo para campos simples editables con reloj confiable y desempate; no asumirlo para programación, árbol, completado, borrado o ejecuciones.

Riesgos del modelo actual: IDs locales y FK obligan a preservar identidad/orden al importar; `scheduledDate` epoch day y horarios son intención local, no UTC; `completedAt` sí es instante; `isDeleted` parcial no retiene deletes offline; índice `(sourceRuleId, scheduledDate)` deduplica materialización local pero no reemplaza una clave de operación remota.

**SYNC STATUS: NOT READY. BACKEND STATUS: PARTIAL.** Fronteras/mappers locales ya ayudan, pero no hay protocolo ni campos mínimos de sync.

## 11. Technical Debt

1. Drift documental: rutas faltantes en raíz y auditoría roadmap de 2026-09-06 anterior a RE-017.
2. Estado de EC ambiguo (APPROVED/CLOSED); hueco EC-004, EC-013/014 duplicadas y dos fichas RE-003.
3. FakeAccountRepository y logout vacío contradicen afirmación de “todos los repositorios fake eliminados”.
4. Loading no representado en Today/Planning/Catalog y estado de error no uniforme.
5. Posible fan-out de consultas/recomputación en Today, Stats y Activity Catalog; riesgo no cuantificado.
6. Sync: timestamps/tombstones/version/outbox/conflictos ausentes o parciales.
7. Deadline y Body carecen de experiencia funcional completa, pero no se demuestra que sean bloqueantes.

Clasificación: Backlog = problema de producto; perf = incógnita/riesgo no medido; loading = problema UX parcial; docs/fake Account = deuda; skeleton refinado = mejora condicionada; Body/backend completo = futuro.

## 12. Future EC Candidates

| Prioridad | Candidato | Evidencia / condición |
|---|---|---|
| P0 | Ninguna confirmada | No se demostró crash/data-loss ni bloqueo severo. |
| P1 | Backlog operativo + Planning (RE-018 tentativa) | Datos persistidos, flujo de usuario ausente y referencia en contexto/RE-016. |
| P1 condicionado | Medición de transiciones y first meaningful UI | Latencia reportada; faltan métricas. Instrumentar antes de EC de optimización. |
| P1/P2 futuro | Preparación de sync | Solo al definir backend, ownership y conflictos. |
| P2 | Consistencia loading/error | Donde blank/empty se mezclen o medición confirme demora local visible. |
| P2 | Account real, logout y preferencias | Fake activo; iniciar cuando haya requisitos de cuenta/autenticación. |
| P2 | Deadline CRUD/uso UI | Persistencia parcial; validar necesidad. |
| FUTURO | Body, backend API y sync multi-device | Sin implementación funcional ni prioridad inmediata demostrada. |
| FUTURO | Pulido adicional de Today/Navigation | RE-015/017 cerradas; exigir hallazgo o meta visual concreta. |

## 13. Priority Matrix

| Nivel | Problema real | Deuda | Cosmético | Futuro |
|---|---|---|---|---|
| P0 | Ninguno demostrado | — | — | — |
| P1 | Backlog no utilizable | Docs drift; loading inconsistente; perfilado faltante | — | Semántica inicial de sync cuando backend se concrete |
| P2 | Account demo/Deadline sin UI, no bloqueantes | Perf de queries por verificar | Skeletons condicionados | API/backend, Body |

## 14. Recommended Order

1. Siguiente EC de producto: definir y luego implementar Backlog operativo dentro de Planning; acordar origen de alta y estados de reabrir/desmarcar/reprogramar.
2. Separadamente, medir las ocho transiciones y primer contenido en dispositivo. Solo entonces decidir una EC de performance sobre cuello confirmado; evitar “navigation cleanup” genérico.
3. Corregir estados loading/empty/error donde se observe blank real; no aplicar skeleton por defecto a Room local.
4. Al existir alcance backend, acordar identidad, timestamps, tombstones, outbox, ownership y conflictos antes de sync engine.

## 15. Evidence

- Roadmap/status: `app/src/main/java/com/alan/routineos/documentation/{04_PROJECT_STATUS.md,05_ROADMAP.md,07_CURRENT_CONTEXT.md,08_ARCHITECTURE_INVARIANTS.md}`.
- ECs/audits RE: `app/src/main/java/com/alan/routineos/documentation/EC/` y `.../AUDITS/`; en particular RE-013…017 y `AUDIT_ROADMAP_NEXT_STEP.md`, `AUDIT_GENERAL_PROJECT_CONSISTENCY.md`.
- Nav: `MainActivity.kt`, `core/navigation/AppNavHost.kt`, `AppRoutes.kt`.
- UI/VM: `feature/today/{TodayRoute,TodayViewModel,TodayScreen,TodayUiState}.kt`; equivalente `planning`, `stats`, `account`, `dashboard/ActivityCatalog*`.
- Analítica: `domain/usecase/GetHistoryAnalyticsUseCase.kt`, `HistoricalOccurrenceResolver.kt`, `GetHierarchicalTimelineUseCase.kt`.
- Datos: `domain/repository/ActivityRepository.kt`, `data/repository/OfflineActivityRepository.kt`, `data/di/RepositoryModule.kt`, `data/local/RoutineOSDatabase.kt`, `data/local/dao/*`, `data/local/entities/*Entity.kt`.
- Fake Account: `feature/account/data/FakeAccountRepository.kt`.
- Backlog: `BacklogItemEntity.kt`, `BacklogItemDao.kt`, `DailyInstanceEntity.kt`; el contrato `ActivityRepository` no muestra API CRUD de backlog.
- Room schema/migrations: `data/di/DatabaseModule.kt` y `app/schemas/`.

La inspección confirma que las rutas pedidas `documentation/EC` y `documentation/AUDITS` no existen en raíz. No se usaron resultados históricos de build/tests como validación actual.

## 16. Risks / Unknowns

- La sensación de demora no se reprodujo ni midió; prioridad/performance root cause abiertas.
- Volumen real de base de datos desconocido; costo de scans/listas depende de tamaño.
- Backend/API/auth y modelo multi-device no existen en el repo.
- Backlog conserva pregunta abierta sobre crear desde catálogo y comportamiento de revertir asignación/completado.
- No se validó manualmente el PASS histórico de RE-017; se contrastó estado documental con estructura/código.
- No se modificó ni corrigió la documentación preexistente durante esta auditoría.

## Resultado final obligatorio

```text
ROADMAP
NEXT REAL EC: EC-RE-018 — Backlog operativo e integración con Planning (tentativa, no creada)

NAVIGATION
PERFORMANCE STATUS: NEEDS OPTIMIZATION (medición pendiente; gravedad no confirmada)
MAIN CAUSE: No demostrada. Candidatos medibles: agregación histórica de Stats, consultas de metadata de Today, fan-out del catálogo y estados iniciales vacíos. Navigation no muestra recreación obvia en código.

SKELETON
STATUS: PARTIAL

OFFLINE-FIRST
STATUS: PARTIAL

SYNC
STATUS: NOT READY

BACKEND
STATUS: PARTIAL

RECOMMENDED NEXT STEP
Definir y posteriormente ejecutar EC-RE-018 para hacer utilizable Backlog en Planning, conservando DailyInstance como ocurrencia operativa.

REASON
Backlog existe en Room/modelo y se vincula a DailyInstance, pero no se encontró flujo/panel. EC-RE-013…017 y los pendientes de consolidación/catalog/Today/Systems constan cerrados o implementados. La preocupación de performance carece aún de medidas para priorizar optimización sobre la brecha funcional.

ALREADY EXISTS
partial

IMPLEMENTATION NEEDED
yes
```
