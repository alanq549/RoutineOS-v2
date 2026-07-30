---
id: EC-008
title: Activity Detail & Nodes Management
phase: 4
priority: High
effort: Medium
owner: AI Agent
status: APPROVED
depends_on: EC-007
branch: feature/ec-008-activity-detail-nodes
audit: AUDIT_EC-008
created: 2026-07-28
updated: 2026-07-29
---

# EC-008: Activity Detail & Nodes Management

## Objetivo
Permitir al usuario ver el detalle de una ActivityDefinition existente y agregarle ActivityNode (sub-pasos/tareas), conectado al ActivityRepository real.

## Contexto
EC-007 excluyó explícitamente ActivityNode del formulario de creación. El Dashboard hoy solo muestra tarjetas sin forma de entrar al detalle ni gestionar sus sub-pasos. Esta EC cierra esa brecha de funcionalidad.

## Problema
Los usuarios pueden crear definiciones de actividad, pero no pueden ver su detalle ni desglosarlas en pasos accionables (nodos), lo cual es fundamental para el motor de planificación de RoutineOS.

## Alcance
- [x] Al tocar una tarjeta de actividad en el Dashboard, navegar a una pantalla de detalle (`ActivityDetailScreen`) mostrando título y descripción de la `ActivityDefinition`.
- [x] Mostrar la lista reactiva de `ActivityNode` asociados a esa `ActivityDefinition` (vacía si no tiene ninguno aún).
- [x] Formulario simple para agregar un nuevo `ActivityNode` (título como mínimo), persistido vía `ActivityRepository/DAO`, respetando la relación de foreign key (`activityDefinitionId`).
- [x] Reutilizar `ActivityFormFields` (extraído en EC-007) si aplica al formulario de creación de nodo, en vez de duplicar código.
- [x] Exclusión: No incluye edición ni borrado de la `ActivityDefinition` padre (sigue fuera de alcance, como en EC-007).
- [x] Exclusión: No incluye edición ni borrado de `ActivityNode` existentes — solo creación y listado.
- [x] Exclusión: No incluye `nodeTimeByDay` ni programación de horarios para los nodos — solo título/descripción básica.

## Archivos Afectados
- `app/src/main/java/com/alan/routineos/core/navigation/AppRoutes.kt`
- `app/src/main/java/com/alan/routineos/core/navigation/AppNavHost.kt`
- `app/src/main/java/com/alan/routineos/feature/dashboard/DashboardScreen.kt`
- `app/src/main/java/com/alan/routineos/feature/dashboard/ActivityDetailScreen.kt`
- `app/src/main/java/com/alan/routineos/feature/dashboard/ActivityDetailViewModel.kt`
- `app/src/main/java/com/alan/routineos/feature/dashboard/components/ActivityCard.kt`

## Plan de Implementación
1. Definir la ruta de detalle en `AppRoutes.kt` (debe aceptar un `activityId` como argumento).
2. Crear `ActivityDetailViewModel` para cargar la definición y sus nodos asociados reactivamente.
3. Implementar `ActivityDetailScreen` con el encabezado de la actividad y la lista de nodos.
4. Integrar la navegación desde `DashboardScreen` (al hacer click en la tarjeta).
5. Añadir el formulario de creación de nodos dentro de la pantalla de detalle.
6. Verificar que la lista de nodos se actualice automáticamente tras cada inserción.

## Validaciones
- [x] Navegación correcta pasando el ID de la actividad. (Verificado: orquestación de `AppNavHost` y `savedStateHandle`).
- [x] Los nodos creados aparecen instantáneamente en la lista. (Verificado: `combine` de flows reactivos).
- [x] Verificación anti-remanente: confirmar antes de cerrar que ningún dato de ejemplo hardcodeado es visible en runtime (solo permitido en `@Preview`). (Verificado: `grep` exhaustivo negativo).

## Resultado Esperado
Una experiencia de usuario donde se puede explorar cada actividad creada, ver sus pasos internos y añadir nuevos pasos de forma fluida.

## Auditoría
Consultar guía en [AUDITS/README.md](../AUDITS/README.md).
- [x] ¿Cumple con la arquitectura MVVM/Clean?
- [x] ¿Las funciones son < 30 líneas? (Verificado: todas las funciones en `ActivityDetailScreen.kt` cumplen el límite).
- [x] ¿Los archivos son < 300 líneas?

## Lecciones Aprendidas
1.  **Conteo Objetivo de Líneas**: Se identificó la importancia de utilizar herramientas del sistema (`Measure-Object` en PowerShell o `wc -l`) para verificar los límites de calidad de código. La estimación manual llevó a una discrepancia entre rondas (34 líneas reales en el cuerpo vs 26 reportadas inicialmente).
2.  **Patrón LazyListScope**: La extracción de lógica de listas hacia funciones de extensión de `LazyListScope` (ej. `NodesContent`) demostró ser una técnica eficaz para mantener los composables de orquestación (como `NodesList`) por debajo del límite de 30 líneas sin sacrificar legibilidad.
3.  **Integridad por FK**: Esta EC confirmó que el filtrado reactivo en Room basado en Foreign Keys es el mecanismo preferido para garantizar el aislamiento de datos en relaciones padre-hijo, evitando bugs de "fuga de datos" entre actividades.

## Definition of Done (Obligatorio)
Antes de marcar como APPROVED, verificar:
- [x] Compila sin warnings nuevos (Verificado: `./gradlew assembleDebug` exitoso).
- [x] Tests existentes pasan (Verificado: 6 unit tests pasados en la última ejecución).
- [x] Checklist de [ARCHITECTURE_INVARIANTS.md](../08_ARCHITECTURE_INVARIANTS.md) revisado y sin violaciones (Verificado: No hay términos de dominio hardcodeados).
- [x] Si se usó Fake*Repository, está registrado en [MOCK_DATA_STATUS.md](../09_MOCK_DATA_STATUS.md) (Verificado: No se introdujeron nuevos fakes en esta EC).
- [x] Lecciones aprendidas documentadas arriba.

## Checklist de Validación de Usuario
- [x] Al hacer clic en una actividad del Dashboard, se abre su pantalla de detalle.
- [x] Se visualiza el título y descripción correctos de la actividad seleccionada.
- [x] Es posible escribir el nombre de un nuevo paso y guardarlo.
- [x] Los nuevos pasos aparecen en una lista debajo del detalle de la actividad.

## Estado
El estado vigente de esta EC es el declarado en el campo `status` del frontmatter (arriba de este documento). No dupliques el valor aquí.
