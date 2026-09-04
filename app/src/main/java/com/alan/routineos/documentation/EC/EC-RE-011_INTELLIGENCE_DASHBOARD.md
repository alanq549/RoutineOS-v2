---
id: EC-RE-011
title: Intelligence Dashboard
phase: 5
priority: High
effort: Medium
owner: AI Agent
status: READY
depends_on: EC-RE-010
branch: feature/ec-re-011-intelligence-dashboard
audit: Pending
created: 2026-09-03
updated: 2026-09-03
---

# EC-RE-011: Intelligence Dashboard

## Objetivo
Visualizar los datos procesados por el motor analítico en una interfaz coherente con el diseño "técnico premium" de RoutineOS, permitiendo al usuario monitorizar su adherencia, carga y progreso de datos de forma objetiva.

## Contexto
Tras cerrar la EC-RE-010 con un motor de resolución e integridad histórica robusto, esta fase se centra en la capa de presentación. El Dashboard debe transformar las métricas crudas en una experiencia visual accionable.

## Invariantes de Diseño
1.  **Neutralidad**: No inferir juicios de valor sobre los datos (ej. omitir no es "malo").
2.  **Transparencia de Datos**: Distinguir claramente entre estados sin datos (EMPTY) y datos insuficientes (N/A).
3.  **Arquitectura Unidireccional**: La UI solo consume y visualiza; el cálculo reside exclusivamente en los casos de uso analíticos.

## Alcance
- [ ] **Migración de StatsViewModel**: Sustituir el repositorio mock por el consumo real de `GetHistoryAnalyticsUseCase`.
- [ ] **Componentes Gráficos**: Implementar gráficas de distribución de carga y líneas de tendencia de metadatos.
- [ ] **Panel de Adherencia**: Visualización detallada de Completed vs Omitted vs Missed por sistema.
- [ ] **Periodización**: Implementar selectores de Día, Semana, Mes y Año.

## Definiciones Semánticas
Ver contrato funcional detallado en: `ec_re_011_functional_contract.artifact.md`.

## Plan de Verificación
- **Verificación de Flujo**: Asegurar que al cambiar el periodo, se dispare una nueva resolución del histórico y la UI se actualice reactivamente.
- **Verificación de N/A**: Confirmar que el Focus Index muestra el estado N/A debido a la falta actual de captura de duraciones reales.
- **Empty States**: Validar visualización correcta en cuentas nuevas sin historial de ejecuciones.
