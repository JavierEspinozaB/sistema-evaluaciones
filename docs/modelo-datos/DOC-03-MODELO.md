# DOC-03 Modelo de datos

**Estado: pendiente de definición y aprobación.** No existen tablas del dominio en este esqueleto.

Posibles conceptos por investigar en el análisis funcional: evaluados, períodos, preguntas, opciones, intentos, respuestas, resultados e informes.

El equipo documentador debe acordar entidades, atributos, relaciones, restricciones, trazabilidad y protección de datos **antes** de generar SQL o clases JPA.

## Decisiones registradas en el repositorio

| Fecha | Tarea | Decisión |
| --- | --- | --- |
| 2026-11-17 | GEN-01-BE | `subprueba.factor` (`VARCHAR(80)`, nulo mientras no se cargue): código del factor al que aporta la subprueba según el Documento BFA §5.3. Varias subpruebas pueden compartir factor (S1 y S1b → S1; N1-A y N1-B → N1). Migración `V202611170600__comun_agregar_factor_subprueba.sql`. |
| 2026-11-17 | GEN-01-BE | E2 no guarda `opciones` ni `numeroPreguntas`: los calcula desde `pregunta` y `opcion_respuesta` de la configuración publicada. |

## Incógnitas y decisiones

**Tarea:** DOC-44 · **Responsable del aporte:** Javier Josué Espinoza Blandón  
**Estado:** borrador de trazabilidad. Pendiente de contrastar con el registro consolidado de DOC-42.

### Evidencias previas a la consulta

DOC-42 debe identificar las filas marcadas «Preguntar al docente». Davide transmite la lista numerada por el canal del curso. El envío y cada respuesta se acreditan con enlace, fecha y contenido pertinente. La fuente revisada es DOC-31 y ClickUp. La lista oficial, el envío y las respuestas siguen pendientes de verificación.

| Referencia | Asunto | Estado comprobado | Evidencia y siguiente actuación |
|---|---|---|---|
| DOC-42 | Consolidación de incógnitas de los cuatro temas | Pendiente de consolidación | Confirmar las filas marcadas para consulta y copiar sus identificadores originales. |
| DOC-32-BFAW / DOC-32-OTR | Reglas de ítems y formularios que queden sin resolver tras el contraste | Pendiente de revisión técnica | Contrastar los aportes y elevar exclusivamente las reglas funcionales que DOC-42 marque para consulta. |
| DOC-31 / GEN-37-BE | Nombres de PK, FK y estructura de evaluación, subprueba y respuesta | Pendiente de confirmación técnica | Validar el SQL y el diff con Desarrollo. Solo elevar al docente una decisión funcional pendiente. |
| DOC-44 | Evidencia de envío y respuesta por cada pregunta oficial | Pendiente de evidencia | Registrar un estado por fila e incorporar únicamente las decisiones con fuente. |


### Registro y cierre

Por cada fila oficial se conserva el ID original, la pregunta, su responsable, el estado, la fecha, la fuente de evidencia y el cambio aplicable. Si consta el envío y a las 10:00 del 25 de noviembre falta respuesta, corresponde «Preguntar al docente – enviada 25-nov». Cuando haya respuesta verificable, se actualizan el registro y el documento del tema. Si afecta al diagrama, se avisa a Fernando Reyes y se traza el cambio a DOC-45. Una decisión posterior debe conservar su evidencia y la fecha real de incorporación.
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
