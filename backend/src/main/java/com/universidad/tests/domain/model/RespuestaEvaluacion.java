package com.universidad.tests.domain.model;

import java.time.Instant;

public class RespuestaEvaluacion {
    private final Long id;
    private final String evaluacionId;
    private final String subpruebaCodigo;
    private final String preguntaId;
    private final String opcionId;
    private final String valorTexto;
    private final int ordenEntrada;
    private final Instant fechaRegistro;

    public RespuestaEvaluacion(
            Long id,
            String evaluacionId,
            String subpruebaCodigo,
            String preguntaId,
            String opcionId,
            String valorTexto,
            int ordenEntrada,
            Instant fechaRegistro) {
        this.id = id;
        this.evaluacionId = evaluacionId;
        this.subpruebaCodigo = subpruebaCodigo;
        this.preguntaId = preguntaId;
        this.opcionId = opcionId;
        this.valorTexto = valorTexto;
        this.ordenEntrada = ordenEntrada;
        this.fechaRegistro = fechaRegistro;
    }

    public Long getId() {
        return id;
    }

    public String getEvaluacionId() {
        return evaluacionId;
    }

    public String getSubpruebaCodigo() {
        return subpruebaCodigo;
    }

    public String getPreguntaId() {
        return preguntaId;
    }

    public String getOpcionId() {
        return opcionId;
    }

    public String getValorTexto() {
        return valorTexto;
    }

    public int getOrdenEntrada() {
        return ordenEntrada;
    }

    public Instant getFechaRegistro() {
        return fechaRegistro;
    }
}
