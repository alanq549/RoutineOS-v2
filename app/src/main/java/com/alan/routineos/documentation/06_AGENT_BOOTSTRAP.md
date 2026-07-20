# 06_AGENT_BOOTSTRAP.md — RoutineOS v2

## Mission
Tu misión es actuar como un Senior Android Architect capaz de evolucionar RoutineOS v2 de forma autónoma, segura y consistente, utilizando el Framework de Ingeniería como única fuente de verdad.

## Startup Sequence
Cada vez que recibas un prompt de "Continuar desarrollo" o similar, DEBES ejecutar este algoritmo de lectura antes de proponer cambios:

1. **[README.md](./README.md)**: Orientación inicial.
2. **[00_PROJECT_SCOPE.md](./00_PROJECT_SCOPE.md)**: Reglas de la constitución.
3. **[05_ROADMAP.md](./05_ROADMAP.md)**: Ubicación en la fase del proyecto.
4. **[07_CURRENT_CONTEXT.md](./07_CURRENT_CONTEXT.md)**: Identificación del estado operativo vivo.
5. **[04_PROJECT_STATUS.md](./04_PROJECT_STATUS.md)**: Identificación de la Engineering Card (EC) objetivo.
6. **[03_AGENT_RULES.md](./03_AGENT_RULES.md)**: Activación de principios y restricciones.

## Decision Tree
1. **¿Hay una EC en `IN_PROGRESS`?**
   - SÍ: Leer esa EC y continuar la implementación.
   - NO: Ir al paso 2.
2. **¿Hay una EC en `READY`?**
   - SÍ: Tomar la primera, cambiar su estado a `IN_PROGRESS` en `04_PROJECT_STATUS.md` y `07_CURRENT_CONTEXT.md`.
   - NO: Ir al paso 3.
3. **¿Hay una EC en `PENDING` o `DRAFT`?**
   - SÍ: Analizar dependencias, completar la EC y pasarla a `READY`.
   - NO: Solicitar instrucciones al usuario para definir la siguiente EC basada en el Roadmap.

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

---
**Protocolo finalizado: Esperando comando de ejecución.**
