---
id: EC-007
title: Activity Creation Flow
phase: 4
priority: High
effort: Medium
owner: AI Agent
status: APPROVED
depends_on: EC-006
branch: feature/ec-007-activity-creation-flow
audit: AUDIT_EC-007
created: 2026-07-27
updated: 2026-07-28
---

# EC-007: Activity Creation Flow

## Objetivo
Implementar el flujo de creación de una nueva `ActivityDefinition` desde la UI, conectado al `ActivityRepository` real, para que el Dashboard deje de depender exclusivamente de datos pre-existentes en la base de datos y el usuario pueda generar contenido real.

## Contexto
EC-006 dejó el Dashboard funcional pero de solo lectura (lista vacía tras la migración destructiva de EC-005, sin mecanismo de creación). Esta EC cierra ese vacío y permite la persistencia de nuevas definiciones de actividad.

## Problema
Actualmente no existe una forma para que el usuario cree sus propias actividades. El sistema depende de que existan datos en la base de datos, los cuales fueron eliminados durante el refactor domain-agnostic. Se requiere un formulario de entrada de datos conectado a la capa de persistencia.

## Alcance
- [x] Crear pantalla de formulario simple (título + descripción como mínimo, según los campos actuales de `ActivityDefinition` — NO inventar campos de dominio específico como categoría/tipo, ya que `ActivityDefinition` no los tiene definidos aún).
- [x] Crear `ActivityCreationViewModel` con inyección de `ActivityRepository` vía Hilt.
- [x] Conectar el guardado real a la base de datos (insert vía DAO).
- [x] Agregar punto de entrada visual desde `DashboardScreen` (ej. FAB o botón "+") que navegue a la pantalla de creación.
- [x] Al guardar, la navegación debe volver al Dashboard y la nueva actividad debe aparecer en la lista reactiva sin necesidad de refrescar manualmente (confirma que el Flow de Room se actualiza solo).
- [x] Exclusión: No incluye edición ni borrado de actividades existentes (CRUD completo se abordará en una EC futura).
- [x] Exclusión: No incluye `ActivityNode` (sub-tareas/pasos) en el formulario de creación — solo la `ActivityDefinition` raíz.

## Archivos Afectados
- `app/src/main/java/com/alan/routineos/feature/dashboard/DashboardScreen.kt`
- `app/src/main/java/com/alan/routineos/feature/dashboard/ActivityCreationScreen.kt`
- `app/src/main/java/com/alan/routineos/feature/dashboard/ActivityCreationViewModel.kt`
- `app/src/main/java/com/alan/routineos/core/navigation/AppRoutes.kt`
- `app/src/main/java/com/alan/routineos/core/navigation/AppNavHost.kt`
- `app/src/main/java/com/alan/routineos/feature/planning/PlanningWorkspace.kt` (Propagación del callback de navegación)
- `app/src/main/java/com/alan/routineos/feature/dashboard/DashboardRoute.kt` (Propagación del callback de navegación)
> [!NOTE]
> `PlanningWorkspace.kt` y `DashboardRoute.kt` son necesarios para propagar el callback de navegación a través del NavHost anidado de Planning — no estaban previstos en el spec original.

## Plan de Implementación
1. Definir la nueva ruta en `AppRoutes.kt`.
2. Implementar `ActivityCreationViewModel` manejando el estado del formulario y la acción de guardado.
3. Crear `ActivityCreationScreen` con campos de texto para Título y Descripción.
4. Actualizar `AppNavHost.kt` para incluir la nueva pantalla.
5. Modificar el FAB en `DashboardScreen.kt` para navegar a la creación.

## Validaciones
- [x] La actividad creada aparece inmediatamente en el Dashboard tras volver atrás. (Verificado: `LaunchedEffect` en la ruta y `Flow` reactivo en Room).
- [x] Verificación explícita anti-remanente: confirmar antes de cerrar que no queda ningún dato hardcodeado de ejemplo visible en runtime (solo permitido en `@Preview`). (Verificado: Grep exhaustivo negativo).

## Resultado Esperado
Un flujo funcional donde el usuario puede ingresar un título y descripción, presionar guardar, y ver su nueva actividad reflejada instantáneamente en el Dashboard principal.

## Auditoría
Consultar guía en [AUDITS/README.md](../AUDITS/README.md).
- [x] ¿Cumple con la arquitectura MVVM/Clean?
- [x] ¿Las funciones son < 30 líneas?
- [x] ¿Los archivos son < 300 líneas?

## Lecciones Aprendidas
- **ActivityCreationViewModelTest:** Se declara que no se creó un archivo de test específico para el ViewModel en esta EC, priorizando la validación mediante tests de integración de base de datos y repositorios existentes.
- **Manejo de Insets:** Se identificó que `RoutineScaffold` no aplica automáticamente padding para la status bar en el slot `topBar`. Se corrigió envolviendo la `RoutineTopBar` en un `Box` con `statusBarsPadding()`.
- **Persistencia Agnóstica:** El uso de `UUID.randomUUID()` permite delegar la responsabilidad del ID a la lógica de negocio, manteniendo las entidades de Room simples y conformes al modelo de dominio.
- **Navegación Reactiva:** La implementación de `LaunchedEffect(uiState.saveSuccess)` en la capa de navegación (`Route`) es un patrón limpio para manejar eventos de "un solo disparo" (como navegar atrás tras un guardado exitoso) sin contaminar el ViewModel con lógica de navegación.

## Definition of Done (Obligatorio)
Antes de marcar como APPROVED, verificar:
- [x] Compila sin warnings nuevos
- [x] Tests existentes pasan (unitarios + los que aplique)
- [x] Checklist de [ARCHITECTURE_INVARIANTS.md](../08_ARCHITECTURE_INVARIANTS.md) revisado y sin violaciones
- [x] Si se usó Fake*Repository, está registrado en [MOCK_DATA_STATUS.md](../09_MOCK_DATA_STATUS.md)
- [x] Lecciones aprendidas documentadas arriba

## Checklist de Validación de Usuario
- [x] Presionar el botón "+" en el Dashboard abre un formulario.
- [x] Al completar título y descripción y presionar "Guardar", se regresa al Dashboard.
- [x] La nueva actividad es visible en la lista del Dashboard.

## Estado
El estado vigente de esta EC es el declarado en el campo `status` del frontmatter (arriba de este documento). No dupliques el valor aquí.
