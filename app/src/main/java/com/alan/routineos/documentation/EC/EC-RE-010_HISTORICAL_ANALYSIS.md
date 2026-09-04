---
id: EC-RE-010
title: Historical Analysis & Trends
phase: 5
priority: High
effort: Large
owner: AI Agent
status: READY
depends_on: EC-RE-009
branch: feature/ec-re-010-historical-analysis
audit: Pending
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

## Definiciones Semánticas
Ver contrato funcional detallado en: `ec_re_010_functional_contract.artifact.md`.

## Plan de Verificación
- **Test de Agregación**: Validar que un sistema con 10 hojas en 2 niveles calcule correctamente el `%` global sin doble conteo.
- **Test de Solapamiento**: Confirmar que el Focus Index de dos tareas de 60min solapadas 30min resulte en 50%/50% de carga relativa.
- **Test de Metadatos**: Extraer una serie temporal de un campo "Peso" y validar su progresión.
