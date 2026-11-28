# REV-S02-WEI-DOC-15 · Escenarios 4+1 y coherencia transversal

**Responsable del aporte:** Javier Josué Espinoza Blandón  
**Fecha:** 27/11/2026  
**Evidencia:** [Subtarea ClickUp](https://app.clickup.com/t/90141806675/1k5fzqazex4) y [carpeta de entrega](https://drive.google.com/drive/folders/1XY4EOjPUWazC2AoPTbcw7UQ6xth6A1Lj).  
**Dictamen individual:** Revisión pendiente de evidencia. No se localizaron editable y versión final en la carpeta consultada.

## Checklist para verificar al recibir la versión

- [ ] Vincular cada escenario a RF/CU y a las vistas lógica, procesos, desarrollo y física.
- [ ] Comprobar nombres, permisos y límites de integración para administración, aplicación y resultados.
- [ ] Registrar la evidencia por vista y distinguir el prototipo en PR de los componentes ya integrados.

**Bloqueo documental (alto):** falta identificar una revisión concreta del entregable principal con enlaces directos y control de versión. Solicitar esos insumos en la subtarea. El checklist y sus criterios quedan listos para verificar, con el dictamen final pendiente.

## Matriz de coherencia para revisar la vista de escenarios

Corte de referencia: commit `a27e684`. Se inspeccionaron [OpenAPI](https://github.com/Nell16/sistema-evaluaciones/blob/a27e684304892d642b122cc64345b630f1993b86/docs/api/openapi.yaml), [vista física DOC-14](https://github.com/Nell16/sistema-evaluaciones/blob/a27e684304892d642b122cc64345b630f1993b86/docs/arquitectura/DOC-14-VISTA-FISICA.md), [Compose](https://github.com/Nell16/sistema-evaluaciones/blob/a27e684304892d642b122cc64345b630f1993b86/deploy/compose.yaml) y [proxy Nginx](https://github.com/Nell16/sistema-evaluaciones/blob/a27e684304892d642b122cc64345b630f1993b86/deploy/nginx.conf). Se distinguió lo definido en contrato, el código visible en ese corte y lo observado en el prototipo.

| ID | Escenario y vínculo entre vistas | Evidencia y observación | Criterio de cierre |
| --- | --- | --- | --- |
| ESC-01 · Aplicación | Lógica: evaluación y subprueba. Procesos: inicio, consulta y registro. Desarrollo: ruta del prototipo y servicios comunes. | [PR #100, commit del prototipo](https://github.com/Nell16/sistema-evaluaciones/tree/2b379a551174eb2ec2fb2eca2ce02629101ed032/frontend/src/features/weill) conserva selección en memoria. [EvaluacionController](https://github.com/Nell16/sistema-evaluaciones/blob/a27e684304892d642b122cc64345b630f1993b86/backend/src/main/java/com/universidad/tests/controller/EvaluacionController.java) expone E4–E8 en el backend común. | Identificar explícitamente qué pasos ejecuta el prototipo y qué pasos requieren integrar la SPA con la API. Probar recuperación con el mismo intento antes de acreditar el escenario conectado. |
| ESC-02 · Consulta y publicación | Lógica: evaluación, resultado y revisión. Procesos: cierre, consulta y publicación. Desarrollo: E8, E9 y contrato E12. | [Migración del contenedor](https://github.com/Nell16/sistema-evaluaciones/blob/a27e684304892d642b122cc64345b630f1993b86/database/migrations/V202611251400__comun_resultado_pendiente.sql) impone un resultado por evaluación. [ResultadosController](https://github.com/Nell16/sistema-evaluaciones/blob/a27e684304892d642b122cc64345b630f1993b86/backend/src/main/java/com/universidad/tests/controller/resultados/ResultadosController.java) expone la consulta E9. OpenAPI describe E12 como una operación separada. | Trazar consulta y publicación como pasos distintos. Para publicación, exigir evidencia de implementación, autorización y revisión vigente antes de marcar el escenario completo. |
| ESC-03 · Acceso desde la SPA | Física: navegador → web → backend → base. Desarrollo: ruta SPA y operaciones de API. | Nginx redirige `/api/` a `backend:8080` y sirve la SPA con retorno a `index.html`. Compose declara web, backend y PostgreSQL. Son referencias del despliegue común. | Usar los mismos nombres en las vistas física y de desarrollo y etiquetar el prototipo de Weill como evidencia local. La prueba de despliegue requiere un entorno autorizado y un recorrido verificable. |

El aporte identifica puntos de comprobación reproducibles en fuentes versionadas para DOC-15. El cotejo del diagrama 4+1 requiere el editable y la exportación final, y confirmar los vínculos con DOC-11, DOC-12 y el modelo editable de DOC-13.
