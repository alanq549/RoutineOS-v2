# Auditorías Técnicas — RoutineOS v2

## Propósito
Las auditorías técnicas son el filtro final de calidad antes de que el código llegue a las ramas estables. Buscan garantizar que la "Constitución" del proyecto ([00_PROJECT_SCOPE.md](../00_PROJECT_SCOPE.md)) se respete escrupulosamente.

## Proceso de Auditoría
1. **Iniciación**: Se realiza cuando una EC llega al estado `IMPLEMENTED`.
2. **Revisión**: Se verifican los puntos críticos de arquitectura, legibilidad y rendimiento.
3. **Documentación**: Los resultados se registran dentro de la misma EC en la sección de "Auditoría".
4. **Corrección**: Si hay hallazgos negativos, se vuelve a fase de implementación.
5. **Aprobación**: Solo con todos los checks en verde se cambia el estado a `APPROVED`.

## Puntos Clave a Revisar
- **Arquitectura**: ¿La lógica de negocio está aislada de la UI?
- **Inyección de Dependencias**: ¿Se usa Hilt correctamente?
- **Manejo de Estado**: ¿El ViewModel expone un estado inmutable?
- **Estándares de Código**:
    - Funciones < 30 líneas.
    - Archivos < 300 líneas.
    - Nombres descriptivos.
- **Recursos**: ¿Se usan strings.xml y temas del Design System?

## Reportes de Auditoría
Para auditorías complejas que afecten a múltiples módulos, se pueden crear reportes detallados en esta carpeta `AUDITS/` con el nombre `AUDIT-XXX-descripcion.md`, aunque la recomendación general es mantener el registro dentro de la EC correspondiente.
