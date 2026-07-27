---
ec_id: EC-005
ronda: 1
fecha: 2026-07-26
resultado: USER_REVIEW_PENDING
---
## Hallazgos
Tras la validación manual del usuario y una revisión exhaustiva de la capa de `feature`, se han detectado fallos críticos que impiden la aprobación de la EC-005.

1.  **Fallo en Validación Manual**: El usuario reporta que la creación de actividades no es funcional. La aplicación sigue mostrando plantillas estáticas y utiliza repositorios falsos (`FakeRoutineRepository`), lo que rompe la expectativa de una refactorización funcional del motor de persistencia.

2.  **Violaciones Residuales a Invariantes (08_ARCHITECTURE_INVARIANTS.md)**:
    - Aunque la capa de `data` y `domain` fue saneada, la capa de `feature` (ViewModels, modelos de vista y navegación) conserva terminología de dominio prohibida:
        - **Modelos:** `RoutineCardModel`, `RoutineTemplateModel`, `RoutineCategory`.
        - **ViewModels:** `RoutineLibraryViewModel`.
        - **Navegación:** `RoutineNavHost`, `RoutineRoutes`, `Routines` route.
        - **Componentes UI:** `RoutineBottomBar`, `RoutineTheme`. (Nota: Los componentes del Design System pueden conservar el prefijo `Routine` si se considera parte del nombre del sistema operativo/app, pero los modelos de datos de negocio NO).
    - El principio *domain-agnostic* exige que la lógica de negocio esté libre de estos conceptos específicos.

3.  **Inconsistencia en la Inyección de Datos**: El `RoutineLibraryViewModel` inicializa manualmente un `FakeRoutineRepository`, ignorando por completo el `ActivityRepository` refactoreado y la inyección de dependencias de Hilt configurada en la EC.

## Plan de corrección
El agente implementador debe realizar las siguientes acciones:

1.  **Limpieza de la Capa de Feature**: Renombrar todos los paquetes, clases, modelos y variables que utilicen "Routine" o "Task" en `app/src/main/java/com/alan/routineos/feature/` y `core/navigation/`.
2.  **Conexión Real**: Eliminar el uso de `FakeRoutineRepository` en los ViewModels y utilizar `ActivityRepository` mediante inyección de dependencias (`@Inject`).
3.  **Sinceridad Documental**: Si la funcionalidad de "crear actividad" no está dentro del alcance técnico de esta EC, se debe corregir el **Checklist de Validación de Usuario** en el spec de la EC para no generar falsas expectativas, limitando la validación a lo que realmente se ha conectado (ej. visualización de datos desde la DB).

## Ronda 2
**Fecha:** 2026-07-26
**Resultado:** USER_REVIEW_PENDING

### Evaluación de Reversión de Veredicto
Tras la revisión de la sección "Alcance" de la EC-005, se confirma la exclusión explícita: *"No se crearán pantallas ni se alterará el flujo de usuario visual en esta EC."*

Por lo tanto, los hallazgos reportados en la Ronda 1 sobre la capa `feature/` (ViewModels, modelos de vista, navegación y uso de `FakeRepositories`) se dictaminan como **fuera del alcance de EC-005**. 

### Conclusiones
1. **Validez Técnica:** La evaluación original de las capas de persistencia, dominio, DI y tests (Ronda 1) sigue siendo válida y aprobada. El motor ha sido refactoreado correctamente a un modelo agnóstico.
2. **Deuda Técnica Identificada:** Los hallazgos de la capa `feature/` se registran como **requisitos obligatorios para EC-006 (Routine Dashboard)**. No son motivo de rechazo para EC-005.
3. **Saneamiento de Spec:** Se requiere ajustar el Checklist de Validación de Usuario en EC-005 para reflejar con honestidad que solo se validan capas internas.

La EC-005 se considera técnicamente cumplida dentro de su alcance definido.
