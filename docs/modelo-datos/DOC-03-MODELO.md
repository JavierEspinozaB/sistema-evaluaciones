# DOC-03 Modelo de datos

**Estado: Propuesta del sprint 03.**

## Diagrama y temas del modelo

El diagrama reúne el núcleo común aprobado en DOC-40, las tablas de claves y baremos de BFA y las propuestas de calificación de DOC-34-BFAW:
[editable en Draw.io](../diagramas/modelo-er.drawio) · [vista PNG](../diagramas/modelo-er.png).
DOC-34-OTR reutiliza las tablas comunes de calificación para Thurstone y Allport. Orientación Vocacional no tiene calificación automática definida. La propuesta de baremos genéricos (OTR-05) y la diferencia de auditoría entre DOC-31 y DOC-35-COM quedan pendientes de decisión.

- [Ítems y formularios](items-formularios.md)
- [Aplicación y respuestas (DOC-33, seguimiento en ClickUp)](https://app.clickup.com/t/1k5fzqb0ft6): el documento del tema aún no está en el repositorio.
- [Calificación, escalas y baremos](calificacion-baremos.md)
- [Identidad, periodos y auditoría](identidad-auditoria.md)

## Decisiones registradas en el repositorio

| Fecha | Tarea | Decisión |
| --- | --- | --- |
| 2026-11-17 | GEN-01-BE | `subprueba.factor` (`VARCHAR(80)`, nulo mientras no se cargue): código del factor al que aporta la subprueba según el Documento BFA §5.3. Varias subpruebas pueden compartir factor (S1 y S1b → S1; N1-A y N1-B → N1). Migración `V202611170600__comun_agregar_factor_subprueba.sql`. |
| 2026-11-17 | GEN-01-BE | E2 no guarda `opciones` ni `numeroPreguntas`: los calcula desde `pregunta` y `opcion_respuesta` de la configuración publicada. |

## Incógnitas y decisiones

Tarea DOC-42. Reúne las decisiones del modelo que tomaron los cuatro temas (DOC-32 a DOC-35) y las incógnitas que siguen abiertas. Cada incógnita queda resuelta con su fuente o marcada «Preguntar al docente». No incluye ítems, claves ni valores de baremos reales.

### Decisiones del modelo

| ID | Decisión | Fuente | Tema |
| --- | --- | --- | --- |
| D-01 | Los nombres que ya existen en `main` o en los PR #89, #41, #91, #50 y #69 no se renombran; los temas cuelgan sus tablas del núcleo. | Núcleo y glosario (DOC-31) | Todos |
| D-02 | Las claves y baremos de BFA conservan `version_clave_bfa`, `entrada_clave_bfa`, `baremo_bfa` y `rango_baremo_bfa`. | PR #50 y #69; DOC-31 | DOC-34 |
| D-03 | Weill Forma A es una sola subprueba de 60 preguntas; la lámina se deriva de la posición (12 por lámina). No se crean cinco subpruebas. | Nota Weill (DOC-36) §1 | DOC-32 / DOC-34 |
| D-04 | Weill usa opción única 1–8 por pregunta; se puede finalizar con preguntas omitidas y valen 0. | Nota Weill §2 y §5 | DOC-33 / DOC-34 |
| D-05 | Weill no tiene límite de tiempo por defecto: `configuracion_subprueba.tiempo_limite_segundos` queda nulo. | Docente 21/11 (H-01); Nota Weill §3 | DOC-32 |
| D-06 | Un dato normativo que no se puede calcular queda nulo con estado y motivo; nunca se rellena con 0. | Documento BFA ENT-BFA-10; Nota Weill §7 | DOC-34 |
| D-07 | `sujeto`, `periodo` y `cuenta` usan los nombres físicos del núcleo (`codigo_institucional`, `fecha_nacimiento`, `fecha_inicio`, `fecha_fin`, `estado` ACTIVA/INHABILITADA…), no los atributos lógicos en camelCase de las fichas ENT-COM. | Núcleo y glosario (DOC-31) | DOC-35 |
| D-08 | La duda I-01 de DOC-32 (estructura de Weill) queda resuelta: la Nota Weill fue aceptada el 24/11. Weill puede seguir el mismo patrón de clave: una entrada por pregunta (60 entradas, la secuencia de 12 repetida en las 5 láminas). | Nota Weill (DOC-36) §1, §2 y §5 | DOC-32 |

### Incógnitas

| ID | Incógnita e impacto en el modelo | Estado | Responsable |
| --- | --- | --- | --- |
| I-01 | Nombres definitivos de las PK de `evaluacion_subprueba`, `respuesta_evaluacion` y `solicitud_calculo_subprueba` (hoy `id` en #41). | Resolver en GEN-37-BE | Backend (GEN-37-BE) |
| I-02 | `baremo_bfa` no tiene FK a `test` ni a `version_formulario`: no se sabe a qué edición pertenece una norma. | Resolver en el modelo | Integración y DOC-34 |
| I-03 | `rango_baremo_bfa.id_factor` es texto y no hay tabla de factores (FactorPsicometrico). | Resolver en el modelo | DOC-34 |
| I-04 | El baremo no tiene grupo de población estructurado (edad mínima y máxima; AN, AD, UN), y Weill lo necesita para elegir la columna. | Resolver en el modelo | DOC-34 |
| I-05 | Dónde se guardan los subtotales por lámina de Weill, que no son factores (H del tema DOC-34). | Resolver en el modelo | DOC-34 |
| I-06 | Columnas de 7 y 9 años no monótonas: E30 las rechaza por rangos superpuestos, pero el docente pidió tomar la tabla tal como está (H-03). | Preguntar al docente | Iván |
| I-07 | Percentil y CI para puntajes fuera de la tabla de Weill (H-04). | Preguntar al docente | Iván |
| I-08 | Cuándo se usa AD o UN para mayores de 16 años (H-06). | Preguntar al docente | Iván |
| I-09 | Lista de categorías de Weill: 7 en la tabla del docente frente a 6 en F-01 (H-02). | Preguntar al docente | Iván |
| I-10 | `permitirRetroceso` (ENT-COM-13) no tiene columna ni aparece en el contrato. | Resolver en el modelo | DOC-32 e Integración |
| I-11 | Cómo se aplica «a lo sumo una clave APROBADA por versión»: índice único parcial o regla de aplicación. | Resolver en el modelo | Backend y custodio de migraciones |
| I-12 | Formato de `recurso_ref` y dónde se guardan las imágenes; además, `opcion_respuesta` no tiene recurso propio y Weill necesita una imagen por opción. | Resolver en el modelo | DOC-32 y Backend |
| I-13 | Catálogo de valores de `accion` y `entidad_tipo` en `evento_auditoria`. | Resolver en el modelo | DOC-35 y Backend |

### Consulta al docente (DOC-44)

**Responsable del aporte:** Javier Josué Espinoza Blandón.  
**Canal:** Davide Patiño remite al docente las preguntas funcionales acordadas con Iván.  
**Evidencia disponible:** la consulta del 21/11/2026 por Google Chat se registra según la [revisión de Iván](https://github.com/Nell16/sistema-evaluaciones/pull/106#pullrequestreview-6058635309) y su [aclaración](https://github.com/Nell16/sistema-evaluaciones/pull/106#issuecomment-7164087416). Falta el enlace directo al hilo de Google Chat. La evidencia revisada no acredita un nuevo envío el 25/11/2026 ni respuestas posteriores.

#### Weill — preguntas para seguimiento con el docente

En la consulta del 21/11 el docente indicó que Weill no tiene límite de tiempo (configurable), que la clave es la misma en las cinco láminas, que el baremo se toma como está y es configurable, y que las omisiones valen cero. Conservar esos antecedentes sin repetir las preguntas ya resueltas.

| ID de DOC-42 | Pregunta de seguimiento | Evidencia del 21/11 | Estado y siguiente paso |
| --- | --- | --- | --- |
| I-06 | ¿Cómo se conserva el baremo oficial de 7 y 9 años si E30 rechaza sus rangos superpuestos? | Según Iván, el docente pidió conservar la tabla tal como está. Falta una instrucción específica para los rangos superpuestos. | Aclaración pendiente. Preservar el baremo de origen y coordinar con Backend el tratamiento autorizado. |
| I-07 | ¿Cómo se obtiene el percentil y el CI cuando el puntaje queda fuera de la tabla de Weill? | El docente indicó que no sabía y remitió a quienes trabajaron el test. | Derivada al equipo que trabajó Weill. Sin criterio confirmado para extrapolar. |
| I-08 | ¿Cuándo corresponde AD y cuándo UN para una persona mayor de 16 años? | Se consultó. La respuesta aclaró que el instrumento no se usa para admisión y dejó pendiente la regla AD/UN. | Respuesta parcial. Solicitar solo el criterio AD/UN. |
| I-09 | ¿Cuál es el listado autorizado de categorías, dado que la tabla tiene siete y F-01 registra seis? | Iván aclaró que esta pregunta no se formuló el 21/11. | Pregunta nueva, sin evidencia de envío. |

#### Otras incógnitas funcionales registradas

Estas filas conservan sus IDs de origen para conciliación con Iván. El seguimiento inmediato indicado en la revisión es I-06 a I-09. Para las filas siguientes, las fuentes acreditan el hallazgo, pero faltan constancia de envío al docente y respuesta.

| ID | Consulta por confirmar | Fuente | Estado |
| --- | --- | --- | --- |
| ALL-01 | Confirmar la numeración canónica de la segunda parte de Allport y cómo se aplica la variante por sexo. | [DOC-38](nota-allport.md), F1 pp. 9–10. | Abierta. |
| ALL-02 | Confirmar cómo calificar Allport si la persona omite una respuesta permitida por las instrucciones. | [DOC-38](nota-allport.md), F1 pp. 1, 6 y 10. | Abierta. |
| ALL-03 | Identificar el manual y el baremo de Allport aplicables a esta edición y población. | [DOC-38](nota-allport.md), F1 pp. 1–11. | Abierta. |
| OTR-01 | Confirmar la corrección de Thurstone, el tratamiento de omisiones, el significado del índice y la fuente normativa. | [DOC-34-OTR, PR #119](https://github.com/Nell16/sistema-evaluaciones/pull/119). | Abierta. |
| OTR-02 | Confirmar los criterios funcionales de Allport y la edición aplicable. Mantener el vínculo con ALL-01 a ALL-03 y las demás filas de DOC-38 para evitar duplicar consultas. | [DOC-34-OTR, PR #119](https://github.com/Nell16/sistema-evaluaciones/pull/119) y [DOC-38](nota-allport.md). | Abierta. |
| OTR-03 | Confirmar la versión autorizada de la entrevista y si existe un informe cualitativo digital, así como su revisión y publicación. | [DOC-34-OTR, PR #119](https://github.com/Nell16/sistema-evaluaciones/pull/119). | Abierta. |
| OV-01 | Confirmar si el cuestionario o entrevista de Orientación vocacional se aplica sin límite de tiempo. | [Registro DOC-39](https://app.clickup.com/t/90141806675/1k5fzqb0ft4), F1 p. 1 y F2 pp. 1–4. | Abierta. |
| OV-02 | Confirmar si la salida de Orientación vocacional es exclusivamente cualitativa, dado que los formatos no definen puntaje ni escalas cuantitativas. | [Registro DOC-39](https://app.clickup.com/t/90141806675/1k5fzqb0ft4), F1 p. 1 y F2 pp. 1–4. | Abierta. |
| OV-03 | Confirmar si la interpretación de Orientación vocacional es cualitativa o si existe una fuente normativa autorizada. | [Registro DOC-39](https://app.clickup.com/t/90141806675/1k5fzqb0ft4), F1 p. 1 y F2 pp. 1–4. | Abierta. |

Las filas I-01 a I-05, I-10 a I-13 y los cinco huecos de DOC-41 se mantienen en el seguimiento técnico de Backend e Integración. Para cada consulta pendiente, registrar su fecha real de envío, quién la envió, el enlace de evidencia y la respuesta o su estado. Si una respuesta afecta el diagrama, trasladar el cambio con su evidencia a DOC-45.

## Endpoints y tablas

**Tarea:** DOC-41 · **Estado:** borrador. Muestra qué tablas lee y escribe cada endpoint del contrato (`docs/api/openapi.yaml`). Esta sección cubre **E1–E15** (Diego); **E16–E31** los completa Daysi en este mismo PR.

**Cómo leer los nombres de tabla**

- **Físico:** la tabla existe en las migraciones del repositorio (`test`, `subprueba`, `configuracion_subprueba`, `version_formulario`, `pregunta`, `opcion_respuesta`) o en la PR #89 (`cuenta`, `cuenta_rol`, `sesion`, aún en borrador).
- *Lógico:* entidad del Documento general (ENT-COM) cuya tabla todavía no está en el repositorio. Se escribe en cursiva y su nombre físico está **por confirmar** al fusionarse la migración correspondiente. Las lecturas y escrituras de estos casos son una **propuesta** derivada del propósito del endpoint en el contrato, no un comportamiento ya implementado.

| Endpoint | Método y ruta | Tablas que lee | Tablas que escribe | Comentario |
| --- | --- | --- | --- | --- |
| E1 | `GET /api/v1/tests` | `test` | — | Lista los tests activos (`activo`). |
| E2 | `GET /api/v1/tests/{testId}` | `test`, `subprueba`, `version_formulario`, `configuracion_subprueba`, `pregunta`, `opcion_respuesta` | — | Lee la edición publicada. `opciones` y `numeroPreguntas` se calculan desde `pregunta` y `opcion_respuesta`, no se guardan (decisión de GEN-01-BE). |
| E3 | `POST /api/v1/evaluaciones` | *sujeto*, *periodo*, `test`, `version_formulario`, `configuracion_subprueba` | *evaluacion*, *evaluacion_subprueba* | Fija sujeto, periodo y edición publicada al iniciar. Puede registrar un evento de auditoría. |
| E4 | `GET /api/v1/evaluaciones/{evaluacionId}` | *evaluacion*, *evaluacion_subprueba*, *respuesta* | — | Estado y avance. Si el límite venció, muestra la subprueba CERRADA con `cerradaEn` igual al límite (según el contrato común, comentario de Iván en DOC-02). |
| E5 | `POST /api/v1/evaluaciones/{evaluacionId}/subpruebas/{codigo}/iniciar` | *evaluacion*, `configuracion_subprueba`, *evaluacion_subprueba* | *evaluacion_subprueba*, *evento de auditoría* | Registra el inicio real y, si hay duración autorizada, el límite. |
| E6 | `GET /api/v1/evaluaciones/{evaluacionId}/subpruebas/{codigo}/preguntas` | *evaluacion_subprueba*, `configuracion_subprueba`, `pregunta`, `opcion_respuesta`, *respuesta* | — | Devuelve los reactivos y la selección previa; no expone la clave. |
| E7 | `PUT /api/v1/evaluaciones/{evaluacionId}/subpruebas/{codigo}/respuestas` | *evaluacion_subprueba*, `pregunta`, `opcion_respuesta` | *respuesta*, *evento de auditoría* | Valida que la opción pertenezca a la pregunta y que la ejecución esté activa. Una fila por ejecución y pregunta. |
| E8 | `POST /api/v1/evaluaciones/{evaluacionId}/finalizar` | *evaluacion*, *evaluacion_subprueba*, *respuesta*, *insumos de cálculo (clave y baremo)* | *evaluacion*, *evaluacion_subprueba*, *resultado*, *revisión de cálculo*, *evento de auditoría* | Cierra y consolida. El cálculo depende de decisiones abiertas de Psicología. |
| E9 | `GET /api/v1/evaluaciones/{evaluacionId}/resultados` | *resultado*, *revisión de cálculo* | — | Solo el resultado publicado, según permisos. |
| E10 | `PUT /api/v1/evaluaciones/{evaluacionId}/calificaciones/{calificacionId}/palabras/{respuestaId}/revision` | *respuesta*, *calificación* | *revisión de palabra*, *evento de auditoría* | Fluidez Verbal (BFA). No aplica a Weill. |
| E11 | `POST /api/v1/evaluaciones/{evaluacionId}/recalculos` | *evaluacion*, *resultado*, *respuesta*, *insumos de cálculo* | *revisión de cálculo*, *evento de auditoría* | Crea una revisión nueva; el historial se conserva. |
| E12 | `PUT /api/v1/evaluaciones/{evaluacionId}/resultados/vigente` | *resultado*, *revisión de cálculo* | *resultado*, *evento de auditoría* | Actualiza la revisión vigente y el estado. Actualización y bitácora en la misma transacción. |
| E13 | `POST /api/v1/auth/registro` | `cuenta` | `cuenta`, `cuenta_rol` | Comprueba que el correo sea único; guarda solo el resumen de la contraseña. El rol inicial lo define la regla de registro (por confirmar). |
| E14 | `POST /api/v1/auth/sesion` | `cuenta`, `cuenta_rol` | `sesion` | Abre sesión; guarda el resumen del token, el rol activo y `expira_en`. |
| E15 | `DELETE /api/v1/auth/sesion` | `sesion` | `sesion` | Cierra la sesión actual escribiendo `cerrada_en`; no borra la fila. |

## Criterios de aceptación

- [ ] Los 31 endpoints están: **E1–E15** aquí; **E16–E31** pendientes de Daysi.
- [x] En E1–E15 solo aparecen nombres de tablas físicas del repositorio o de la PR #89, o entidades lógicas marcadas en cursiva por confirmar.
- [ ] Reemplazar los nombres en cursiva por los físicos cuando se fusionen las migraciones de evaluación, resultado y auditoría.

## Núcleo y glosario

Tarea DOC-31. Congela las tablas comunes y los nombres canónicos que usan todos los temas de BD. Cada tema (claves, baremos, resultados por test, etc.) cuelga de estas tablas sin renombrarlas.

### Reglas de nombres

1. Lo que ya existe en `main` o en los PR #89, #41, #91, #50 y #69 **no se renombra**; el núcleo adopta esos nombres.
2. Tablas y columnas en `snake_case`, en singular. PK `id_<tabla>` de tipo `BIGINT GENERATED BY DEFAULT AS IDENTITY`. FK con el nombre de la PK a la que apuntan (`id_<tabla>`).
3. Excepción registrada: `cuenta_rol` tiene PK compuesta `(id_cuenta, rol)`.
4. Una migración nueva se llama `V<AAAAMMDDhhmm>__<modulo>_<descripcion>.sql`; el custodio (Kisnner) valida el nombre y nunca se edita una migración fusionada.
5. Fluidez Verbal no tiene tabla propia: son columnas de `evaluacion_subprueba` y `respuesta_evaluacion`.

### Tablas del núcleo

Origen: **main** = ya fusionada; **#89**, **#41**, **#91** = en el PR indicado (se fusionan en GEN-06-BE, GEN-37-BE y GEN-35-BE); **nueva** = falta crearla (este tema la define, no la crea).

| Tabla | Origen | PK | FK | Atributos principales |
| --- | --- | --- | --- | --- |
| `test` | main | `id_test` | — | `codigo` (único), `nombre`, `descripcion`, `activo` |
| `subprueba` | main | `id_subprueba` | `id_test` → test | `codigo` (único por test), `nombre`, `descripcion`, `factor` |
| `version_formulario` | main | `id_version_formulario` | `id_test` → test | `codigo_version` (único por test), `estado` (BORRADOR, PUBLICADA, RETIRADA), `fuente_ref`, `publicada_en`, `evidencia_validacion` |
| `configuracion_subprueba` | main | `id_configuracion_subprueba` | `id_version_formulario` → version_formulario; `id_subprueba` → subprueba | `orden_aplicacion`, `tiempo_limite_segundos`, `tipo_respuesta` (SELECCION, TEXTO_LIBRE), `instrucciones_ref`, `practica_ref`, `maximo_entradas` |
| `pregunta` | main | `id_pregunta` | `id_configuracion_subprueba` → configuracion_subprueba | `numero_visible`, `orden_presentacion`, `enunciado`, `recurso_ref` |
| `opcion_respuesta` | main | `id_opcion` | `id_pregunta` → pregunta | `codigo`, `orden_presentacion`, `texto`, `recurso_ref` |
| `cuenta` | #89 | `id_cuenta` | — | `correo` (único sin distinguir mayúsculas), `resumen_contrasena`, `nombres`, `apellidos`, `estado` (ACTIVA, INHABILITADA) |
| `cuenta_rol` | #89 | `(id_cuenta, rol)` | `id_cuenta` → cuenta | `rol` (SUJETO, ACADEMICO, COORDINADOR, PSICOLOGO, DECANO) |
| `sesion` | #89 | `id_sesion` | `id_cuenta` → cuenta; `(id_cuenta, rol_activo)` → cuenta_rol | `resumen_token` (único), `rol_activo`, `abierta_en`, `expira_en`, `cerrada_en` |
| `sujeto` | #91 | `id_sujeto` | — | `codigo_institucional` (único, opcional), `nombres`, `apellidos`, `fecha_nacimiento`, `sexo_referencia` |
| `periodo` | #91 | `id_periodo` | — | `codigo` (único), `nombre`, `fecha_inicio`, `fecha_fin`, `activo` |
| `evaluacion` | #41 (ids ajustados en GEN-37-BE) | `id_evaluacion` | `id_sujeto` → sujeto; `id_periodo` → periodo; `id_test` → test; `id_version_formulario` → version_formulario | `estado`, `fecha_inicio`, `fecha_fin`, `creado_en`, `actualizado_en` |
| `evaluacion_subprueba` | #41 (ids ajustados en GEN-37-BE) | `id_evaluacion_subprueba` | `id_evaluacion` → evaluacion; `id_configuracion_subprueba` → configuracion_subprueba | `estado` (PENDIENTE, ACTIVA, CERRADA), `fecha_inicio`, `fecha_fin_limite`, `fecha_cierre`, `letra_inicial` y `maximo_entradas` (Fluidez Verbal) |
| `respuesta_evaluacion` | #41 (ids ajustados en GEN-37-BE) | `id_respuesta_evaluacion` | `id_evaluacion` → evaluacion; `id_pregunta` → pregunta (nula en Fluidez Verbal); `id_opcion` → opcion_respuesta (nula en texto libre) | `valor_texto`, `orden_entrada`, `fecha_registro` |
| `solicitud_calculo_subprueba` | #41 | `id_evaluacion_subprueba` (PK y FK) | `id_evaluacion_subprueba` → evaluacion_subprueba; `id_evaluacion` → evaluacion | `codigo_test`, `codigo_subprueba`, `fecha_cierre`, `procesada_en` |
| `resultado` | **nueva** (ENT-COM-10) | `id_resultado` | `id_evaluacion` → evaluacion (única: 0..1 por evaluación) | `estado` (CALCULANDO, PENDIENTE_VALIDACION, DISPONIBLE), `creado_en`, `actualizado_en`, `referencia_revision_vigente` (obligatoria en DISPONIBLE; apunta a la revisión de cálculo del módulo, sin FK en el núcleo) |
| `evento_auditoria` | **nueva** (ENT-COM-11) | `id_evento_auditoria` | `id_evaluacion` → evaluacion (nula en eventos administrativos); `id_evaluacion_subprueba` → evaluacion_subprueba (nula) | `actor_ref`, `accion`, `entidad_tipo`, `entidad_id`, `ocurrido_en`; inmutable (solo inserciones) |

Tablas de los temas que cuelgan del núcleo (nombres ya fijados por sus PR; no se redefinen aquí):

| Tabla | Origen | Entidad del diseño | Cuelga de |
| --- | --- | --- | --- |
| `version_clave_bfa`, `entrada_clave_bfa` | #50 (PK `id_clave`, `id_entrada_clave`) | clave de corrección de BFA | `version_formulario`, `pregunta`, `opcion_respuesta` |
| `baremo_bfa`, `rango_baremo_bfa` | #69 (PK `id_baremo`, `id_rango`) | NormaBaremo, EquivalenciaBaremo | `rango_baremo_bfa.id_factor` es texto (código del factor); aún sin tabla de factores |

Sin tabla definida todavía (no inventar nombres; los fija el tema que los crea): FactorPsicometrico, AplicacionBFA, ResultadoFactor.

### Puntos de extensión

Un test agrega tablas propias con FK a estas tablas del núcleo, sin modificar su estructura:

| Tabla del núcleo | Qué cuelga de ella |
| --- | --- |
| `version_formulario` | claves de corrección y normas por versión (por ejemplo `version_clave_bfa`) |
| `configuracion_subprueba` | reglas propias de una subprueba dentro de una versión |
| `evaluacion_subprueba` | datos propios de una aplicación de subprueba (el patrón de Fluidez Verbal, hoy como columnas) |
| `resultado` | revisiones de cálculo y puntajes por factor del módulo (`referencia_revision_vigente`) |

### Glosario

| Término | Definición |
| --- | --- |
| Test | Instrumento de evaluación del catálogo (tabla `test`). |
| Subprueba | Parte nombrada de un test que se aplica por separado (tabla `subprueba`). |
| Versión de formulario | Edición del contenido de un test, con estados BORRADOR, PUBLICADA y RETIRADA; una vez publicada es inmutable (tabla `version_formulario`). |
| Configuración | Ajuste de una subprueba dentro de una versión: orden, tiempo límite, tipo de respuesta e instrucciones (tabla `configuracion_subprueba`). |
| Ítem / pregunta | Reactivo que se presenta al sujeto dentro de una configuración (tabla `pregunta`). |
| Opción | Alternativa de respuesta de una pregunta de selección (tabla `opcion_respuesta`). |
| Evaluación | Aplicación de una versión de un test a un sujeto en un período (tabla `evaluacion`). |
| Aplicación de subprueba | Ejecución de una subprueba dentro de una evaluación, con su estado y sus tiempos (tabla `evaluacion_subprueba`). |
| Respuesta | Valor elegido o escrito por el sujeto en una aplicación de subprueba (tabla `respuesta_evaluacion`). |
| Escala / factor | Dimensión que se mide y a la que aportan una o varias subpruebas (columna `subprueba.factor`). |
| Puntaje bruto | Número que resulta de corregir las respuestas de una subprueba o factor, sin convertir. |
| Puntaje convertido | Valor que resulta de pasar el puntaje bruto por un baremo (por ejemplo, el percentil). |
| Baremo | Tabla de equivalencias que convierte un puntaje bruto en convertido para una población de referencia. |
| Resultado | Contenedor de salida de una evaluación, con su estado y la referencia a la revisión de cálculo vigente (tabla `resultado`). |
| Cuenta | Credencial de acceso al sistema; no es una persona evaluada (tabla `cuenta`). |
| Rol | Perfil de permisos de una cuenta: SUJETO, ACADEMICO, COORDINADOR, PSICOLOGO o DECANO (tabla `cuenta_rol`). |
| Sujeto | Persona evaluada (tabla `sujeto`). |
| Período | Intervalo institucional en el que se programan las evaluaciones (tabla `periodo`). |

### Pendientes de confirmación en GEN-37-BE

- Nombres definitivos de las PK de `evaluacion_subprueba`, `respuesta_evaluacion` y `solicitud_calculo_subprueba`: #41 hoy usa `id` y, en `solicitud_calculo_subprueba`, `id_subprueba` (que choca con la PK de `subprueba`). Este documento fija `id_evaluacion_subprueba`, `id_respuesta_evaluacion` y `id_evaluacion_subprueba`.
- #41 repite en `evaluacion_subprueba` datos que ya están en la configuración (`codigo`, `nombre`, `orden`, `duracion_segundos`, `instrucciones`); no son atributos del núcleo.
- `id_sujeto` e `id_periodo` entran en `evaluacion` sin FK hasta que GEN-35-BE cree `sujeto` y `periodo`.
