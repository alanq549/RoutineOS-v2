---
id: EC-RE-007
title: Contextual Organization (Systems)
phase: 4
priority: Medium
effort: Medium
owner: AI Agent
status: APPROVED
depends_on: EC-RE-006
branch: feature/ec-re-007-contextual-organization
audit: AUDIT_EC-RE-007
created: 2026-08-18
updated: 2026-08-18
---

# EC-RE-007: Contextual Organization (Systems)

## Objetivo
Implementar la capa de "Sistemas" (Systems) para categorizar las actividades, permitiendo una organización de alto nivel de las diferentes áreas de la vida sin forzar lógicas de dominio predefinidas.

## Contexto
RoutineOS ya es capaz de gestionar actividades jerárquicas y capturar metadatos. El siguiente paso evolutivo es agrupar estas actividades en "Sistemas" (ej: Salud, Carrera, Finanzas, Personal). Esto proporciona al usuario una vista macro de su vida y prepara el terreno para el análisis estadístico por áreas.

## Problema
Actualmente, las actividades están en una lista única (catálogo). A medida que el número de actividades crece, la navegación se vuelve ineficiente. Además, la pantalla de "Sistemas" existe solo como un prototipo estático con datos falsos.

## Alcance
- [ ] **Infraestructura de Datos**: Crear entidad `SystemEntity` y DAO correspondiente.
- [ ] **Relación de Actividades**: Vincular cada `ActivityDefinition` con un `SystemId`.
- [ ] **Capa de Dominio**: Implementar modelos y casos de uso para obtener sistemas con sus estadísticas calculadas (conteo de actividades, sesiones semanales).
- [ ] **Migración de Funcionalidad**: Migrar `SystemViewModel` a Hilt y conectar con el repositorio real.
- [ ] **UI de Sistemas**: Actualizar `SystemScreen` para mostrar tarjetas reales con datos dinámicos.

### Exclusiones
- No incluye el análisis histórico avanzado (EC-RE-008).
- No incluye la asignación de objetivos (Goals) por sistema.

## Archivos Afectados
- `data/local/entities/SystemEntity.kt` (NEW)
- `data/local/dao/SystemDao.kt` (NEW)
- `domain/model/LifeSystem.kt` (NEW)
- `feature/system/SystemViewModel.kt` (REFACTOR)
- `feature/system/SystemScreen.kt` (REFACTOR)

## Plan de Implementación
1. Crear el esquema de base de datos para Sistemas (V1 Baseline update).
2. Definir la lógica de negocio para el cálculo de estadísticas de sistemas.
3. Refactorizar el ViewModel para eliminar el repositorio falso y usar inyección de dependencias.
4. Rediseñar la pantalla de Sistemas para soportar los diferentes tamaños de tarjetas (Large, Medium, Small) según la prioridad o uso.

## Validaciones
- [ ] Crear un sistema y asignar actividades; verificar que aparecen agrupadas.
- [ ] Verificar que las estadísticas de la tarjeta (ej: "8 materias") coinciden con la realidad del árbol de actividades.

## Auditoría
- [ ] ¿Los nombres de los sistemas son totalmente libres para el usuario?
- [ ] ¿Se cumple con la regla de agnosticismo de dominio?

## Definition of Done (Obligatorio)
- [ ] Compila sin errores.
- [ ] CRUD de Sistemas funcional.
- [ ] Pantalla de Sistemas reactiva con datos reales.
- [ ] Tests de orquestación de estadísticas pasando.
