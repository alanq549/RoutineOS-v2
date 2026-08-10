# Invariantes de Interacción — RoutineOS (NO NEGOCIABLES)

## Principio central
RoutineOS es un motor de planificación temporal de uso diario. La carga cognitiva
de ajustar o ejecutar algo debe ser proporcional a la frecuencia con la que esa
acción ocurre: lo frecuente (completar, saltar, posponer) es de un toque y sin
navegación; lo infrecuente (crear una entidad nueva, definir su estructura) puede
requerir una pantalla completa. Ninguna pantalla de ejecución (Today y equivalentes
futuros) puede depender de navegar a otra pantalla para realizar su acción primaria.

## Regla explícita
PROHIBIDO: que la acción primaria de un nodo/bloque en una vista de ejecución
(marcar completado, saltar, posponer) requiera `navController.navigate(...)` a
otra pantalla. Si el ViewModel de esa pantalla ya expone (o puede exponer) el
método correspondiente del repositorio, la UI debe invocarlo directamente desde
un `clickable` / gesto en el propio componente.

PROHIBIDO: diálogos de confirmación (`AlertDialog`) para acciones reversibles.
Toda acción reversible (completar, saltar, eliminar un nodo sin historial de
ejecución) se ejecuta de inmediato y ofrece deshacer vía `Snackbar`.

PROHIBIDO: que un icono o botón interactivo (`IconButton`, `FloatingActionButton`,
`clickable`) se implemente con `onClick = { }` como placeholder permanente. Si la
acción aún no está implementada, el componente debe estar deshabilitado
(`enabled = false`) o no renderizarse — un tap target vacío es peor que ausente,
porque el usuario cree que algo pasó.

## Patrones núcleo válidos
- **Tap directo**: acción primaria de un item en una lista/timeline. Ejecuta el
  caso de uso inmediatamente, sin pantalla intermedia.
- **Swipe / long-press**: acciones secundarias del mismo item (saltar, posponer,
  eliminar). Revela affordance in situ (iconos de acción o menú contextual
  anclado), nunca navega por sí solo.
- **Bottom sheet (`ModalBottomSheet`)**: edición de 1-4 campos de una entidad ya
  existente (hora, título corto, estado). Mantiene visible el contexto de origen
  detrás del scrim.
- **Manipulación directa (drag)**: ajuste de atributos espaciales/temporales
  representados visualmente (mover o extender un bloque en una vista de
  calendario/planning). Reemplaza al formulario cuando el dato ya se está
  mostrando en un eje espacial.
- **Navegación completa (`navController.navigate`)**: reservada para crear una
  entidad nueva desde cero o editar una con más de 4 campos / estructura anidada
  (p. ej. `ActivityCreationScreen`, gestión de nodos en `ActivityDetailScreen`).
- **Snackbar con deshacer**: confirmación no bloqueante tras cualquier acción
  reversible ejecutada por tap/swipe.

## Checklist obligatorio antes de cerrar cualquier EC de UI
- [ ] ¿La acción más frecuente de esta pantalla requiere salir de ella para
      completarse? Si la respuesta es sí, justificar por qué no es tap directo
      o bottom sheet.
- [ ] ¿Hay algún `onClick = { }`, `TODO()` o callback vacío quedando como
      placeholder de producción en este EC?
- [ ] ¿Se usó `AlertDialog` para una acción reversible en vez de
      snackbar-con-deshacer?
- [ ] ¿El componente fue copiado de un mockup de Stitch sin revisar si su patrón
      de interacción (navegación completa, menú "...") es el mínimo necesario
      para esa frecuencia de uso?

Si la respuesta a la primera o la tercera pregunta compromete la acción primaria
de una vista de ejecución, el EC no puede cerrarse hasta corregirlo.

## Nota de origen
Este documento nace de una auditoría sobre el estado real de `TodayScreen` y
`PlanningTimeBlock`: el FAB de Today (`onClick = { }`), el menú "..." de
`PlanningTimeBlock` (`onClick = { }`), y la ausencia total de `clickable` en
`TimelineItemCard`/`NodeRow` heredaron el patrón de navegación de los mockups de
Stitch sin adaptarlo a las reglas de arriba. La única acción de completar nodo
que funciona hoy vive en `ActivityDetailViewModel.completeNode()`, accesible solo
navegando desde Planning → catálogo → detalle — tres pantallas de distancia de
donde el usuario debería poder hacerlo en un toque. @documentation
