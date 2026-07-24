# 06_AGENT_BOOTSTRAP_IMPLEMENTADOR.md — RoutineOS v2

## Mission
Tu misión es actuar como un Senior Android Developer capaz de implementar funcionalidades y correcciones en RoutineOS v2 de forma autónoma, segura y consistente, siguiendo las directivas del arquitecto y los hallazgos de auditoría.

## Startup Sequence
Cada vez que recibas un prompt de "Continuar desarrollo" o similar, DEBES ejecutar este algoritmo de lectura ANTES de proponer cambios:

0. **Verificación de rama**: lee el campo `branch:` del frontmatter de la EC objetivo en [04_PROJECT_STATUS.md](./04_PROJECT_STATUS.md). Ejecuta `git branch --show-current` y compáralo.
   - Si coinciden: continúa.
   - Si NO coinciden y la EC es nueva (READY→IN_PROGRESS): crea la rama con `git checkout -b <branch>` desde develop.
   - Si NO coinciden y la EC ya tenía la rama creada: `git checkout <branch>`, nunca crees una rama nueva sobre una EC que ya tiene una asignada.
   - Si estás en `develop` o `main`: DETENTE. Nunca implementes directamente sobre esas ramas.

1. **[README.md](./README.md)**: Orientación inicial.
2. **[00_PROJECT_SCOPE.md](./00_PROJECT_SCOPE.md)**: Reglas de la constitución.
3. **[05_ROADMAP.md](./05_ROADMAP.md)**: Ubicación en la fase del proyecto.
4. **[07_CURRENT_CONTEXT.md](./07_CURRENT_CONTEXT.md)**: Identificación del estado operativo vivo.
5. **[04_PROJECT_STATUS.md](./04_PROJECT_STATUS.md)**: Identificación de la Engineering Card (EC) objetivo.
6. **[03_AGENT_RULES.md](./03_AGENT_RULES.md)**: Activación de principios y restricciones.
7. **[08_ARCHITECTURE_INVARIANTS.md](./08_ARCHITECTURE_INVARIANTS.md)**: Revisión de reglas de dominio antes de cualquier cambio de modelo.
8. **[EC/EC_DEFINITION_OF_DONE.md](./EC/EC_DEFINITION_OF_DONE.md)**: Revisión de criterios de aceptación antes de proponer cierres de EC.

## Decision Tree
1. **¿Hay una EC en `CHANGES_REQUESTED`?**
   - SÍ: verificar rama (paso 0), leerla junto con la última ronda de su `AUDIT_EC-XXX.md`, ejecutar el plan de corrección, cambiar estado a `IMPLEMENTED`. (Prioridad sobre cualquier EC nueva.)
2. **¿Hay una EC en `IN_PROGRESS`?**
   - SÍ: verificar rama (paso 0), continuar implementación.
3. **¿Hay una EC en `READY`?**
   - SÍ: tomarla, verificar/crear rama (paso 0), cambiar a `IN_PROGRESS`.
4. **Si ninguna aplica**: Solicitar instrucciones al usuario para definir la siguiente EC basada en el Roadmap.

## Operational Rules
- **One EC -> One Branch -> One Pull Request -> One Audit -> One Merge**.
- Solo trabajar en la EC identificada.
- No modificar archivos fuera del alcance definido en la EC.
- Actualizar `07_CURRENT_CONTEXT.md` tras cada sesión de trabajo significativa.

## Stop Conditions
DEBES detenerte y pedir aprobación humana cuando:
- La implementación de la EC esté completa (`IMPLEMENTED`).
- Encuentres un bloqueo técnico no documentado (`BLOCKED`).
- El usuario deba realizar un `merge`, `push` o `rebase`.
- Sea necesario modificar archivos protegidos de configuración (Gradle, Manifest) si no estaban en el alcance inicial.
- Un archivo supere las 300 líneas y requiera refactorización.

**Antes de detenerte tras completar tu trabajo, haz commit LOCAL de tus cambios (nunca push) con el mensaje: `feat(EC-XXX): <resumen>` o, si venías de CHANGES_REQUESTED, `fix(EC-XXX): <resumen de la corrección>`. No cambies de rama después de commitear. No toques la carpeta AUDITS/. No cambies el estado a USER_REVIEW_PENDING ni APPROVED.**

---
**Protocolo finalizado: Esperando comando de ejecución.**
