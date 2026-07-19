# Plan de Arquitectura Móvil - RoutineOS Redesign

## 1. Estructura de Módulos y Paquetes (Feature-First)

Se adopta una estructura jerárquica que separa el núcleo del sistema de las funcionalidades de usuario.

### `core/` (Estructura Base)
- **`designsystem/`**: Implementación de tokens de color, tipografía y componentes compartidos.
- **`ui/`**: Utilidades de UI, formateadores de datos y estados comunes.
- **`navigation/`**: Definición central de rutas y grafos de navegación.
- **`model/`**: Entidades de dominio compartidas.
- **`common/`**: Extensiones y utilidades genéricas de Kotlin/Android.

### `feature/` (Funcionalidades)
- **`today/`**: Pantalla principal, timeline de ejecución diaria.
- **`planning/`**: Vista semanal y gestión de excepciones.
- **`routines/`**: Editor de plantillas y estructuras de rutinas.
- **`system/`**: Vista general de áreas de vida.
- **`stats/`**: Gráficas de ritmo y ciclos.
- **`account/`**: Perfil y configuración de usuario.

### `data/` (Capa de Datos)
- **`local/`**: Room database, DataStore.
- **`repository/`**: Implementación de la lógica de acceso a datos.
- **`preferences/`**: Gestión de ajustes locales.

## 2. Patrón de Presentación
Cada feature seguirá preferentemente:
- `Screen.kt`: UI Composable principal.
- `Route.kt`: Punto de entrada que conecta con el ViewModel.
- `ViewModel.kt`: Gestión de estado y lógica de UI.
- `UiState.kt`: Clase de datos inmutable para el estado de la pantalla.
- `components/`: Compones específicos de la feature.

## 3. Estrategia de Implementación
- **Consistencia:** Se reutilizarán los ViewModels y repositorios existentes, adaptando únicamente la capa de presentación a los nuevos diseños.
- **Interoperabilidad:** El nuevo tema convivirá con el actual hasta que la migración sea completa.
