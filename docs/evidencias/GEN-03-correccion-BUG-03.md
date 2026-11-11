# GEN-03 — Corrección de BUG-03 y contrato de Fluidez Verbal

> Evidencia histórica de la corrección del PR #34. Para la entrega posterior de backend y la adaptación al catálogo GEN-07, consultar [GEN-03-BE-implementacion.md](GEN-03-BE-implementacion.md). Las observaciones sobre esquema y frontend de esta evidencia describen aquella versión anterior.

Fecha: 2026-11-10 (Managua). Rama: `feature/javier-espinoza-GEN-03`.
Base incorporada: `upstream/main` en `5093a48`, mediante merge local.
Fuentes: `docs/api/openapi.yaml` y `docs/pruebas/GEN-03-casos-prueba-v3.md`.

## Cambios

- FV registra `{"palabras":[{"ordenEntrada":1,"texto":"mesa"}]}` sin exigir una pregunta de catálogo. Conserva el texto original, incluyendo espacios y acentos. E6 devuelve `palabras` ordenadas y el frontend restaura cada posición, sin compactar huecos.
- E5 devuelve la letra asignada y persistida. La interfaz utiliza `letraInicial`, aunque las instrucciones mencionen otra letra. Sin letra se devuelve `409 LETRA_NO_CONFIGURADA` antes de iniciar el tiempo.
- Se valida todo el lote antes de guardar: texto no vacío, orden único dentro del lote y posiciones entre 1 y el máximo configurado (hasta 75). Los errores estructurales usan `400 SOLICITUD_INVALIDA` y `detalles` con campo y mensaje. La consulta C-7 sobre los códigos continúa abierta en los casos v3; esta es la decisión implementada y comprobada, no una resolución documental de Testing.
- Reenviar una posición actualiza esa entrada. Una transacción bloquea la evaluación antes de comprobar el reloj y escribir, para evitar duplicados y errores de carrera. Los errores de integridad ya no se ocultan dentro de una transacción fallida.
- El frontend conserva el último cambio mientras hay un guardado en curso, distingue guardando/guardado/error y permite reintentar. Vaciar una entrada previamente escrita se envía al servidor y se informa como error; el contrato vigente no define eliminación de palabras mediante texto vacío.
- E7 añade `codigo`, `registradas`, `totalRespondidas` y `segundosRestantes`, manteniendo campos anteriores por compatibilidad.
- E4 añade los campos de metadatos y `subpruebaActiva`. Los identificadores de test, periodo y formulario se recuperan de columnas persistidas; pueden ser nulos en evaluaciones antiguas. Su creación corresponde a GEN-02/E3, que no está implementado en esta rama. No se inventan esos identificadores.
- La práctica ahora muestra un ejercicio interactivo de selección o texto antes de E5. Es un ejemplo de interfaz; E24 y el contenido oficial por subprueba siguen sujetos a la consulta C-6.
- Migración nueva `V202611100001__gen03_fluidez_verbal.sql`. La migración existente permanece intacta. Se permite una respuesta FV sin pregunta y un índice único protege su posición.

## Verificación local

PostgreSQL **17** en Docker, base nueva y aislada en `127.0.0.1:55434`; sin datos de la base local del usuario. Java instalado: 25, compilación con `release 17`. El CI del repositorio usa Java 17 y deberá confirmar esa ejecución tras publicar la rama.

```powershell
docker run --name gen03-postgres-test-20261110 `
  -e POSTGRES_DB=evaluaciones -e POSTGRES_USER=evaluaciones `
  -e POSTGRES_PASSWORD=gen03_local_test -p 127.0.0.1:55434:5432 `
  -d postgres:17-alpine
$env:POSTGRES_PORT='55434'
$env:POSTGRES_PASSWORD='gen03_local_test'
mvn -f backend/pom.xml verify
```

La contraseña del ejemplo es exclusivamente para el contenedor desechable de pruebas. En esta máquina se usó Maven 3.9.9 instalado en la caché del wrapper, porque el lanzador del wrapper no pudo resolver su entorno.

```powershell
cd frontend
npm.cmd run lint
npm.cmd run format:check
npm.cmd run typecheck
npm.cmd test
npm.cmd run build
```

Resultados:

- Backend: **38 pruebas**, sin fallos ni errores; incluye arquitectura, controladores, servicios, migraciones y **7 pruebas nuevas con PostgreSQL real**.
- Frontend: **13 pruebas**, sin fallos. ESLint, Prettier, TypeScript y build correctos.
- Regresiones FV: letra asignada y ausencia de letra; E4 con metadatos; catálogo vacío; texto original y orden con huecos; edición sin sobreescritura de otras posiciones; tres reenvíos simultáneos; lote inválido sin escrituras; 75 entradas y reintentos; cierre persistido y rechazo de palabras tardías.
- Regresión temporal adicional: una respuesta que espera el bloqueo hasta el instante exacto de `finLimite` se rechaza.
- Frontend: serialización real de `palabras`, recuperación por posición, cambios rápidos durante guardado, reintento tras error y ejercicios de práctica renderizados.

## Pendientes de integración y Testing

- La comprobación visual completa en navegador **no se ejecutó**: el servicio de automatización devolvió `The browser access host is unavailable`. Las pruebas de componentes renderizan HTML; no sustituyen la repetición manual de TC-GEN-03-002 con navegador y recarga.
- No se declara ejecutada ni aprobada toda la matriz manual GEN-03 v3. Testing debe repetirla sobre el SHA que se publique.
- TC-GEN-03-022 y 024 continúan bloqueados por GEN-06. No se implementaron autenticación ni permisos como parte de esta corrección.
- Esta evidencia no constituye aprobación de Testing. La publicación de las correcciones y la solicitud de revisión se realizan por el PR #34; las etiquetas y la conformidad corresponden a Testing e Integración.
