# S02-WEI-DOC-10 · Mapa de navegación y validación del prototipo

**Responsable del aporte:** Javier Josué Espinoza Blandón  
**Fecha:** 27/11/2026  
**Base:** [PR #100](https://github.com/Nell16/sistema-evaluaciones/pull/100), commit `2b379a5`, [ClickUp](https://app.clickup.com/t/90141806675/1k5fzqazdpx), [editables y documentación del sprint](https://drive.google.com/drive/folders/1XY4EOjPUWazC2AoPTbcw7UQ6xth6A1Lj).

El prototipo existente cubre inicio, instrucciones, reactivo y cierre. Usa datos de demostración y conserva la selección en memoria durante la ejecución. El mapa amplía la cobertura documental con estados de administración, recuperación y resultados. Las pantallas y transiciones marcadas como propuestas son criterios para la siguiente iteración.

```mermaid
flowchart TD
  A["Administración: preparar versión"] --> B["Inicio: verificar autorización"]
  B --> C["Instrucciones y práctica"]
  C --> D["Reactivo y progreso"]
  D --> E["Confirmar cierre"]
  E --> F["Cierre y estado"]
  F --> G["Consulta autorizada"]
  B --> H["Carga, vacío o acceso denegado"]
  D --> I["Interrupción o error de guardado"]
  I --> J["Recuperar el mismo intento"]
  J --> D
```

| Pantalla | Requisitos y operaciones de referencia | Evidencia en #100 | Validación esperada |
| --- | --- | --- | --- |
| Administración | WEI-RF-001, 013 · E25–E31 | No incluida | Diferenciar versión en borrador de publicada y comunicar restricciones de edición por permisos. |
| Inicio | WEI-RF-003 · E3 | Formulario local | Mantener el foco en el primer error, avisar de campos faltantes y evitar doble envío. |
| Instrucciones y práctica | WEI-RF-002 · E24, E5 | Instrucciones de demostración | Distinguir la práctica de la evaluación. Mantener pendiente el texto autorizado. |
| Reactivo y progreso | WEI-RF-005, 007 · E6, E7 | Vista por lámina y selección local | Navegar con teclado, anunciar posición y selección, comunicar guardado y excluir la clave de la interfaz. |
| Recuperación | WEI-RF-008, 009 · E4, E7 | No incluida | Recuperar el mismo intento y sus respuestas confirmadas. Diferenciar una selección pendiente de una guardada. |
| Cierre | WEI-RF-010 · E8 | Pantalla de fin local | Confirmar el envío y mostrar un recibo de cierre. Tratar omisiones según reglas versionadas y validadas. |
| Resultados | WEI-RF-014 · E9, E12 | No incluida | Representar cálculo, validación y publicación. Aplicar permisos y alcance antes de mostrar información. |

## Escenarios para validar

| Escenario con datos sintéticos | Resultado esperado |
| --- | --- |
| Catálogo sin versión disponible o petición fallida | Estado vacío o error con acción de volver o reintentar. |
| Pérdida de conexión durante el guardado | Selección visible como pendiente. El reintento evita duplicación y recupera el estado del servidor. |
| Solicitud de cierre repetida | Una sola transición y confirmación del cierre persistido. |
| Resultado pendiente o acceso denegado | Estado explícito, sin puntajes ni información restringida. |
| Uso sin ratón o lector de pantalla | Orden y foco visibles, controles con nombres accesibles, progreso y error anunciados sin depender del color. |

**Verificación de este aporte:** inspección estática del código y contraste con DOC-09 v02 y OpenAPI. Falta ejecutar una validación funcional y de accesibilidad y contrastar los wireframes editables con la cobertura propuesta. El PR #100 conserva su propia revisión y ya tiene una observación sobre la convención del nombre de la rama.
