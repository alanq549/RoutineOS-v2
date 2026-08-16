---
ec_id: EC-RE-003
ronda: 1
fecha: 2026-08-15
resultado: REJECTED
---
## Hallazgos
El auditor ha revisado el estado actual de la implementación del Motor de Agendamiento Flexible.

1.  **Mismatch de Modelos y Contrato**:
    - **CRÍTICO**: El contrato técnico exige la inclusión de campos temporales (`startTime`, `endTime`, `duration`) en `ScheduleRule` (tanto en la entidad Room como en el modelo de dominio). Actualmente, estos campos **no existen** en el código.
    - La base de datos se encuentra en la versión 7, pero la migración parece haber sido incompleta o no ha incluido los campos requeridos por la semántica de agendamiento flexible.

2.  **Componentes Faltantes**:
    - No se encontró el `ConflictDetectorUseCase` mencionado en el alcance de la EC.
    - La lógica de resolución en `ResolveTimelineUseCase` es parcial: solo soporta `FIXED_DAYS` y no contempla ventanas de tiempo ni duraciones, ya que los campos base están ausentes.

3.  **Arquitectura e Integridad**:
    - **PASS**: La entidad `DailyInstance` y su mecanismo de "Materialización" (Snapshotting) están correctamente implementados, respetando la separación entre Intención y Realidad.
    - **PASS**: El filtrado de `isDeleted` se respeta correctamente a nivel de DAO (`getAllNodes`).

4.  **Pruebas**:
    - Se requiere una suite de tests que valide la coexistencia de reglas en diferentes niveles del árbol (Padre vs Hijo), tal como se especifica en el contrato. Actualmente, la lógica de resolución no permite probar estos escenarios complejos por la falta de campos temporales.

## Plan de corrección
1.  **Actualizar Modelos**: Agregar `startTime`, `endTime`, `duration` y `metadataJson` a `ScheduleRuleEntity.kt` y `ScheduleRule.kt`.
2.  **Migración V8**: Realizar la migración de esquema para incluir estos campos.
3.  **Implementar ConflictDetector**: Crear el UseCase para detección de solapamientos.
4.  **Refinar Resolución**: Actualizar `ResolveTimelineUseCase` para manejar la lógica de herencia/independencia temporal definida en el contrato.

## Conclusión
La implementación actual es una estructura base sólida (DailyInstances), pero carece del motor funcional (Time windows/Rules) que define el éxito de esta Épica.
