package com.universidad.tests.infrastructure.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "evaluacion")
public class EvaluacionEntity {

    @Id
    @Column(name = "id", length = 50)
    private String id;

    @Column(name = "sujeto_id", nullable = false, length = 50)
    private String sujetoId;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn;

    @OneToMany(mappedBy = "evaluacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden ASC")
    private List<EvaluacionSubpruebaEntity> subpruebas = new ArrayList<>();

    @Column(name = "test_id")
    private String testId;

    @Column(name = "periodo_id")
    private String periodoId;

    @Column(name = "version_formulario_id")
    private String versionFormularioId;

    @Column(name = "fecha_inicio")
    private Instant fechaInicio;

    @Column(name = "fecha_fin")
    private Instant fechaFin;

    public String getTestId() {
        return testId;
    }

    public void setTestId(String value) {
        testId = value;
    }

    public String getPeriodoId() {
        return periodoId;
    }

    public void setPeriodoId(String value) {
        periodoId = value;
    }

    public String getVersionFormularioId() {
        return versionFormularioId;
    }

    public void setVersionFormularioId(String value) {
        versionFormularioId = value;
    }

    public Instant getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Instant value) {
        fechaInicio = value;
    }

    public Instant getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(Instant value) {
        fechaFin = value;
    }

    public EvaluacionEntity() {}

    public EvaluacionEntity(
            String id, String sujetoId, String estado, Instant creadoEn, Instant actualizadoEn) {
        this.id = id;
        this.sujetoId = sujetoId;
        this.estado = estado;
        this.creadoEn = creadoEn;
        this.actualizadoEn = actualizadoEn;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSujetoId() {
        return sujetoId;
    }

    public void setSujetoId(String sujetoId) {
        this.sujetoId = sujetoId;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(Instant creadoEn) {
        this.creadoEn = creadoEn;
    }

    public Instant getActualizadoEn() {
        return actualizadoEn;
    }

    public void setActualizadoEn(Instant actualizadoEn) {
        this.actualizadoEn = actualizadoEn;
    }

    public List<EvaluacionSubpruebaEntity> getSubpruebas() {
        return subpruebas;
    }

    public void setSubpruebas(List<EvaluacionSubpruebaEntity> subpruebas) {
        this.subpruebas = subpruebas;
    }
}
