# 03_AGENT_RULES.md — RoutineOS v2

## Protocolo Operativo para Agentes IA
Este documento establece el algoritmo de trabajo y las restricciones de seguridad para agentes IA que operan en RoutineOS v2.

### Principios de Ingeniería
El desarrollo debe seguir este flujo atómico para garantizar la trazabilidad:
**One Engineering Card (EC) -> One Branch -> One Pull Request -> One Audit -> One Merge**

### Algoritmo de Trabajo (10 Pasos)
Cada vez que un agente inicie una sesión de trabajo, DEBE ejecutar exactamente este orden:

1. **Constitución**: Leer [00_PROJECT_SCOPE.md](./00_PROJECT_SCOPE.md).
2. **Contexto Largo Plazo**: Leer [05_ROADMAP.md](./05_ROADMAP.md).
3. **Contexto Vivo**: Leer [07_CURRENT_CONTEXT.md](./07_CURRENT_CONTEXT.md).
4. **Dashboard**: Leer [04_PROJECT_STATUS.md](./04_PROJECT_STATUS.md).
5. **Selección de Tarea**: Seguir el Decision Tree de [06_AGENT_BOOTSTRAP.md](./06_AGENT_BOOTSTRAP.md).
6. **Verificación de Dependencias**: Confirmar que todas las dependencias de la EC seleccionada estén en estado `MERGED` o `CLOSED`.
7. **Entorno de Trabajo**: Verificar si se requiere una nueva rama según [02_BRANCH_STRATEGY.md](./02_BRANCH_STRATEGY.md).
8. **Diseño e Implementación**: Seguir estrictamente el plan definido en la EC.
9. **Actualización de Status**: Reflejar el avance en `07_CURRENT_CONTEXT.md` y `04_PROJECT_STATUS.md`.
10. **Finalización**: Detenerse y esperar feedback humano tras alcanzar una Stop Condition.

---

### Restricciones de Seguridad Críticas
Un agente NUNCA debe realizar las siguientes acciones sin aprobación humana explícita:
- **Merge**: No integrar ramas de trabajo.
- **Push**: No subir cambios a repositorios remotos.
- **Borrar Ramas**: No eliminar ninguna rama.
- **Modificación de Alcance**: No alterar el objetivo de una EC aprobada.

### Reglas de Calidad y Limpieza
- **Nunca modificar archivos no relacionados**: El diff debe ser mínimo y centrado en la EC.
- **Responsabilidad Única**: Un archivo = Una responsabilidad.
- **Límites de Tamaño**:
    - Funciones < 30 líneas.
    - Archivos < 300 líneas.
- **No romper compilación**: Verificar siempre el estado del build tras cambios significativos.
- **No Deuda Técnica**: No usar "TODO" sin una EC asociada o comentarios temporales.

## Auditor Agent

El Auditor es un rol de solo lectura.

Puede:

- inspeccionar el proyecto;
- ejecutar validaciones;
- revisar arquitectura;
- revisar calidad;
- clasificar hallazgos.

No puede:

- modificar código;
- modificar documentación de implementación;
- crear commits;
- actualizar Engineering Cards;
- iniciar nuevas tareas;
- corregir defectos detectados.

Su único entregable es un informe técnico con hallazgos clasificados como:

- BLOCKER
- MAJOR
- MINOR
- ACCEPTED