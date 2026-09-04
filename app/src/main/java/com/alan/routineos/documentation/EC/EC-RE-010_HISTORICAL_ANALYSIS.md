---
id: EC-RE-010
title: Historical Analysis & Trends
phase: 5
priority: High
effort: Large
owner: AI Agent
status: CLOSED
depends_on: EC-RE-009
branch: feature/ec-re-010-historical-analysis
audit: PASS
created: 2026-09-03
updated: 2026-09-03
---

# EC-RE-010: Historical Analysis & Trends

## Objetivo
Implementar el motor analítico de RoutineOS que transforma los registros inmutables de ejecución en métricas de cumplimiento, varianza temporal y tendencias de datos.

## Contexto
Tras consolidar el motor de interrupciones (EC-RE-009), el sistema cuenta con una distinción clara entre lo que el usuario planea y lo que realmente ocurre. Esta EC construye la capa de inteligencia que permite al usuario entender su comportamiento pasado.

## Invariantes de Diseño
1.  **Agregación Basada en Hojas**: Solo los nodos ejecutables computan para tasas de éxito.
2.  **Integridad Histórica**: Los registros de `ActivityExecution` son la única fuente de verdad para la "realidad".
3.  **No Inferencia**: Ante la ausencia de datos de ejecución, se reporta `N/A` en lugar de asumir puntualidad ideal.
4.  **Carga Relativa**: El Focus Index se normaliza contra la suma de duraciones para manejar solapamientos.

## Alcance
- [ ] **HistoryAnalyticsUseCase**: Motor central de agregación por día/semana/mes.
- [ ] **Métricas de Varianza**: Cálculo de `Start Deviation` y `Duration Deviation`.
- [ ] **Extractor de Tendencias**: Parser de metadatos JSON para campos numéricos históricos.
- [ ] **Repositorio de Solo Lectura**: Implementación de consultas complejas optimizadas para análisis.

## Definiciones Semánticas y Métricas
Ver contrato funcional detallado en: `ec_re_010_functional_contract.artifact.md`.

### Resumen de Fórmulas Core:
1. **Occurrence Resolution**: Reconstrucción del pasado mediante `Rules + Exceptions + Overrides`.
2. **Completion Rate**: `completedLeaves / (totalPlannedLeaves - omittedLeaves)`.
3. **Consistency Score**:
    - **Puntualidad**: `actualStartTime - effectiveStartTime`. (Actualmente `N/A`).
    - **Precisión**: `actualDuration - plannedDuration`. (Actualmente `N/A`).
4. **Focus Index**: Distribución Relativa del Tiempo Ejecutado (Normalizada).
5. **ExecutionConsistency**: Días con cumplimiento global >= 70%.

## Próximos Pasos Técnicos
1. Implementar `HistoricalOccurrenceResolver`: Capaz de proyectar el estado de la línea de tiempo para cualquier rango de fechas pasadas.
2. Motor de Agregación: Filtrado de hojas ejecutables y cálculo de KPIs.

## Plan de Verificación
- **Test de Agregación**: Validar que un sistema con 10 hojas en 2 niveles calcule correctamente el `%` global sin doble conteo.
- **Test de Solapamiento**: Confirmar que el Focus Index de dos tareas de 60min solapadas 30min resulte en 50%/50% de carga relativa.
- **Test de Metadatos**: Extraer una serie temporal de un campo "Peso" y validar su progresión.
