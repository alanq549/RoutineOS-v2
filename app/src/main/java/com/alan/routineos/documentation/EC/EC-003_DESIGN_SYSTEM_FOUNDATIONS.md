---
id: EC-003
title: Design System Foundations & Refinement
phase: 3
priority: High
effort: Medium
owner: AI Agent
status: READY
depends_on: EC-000
branch: feature/ec-003-design-system
audit: Pending
created: 2026-07-22
updated: 2026-07-22
---

# EC-003: Design System Foundations & Refinement

## Objetivo
Solidificar las bases visuales del proyecto integrando recursos de diseño faltantes (fuentes), refinando los componentes atómicos y auditando el uso del espacio vertical para maximizar la densidad de información.

## Contexto
Aunque existe una implementación inicial del Design System (Phase 1), la auditoría técnica de Stitch V3 reveló la falta de fuentes personalizadas (Inter y JetBrains Mono) y la necesidad de un refinamiento en los componentes base para alinearse con la "Technical Premium Narrative".

## Problema
- Se están utilizando fuentes nativas como fallback en lugar de las oficiales.
- El catálogo de componentes está incompleto.
- Existen inconsistencias en el manejo de WindowInsets y paddings que restan espacio vertical útil.

## Alcance
- [ ] Integración de fuentes oficiales (**Inter** y **JetBrains Mono**) en `res/font/`.
- [ ] Actualización de `RoutineTypography` para usar las nuevas familias de fuentes.
- [ ] Auditoría y refinamiento de componentes atómicos: `RoutineButton`, `RoutineCard`, `RoutineChip`.
- [ ] Implementación de componentes de navegación refinados: `RoutineTopBar`, `RoutineBottomBar` (Glass effect).
- [ ] Expansión del `RoutineDesignSystemCatalog` para incluir todos los estados de componentes.
- [ ] Refactorización de `RoutineScaffold` para un manejo de insets centralizado (Vertical Space Audit).

## Archivos Afectados
- `app/src/main/res/font/` (Nuevos recursos)
- `app/src/main/java/com/alan/routineos/core/designsystem/typography/RoutineTypography.kt`
- `app/src/main/java/com/alan/routineos/core/designsystem/component/` (Todos los componentes)
- `app/src/main/java/com/alan/routineos/core/designsystem/catalog/RoutineDesignSystemPreview.kt`

## Plan de Implementación
1. Descargar e integrar los archivos `.ttf` para Inter y JetBrains Mono.
2. Configurar la tipografía en Compose para mapear los estilos de Stitch.
3. Refinar los radios de curvatura y bordes en `RoutineShapes` y `RoutineColors`.
4. Implementar el efecto "Glass" (tonal layering + alpha) en barras de navegación.
5. Realizar el "Layout Audit" para eliminar paddings redundantes.
6. Actualizar el catálogo con una sección de "Playground" para cada componente.

## Validaciones
- [ ] Verificación visual de fuentes en previews de Compose.
- [ ] Verificación de integridad de Insets en dispositivos con diferentes configuraciones de sistema.
- [ ] Compilación exitosa sin recursos duplicados.

## Resultado Esperado
Un Design System de clase mundial, autocontenido y fiel a la narrativa técnica, listo para ser consumido por todas las pantallas de feature.

## Auditoría
- [ ] ¿Se utilizan fuentes oficiales?
- [ ] ¿El catálogo es exhaustivo?
- [ ] ¿Se maximiza el espacio vertical?

## Lecciones Aprendidas
(A completar tras la implementación).

## Estado
**ESTADO ACTUAL:** READY
