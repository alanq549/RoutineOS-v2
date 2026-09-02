---
id: EC-RE-009
title: Interruption & Intersection Logic
phase: 5
priority: High
effort: Medium
owner: AI Agent
status: IN_PROGRESS
depends_on: EC-RE-008
branch: feature/ec-re-009-interruption-logic
audit: Pending
created: 2026-08-19
updated: 2026-09-02
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
- [ ] **Modelo de Movilidad**: Incorporar la propiedad `mobility` en `ScheduleRule` y `DailyInstance`.
- [ ] **Motor de Intersecciones**: 
    - Identificar `OVERLAP` vs `NESTED`.
    - Distinguir entre anidamiento estructural (Hijo dentro de Padre) e interrupción externa.
- [ ] **Sistema de Impactos**: Definir `INFO`, `WARNING`, `INTERRUPTION_LABEL`.
- [ ] **Generador de Sugerencias**: Algoritmo para encontrar huecos libres y validarlos preventivamente.

## Decisión UX: Intercepción conectada
La vista Today empleará el patrón **Split & Connected** para una intercepción. Es la
opción elegida frente a "Overlay & Layers" y "Smart Suggestions" porque conserva
el orden cronológico, explica causalidad y no obliga al usuario a interpretar dos
tarjetas aparentemente independientes.

Una intercepción se representa como un único bloque con tres estados visibles:

1. **Actividad pausada**: identifica la actividad original y su hora de inicio.
2. **Evento interceptor**: tarjeta ad-hoc indentada, con icono de rayo y acento
   púrpura; se conecta mediante un riel de impacto y el texto de transición.
3. **Reanudación**: confirma que la actividad original continúa al terminar el
   evento, sin inventar una duración ni una hora de reanudación que el motor aún
   no haya calculado.

El bloque usa un fondo translúcido, sin borde perimetral ni desenfoque. El riel
discontinuo y las fases en baja opacidad convierten la actividad víctima en contexto;
la tarjeta ad-hoc púrpura es el único foco accionable. Un solapamiento normal sigue
siendo una advertencia; sólo un evento ad-hoc o una restricción inmóvil se eleva a
intercepción. Se evita `blur()` porque una lista de timeline puede contener varios
bloques y la transparencia conserva tanto rendimiento como legibilidad.

## Archivos Clave
- `domain/model/TemporalMobility.kt` (NEW)
- `domain/model/TemporalImpact.kt` (NEW)
- `domain/usecase/ConflictDetectorUseCase.kt` (REFACTOR)
- `domain/usecase/GenerateAdjustmentSuggestionsUseCase.kt` (NEW)

## Plan de Verificación
- **Test de Oro de Interrupciones**: Validar que una "Salida Express" dentro de la Universidad se marque como interrupción pero no mueva la Universidad.
- **Test de Validación de Sugerencias**: Asegurar que una sugerencia para evitar el Conflicto A no genere un nuevo Conflicto B.
- **Test de proyección Today**: Comprobar que `ResolveTimelineUseCase` devuelva los
  conflictos calculados; de lo contrario, la UI no puede agrupar la relación.
