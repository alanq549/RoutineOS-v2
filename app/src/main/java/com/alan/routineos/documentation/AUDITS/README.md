# Auditorías Técnicas — RoutineOS v2

## Propósito
Las auditorías técnicas son el filtro final de calidad antes de que el código llegue a las ramas estables. Buscan garantizar que la "Constitución" del proyecto ([00_PROJECT_SCOPE.md](../00_PROJECT_SCOPE.md)) se respete escrupulosamente.

## Proceso de Auditoría
1. **Iniciación**: Se realiza cuando una EC llega al estado `AUDIT_PENDING` (tras ser marcada como `IMPLEMENTED` por el implementador).
2. **Revisión**: El agente auditor verifica arquitectura, invariantes de dominio ([08_ARCHITECTURE_INVARIANTS.md](../08_ARCHITECTURE_INVARIANTS.md)), legibilidad y rendimiento.
3. **Documentación**: Los resultados SIEMPRE se registran en `AUDITS/AUDIT_EC-XXX.md`, usando [AUDIT_TEMPLATE.md](./AUDIT_TEMPLATE.md) como base — nunca dentro del archivo de la EC.
4. **Corrección**: Si hay hallazgos negativos, el estado cambia a `CHANGES_REQUESTED` y el plan de corrección queda documentado en ese mismo archivo de auditoría, para que el implementador lo ejecute.
5. **Validación de usuario**: Si no hay hallazgos, el estado cambia a `USER_REVIEW_PENDING` y el auditor completa el **Checklist de Validación de Usuario** dentro del archivo de la EC.
6. **Aprobación**: El estado `APPROVED` lo asigna EXCLUSIVAMENTE el usuario, tras validar manualmente el comportamiento en la app. Ningún agente puede asignar este estado bajo ninguna circunstancia.

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
Todo reporte de auditoría vive en esta carpeta `AUDITS/` con el nombre `AUDIT_EC-XXX.md`, usando [AUDIT_TEMPLATE.md](./AUDIT_TEMPLATE.md) como base obligatoria — sin excepción, independientemente de la complejidad de la EC.
