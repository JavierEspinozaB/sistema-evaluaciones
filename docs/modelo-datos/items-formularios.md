# Tema 1 — Ítems y formularios

Recorrido común: `test` → `version_formulario` → `configuracion_subprueba` → `pregunta` → `opcion_respuesta` (y `subprueba` cuelga de `test`). Los nombres, PK y FK de esas seis tablas están congelados en «Núcleo y glosario» de `DOC-03-MODELO.md` y **no se repiten aquí**. La cardinalidad se escribe de padre a hijo.

## BFA y Weill

### Qué cabe en el núcleo (sin tabla nueva)

| Dato que pide la tarea | Dónde vive | Tabla del núcleo |
| --- | --- | --- |
| Test, versión de formulario | BFA y Weill son filas de `test`; cada edición es una fila de `version_formulario` | `test`, `version_formulario` |
| Subprueba / parte | Bloques de BFA y partes de Weill; el factor de BFA es `subprueba.factor` | `subprueba` |
| Configuración (tiempo, tipo de respuesta, orden, instrucciones) | `tiempo_limite_segundos`, `tipo_respuesta` (SELECCION, TEXTO_LIBRE), `orden_aplicacion`, `instrucciones_ref`, `practica_ref`, `maximo_entradas` | `configuracion_subprueba` |
| Pregunta / ítem y opción, con recursos (imágenes) | `enunciado`, `recurso_ref` en pregunta y opción | `pregunta`, `opcion_respuesta` |
| Fluidez Verbal (BFA) | Columnas de aplicación y respuesta; no es catálogo de ítems | `evaluacion_subprueba`, `respuesta_evaluacion` (fuera de este tema) |

### Tablas propias: clave de corrección (solo BFA)

**Por qué hace falta tabla propia:** la opción correcta no puede ser una columna (`es_correcta`) en `opcion_respuesta`, porque el evaluado recibe esa tabla y la clave debe estar protegida y versionarse aparte de la edición del contenido (Documento BFA §4.3, ENT-BFA-02 y 03; D03). Una clave puede corregirse sin cambiar las preguntas. Nombres y columnas son los ya fijados en el PR #50 (migración `V202611121500__bfa_claves_versionadas.sql`); no se renombran.

#### `version_clave_bfa` (ENT-BFA-02)

| Campo | Detalle |
| --- | --- |
| PK | `id_clave` (BIGINT) |
| FK | `id_version_formulario` → `version_formulario(id_version_formulario)`, obligatoria |
| Atributos | `version` (única por formulario), `estado` (BORRADOR, APROBADA, RETIRADA), `referencia_fuente` (obligatoria, ubicación del material autorizado, nunca las respuestas), `evidencia_aprobacion` (obligatoria si el estado no es BORRADOR) |
| Cardinalidad | `version_formulario` 1 : 0..N `version_clave_bfa`. Regla de diseño: a lo sumo 1 clave APROBADA por versión; para aprobar otra se retira la anterior. |

#### `entrada_clave_bfa` (ENT-BFA-03)

| Campo | Detalle |
| --- | --- |
| PK | `id_entrada_clave` (BIGINT) |
| FK | `id_clave` → `version_clave_bfa(id_clave)`; `id_pregunta` → `pregunta(id_pregunta)`; `id_opcion_correcta` → `opcion_respuesta(id_opcion)`; las tres obligatorias |
| Atributos | Solo las tres referencias. Único `(id_clave, id_pregunta)`: una sola opción correcta por pregunta y clave. |
| Cardinalidad | `version_clave_bfa` 1 : 0..N `entrada_clave_bfa`; `pregunta` 1 : 0..N `entrada_clave_bfa` (máximo 1 por clave); `opcion_respuesta` 1 : 0..N `entrada_clave_bfa`. |
| Regla de integridad | La pregunta debe pertenecer a la versión de la clave, y la opción a esa pregunta (se valida en aplicación o con restricción compuesta; las dos migraciones posteriores de #50 refuerzan esto). No aplica a Fluidez Verbal ni a la entrevista. |

Relaciones entre las dos tablas y el núcleo: `version_formulario` 1:N `version_clave_bfa` 1:N `entrada_clave_bfa` N:1 `pregunta` 1:N `opcion_respuesta`. Entre `pregunta` y `opcion_respuesta` por medio de la entrada es una relación N:M resuelta con `entrada_clave_bfa` (con la restricción de que la opción pertenezca a la pregunta).

### Weill

No se propone tabla propia. Weill usa las seis tablas del núcleo para test, partes, configuración, preguntas, opciones y recursos. Lo que falte para corregirlo se decide cuando se confirme la estructura en la Nota Weill (DOC-36, aceptada); si Weill necesita clave de corrección, se evalúa reutilizar `version_clave_bfa`/`entrada_clave_bfa` (nombre con sufijo `_bfa`) o crear una pareja propia, y esa decisión se toma con evidencia, no aquí.

### Tablas que NO se crean en este tema

`factor`, `baremo`, `rango_baremo`, `resultado`, puntajes por factor y aplicación BFA (ENT-BFA-01, 04 a 13) pertenecen a otros temas (baremos, resultados, ejecución). `baremo_bfa` y `rango_baremo_bfa` ya existen en #69.

### Incógnitas para el Registro

| ID | Incógnita | Impacto |
| --- | --- | --- |
| I-01 | Qué estructura de Weill (partes, tipo de ítem, tiempo, corrección) dicta la Nota Weill: el documento no estaba legible con la cuenta del autor al redactar esta sección | Decide si Weill necesita extensión de clave |
| I-02 | Claves oficiales BFA (D03): quién las aprueba y cobertura contra el cuadernillo definitivo | Cobertura de `entrada_clave_bfa` |
| I-03 | Cómo se aplica «a lo sumo 1 clave APROBADA por versión»: índice único parcial o regla de aplicación | Migración futura (nombre lo valida Kisnner) |
| I-04 | `recurso_ref` de pregunta y opción: formato y dónde se guardan las imágenes | Recursos (imágenes) de BFA y Weill |

Este documento no contiene ítems reales de ninguna prueba, respuestas correctas ni datos personales.

## Thurstone, Allport y Orientación vocacional
