# Mapeo de Diseños Stitch a Features Android

| Pantalla Stitch | Feature Android | Responsabilidad Principal |
| :--- | :--- | :--- |
| **Today - Advanced Timeline** | `feature/today` | Ejecución diaria, timeline interactivo, estados de tareas. |
| **Planning - Vista Semanal** | `feature/planning` | Proyección semanal, gestión de excepciones y conflictos. |
| **Planning - Rutinas** | `feature/routines` | Definición de estructuras de rutinas y plantillas (Feature independiente). |
| **System - Vista General** | `feature/system` | Índice de áreas de vida y estado estructural de los sistemas. |
| **Stats - Seguimiento por Ritmo** | `feature/stats` | Visualización de desempeño semanal (Ritmo). |
| **Stats - Seguimiento por Ciclos** | `feature/stats` | Visualización de desempeño mensual/anual (Ciclos). |
| **Account - Perfil** | `feature/account` | Configuración, preferencias y gestión de perfil. |
| **Design System** | `core/designsystem` | Tokens compartidos, componentes base y temas. |

## Notas de Integración

1.  **Routines como Feature Independiente:** Aunque Stitch agrupa "Rutinas" bajo "Planning", se implementará como una feature separada para facilitar la reutilización desde `system` y `planning`.
2.  **Unificación de Stats:** `stats` manejará múltiples representaciones (Ritmo y Ciclo) dentro del mismo módulo, alternando según el periodo seleccionado.
