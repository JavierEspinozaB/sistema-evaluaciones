# GEN-03-BE — Aplicación de subpruebas y registro de respuestas

Entrega de desarrollo preparada el 2026-11-10 (Managua).
Rama: `feature/javier-espinoza-GEN-03-BE`.

## Base y alcance

Se reutilizó el clon existente y se actualizaron las referencias remotas. Esta rama parte de la corrección del PR #34 (`718cdb0`), incorpora `upstream/main` (`5093a48`) y el catálogo del PR #23 (`69c7312`) como dependencia de desarrollo. **Esto no fusiona esos PR en main ni sustituye su aprobación.**

La entrega final de esta rama deja `frontend/` igual a `upstream/main`. La pantalla y las correcciones de frontend anteriores siguen disponibles en `feature/javier-espinoza-GEN-03` y en el PR #34. La integración del frontend corresponde al PR #22 y a la decisión de Integración.

Fuentes: tarea GEN-03-BE suministrada por el responsable; `docs/api/openapi.yaml`; `docs/pruebas/GEN-03-casos-prueba-v3.md`; reglas de arquitectura ejecutables; decisión de Integración en [PR #34](https://github.com/Nell16/sistema-evaluaciones/pull/34#issuecomment-6700250546) y modelo de GEN-07-BE del [PR #23](https://github.com/Nell16/sistema-evaluaciones/pull/23).

## Implementación

E5 (`POST .../subpruebas/{codigo}/iniciar`) comienza el tiempo oficial. Consultar el estado, leer instrucciones o practicar no inicia la subprueba. Un reintento devuelve el mismo inicio y límite persistidos. Se normaliza la precisión a microsegundos, que corresponde a PostgreSQL. FV requiere una letra asignada; no se inventa ni se extrae de las instrucciones.

E6 (`GET .../preguntas`) consulta exclusivamente la configuración de la edición fijada para la evaluación. Devuelve alternativas, recursos, respuesta actual y, para FV, todas las palabras con su orden. La proyección de aplicación no contiene claves de corrección.

E7 (`PUT .../respuestas`) registra o actualiza selección, texto libre y palabras FV. Valida estado, plazo, pregunta y opción de la configuración aplicada, estructura y capacidad. Las entradas FV conservan texto y posición; no se decide validez léxica. Una pregunta no puede multiplicarse mediante `ordenEntrada`. Un lote rechazado no deja respuestas parciales.

Todas las operaciones públicas exigen autorización antes de buscar datos. Las escrituras bloquean la evaluación; el reloj se consulta después de obtener ese bloqueo. No se aceptan respuestas al llegar a `finLimite`. Los reintentos simultáneos actualizan una sola respuesta. El estado requerido para ejecutar es `EN_CURSO`.

El catálogo de aplicación usa **`pregunta`, `opcion_respuesta`, `configuracion_subprueba` y `version_formulario` de GEN-07**. Se eliminaron las entidades y tablas paralelas `pregunta_catalogo`/`opcion_pregunta`. `PreguntaAplicacion` es una proyección de lectura, no un segundo catálogo. La duración, el orden y la capacidad se leen de la configuración fijada; los valores de snapshot no sustituyen esa configuración. Las evaluaciones ya iniciadas pueden consultar su edición retirada, cuyo contenido sigue siendo inmutable.

## Cierre y solicitud al evaluador

El temporizador del servidor busca subpruebas vencidas, incluso sin actividad del navegador. Cada evaluación se procesa en su propia transacción. Los endpoints también comprueban el vencimiento y permiten pasar a la siguiente subprueba una vez cerrado el tiempo anterior.

El cierre persiste `CERRADA`, `fecha_cierre` y una fila única en `solicitud_calculo_subprueba`, dentro de la misma transacción. Repetir o ejecutar cierres concurrentes no cambia la fecha ni duplica la solicitud. Se conservan los cierres cuando E5/E6/E7 devuelven un conflicto de negocio por vencimiento.

`DespachoCalculoSubpruebaService` selecciona una estrategia `EvaluadorSubpruebaPort` por código de test. Procesa solicitudes con bloqueo `FOR UPDATE SKIP LOCKED` y solo las marca procesadas si el evaluador termina correctamente. Ante fallo, la solicitud permanece pendiente. Si no hay evaluador, no se marca como calculada ni se inventa un puntaje. El lote automático consulta solo tests con una estrategia disponible.

La clave idempotente para el evaluador es `idSubprueba`. La entrega puede repetirse después de un fallo de transacción: el evaluador debe respetar esa clave, especialmente si produce efectos fuera de la base. Se garantiza un solo cierre y una sola solicitud persistida, no una garantía ficticia de entrega externa exactamente una vez.

Configuración de operación:

```properties
motor.temporizador.habilitado=true
motor.temporizador.intervalo-ms=1000
motor.calculo.intervalo-ms=1000
```

Cada lote consulta hasta 100 elementos. En pruebas se desactiva la planificación automática para evitar carreras entre contextos; se invocan explícitamente los mismos métodos del temporizador con PostgreSQL real.

## Conexiones que deben completar los equipos dependientes

**GEN-06:** implementar `AutorizacionEvaluacionPort.exigirAcceso(evaluacionId)` con identidad autenticada, permiso y alcance sobre la evaluación. El adaptador actual deniega por defecto: sin identidad devuelve `401 NO_AUTENTICADO`; una identidad sin autorización devuelve `403 SIN_PERMISO`. No acepta encabezados inventados como identidad o permiso. El perfil `seguridad-integrada` requiere que el adaptador real exista; no habilita acceso por sí solo. Las pruebas positivas sustituyen este puerto exclusivamente dentro de los tests. **No se declara implementada la autenticación ni el control de roles de GEN-06.**

**GEN-02/E3:** crear las evaluaciones con estado `EN_CURSO`, `test_id`, `version_formulario_id` correspondientes al catálogo y `evaluacion_subprueba.configuracion_id` fijada a esa edición. Incluir únicamente las subpruebas seleccionadas, con su código canónico; FV debe tener `letra_inicial`. El motor comprueba la correspondencia entre test, versión, configuración y código. No se añadió un E3 paralelo a la tarea responsable.

**Evaluador/GEN-04 y módulo del test:** proporcionar una estrategia `EvaluadorSubpruebaPort` idempotente por aplicación de subprueba, con las reglas aprobadas. Esta tarea solicita su cálculo y conserva los reintentos; no implementa fórmulas de calificación, publicación de resultados ni finalización global de evaluación.

## Base de datos

- La migración del catálogo es la del PR #23, conservada sin cambios.
- `V202611060553__comun_motor_subpruebas.sql` se adapta para referenciar ese catálogo y deja de crearlo en paralelo, como pidió Integración. Esta migración **no está fusionada en main**: es una corrección del esquema de desarrollo del PR #34.
- Se mantiene la corrección de persistencia FV y se añade `V202611110001__comun_ejecucion_subpruebas.sql` para las solicitudes de cálculo, referencias de respuestas, unicidad de subprueba activa y coherencia de tiempos/estados.
- Quien haya aplicado la versión anterior del esquema del PR #34 debe usar **una base de pruebas nueva y desechable** para esta entrega. No se ejecutó `flyway repair`, no se alteró la base del usuario y no se propone sobrescribir migraciones ya desplegadas. Un despliegue que tuviera ese esquema necesitaría una migración de datos acordada con Integración.

## Verificación reproducible

Entorno: PostgreSQL **17.11** en contenedor aislado, puerto local `55435`; JDK **17.0.19**, Maven **3.9.9**. Solo datos sintéticos. Los catálogos ficticios se construyen en borrador y se publican mediante las reglas de GEN-07.

En esta máquina, OneDrive bloqueó la eliminación de algunos directorios de `backend/target` durante `clean`. La verificación final se ejecutó sobre una copia de las fuentes y migraciones en una carpeta temporal fuera de OneDrive, con una salida de compilación nueva. Se comprobó que los archivos de código, recursos, POM y migraciones coincidieran con los del repositorio. Véase [resumen de la ejecución con Java 17](GEN-03-BE-java17-resultados.txt).

```powershell
docker run --name gen03-be-postgres-20261110 `
  -e POSTGRES_DB=evaluaciones -e POSTGRES_USER=evaluaciones `
  -e POSTGRES_PASSWORD=gen03_local_test -p 127.0.0.1:55435:5432 `
  -d postgres:17-alpine
$env:POSTGRES_PORT='55435'
$env:POSTGRES_PASSWORD='gen03_local_test'
# Usar JAVA_HOME apuntando a un JDK 17 instalado.
mvn -f backend/pom.xml clean verify
```

La contraseña mostrada es únicamente para la base desechable del ejemplo. El contenedor se detiene después de verificar. Para repetir localmente: `docker start gen03-be-postgres-20261110`.

Resultados: **69 pruebas, 0 fallos, 0 errores, 0 omitidas**. `BUILD SUCCESS`, incluida la revisión Spotless y las reglas ArchUnit. Se conservan las pruebas de catálogo de GEN-07.

Cobertura nueva y ampliada:

- Inicio único, precisión persistida y lectura de configuración versionada.
- Preguntas de otra edición/subprueba, recursos y ausencia de claves.
- Tres guardados concurrentes, actualización de selección, texto libre y límites FV.
- Rechazo de lotes inválidos sin escrituras parciales; errores 400/401/403/404/409/422.
- Evaluación finalizada, orden y otra subprueba activa.
- Cierre sin peticiones del cliente, cierre por E5/E6/E7 y tres cierres concurrentes.
- Una solicitud de cálculo por cierre; evaluador ausente, fallo, reintento y procesamiento único tras éxito.
- Adaptador real de denegación por defecto: sesión ausente, identidad sin permiso y encabezados falsos.

## Estado para revisión

La implementación y las pruebas locales están preparadas; esta evidencia **no equivale a `testing-ok` ni a cierre de la tarea integrada**. Quedan la fusión previa de GEN-07, la conexión real de GEN-02 y GEN-06, la estrategia de cálculo correspondiente y la revisión de Integración/Testing sobre el SHA que se publique. Los casos de autorización con roles y alcance reales requieren GEN-06. Las consultas documentales C-6/C-7 siguen perteneciendo a Documentación y Testing.

La rama y los cambios nuevos no se publicaron en GitHub como parte de esta entrega.
