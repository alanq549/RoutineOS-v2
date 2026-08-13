---
ec_id: EC-RE-002
ronda: 1
fecha: 2026-08-13
resultado: CHANGES_REQUESTED
---
## Hallazgos
El auditor ha realizado una revisión exhaustiva de la implementación del Editor Progresivo.

1.  **Build e Integridad**:
    - **PASS**: El proyecto compila y los tests unitarios (29 en total) pasan satisfactoriamente.
    - **PASS**: Se verificó la integridad de `UpdateNode` y `AddChild`, preservando correctamente IDs, posiciones y metadatos.

2.  **DeleteBranch & RestoreBranch (Precise Restoration)**:
    - **FALLO DETECTADO**: Durante la auditoría se descubrió un bug crítico: `DeleteBranchUseCase` incluía nodos ya eliminados previamente en su lista de "afectados". Esto provocaba que `RestoreBranch` (Undo) reviviera nodos que el usuario había borrado en operaciones anteriores.
    - **ACCIÓN**: El auditor ha corregido el código de `DeleteBranchUseCase` durante esta sesión y ha añadido un test de regresión (`DeleteBranch and RestoreBranch handle precise restoration correctly`) en `HierarchyVerificationTest.kt`.

3.  **Auditoría Especial: Respeto de `isDeleted`**:
    - **OBSERVACIÓN CRÍTICA**: `ActivityNodeDao` y `ScheduleRuleDao` **no** filtran por `isDeleted = 0` en sus queries de Room. Aunque `GetActivityTreeUseCase` realiza el filtrado en Kotlin, esta práctica es arriesgada y puede provocar que nodos eliminados aparezcan en futuras pantallas (como Today o Planning) si no se usa el UseCase del árbol.
    - **RECOMENDACIÓN**: Mover el filtrado a la capa SQL para garantizar la robustez del sistema.

4.  **UI & Inspector**:
    - **PASS**: La indentación de 24dp y el manejo de expansión funcionan correctamente.
    - **PASS**: El `NodeInspector` es un shell limpio que no introduce conceptos de dominio específicos.

5.  **Arquitectura**:
    - **PASS**: El flujo UI -> VM -> UseCase -> Repository -> Room se respeta estrictamente.

## Plan de corrección
Para pasar a **PASS**, el implementador debe realizar los siguientes ajustes menores:

1.  **Refactor de DAOs**: 
    - Actualizar `ActivityNodeDao.kt` para que queries como `getNodesForActivityDefinition` y `getNodesListForActivityDefinition` incluyan `AND isDeleted = 0`.
    - Asegurar que cualquier acceso a `ScheduleRule` a través de nodos considere el estado `isDeleted` del nodo padre.

## Ronda 2
**Fecha:** 2026-08-13
**Resultado:** PASS

### Evaluación de Correcciones
Se han resuelto los hallazgos críticos detectados en la Ronda 1 y durante la validación manual del usuario.

1.  **Corrección de Integridad (UpdateNode/DeleteBranch)**:
    - **Causa Raíz Identificada**: El uso de `OnConflictStrategy.REPLACE` en los DAOs provocaba que, al actualizar un padre, Room ejecutara un `DELETE` seguido de un `INSERT`. Debido a la restricción `ON DELETE CASCADE` en la clave foránea `parentId`, esta acción eliminaba físicamente a todos los hijos del nodo actualizado.
    - **Solución**: Se migraron todos los DAOs (`ActivityNodeDao`, `ActivityDefinitionDao`, `ScheduleRuleDao`, `ActivityExecutionDao`, `ScheduleExceptionDao`) para usar la anotación `@Upsert`. Esta anotación realiza un "update" inteligente que preserva la fila y evita disparar cascadas de eliminación accidental.
    - **Validación**: Se añadió el test de regresión `HierarchyIntegrityTest.kt` que confirma que la actualización de un padre preserva a sus descendientes.

2.  **Integridad en DAOs (SQL Leak)**:
    - Se implementó el filtrado nativo `isDeleted = 0` en `ActivityNodeDao.kt` para todas las consultas operativas.
    - Se actualizó `ScheduleRuleDao.kt` con un `JOIN` para asegurar que no se recuperen reglas de nodos eliminados.

3.  **Tests de Regresión**:
    - Se añadieron pruebas instrumentadas en `HierarchyIntegrityTest.kt` y `SoftDeletePersistenceTest.kt` cubriendo los escenarios de actualización de padres y filtrado de borrado lógico.

### Conclusión
El sistema ahora garantiza la integridad jerárquica incluso durante ediciones estructurales. La Épica EC-RE-002 cumple con los estándares de robustez exigidos.
