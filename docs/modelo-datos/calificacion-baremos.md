## BFA y Weill

**DOC-34-BFAW · Tema 3 · 24 de noviembre de 2026.** Responsable: Roberto Sánchez. Revisión previa: Iván Gerardo Chavarría Blandón. Esta versión incorpora sus observaciones para el PR. Describe cuatro tablas con nombres fijados y seis propuestas de persistencia. La aprobación en GitHub queda pendiente.

**1. Del formulario al resultado**

El cálculo parte de la versión de formulario aplicada y de las respuestas guardadas. En BFA produce puntajes de subprueba y de factor. En Weill produce cinco subtotales de lámina y un total para una sola aplicación de subprueba. La revisión vincula esos valores con las reglas que los originaron. El resultado común determina qué revisión se puede consultar.

| Tabla del núcleo y PK | Relaciones físicas relevantes | Uso en este tema |
|---|---|---|
| `test.id_test`, `subprueba.id_subprueba`, `version_formulario.id_version_formulario` | `subprueba.id_test` y `version_formulario.id_test` → `test.id_test`. Test 1:N subpruebas y 1:N versiones. | Identifican el instrumento y la edición. En BFA se conserva `subprueba.factor` como código de aporte al factor. |
| `configuracion_subprueba.id_configuracion_subprueba`, `evaluacion.id_evaluacion` | Cada configuración se vincula por `id_subprueba` e `id_version_formulario` a sus respectivas PK. Cada evaluación tiene un test, una versión, un sujeto y un período. Cada padre tiene 0..N hijos. | Fijan la configuración y la edición aplicadas. La edad se calcula en años cumplidos a la fecha de la evaluación, con `sujeto.fecha_nacimiento` y `evaluacion.fecha_inicio`. |
| `evaluacion_subprueba.id_evaluacion_subprueba` | `id_evaluacion` → `evaluacion.id_evaluacion`; `id_configuracion_subprueba` → `configuracion_subprueba.id_configuracion_subprueba`. Ambas 1:N. | Identifica la aplicación de subprueba y conserva sus tiempos. Weill tiene una sola ejecución para las cinco láminas. |
| `pregunta.id_pregunta`, `opcion_respuesta.id_opcion`, `respuesta_evaluacion.id_respuesta_evaluacion` | Configuración 1:N preguntas; pregunta 1:N opciones. La respuesta referencia `evaluacion.id_evaluacion`, y, cuando corresponda, `pregunta.id_pregunta` y `opcion_respuesta.id_opcion`. | La opción pertenece a la pregunta y la pregunta a la edición aplicada. DOC-31 vincula físicamente la respuesta con la evaluación. La aplicación de subprueba se identifica mediante su configuración. |
| `resultado.id_resultado` | `id_evaluacion` → `evaluacion.id_evaluacion` con unicidad. Evaluación 1:0..1 resultado. | Contenedor y estado común. `referencia_revision_vigente` señala la revisión publicada del módulo; DOC-31 la define sin FK en el núcleo. |
| `evento_auditoria.id_evento_auditoria`, `cuenta.id_cuenta` | `evento_auditoria.id_evaluacion` e `id_evaluacion_subprueba` son FK opcionales a sus PK del núcleo. Evaluación y aplicación 1:N eventos. | `actor_ref` identifica al actor del evento. La validación profesional de una revisión puede apuntar a una cuenta. Se reutilizan los roles y las consultas autorizadas del núcleo. |

DOC-31 fija los nombres físicos incluso donde una migración sigue pendiente. En particular, identifica `resultado` y `evento_auditoria` como tablas comunes por crear. Las cuatro tablas BFA siguientes y las seis propuestas se distinguen de esas tablas comunes.

**2. Clave y baremo BFA que se reutilizan**

| Tabla y PK | FK y cardinalidad verificadas en el PR | Atributos, unicidad y uso |
|---|---|---|
| `version_clave_bfa` · `id_clave` | `id_version_formulario` → `version_formulario.id_version_formulario`. Una versión de formulario tiene 0..N versiones de clave. | `version`, `estado`, `referencia_fuente`, `evidencia_aprobacion`. Única por formulario y versión. BFA admite una sola clave aprobada por formulario; las revisiones previas conservan la usada. PR #50 y ENT-BFA-02. |
| `entrada_clave_bfa` · `id_entrada_clave` | `id_clave` → `version_clave_bfa.id_clave`; `id_pregunta` → `pregunta.id_pregunta`; `id_opcion_correcta` → `opcion_respuesta.id_opcion`. Cada padre 1:N entradas. | Única por clave y pregunta. Verificar que la opción pertenezca a la pregunta y que esta corresponda al formulario. El acceso a la clave se restringe al proceso de corrección. PR #50 y ENT-BFA-03. |
| `baremo_bfa` · `id_baremo` | El PR #69 define la cabecera **sin FK saliente**. Desde `rango_baremo_bfa` y desde las revisiones BFA que la usen se llega a ella por `id_baremo`. Baremo 1:N rangos y 1:N revisiones. | `nombre`, `version`, `anio_norma`, `poblacion_referencia`, `referencia_fuente`. Única por nombre y versión. La compatibilidad con el formulario se valida al asignar la norma. PR #69 y ENT-BFA-06. |
| `rango_baremo_bfa` · `id_rango` | `id_baremo` → `baremo_bfa.id_baremo`. Cada fila pertenece a un baremo. `id_factor` es un código de texto, sin FK física al catálogo de escalas. | `id_factor`, `percentil`, `limite_inferior`, `limite_superior`, `estado_celda`, `valor_original`, `localizador_fuente`. Única por baremo, código de factor y percentil. Una celda `RANGO` lleva ambos extremos inclusivos; `SIN_DATO` deja ambos nulos. PR #69 y ENT-BFA-07. |

Se conserva la grafía de las cuatro PK y el alcance BFA de estas tablas. DOC-42 decidirá la representación física de claves, normas y equivalencias de Weill. Esta entrega no crea tablas genéricas para esos datos ni dirige FK de Weill a tablas BFA.

**3. Tablas propuestas para las reglas y el cálculo**

Todas las PK nuevas se proponen como `BIGINT GENERATED BY DEFAULT AS IDENTITY`. Cada FK toma el tipo de la PK referenciada. En las relaciones 1:N el padre admite cero hijos salvo mínimo expreso. Las referencias dentro del tema terminan en el núcleo o en las cuatro tablas BFA ya fijadas.

| Tabla propuesta y PK | FK y cardinalidad | Atributos principales y razón de persistencia |
|---|---|---|
| `escala` · `id_escala` | `id_test` → `test.id_test`. Test 1:N escalas. | `codigo`, `nombre`, `tipo_salida`, `nivel_calculo` cuando proceda. Única por test y código. Cataloga factores BFA, subtotales técnicos y totales. En BFA el código concuerda con el de la fuente. `subprueba.factor` conserva la asignación de la subprueba; este catálogo identifica también los resultados agregados. |
| `regla_calificacion` · `id_regla_calificacion` | `id_version_formulario` → `version_formulario.id_version_formulario`. Versión de formulario 1:N versiones de reglas. | `version`, `estado`, `referencia_especificacion`, `referencia_implementacion` opcional y `evidencia_aprobacion`. Única por formulario y versión. Identifica la regla reproducible de cada cálculo. ENT-BFA-04. |
| `revision_resultado` · `id_revision_resultado` | `id_resultado` → `resultado.id_resultado`; `id_regla_calificacion` → `regla_calificacion.id_regla_calificacion`. Ambas 1:N. `id_cuenta` → `cuenta.id_cuenta`, 0..1 revisora por revisión y 0..N revisiones por cuenta. Solo BFA: `id_clave` → `version_clave_bfa.id_clave` e `id_baremo` → `baremo_bfa.id_baremo`, 0..1 de cada uno por revisión y 0..N revisiones por versión. | `numero_revision`, `fecha_calculo`, `estado_revision`, `fecha_revision`, `motivo`. Única por resultado y número. Conserva la edad aplicada y el criterio normativo elegido cuando corresponda. En Weill se requieren referencias inmutables a las versiones de clave y baremo utilizadas. DOC-42 fijará sus FK. Mantener nulas las dos FK exclusivas de BFA para Weill. |
| `puntaje_subprueba` · `id_puntaje_subprueba` | `id_revision_resultado` → `revision_resultado.id_revision_resultado`; `id_evaluacion_subprueba` → `evaluacion_subprueba.id_evaluacion_subprueba`. Revisión y aplicación son N:M mediante esta tabla. | Única por revisión y aplicación. `puntaje_bruto`, `estado_calculo` y conteos aplicables de aciertos, errores, omisiones o palabras. Conserva el cálculo de la ejecución. En Weill cabe un solo registro por revisión; los parciales por lámina van en `resultado_escala`. ENT-BFA-09. |
| `resultado_escala` · `id_resultado_escala` | `id_revision_resultado` → `revision_resultado.id_revision_resultado`; `id_escala` → `escala.id_escala`. Revisión y escala son N:M mediante esta tabla. | Única por revisión y escala. `puntaje_bruto`, `estado_calculo`, `estado_normativo`, `motivo`. Un valor ausente se distingue de cero. Permite conservar factores BFA, subtotales por lámina y el total Weill. ENT-BFA-10 y DOC-09 v02 §8.3. |
| `conversion_resultado_escala` · `id_conversion_resultado_escala` | `id_resultado_escala` → `resultado_escala.id_resultado_escala` (1:N). Para el percentil BFA, `id_rango` → `rango_baremo_bfa.id_rango` (cada salida usa un rango, cada rango respalda 0..N salidas). Una FK opcional `id_conversion_origen` a la PK de esta tabla identifica la salida anterior, que admite 0..N derivadas. | `tipo_salida`, `puntaje_convertido` numérico o `valor_categoria` según tipo, y referencia de fuente versionada. Única por resultado de escala y tipo de salida. Para Weill, la fila normativa debe ser identificable en su fuente propia; DOC-42 decidirá su FK. Esta tabla persiste valores obtenidos, no almacena la norma ni sustituye sus equivalencias. |

**4. Weill Forma A: una ejecución y seis salidas**

| Código en el contrato E9 | Fila por revisión | Valor, estado y uso |
|---|---|---|
| `WEI-L1`, `WEI-L2`, `WEI-L3`, `WEI-L4`, `WEI-L5` | Una fila de `resultado_escala` por lámina, cinco en total al completar el cálculo. | Subtotal de aciertos de esa lámina (0–12). `estado_normativo = NO_APLICA`; percentil y rango ausentes. Son parciales técnicos. La colección `factores` del contrato E9 los transporta sin atribuirles significado factorial. |
| `WEI` | Una fila de `resultado_escala` por total. | Puntaje bruto de 0–60, suma de los cinco subtotales. Es la única salida Weill que admite conversión normativa. Con una fila válida: `CONVERTIDO` y procedencia. Sin coincidencia: `SIN_COINCIDENCIA`. Sin baremo o grupo validado: `SIN_BAREMO_VALIDADO`. Conservar siempre el motivo cuando falte la conversión. |

Incorrecta u omitida aporta cero según la Nota. Una clave ausente o una lámina todavía sin calcular deja pendiente el valor afectado. Las láminas no crean nuevas aplicaciones de subprueba. Los valores convertidos de interés son percentil, CI y, con una clasificación autorizada, categoría. El contrato DOC-09 v02 §8.3 aún carece de campos para CI y categoría (B-5). El eneatipo solo procede si una norma del instrumento documenta tal conversión.

**5. Selección del baremo por población**

El texto descriptivo conserva la procedencia. La selección automática de una columna normativa Weill necesita además los siguientes atributos tipados por criterio y versión del baremo. Son requisitos de datos para DOC-42. No son columnas existentes del PR #69.

| Atributo propuesto | Tipo y presencia | Regla de selección |
|---|---|---|
| `edad_minima`, `edad_maxima` | Enteros en años cumplidos. Ambos obligatorios para un criterio por edad. | Extremos inclusivos y mínimo ≤ máximo. Conservar cada columna de edad documentada de 7 a 16 años. Probar la edad en la fecha de la evaluación; un rango que cubra varias edades requiere respaldo de la fuente. |
| `grupo` | Código opcional para criterio etario; obligatorio para una columna de grupo identificado. Dominio documentado: `AN`, `AD`, `UN`. | Conservar el grupo explícito y su evidencia de selección. El esquema admite los criterios por edad o por grupo que consten en la fuente. No deducir el grupo a partir del sexo ni fijar una prioridad entre grupos por conveniencia. |
| `criterio_poblacion` | Texto de la fuente, complementario. | Registrar junto con la versión y el localizador de la columna. Una revisión conserva la edad empleada y la columna seleccionada, o el motivo de la ausencia. |

Para mayores de 16 años la elección entre AD y UN sigue abierta en **H-06**. Mientras no se pueda justificar un único grupo, se conserva el bruto con `SIN_BAREMO_VALIDADO`, sin percentil ni CI. Un bruto fuera de la tabla queda con `SIN_COINCIDENCIA` (H-04). La discrepancia sobre la lista de categorías sigue en H-02.

**6. Revisión, publicación e integridad**

1. La ejecución que origina un puntaje pertenece a la evaluación del resultado. Regla y clave se aplican a su versión de formulario. Comprobar que escala, norma y población corresponden al test y que el rango BFA pertenece al baremo seleccionado y al código de factor correcto.
2. Mantener reglas, claves, baremos y localizadores por versión. Preservar la celda BFA sin dato y comprobar que los intervalos válidos del mismo factor y baremo no se solapan. Para Weill se conserva la tabla tal como la documenta la fuente. Sin correspondencia validada se guarda el motivo y se omite la conversión.
3. Un recálculo crea otra revisión del mismo `resultado` y mantiene la publicación vigente. Para publicar, la cuenta autorizada valida una revisión completa del propio resultado. Cambiar juntos `resultado.referencia_revision_vigente` y `resultado.estado` a la nueva referencia y a `DISPONIBLE`, respectivamente, y registrar el evento en la misma transacción. No escoger la revisión por el mayor identificador.
4. `evento_auditoria` registra `actor_ref`, `accion`, `entidad_tipo`, `entidad_id` y `ocurrido_en`, con la evaluación y la revisión afectadas. El flujo de acceso común gobierna la lectura. E9 entrega la revisión publicada y conserva la distinción entre pendiente de validación y disponible. Antes de publicar, DOC-09 v02 §8.3 deja vacía la colección de salidas.

**7. Casos de aceptación** · Escenarios ficticios de diseño para revisión o pruebas posteriores.

| Caso | Resultado esperado |
|---|---|
| Una evaluación Weill, una ejecución, cinco láminas con subtotales ficticios 1, 2, 3, 4 y 5. | Un puntaje por ejecución, cinco salidas `WEI-L1`…`WEI-L5` con `NO_APLICA` y un total `WEI` de 15. Los subtotales no tienen conversiones. |
| Omitir todos los ítems con una clave válida. Comparar con la misma ejecución sin clave validada. | En el primer caso el bruto es cero. En el segundo queda sin calcular y con motivo. |
| Probar cumpleaños que cambian el tramo etario, una edad sin columna y un mayor de 16 años con AD/UN sin resolver. | Se usa la edad cumplida el día de aplicación. Si falta criterio único se conserva el bruto, con `SIN_BAREMO_VALIDADO` y sin conversión. |
| Probar un límite BFA, una celda `SIN_DATO` y un bruto Weill fuera de las filas publicadas. | El límite BFA corresponde a una sola fila válida. La celda ausente no respalda valores. El total Weill conserva el bruto con `SIN_COINCIDENCIA`. |
| Repetir una pareja revisión/escala o usar una ejecución, clave o fila normativa ajenas. | Se rechaza la duplicación o la referencia incompatible. Ninguna revisión Weill admite referencias BFA. |
| Publicar una revisión validada, recalcular y consultar E9 antes de validar el recálculo. | E9 sigue mostrando la revisión publicada. La nueva se conserva por separado. Al sustituirla queda un evento con actor, fecha y revisión. |

**8. Decisiones e incógnitas**

| Identificador | Estado al 24/11/2026 | Evidencia o siguiente decisión |
|---|---|---|
| I-34-01 · Nombres y núcleo | Cerrada | DOC-31, PR #97 y DDL de #50/#69 consultados. Se mantienen las cuatro tablas BFA y los nombres físicos de sus PK/FK. |
| I-34-02 · Estructura y salidas Weill | Cerrada en alcance estructural | DOC-36 y DOC-09 v02 §8.3 fijan una ejecución, cinco subtotales técnicos y un total. H-02, H-04 y H-06 continúan abiertos en el registro de DOC-36. |
| I-34-03 · Permisos de publicación y sustitución | Parcial | Usar el flujo común y la validación profesional descritos en DOC-36 y DOC-06 v03. Iván e Integración deben confirmar la matriz operativa de permisos de sustitución. |
| I-34-04 · Claves y normas Weill | Abierta para DOC-42 | Definir dónde persistir versiones, criterios etarios y de grupo, filas y equivalencias de Weill, y cómo referenciar una o más fuentes versionadas por revisión. Mantener las FK BFA restringidas a BFA. |
| H-02, H-04, H-06 y trazabilidad | Abiertas en DOC-36 | Psicología y DOC-01 deben resolver categorías, casos fuera de rango, AD/UN y procedencia/aprobación de F-14/F-15. La aceptación de la estructura no habilita una norma psicométrica. |

**Fuentes**

- [DOC-31 · Núcleo y glosario en main](https://github.com/Nell16/sistema-evaluaciones/blob/main/docs/modelo-datos/DOC-03-MODELO.md) · [PR #97](https://github.com/Nell16/sistema-evaluaciones/pull/97) · [PR #50 · Claves BFA](https://github.com/Nell16/sistema-evaluaciones/pull/50) · [PR #69 · Baremos BFA](https://github.com/Nell16/sistema-evaluaciones/pull/69).
- [Documento BFA, §4 · ENT-BFA-02 a ENT-BFA-10](https://docs.google.com/document/d/12BEna1eJiSFJ6_1Re0qVObeJMKXZCOOTQ866DY678PA/edit).
- [DOC-36 · Nota de estructura Weill](https://docs.google.com/document/d/1qOxL7Hdxi-4w1GK7p-lk76AuhCzE46A-N3l8f7EbhaU/edit) · [Registro de incógnitas DOC-36](https://docs.google.com/spreadsheets/d/18CnsGf7UCwBQQSUAnZQx8KXE_rXq8Ml1lHpg_VkqV30/edit).
- [S02-WEI-DOC-06 v03, §§2–4 y 8 · Borrador técnico](https://docs.google.com/document/d/1Ts_eYyL3TyeuNZngVHXyI2OAa9MRiKVvNUs-Kz3kbPk/edit) · [S02-WEI-DOC-09 v02, §8.3 · Contrato de resultados E9](https://docs.google.com/document/d/1ibpisceSEzyksY-psPy6g2zhRJIn_VdJQb4MrYAy2Yo/edit).

## Thurstone, Allport y Orientación vocacional
