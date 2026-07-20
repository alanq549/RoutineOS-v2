# 05_ROADMAP.md — RoutineOS v2

## Visión de Largo Plazo
RoutineOS v2 busca ser la plataforma de referencia para la gestión de rutinas con un enfoque local-first, priorizando la privacidad y la excelencia técnica.

---

## Fases del Proyecto

### Fase 1: Infraestructura de Ingeniería (Actual)
Establecimiento de las bases sobre las cuales se construirá todo el sistema.
- **Objetivos**: Framework documental, base de datos local, inyección de dependencias, base del sistema de diseño.
- **Dependencias**: Ninguna.

### Fase 2: Core Architecture & Data Layer
Implementación de los cimientos técnicos de la aplicación.
- **Objetivos**: Configuración de Hilt, base de Room, repositorios base, entidades core (Routine, Task, Execution).
- **Dependencias**: Fase 1.

### Fase 3: Foundations del Design System
Creación de la biblioteca de componentes UI reutilizables.
- **Objetivos**: Tokens de color, tipografía, formas, componentes base (Botones, Cards, Inputs).
- **Dependencias**: Fase 1.

### Fase 4: Gestión de Rutinas (MVP)
Funcionalidad principal para el usuario final.
- **Objetivos**: Listado, creación, edición y visualización de rutinas.
- **Dependencias**: Fase 2, Fase 3.

### Fase 5: Ejecución y Tracking
El motor de RoutineOS.
- **Objetivos**: Sistema de temporizadores, estados de ejecución, persistencia de resultados de sesión.
- **Dependencias**: Fase 4.

### Fase 6: Análisis y Estadísticas
Valor agregado basado en los datos locales.
- **Objetivos**: Visualizaciones de progreso, insights de cumplimiento, exportación de datos.
- **Dependencias**: Fase 5.

---

## Notas de Implementación
- Las fases pueden solaparse ligeramente en el tiempo (paralelismo), pero sus dependencias estructurales deben respetarse.
- Cada fase se compone de múltiples Engineering Cards (ECs).
- El progreso detallado de cada fase se consulta en [04_PROJECT_STATUS.md](./04_PROJECT_STATUS.md).
