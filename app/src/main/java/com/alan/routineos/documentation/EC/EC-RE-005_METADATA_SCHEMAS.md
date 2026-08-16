---
id: EC-RE-005
title: Metadata Schemas & Context
phase: 3
priority: High
effort: Medium
owner: AI Agent
status: IN_PROGRESS
depends_on: EC-RE-004
branch: feature/ec-re-005-metadata-schemas
audit: Pending
created: 2026-08-16
updated: 2026-08-16
---

# EC-RE-005: Metadata Schemas & Context

## Objetivo
Implementar la infraestructura para definir **qué datos** se deben capturar durante la ejecución de una actividad. Esto transforma el campo `metadataJson` genérico en una estructura validada y personalizada por el usuario.

## Contexto
En la Fase 2 establecimos el **Cuándo** (Scheduling). En esta Fase 3 establecemos el **Qué** (Metadata). Un usuario que entrena en el gimnasio querrá capturar "Peso" y "Reps", mientras que uno que estudia querrá capturar "Páginas leídas" o "Nivel de enfoque".

## Problema
Actualmente, las ejecuciones guardan un JSON opaco. No hay forma de que la interfaz de usuario sepa qué campos mostrar para que el usuario rellene durante la ejecución en "Today". Sin esquemas, no podemos habilitar el seguimiento de progreso cuantitativo.

## Alcance
- [ ] **Modelo de Dominio `MetadataSchema`**: Estructura que contiene una lista de campos (`MetadataField`).
- [ ] **Tipos de Campo Soportados**:
    - `NUMBER`: Para métricas cuantitativas (kg, reps, min).
    - `TEXT`: Para notas rápidas o descripciones.
    - `BOOLEAN`: Para verificaciones de sí/no (¿Tomaste agua?).
    - `SELECT`: Para opciones predefinidas.
- [ ] **Persistencia**: Entidad `MetadataSchemaEntity` vinculada polimórficamente a `ActivityDefinition` o `ActivityNode`.
- [ ] **Integración en Node Inspector**: Habilitar el chip "Metadata" para abrir un editor de esquema.
- [ ] **Validación**: Asegurar que los nombres de campos sean únicos dentro de un esquema y no estén vacíos.

### Exclusiones
- No incluye la captura de datos en "Today" (EC-RE-006).
- No incluye gráficas de progreso (Fase 4).

## Archivos Afectados
- `data/local/entities/MetadataSchemaEntity.kt` (NEW)
- `domain/model/MetadataSchema.kt` (NEW)
- `feature/dashboard/components/MetadataEditorSheet.kt` (NEW)
- `data/local/RoutineOSDatabase.kt` (V2 Baseline)
- `domain/repository/ActivityRepository.kt`

## Plan de Implementación
1. Definir entidades y modelos de `MetadataSchema`.
2. Actualizar el repositorio para soportar CRUD de esquemas.
3. Implementar `MetadataEditorSheet` en Compose.
4. Conectar el chip de "Metadata" del Inspector para abrir el editor.
5. Validar que la edición del esquema preserve la integridad de ejecuciones pasadas (vía agnosticismo de JSON).

## Validaciones
- [ ] Crear un esquema con 2 campos numéricos y verificar persistencia.
- [ ] Verificar que no se pueden añadir dos campos con el mismo nombre.
- [ ] Confirmar que el esquema es accesible tanto para actividades raíz como para sub-pasos.

## Auditoría
- [ ] ¿Los nombres de los campos son libres (definidos por el usuario)?
- [ ] ¿Se evita cualquier lógica de dominio específica (gym, etc.) en el motor?
- [ ] ¿El esquema se guarda de forma independiente a la ejecución?

## Definition of Done (Obligatorio)
- [ ] Compila sin warnings nuevos.
- [ ] CRUD de esquemas funcional y testeado.
- [ ] UI intuitiva integrada en el Node Inspector.
