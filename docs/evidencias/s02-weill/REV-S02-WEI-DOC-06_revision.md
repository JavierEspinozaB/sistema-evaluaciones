# REV-S02-WEI-DOC-06 · Revisión individual

**Responsable del aporte:** Javier Josué Espinoza Blandón  
**Fecha:** 27/11/2026  
**Evidencia:** [DOC-06 v03, secciones 4, 8–10](https://docs.google.com/document/d/1Ts_eYyL3TyeuNZngVHXyI2OAa9MRiKVvNUs-Kz3kbPk/edit?tab=t.0) · [ClickUp](https://app.clickup.com/t/90141806675/1k5fzqazewv).  
**Dictamen individual:** Con observaciones; usar como borrador.

| ID | Nivel | Hallazgo | Criterio de cierre |
| --- | --- | --- | --- |
| CAL-01 | Alta | El ejemplo A de §10 asigna P50 para PD=36 mediante el intervalo 33≤36<42. El paso 3 de §8 permite conversión solo bajo una regla de lectura validada. | Declarar el intervalo como hipótesis del ejemplo ficticio o reemplazarlo por coincidencia exacta sintética; conservar un caso sin conversión. |
| CAL-02 | Alta | §8 usa E12 para consultar. En el contrato común E9 consulta resultados y E12 publica la revisión vigente. | Corregir narrativa y ejemplos. Contrastar con DOC-09 y OpenAPI. |
| CAL-03 | Alta | §8 mezcla identificadores y estados de BFA con propuestas de Weill. H-02, H-04 y H-06 siguen sin criterio aplicado. | Etiquetar las referencias al módulo común y separar la ausencia de grupo, la ausencia de coincidencia y la validación pendiente. Conservar el puntaje directo cuando proceda, sin extrapolar. |

**Checklist:** cotejados requisitos y referencias de la v03, consistencia interna y un ejemplo ficticio. Pendientes la validación de las conversiones, un PDF final concordante y la comprobación de correcciones. Derivar CAL-01/03 a validación funcional y CAL-02 a revisión de contrato.
