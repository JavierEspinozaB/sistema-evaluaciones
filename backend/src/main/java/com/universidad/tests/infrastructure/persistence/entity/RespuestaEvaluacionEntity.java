package com.universidad.tests.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "respuesta_evaluacion")
public class RespuestaEvaluacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "evaluacion_id", nullable = false, length = 50)
    private String evaluacionId;

    @Column(name = "subprueba_codigo", nullable = false, length = 50)
    private String subpruebaCodigo;

    @Column(name = "pregunta_id", nullable = false, length = 50)
    private String preguntaId;

    @Column(name = "opcion_id", length = 50)
    private String opcionId;

    @Column(name = "valor_texto", columnDefinition = "TEXT")
    private String valorTexto;

    @Column(name = "orden_entrada", nullable = false)
    private int ordenEntrada;

    @Column(name = "fecha_registro", nullable = false)
    private Instant fechaRegistro;

    public RespuestaEvaluacionEntity() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEvaluacionId() {
        return evaluacionId;
    }

    public void setEvaluacionId(String evaluacionId) {
        this.evaluacionId = evaluacionId;
    }

    public String getSubpruebaCodigo() {
        return subpruebaCodigo;
    }

    public void setSubpruebaCodigo(String subpruebaCodigo) {
        this.subpruebaCodigo = subpruebaCodigo;
    }

    public String getPreguntaId() {
        return preguntaId;
    }

    public void setPreguntaId(String preguntaId) {
        this.preguntaId = preguntaId;
    }

    public String getOpcionId() {
        return opcionId;
    }

    public void setOpcionId(String opcionId) {
        this.opcionId = opcionId;
    }

    public String getValorTexto() {
        return valorTexto;
    }

    public void setValorTexto(String valorTexto) {
        this.valorTexto = valorTexto;
    }

    public int getOrdenEntrada() {
        return ordenEntrada;
    }

    public void setOrdenEntrada(int ordenEntrada) {
        this.ordenEntrada = ordenEntrada;
    }

    public Instant getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Instant fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}

