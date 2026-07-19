# Auditoría del Design System - Technical Premium Narrative

## 1. Identidad Visual
- **Enfoque:** Minimalismo de Alta Densidad, Glassmorphism Selectivo.
- **Personalidad:** Profesional, Técnico, Sofisticado.

## 2. Paleta de Colores (Tonal Layering)
- **Background (Base):** `#0A0D12`
- **Surface Level 1:** `#12161D` (Contenedores principales)
- **Surface Level 2:** `#171C24` (Estados hover/secciones elevadas)
- **Surface Level 3 (Glass):** `rgba(18, 22, 29, 0.55)` con Blur 12px-20px.
- **Borders:** `#232A34` (1px hairline)
- **Primary (Signal):** `#5AF0B3` (Brand Green)
- **Secondary:** `#A4C8FF`
- **Tertiary:** `#DBD1FF`
- **Error:** `#FFB4AB`

## 3. Tipografía
- **UI & Body:** **Inter** (Fallback: SansSerif)
    - Legibilidad máxima, tono neutro.
- **Data & Metrics:** **JetBrains Mono** (Fallback: Monospace)
    - Usado para timestamps, duraciones, contadores y metadatos técnicos.
- **Estado Actual:** Los recursos de fuente no se encuentran en `res/font/`. Se utilizan fallabas nativos en la foundation.
- **Escala:**
    - `display-lg`: 32px (600)
    - `headline-md`: 20px (600)
    - `body-base`: 16px (400)
    - `data-lg`: 18px (500)
    - `label-caps`: 12px (600) - Usado para encabezados de sección.

## 4. Espaciado y Rejilla
- **Sistema Base:** 4dp.
- **Márgenes:** 16px (Mobile).
- **Gaps Estructurales:** 16px (md), 24px (lg).
- **Indentación:** 20px para jerarquías anidadas.

## 5. Formas y Radios
- **Cards/Containers:** 16px.
- **Buttons/Interactive:** 8px.
- **Chips:** Pill-shaped (999px).
- **Modales/Sheets:** 24px (esquinas superiores).

## 6. Componentes Clave
- **Botones Primarios:** Fondo `#5AF0B3`, Texto `#0A0D12`.
- **Botones Secundarios:** Borde 1px `#232A34`, Fondo Surface 2.
- **Listas:** Padding vertical 16px, separadas por hairlines.
- **Indicators:** Barras laterales tipo "pill" para marcar el elemento activo.
