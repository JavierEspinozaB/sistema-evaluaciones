# REV-S02-WEI-DOC-12 · Procesos UML: actividades y estados

**Responsable del aporte:** Javier Josué Espinoza Blandón  
**Fecha:** 27/11/2026  
**Evidencia:** [Subtarea ClickUp](https://app.clickup.com/t/90141806675/1k5fzqazex1) y [carpeta de entrega](https://drive.google.com/drive/folders/1XY4EOjPUWazC2AoPTbcw7UQ6xth6A1Lj).  
**Dictamen individual:** Revisión pendiente de evidencia. No se localizaron editable y versión final en la carpeta consultada.

## Checklist para verificar al recibir la versión

- [ ] Contrastar transiciones de inicio, respuesta, interrupción, recuperación y cierre con DOC-04 y DOC-09.
- [ ] Distinguir ausencia de límite por defecto de un intento con límite configurado y probar rechazo tras el cierre.
- [ ] Vincular las guardas a RF y operaciones comunes. Adjuntar editable y versión final del proceso.

**Bloqueo documental (alto):** falta identificar una revisión concreta del entregable principal con enlaces directos y control de versión. Solicitar esos insumos en la subtarea. El checklist y sus criterios quedan listos para verificar, con el dictamen final pendiente.

## Cotejo de transiciones con el motor común

Base de contraste: [OpenAPI](https://github.com/Nell16/sistema-evaluaciones/blob/a27e684304892d642b122cc64345b630f1993b86/docs/api/openapi.yaml), [controlador de evaluación](https://github.com/Nell16/sistema-evaluaciones/blob/a27e684304892d642b122cc64345b630f1993b86/backend/src/main/java/com/universidad/tests/controller/EvaluacionController.java) y [evidencia local del motor del 25/11](https://github.com/Nell16/sistema-evaluaciones/blob/a27e684304892d642b122cc64345b630f1993b86/docs/evidencias/sprint03/flujo-motor-2026-11-25.md). Es una revisión de referencias del sistema común para inspectoría del diagrama de Weill. Los resultados de ejecución citados provienen del informe del motor.

| ID | Hallazgo verificable | Criterio para el diagrama y su prueba |
| --- | --- | --- |
| UML-01 · Recuperación | E4 consulta el estado y progreso para retomar una evaluación. E5 tiene una operación de inicio aparte y exige guardas de orden y actividad. | Modelar el retorno mediante E4 con el mismo identificador. Dibujar por separado el inicio E5 y el rechazo de un inicio fuera de orden. En un caso de interrupción, verificar que retomar no inicia otro reloj. |
| UML-02 · Respuesta | E7 documenta escritura idempotente mientras la subprueba está activa y devuelve 409 si está inactiva o expiró. | Agregar una guarda de subprueba activa y, cuando aplique, de plazo. Verificar que repetir una escritura válida conserva el avance y que un intento posterior al cierre no agrega respuestas. La política de límite de Weill debe trazarse a su fuente aprobada. |
| UML-03 · Cierre | E8 distingue subpruebas pendientes o activas no vencidas (409) del cierre permitido. El informe común observó el mismo resultado al repetir un cierre y un contenedor en estado pendiente. | Distinguir el estado de la evaluación del estado del resultado. Verificar 409 con una subprueba activa no vencida y un único cierre/resultado al repetir E8. Mantener la publicación como una transición posterior. |

Este cotejo se puede revisar contra los enlaces indicados. Para comprobar que DOC-12 reproduce las guardas y flujos, queda pendiente abrir su edición editable y final, y cotejar DOC-11 y las decisiones propias de Weill.
