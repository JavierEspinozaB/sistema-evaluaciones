package com.universidad.tests.domain.model;

import com.universidad.tests.domain.exception.SubpruebaCerradaException;
import java.time.Duration;
import java.time.Instant;

public class EvaluacionSubprueba {
    private final Long id;
    private final String evaluacionId;
    private final String codigo;
    private final String nombre;
    private final int orden;
    private final int duracionSegundos;
    private EstadoSubprueba estado;
    private final String instrucciones;
    private final boolean permitirRetroceso;
    private Instant fechaInicio;
    private Instant fechaFinLimite;
    private Instant fechaCierre;

    public EvaluacionSubprueba(
            Long id,
            String evaluacionId,
            String codigo,
            String nombre,
            int orden,
            int duracionSegundos,
            EstadoSubprueba estado,
            String instrucciones,
            boolean permitirRetroceso,
            Instant fechaInicio,
            Instant fechaFinLimite,
            Instant fechaCierre) {
        this.id = id;
        this.evaluacionId = evaluacionId;
        this.codigo = codigo;
        this.nombre = nombre;
        this.orden = orden;
        this.duracionSegundos = duracionSegundos;
        this.estado = estado == null ? EstadoSubprueba.PENDIENTE : estado;
        this.instrucciones = instrucciones;
        this.permitirRetroceso = permitirRetroceso;
        this.fechaInicio = fechaInicio;
        this.fechaFinLimite = fechaFinLimite;
        this.fechaCierre = fechaCierre;
    }

    public void iniciar(Instant ahora) {
        if (this.estado == EstadoSubprueba.CERRADA) {
            throw new SubpruebaCerradaException("La subprueba " + codigo + " ya ha sido cerrada.");
        }
        if (this.estado == EstadoSubprueba.ACTIVA) {
            return;
        }
        this.estado = EstadoSubprueba.ACTIVA;
        this.fechaInicio = ahora;
        this.fechaFinLimite = ahora.plus(Duration.ofSeconds(duracionSegundos));
    }

    public void cerrar(Instant ahora) {
        if (this.estado == EstadoSubprueba.CERRADA) {
            return;
        }
        this.estado = EstadoSubprueba.CERRADA;
        this.fechaCierre = ahora;
    }

    public boolean haExpirado(Instant ahora) {
        return fechaFinLimite != null && ahora.isAfter(fechaFinLimite);
    }

    public Long calcularSegundosRestantes(Instant ahora) {
        if (estado != EstadoSubprueba.ACTIVA || fechaFinLimite == null) {
            return null;
        }
        long restantes = Duration.between(ahora, fechaFinLimite).getSeconds();
        return Math.max(0, restantes);
    }

    public Long getId() {
        return id;
    }

    public String getEvaluacionId() {
        return evaluacionId;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public int getOrden() {
        return orden;
    }

    public int getDuracionSegundos() {
        return duracionSegundos;
    }

    public EstadoSubprueba getEstado() {
        return estado;
    }

    public String getInstrucciones() {
        return instrucciones;
    }

    public boolean isPermitirRetroceso() {
        return permitirRetroceso;
    }

    public Instant getFechaInicio() {
        return fechaInicio;
    }

    public Instant getFechaFinLimite() {
        return fechaFinLimite;
    }

    public Instant getFechaCierre() {
        return fechaCierre;
    }
}

