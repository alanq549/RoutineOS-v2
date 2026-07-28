---
id: EC-006
title: Routine Dashboard Screen
phase: 4
priority: High
effort: Medium
owner: AI Agent
status: CHANGES_REQUESTED
depends_on: EC-005
branch: feature/ec-006-routine-dashboard
audit: AUDIT_EC-006
created: 2026-07-23
updated: 2026-07-26
---

# EC-006: Routine Dashboard Screen

## Objetivo
Implementar la pantalla del Dashboard reactivo principal de la aplicación, conectándolo con el repositorio de datos y utilizando los componentes y guías visuales del Design System.

## Contexto
El Dashboard es el núcleo de interacción de RoutineOS v2 (Fase 4 del roadmap). Consumirá la información expuesta por el motor temporal a través de `ActivityRepository` (anteriormente `RoutineRepository`) y la presentará al usuario de manera intuitiva y fluida.

## Problema
Actualmente no existe una pantalla de inicio real conectada a la persistencia de datos. Las vistas previas utilizan datos estáticos aislados. Se requiere una pantalla interactiva y reactiva conectada al flujo de datos de la base de datos local.

## Alcance
- [x] Creación de `DashboardScreen` usando Jetpack Compose.
- [x] Implementación de `DashboardViewModel` consumiendo `ActivityRepository`.
- [x] Renderizado de tarjetas de actividades con elevación/paddings de 16dp.
- [x] Aplicar la tipografía oficial **Inter** en todos los textos del Dashboard.
- [x] Conectar la navegación en `MainActivity`.
- [x] Eliminar terminología de dominio prohibida en la capa feature/ (RoutineCardModel, RoutineLibraryViewModel, RoutineRoutes, etc.), renombrando a equivalentes agnósticos.
- [x] Reemplazar FakeRoutineRepository por ActivityRepository real vía inyección de dependencias Hilt.
- [x] Exclusión: Esta EC no incluye pantallas de creación ni edición detallada de actividades (CRUD de creación se abordará en futuras ECs).

## Archivos Afectados
- `app/src/main/java/com/alan/routineos/feature/dashboard/DashboardScreen.kt`
- `app/src/main/java/com/alan/routineos/feature/dashboard/DashboardViewModel.kt`
- `app/src/main/java/com/alan/routineos/MainActivity.kt`

## Plan de Implementación
1. Crear el paquete de feature `feature/dashboard`.
2. Implementar `RoutineDashboardViewModel` expuesto por Hilt, que consuma `ActivityRepository` para recibir el flujo reactivo de definiciones de actividades.
3. Crear `RoutineDashboardScreen` estructurado con `RoutineScaffold` y mostrar la lista de actividades en un `LazyColumn`.
4. Diseñar tarjetas (`RoutineCard`) con espaciado interno y externo de 16dp y tipografía `Inter`.
5. Integrar la pantalla en la navegación de `MainActivity`.

## Validaciones
- [x] Compilación exitosa del módulo `app`.
- [x] Previews de Compose funcionales para diferentes estados del Dashboard (Vacío, Cargando, Con Datos).
- [ ] Pruebas unitarias para `DashboardViewModel` simulando `ActivityRepository`.

## Resultado Esperado
Un Dashboard interactivo y visualmente impecable que carga automáticamente las definiciones de actividades almacenadas localmente, respetando las guías de diseño y tipografía del proyecto.

## Auditoría
Consultar guía en [AUDITS/README.md](../AUDITS/README.md).
- [ ] ¿Cumple con la arquitectura MVVM/Clean?
- [ ] ¿Se inyecta `ActivityRepository` en lugar de la implementación concreta?
- [ ] ¿Las funciones de la UI son < 30 líneas?
- [ ] ¿Los archivos son < 300 líneas?

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
- [ ] Abre la app y navega a "Planificar" -> "ACTIVIDADES". Confirma que se visualiza el Dashboard (Biblioteca de Actividades).
- [ ] Verifica que los elementos visuales (tarjetas, textos) respetan el diseño (16dp de margen, fuente Inter).

**Nota:** La pantalla de inicio ("Today") y otras secciones siguen utilizando datos simulados (Fakes) por estar fuera del alcance de esta Épica. Esta actualización solo habilita la conexión real en la pestaña de Actividades.

## Estado
El estado vigente de esta EC es el declarado en el campo `status` del frontmatter (arriba de este documento). No dupliques el valor aquí.
