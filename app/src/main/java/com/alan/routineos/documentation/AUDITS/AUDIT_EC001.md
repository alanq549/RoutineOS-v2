# Auditoría Técnica: EC-001_DATA_FOUNDATION

Este documento detalla los hallazgos de la auditoría técnica realizada sobre la implementación de la infraestructura de datos (Room, Hilt, KSP).

## Resumen de Estado
| Categoría | Estado |
| :--- | :--- |
| Gradle | **MAJOR** |
| Room | **MAJOR** |
| Hilt | **ACCEPTED** |
| Tests | **MINOR** |
| Arquitectura | **ACCEPTED** |

---

## Hallazgos

### 🚨 BLOCKER
*Ninguno.*

### ⚠️ MAJOR

#### 1. Schema Export Desactivado y Sin Configurar
- **Archivo:** [RoutineDatabase.kt](file:///C:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/main/java/com/alan/routineos/data/local/RoutineDatabase.kt)
- **Detalle:** `exportSchema` está establecido en `false`. No se ha configurado la ruta de exportación en el bloque `ksp` de `build.gradle.kts`.
- **Impacto:** Impide el seguimiento de versiones del esquema y la generación de migraciones automáticas confiables en el futuro.
- **Recomendación:** Cambiar a `true` y configurar `app/schemas` en el `build.gradle.kts`.

#### 2. Ausencia de Plugin de Kotlin en Gradle
- **Archivo:** [build.gradle.kts](file:///C:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/build.gradle.kts)
- **Detalle:** Se está utilizando `android.builtInKotlin=true` en `gradle.properties`, lo cual permite omitir el plugin `org.jetbrains.kotlin.android`. Sin embargo, esto es una configuración poco convencional que puede causar fricción con herramientas de análisis estático o plugins de terceros que esperan el plugin estándar de Kotlin.
- **Justificación de Propiedades:**
    - `android.builtInKotlin=true`: Habilita el soporte nativo de Kotlin en AGP para simplificar la configuración.
    - `android.disallowKotlinSourceSets=false`: Permite el uso de estructuras de carpetas tradicionales para Kotlin.
- **Impacto Futuro:** Si bien simplifica el archivo de build, reduce la interoperabilidad estándar. Se recomienda evaluar si la ganancia en simplicidad compensa el alejamiento del estándar de la industria.

### 💡 MINOR

#### 1. Inconsistencia en Cantidad de Tests
- **Archivo:** [RoomDatabaseTest.kt](file:///C:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/app/src/androidTest/java/com/alan/routineos/data/local/RoomDatabaseTest.kt)
- **Detalle:** La documentación menciona "5/5" tests, pero el archivo solo contiene 4 métodos de prueba.
- **Recomendación:** Implementar un quinto test (ej. `getTasksForRoutine` sin tareas previas o `getRoutineById` con ID inexistente) para cumplir con la métrica.

#### 2. Versión de Room Inusualmente Alta
- **Archivo:** [libs.versions.toml](file:///C:/Users/alanq/AndroidStudioProjects/RoutineOS-v2/gradle/libs.versions.toml)
- **Detalle:** La versión `2.8.4` de Room es extremadamente reciente o futura (considerando que 2.7.0 es la estable actual).
- **Recomendación:** Verificar si es una versión experimental intencional o un error de tipografía en el catálogo de versiones.

### ✅ ACCEPTED

#### 1. Implementación de Room (Modelado)
- Las entidades `RoutineEntity` y `TaskEntity` están correctamente normalizadas.
- `ForeignKey.CASCADE` implementado correctamente para integridad referencial.
- Índices presentes en claves foráneas para optimización de consultas.

#### 2. Inyección de Dependencias (Hilt)
- Configuración de `RoutineApp` y `AndroidManifest.xml` es correcta.
- `DatabaseModule` provee las dependencias como Singletons adecuadamente.
- Uso de `@ApplicationContext` para la creación de la base de datos.

#### 3. Aislamiento Arquitectónico
- No se detectaron Repositories ni Use Cases en la capa de datos, respetando el alcance de EC-001.
- No hay acoplamiento con la UI.

---

## Próximos Pasos (Pendiente de Aprobación)

1.  **Configurar Schema Export:**
    - Modificar `RoutineDatabase` para `exportSchema = true`.
    - Añadir configuración KSP en `app/build.gradle.kts` para exportar a `app/schemas`.
2.  **Corregir Métrica de Tests:**
    - Añadir 1 test adicional para alcanzar los 5 prometidos.
3.  **Normalización de Gradle (Opcional pero recomendado):**
    - Evaluar el retorno al plugin `org.jetbrains.kotlin.android` para mayor compatibilidad.
