package com.universidad.tests.domain.model;

import java.util.Collections;
import java.util.List;

public class PreguntaAplicacion {
    private final String id;
    private final String subpruebaCodigo;
    private final int numero;
    private final String enunciado;
    private final String recursoMultimedia;
    private final TipoRespuestaAplicacion tipoRespuesta;
    private final int orden;
    private final List<Opcion> opciones;

    public PreguntaAplicacion(
            String id,
            String subpruebaCodigo,
            int numero,
            String enunciado,
            String recursoMultimedia,
            TipoRespuestaAplicacion tipoRespuesta,
            int orden,
            List<Opcion> opciones) {
        this.id = id;
        this.subpruebaCodigo = subpruebaCodigo;
        this.numero = numero;
        this.enunciado = enunciado;
        this.recursoMultimedia = recursoMultimedia;
        this.tipoRespuesta = tipoRespuesta;
        this.orden = orden;
        this.opciones = opciones == null ? Collections.emptyList() : List.copyOf(opciones);
    }

    public String getId() {
        return id;
    }

    public String getSubpruebaCodigo() {
        return subpruebaCodigo;
    }

    public int getNumero() {
        return numero;
    }

    public String getEnunciado() {
        return enunciado;
    }

    public String getRecursoMultimedia() {
        return recursoMultimedia;
    }

    public TipoRespuestaAplicacion getTipoRespuesta() {
        return tipoRespuesta;
    }

    public int getOrden() {
        return orden;
    }

    public List<Opcion> getOpciones() {
        return opciones;
    }

    public boolean tieneOpcion(String opcionId) {
        if (opcionId == null) {
            return false;
        }
        return opciones.stream().anyMatch(o -> o.getId().equals(opcionId));
    }
}
