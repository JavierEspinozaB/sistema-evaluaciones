# Contrato del motor de evaluación

GEN-03-BE define `domain.port.EvaluadorSubpruebaPort`: una estrategia por código de test recibe una `SolicitudCalculoSubprueba` y solicita el cálculo correspondiente. El dominio permanece independiente de Spring y JPA.

El cierre guarda una solicitud única en `solicitud_calculo_subprueba`. `DespachoCalculoSubpruebaService` la entrega a la estrategia del test y solo la confirma si termina sin error. Sin estrategia, la solicitud permanece pendiente. Una estrategia debe ser idempotente por `idSubprueba`, pues puede recibir un reintento después de un fallo de transacción. Los efectos externos requieren la misma clave idempotente.

Las implementaciones BFA, Weill, Thurstone, Allport y Vocacional deben usar las reglas aprobadas de DOC-02/DOC-04 y pruebas de resultados conocidos. **No crear fórmulas ni puntajes supuestos.** GEN-03-BE no sustituye las tareas de calificación ni la finalización global de GEN-04.
