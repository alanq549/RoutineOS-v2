# Engineering Cards (EC) — RoutineOS v2

## ¿Qué es una Engineering Card?
Una Engineering Card es un documento de diseño técnico y seguimiento para una tarea específica y atómica. Es el contrato entre la intención del desarrollador y la implementación final.

## Propósito
- **Trazabilidad**: Saber qué se hizo, por qué y cómo.
- **Consistencia**: Asegurar que todos los desarrolladores (humanos e IA) siguen los mismos estándares.
- **Documentación Viva**: El código cambia, pero la intención original y los hallazgos de la auditoría quedan registrados aquí.

## Ciclo de vida de una EC
1. **Creación**: Se utiliza la [TEMPLATE.md](./TEMPLATE.md).
2. **Análisis**: Se mapean los archivos afectados y las dependencias.
3. **Ejecución**: Se registra el progreso y cualquier desviación del plan original.
4. **Auditoría**: Se anotan los resultados de la revisión técnica.
5. **Cierre**: Se marca como `MERGED` o `CLOSED`.

## Ubicación
Todas las ECs deben vivir en este directorio (`app/src/main/java/com/alan/routineos/documentation/EC/`) con el formato de nombre `EC-XXX-nombre-descriptivo.md`.
