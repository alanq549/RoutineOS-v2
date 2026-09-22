---
id: EC-RE-013
title: Refinamiento de Catálogo y Constructor de Actividades (Stitch V2)
phase: 6
priority: High
effort: Medium
owner: AI Agent
status: CHANGES_REQUESTED
depends_on: [EC-RE-012]
branch: feature/planning
audit: CHANGES_REQUESTED
created: 2026-09-06
updated: 2026-09-06
---

# EC-RE-013: Refinamiento de Catálogo y Constructor de Actividades (Stitch V2)

## Objetivo
Refinar el Catálogo de Actividades y el Constructor de Actividades (Builder Protocol) implementando la estética "Technical Premium" de Stitch V2 y conectando dinámicamente la creación de actividades con los sistemas de vida reales (`LifeSystem`) del repositorio de datos.

## Contexto
RoutineOS estructura las rutinas y actividades asignadas a sistemas primarios de vida (`LifeSystem`). Anteriormente, el flujo de creación de actividades contenía un selector visual con datos en duro (mocks) que no persistían el identificador real del sistema al guardar nuevas `ActivityDefinition`. Asimismo, el catálogo requería refinamientos estéticos para compactar la previsualización de días programados.

## Problema
1. **Falta de sincronización real de sistemas**: El creador de actividades (`ActivityCreationScreen`) mostraba tarjetas estáticas ("Carrera", "Salud") en lugar de consumir los sistemas reales almacenados en `LifeSystemRepository`.
2. **Asignación nula de sistema**: Al guardar una nueva actividad, el campo `systemId` no se vinculaba al sistema seleccionado en la interfaz.
3. **Carga visual en catálogo**: La tarjeta de actividad en el catálogo renderizaba todos los días programados sin límite, saturando la tarjeta en actividades con múltiples reglas.

## Alcance (MUST)
- [x] **Conexión Dinámica de Sistemas**: Conectar `ActivityCreationViewModel` al flujo `getAllSystems()` del `LifeSystemRepository`.
- [x] **Rejilla Técnica Interactiva**: Renderizar una rejilla responsiva de `SystemTile` en `ActivityCreationScreen` con soporte para selección reactiva, iconos dinámicos (`getTechnicalIcon`) y estados visuales seleccionados/no seleccionados.
- [x] **Persistencia de Sistema Primario**: Pasar el `selectedSystemId` a `saveActivity` para asociar la actividad creada con el `LifeSystem` real.
- [x] **Compactación de Vista Previa de Días**: Limitar la visualización en `ActivityCard` a los primeros 2 días programados y mostrar la etiqueta `+ N DÍAS` si existen más.
- [x] **Resiliencia y Mensajes de Respaldo**: Mostrar mensaje descriptivo si aún no se han configurado sistemas en el entorno.

## Archivos Afectados
- [ActivityCreationViewModel.kt](file:///C:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/feature/dashboard/ActivityCreationViewModel.kt) (MODIFY)
- [ActivityCreationScreen.kt](file:///C:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/feature/dashboard/ActivityCreationScreen.kt) (MODIFY)
- [DashboardViewModel.kt](file:///C:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/feature/dashboard/DashboardViewModel.kt) (MODIFY)
- [ActivityCard.kt](file:///C:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/feature/dashboard/components/ActivityCard.kt) (MODIFY)

## Plan de Implementación
1. **Flujo de Datos**: Conectar `ActivityCreationViewModel` a `getAllSystems()` expuesto en `UiState`.
2. **Lógica de Selección**: Implementar `onSystemSelected(systemId: String?)` en ViewModel para alternar selección y persistir en `saveActivity`.
3. **Rediseño UI de Creador**: Reemplazar la fila mock por rejilla técnica basada en `chunked(2)` renderizando `SystemTile`.
4. **Optimización de Tarjeta**: Calcular `moreDaysCount` en el mapeo de `DashboardViewModel` y consumirlo en `ActivityCard`.

## Validaciones
- [x] Las actividades creadas quedan asociadas correctamente a su `systemId` en Room DB.
- [x] La pantalla de creación refleja dinámicamente cualquier sistema añadido previamente por el usuario.
- [x] La tarjeta del catálogo muestra únicamente 2 días y la efigie `+ N DÍAS` si sobrepasa el límite.
- [x] Compilación limpia (`assembleDebug`) y suite de tests ejecutada exitosamente.

## Definition of Done
- [x] Compila sin errores ni advertencias de deprecación.
- [x] Abstracción agnóstica de dominio respetada (no se hardcodean categorías ni dominios específicos).
- [x] Sin mutación de lógica de base de datos ni modificaciones destructivas de esquemas.

## Auditoría
Ver informe de auditoría pendiente en: `AUDITS/AUDIT_EC-RE-013_SYSTEMS_AND_ACTIVITIES_REFINEMENT.md`
