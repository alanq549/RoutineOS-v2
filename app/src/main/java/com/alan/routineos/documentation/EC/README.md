# Engineering Cards (EC) — RoutineOS v2

## ¿Qué es una Engineering Card?
Una Engineering Card es un documento de diseño técnico y seguimiento para una tarea específica y atómica. Es el contrato entre la intención del desarrollador y la implementación final.

## Propósito
- **Trazabilidad**: Saber qué se hizo, por qué y cómo.
- **Consistencia**: Asegurar que todos los desarrolladores (humanos e IA) siguen los mismos estándares.
- **Documentación Viva**: El código cambia, pero la intención original y los hallazgos de la auditoría quedan registrados aquí.

## Ciclo de vida de una EC
1. **Creación**: Se utiliza la [TEMPLATE.md](./TEMPLATE.md). Estado: `DRAFT`.
2. **Análisis**: Se mapean los archivos afectados y las dependencias. Estado: `PENDING` o `READY`.
3. **Ejecución**: Se registra el progreso y cualquier desviación del plan original. Estado: `IN_PROGRESS` e `IMPLEMENTED`.
4. **Auditoría**: Se anotan los resultados de la revisión técnica. Estado: `AUDIT_PENDING` y `APPROVED`.
5. **Cierre**: Se marca como `MERGED` o `CLOSED`. Previo a esto, se debe verificar el cumplimiento del [EC_DEFINITION_OF_DONE.md](./EC_DEFINITION_OF_DONE.md).

## Ubicación
Todas las ECs deben vivir en este directorio (`app/src/main/java/com/alan/routineos/documentation/EC/`) con el formato de nombre `EC-XXX-nombre-descriptivo.md`.
