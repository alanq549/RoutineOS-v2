# 05_ROADMAP.md — RoutineOS v2

## Visión de Largo Plazo
RoutineOS v2 busca ser la plataforma de referencia para la gestión de rutinas con un enfoque local-first, priorizando la privacidad y la excelencia técnica.

---

## Flujo de Dependencias de Fases
El proyecto sigue una progresión lógica donde cada capa sostiene a la siguiente:

**Foundation (Fase 1)**
↓
**Core & Data Layer (Fase 2)**
↓
**Design System Foundations (Fase 3)**
↓
**Routine Management (Fase 4)**
↓
**Execution & Tracking (Fase 5)**
↓
**Analytics & Insights (Fase 6)**

---
---
**Nota de reordenamiento**: EC-006 (Routine Dashboard, originalmente numerada EC-004) quedó bloqueada por EC-005 (Domain Model Agnostic Refactor), tras detectarse en auditoría retroactiva que la capa de dominio (EC-001, EC-002) violaba el invariante domain-agnostic. Orden de ejecución real: EC-005 → EC-006, no por número sino por dependencia técnica.
---

## Fases del Proyecto

### Fase 1: Infraestructura de Ingeniería
Establecimiento de las bases sobre las cuales se construirá todo el sistema.
- **Objetivos**: Framework documental, base de la arquitectura.
- **Estado**: Activo.

### Fase 2: Core Architecture & Data Layer
Implementación de los cimientos técnicos de la aplicación.
- **Objetivos**: Configuración de Hilt, Room, Repositorios base.
- **Dependencia**: Fase 1 finalizada.

### Fase 3: Foundations del Design System
Creación de la biblioteca de componentes UI reutilizables.
- **Objetivos**: Tokens de diseño, componentes atómicos.
- **Dependencia**: Fase 1 finalizada.

### Fase 4: Gestión de Rutinas (MVP)
Funcionalidad principal para el usuario final.
- **Objetivos**: CRUD de rutinas.
- **Dependencia**: Fase 2 y Fase 3 finalizadas.

### Fase 5: Ejecución y Tracking
El motor de RoutineOS.
- **Objetivos**: Temporizadores, ejecución de sesiones.
- **Dependencia**: Fase 4 finalizada.

### Fase 6: Análisis y Estadísticas
Valor agregado basado en los datos locales.
- **Objetivos**: Visualizaciones, insights.
- **Dependencia**: Fase 5 finalizada.
