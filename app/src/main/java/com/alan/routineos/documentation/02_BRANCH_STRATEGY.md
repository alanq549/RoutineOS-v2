# 02_BRANCH_STRATEGY.md — RoutineOS v2

## Estrategia de Ramas
RoutineOS v2 utiliza un modelo de branching basado en objetivos funcionales, manteniendo la estabilidad de las ramas principales.

### Ramas Protegidas
- **main**: Código en producción. Solo recibe merges de `develop`.
- **develop**: Rama principal de integración. Debe estar siempre en estado "buildable".

### Ramas de Trabajo
Toda rama de trabajo debe nacer de `develop` y seguir la convención:
- `feature/`: Nuevas funcionalidades o ECs de desarrollo.
- `fix/`: Corrección de errores.
- `docs/`: Cambios exclusivos de documentación.
- `refactor/`: Mejoras de código sin cambio de comportamiento.
- `chore/`: Tareas de mantenimiento, dependencias o configuración.

## Ciclo de Vida de una Rama
1. **Creación**: Se crea al iniciar una EC en estado `IN_PROGRESS`.
2. **Desarrollo**: Commits frecuentes y descriptivos.
3. **Merge**: Solo tras alcanzar el "Definition of Done" y pasar la auditoría. Se prefiere `merge --no-ff` para mantener la historia.
4. **Eliminación**: Las ramas de trabajo deben eliminarse inmediatamente después del merge exitoso a `develop`.

## Convención de Commits
Se recomienda el uso de Conventional Commits:
`<tipo>(<scope>): <descripción>`
Ejemplos:
- `feat(ui): add routine list screen`
- `fix(data): resolve race condition in repository`
- `docs(core): update agent rules`

## Definition of Ready (DoR)
Una tarea (EC) está lista para empezar si:
- Tiene un objetivo claro.
- Las dependencias están identificadas y resueltas.
- Los archivos afectados están mapeados.
- El estado en `04_PROJECT_STATUS.md` es `READY`.

## Definition of Done (DoD)
Una tarea se considera terminada si:
- La implementación cumple el objetivo de la EC.
- El código compila sin errores.
- Se ha realizado y documentado la auditoría técnica.
- La documentación ha sido actualizada.
- El estado en `04_PROJECT_STATUS.md` es `IMPLEMENTED` o `MERGED`.
