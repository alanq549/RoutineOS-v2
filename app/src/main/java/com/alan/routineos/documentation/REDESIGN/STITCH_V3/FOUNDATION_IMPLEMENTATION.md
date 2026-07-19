# Foundation Implementation - Design System V3

Esta infraestructura proporciona la base visual para el rediseño de RoutineOS, utilizando un modelo de tokens centralizado y componentes compartidos presentation-only.

## Estructura Final
```text
core/designsystem/
├── color/
│   └── RoutineColors.kt      (ColorScheme & CompositionLocal)
├── typography/
│   └── RoutineTypography.kt  (Escala tipográfica & Fallbacks)
├── shape/
│   └── RoutineShapes.kt      (Radios oficiales)
├── spacing/
│   └── RoutineSpacing.kt     (Rejilla 4dp & Márgenes)
├── dimension/
│   └── RoutineDimensions.kt  (Iconos, Toolbars, FABs)
├── theme/
│   └── RoutineTheme.kt       (Proveedor central & M3 Integration)
├── component/                (Componentes base compartidos)
│   ├── RoutineSurface.kt
│   ├── RoutineCard.kt
│   ├── RoutineButton.kt
│   ├── RoutineChip.kt
│   ├── RoutineTopBar.kt
│   ├── RoutineBottomBar.kt
│   ├── RoutineScaffold.kt
│   └── RoutineSectionHeader.kt
└── catalog/
    └── RoutineDesignSystemPreview.kt (Vista previa del sistema)
```

## APIs Públicas
El sistema se consume a través del objeto `RoutineTheme`:

```kotlin
val colors = RoutineTheme.colors
val typography = RoutineTheme.typography
val spacing = RoutineTheme.spacing
```

## Decisiones Arquitectónicas

### 1. Inmutabilidad y Performance
Todos los tokens están definidos como clases `@Immutable` y se proveen mediante `staticCompositionLocalOf` para minimizar las recomposiciones innecesarias en cambios de tema globales.

### 2. Capa sobre Material 3
`RoutineTheme` inicializa internamente un `MaterialTheme` con un `colorScheme` derivado de los tokens de RoutineOS. Esto garantiza que los componentes estándar de M3 (como `Switch` o `Slider`) mantengan la coherencia visual sin intervención manual.

### 3. Apariencia de Cristal (Glass)
Siguiendo las restricciones, no se ha utilizado `backdrop-filter` por software. El efecto "glass" se logra en `RoutineTopBar` y `RoutineBottomBar` mediante:
- Fondos con alfa (85%).
- Tonal layering (Surface1 sobre Background).
- Bordes de 1px con colores de contraste sutil (`#232A34`).

### 4. Independencia de Navegación
Los componentes de navegación (`TopBar`, `BottomBar`) son estrictamente visuales. No poseen lógica de `NavController` ni estado de rutas, permitiendo su uso flexible en cualquier feature.

## Ejemplo de Uso

```kotlin
RoutineTheme {
    RoutineScaffold(
        topBar = { RoutineTopBar(title = { Text("Today") }) }
    ) { padding ->
        RoutineCard(modifier = Modifier.padding(padding)) {
            Text("Content here", style = RoutineTheme.typography.bodyBase)
        }
    }
}
```
