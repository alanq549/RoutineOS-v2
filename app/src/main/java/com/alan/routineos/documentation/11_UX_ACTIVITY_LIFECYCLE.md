# 11_UX_ACTIVITY_LIFECYCLE.md — RoutineOS v2

# Análisis UX: Ciclo de Vida de la Actividad (Rethinking the MVP)

## 1. Visión General: De la Definición a la Realidad
En RoutineOS, una "Actividad" (anteriormente Rutina) no es un simple recordatorio. Es una **entidad viva** que evoluciona desde una intención abstracta hasta un dato de rendimiento concreto. El "MVP" de Título + Descripción es solo el punto de entrada.

### ¿Por qué esta estructura?
Para cumplir con el invariante de **Agnosticismo de Dominio**, el sistema debe tratar de la misma forma un "Entrenamiento de Empuje" que un "Bloque de Deep Work" o un "Control de Gastos Semanal".

---

## 2. Anatomía de una Actividad (El "Cómo")

Una Actividad completa se compone de cuatro capas jerárquicas:

### A. La Identidad (`ActivityDefinition`)
*   **Qué es**: El contenedor raíz. Define el "Qué" y el "Para qué".
*   **UX de Creación**: Debe permitir capturar la intención. ¿Es una actividad recurrente? ¿Es un proyecto único?
*   **Relación**: Es el "padre" de todos los nodos y reglas.

### B. La Estructura (`ActivityNode`)
*   **Qué es**: Los pasos accionables o "sub-tareas".
*   **UX de Creación**: No debe ser una lista estática. El usuario debe poder definir el orden, la importancia y, en el futuro, dependencias entre pasos.
*   **Agnosticismo**: Un nodo puede ser "5 series de Press Banca" o "Revisar logs del servidor". El motor solo ve "Paso 1", "Paso 2".

### C. El Ritmo (`ScheduleRule` & `ScheduleException`)
*   **Qué es**: La inteligencia temporal.
*   **UX de Creación**: Definir el patrón de aparición.
    *   *Fijo*: "Todos los Lunes a las 8:00".
    *   *Flexible*: "3 veces por semana", dejando que el algoritmo de Today lo sugiera según huecos libres.
*   **Relación con Planning**: Aquí es donde la actividad "pinta" el calendario semanal.

### D. La Métrica (`MetadataSchema` & `ActivityExecution`)
*   **Qué es**: La captura de valor.
*   **UX de Ejecución**: Al completar un paso en `Today`, el sistema debe preguntar: "¿Cuánto pesaste?", "¿Cuántas páginas leíste?", "¿Qué tal fue tu enfoque?".
*   **JSON-First**: Los campos de metadata no están en columnas de base de datos fijas, permitiendo que el usuario cree su propio formulario de seguimiento sin cambiar la arquitectura.

---

## 3. Relación con las Vistas (El "Por Qué")

La Actividad es el hilo conductor que une todas las pantallas del sistema:

| Vista | Relación con la Actividad | El "Por Qué" |
| :--- | :--- | :--- |
| **Catalog (Dashboard)** | Inventario de Blueprints | El usuario ve su "librería de capacidades". Es un lugar de reflexión y diseño, no de acción inmediata. |
| **Planning** | Proyección de Intenciones | Aquí el `ScheduleRule` se convierte en un bloque de tiempo. El usuario negocia con su futuro yo cuánto tiempo dedicará a cada actividad. |
| **Today** | Instancia de Ejecución | La pantalla más crítica. Aquí la `ActivityDefinition` se "resuelve" en `TimelineInstances`. Es pura acción y captura de metadata rápida. |
| **Stats** | Retroalimentación de Datos | Las `ActivityExecutions` se agregan para mostrar el progreso real contra el diseño original. |
| **System** | Balance de Áreas de Vida | Las actividades se agrupan en "Sistemas" (ej. Salud, Carrera) para asegurar que el usuario no descuida ninguna faceta importante. |

---

## 4. El Flujo de Creación Ideal (Propuesta Post-MVP)

Para superar la simplicidad del MVP, la creación debe ser un **flujo guiado (Wizard)** o un **desglose progresivo**:

1.  **Paso 1: Identidad**: Título, icono genérico y descripción de alto nivel.
2.  **Paso 2: Composición**: ¿Tiene pasos internos? (Añadir Nodos).
3.  **Paso 3: Frecuencia**: ¿Cómo quieres que aparezca en tu vida? (Reglas de Scheduling).
4.  **Paso 4: Seguimiento**: ¿Qué datos quieres medir al terminar? (Configuración de Metadata Schema).

---

## 5. Invariantes de Interacción en la Creación

Siguiendo `10_INTERACTION_INVARIANTS.md`:
*   **Contexto**: Crear una actividad es una acción "Infrecuente", por lo tanto, **merece una pantalla completa** y una carga cognitiva mayor que la ejecución diaria.
*   **Flexibilidad**: El usuario debe poder saltar pasos (ej. crear una actividad sin nodos si no los necesita).
*   **Previsualización**: A medida que el usuario define la recurrencia, la UI debería mostrar un mini-calendario con los días que se verán afectados.

---

## 6. Conclusión
La simplicidad actual de la **EC-007** fue necesaria para habilitar la persistencia real, pero el objetivo final es un motor donde la creación de una actividad sea el acto de **programar una parte de tu sistema de vida**. La próxima gran iteración (EC-014+) debe enfocarse en este flujo enriquecido.
