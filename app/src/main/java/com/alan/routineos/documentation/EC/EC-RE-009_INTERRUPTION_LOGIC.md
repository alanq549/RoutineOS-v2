---
id: EC-RE-009
title: Interruption & Intersection Logic
phase: 5
priority: High
effort: Medium
owner: AI Agent
status: CLOSED
depends_on: EC-RE-008
branch: feature/ec-re-009-interruption-logic
audit: PASS
created: 2026-08-19
updated: 2026-09-03
---

# EC-RE-009: Interruption & Intersection Logic

## Objetivo
Implementar un motor de relaciones temporales genérico que gestione intersecciones, anidamientos y sugerencias de ajuste, diferenciando entre la estructura jerárquica y las interrupciones imprevistas.

## Contexto
Con un timeline funcional (EC-RE-008), el siguiente reto es la gestión de la "densidad temporal". Los eventos espontáneos no deben verse como errores de base de datos, sino como interrupciones que el sistema debe entender matemáticamente.

## Invariantes de Diseño
1.  **Independencia de Dominio**: El motor solo conoce Intervalos, Movilidad y Jerarquía.
2.  **Movilidad Desacoplada**: El tipo de horario (Fijo/Rango) no dicta la movilidad (`IMMOBILE`/`FLEXIBLE`).
3.  **Impacto No Bloqueante**: Los conflictos generan advertencias e información, nunca impiden la persistencia.
4.  **Sugerencias Validadas**: Una sugerencia de movimiento debe ser probada contra el timeline completo antes de ser presentada.

## Alcance
- [x] **Modelo de Movilidad**: Incorporar la propiedad `mobility` en `ScheduleRule` y `DailyInstance`.
- [x] **Motor de Intersecciones**: 
    - Identificar `OVERLAP` vs `NESTED`.
    - Distinguir entre anidamiento estructural (Hijo dentro de Padre) e interrupción externa.
- [x] **Sistema de Impactos**: Definir `INFO`, `WARNING`, `INTERRUPTION_LABEL`.
- [x] **Generador de Sugerencias**: Algoritmo para encontrar huecos libres y validarlos preventivamente (`SuggestionEngine`).

## Decisión UX: Intercepción Cronológica Fluida
La vista Today emplea el patrón **Fluid & Connected** para una intercepción. Se aleja de la semántica de "Pausa/Reanudación" para abrazar la **coexistencia**.

Una intercepción se representa como un bloque jerárquico unificado con las siguientes características:

1. **Orden Cronológico Mixto**: Los sub-pasos programados y los eventos espontáneos se mezclan en una única lista ordenada por su hora de inicio real.
2. **Evento Simultáneo**: El elemento interceptor se muestra indentado, con un fondo púrpura sutil, icono de rayo y la etiqueta "EVENTO SIMULTÁNEO", integrándose perfectamente en el flujo del padre.
3. **Cálculo de Duración Expandida**: La tarjeta padre expande su rango visual (`HH:mm - HH:mm`) para cubrir desde el primer sub-paso hasta el último, asegurando que las colisiones externas se detecten correctamente.
4. **Dualidad de Diseño**: Los eventos espontáneos usan un diseño de tarjeta completa (`FULL`) cuando son independientes, y una fila minimalista (`MINIMAL`) cuando están dentro de una intercepción.

## Archivos Clave
- `domain/model/TemporalMobility.kt`
- `domain/model/TemporalImpact.kt`
- `domain/usecase/ConflictDetectorUseCase.kt`
- `domain/usecase/SuggestionEngine.kt`
- `feature/today/components/InterceptionContainer.kt`

## Post-Auditoría: Refinamientos Semánticos y Operativos
1.  **Política de RESET**: La acción de "Desmarcar" (`RESET`) es una operación únicamente operativa. Preserva físicamente los registros en `ActivityExecution` para garantizar la integridad histórica y permitir análisis de consistencia.
2.  **Heurística Temporal**: El valor de 30 minutos utilizado en resoluciones sin duración explícita se define como una **heurística de cálculo de intervalos** para el motor de conflictos, no como una duración real de la actividad.
3.  **Acciones en Contenedores**: El espacio de trabajo Today permite operaciones en bloque (`SKIP`, `MOVE`, `RESET`) sobre nodos contenedores, las cuales se propagan recursivamente a sus descendientes ejecutables para mantener la integridad del flujo operativo.

## Plan de Verificación
- [x] **Test de Oro de Interrupciones**: Validar que una "Salida Express" se marque como interrupción sin desplazar la actividad principal.
- [x] **Test de Validación de Sugerencias**: Asegurar que las sugerencias de movimiento no generen nuevos conflictos.
- [x] **Integridad Jerárquica**: Verificación de que los sub-pasos no se desagrupan si el padre no está programado.
- [x] **Eliminación de Redundancias**: Colapso de títulos duplicados en jerarquías de nivel único.
