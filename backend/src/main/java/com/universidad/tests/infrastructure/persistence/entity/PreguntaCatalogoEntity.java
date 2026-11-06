package com.universidad.tests.infrastructure.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pregunta_catalogo")
public class PreguntaCatalogoEntity {

    @Id
    @Column(name = "id", length = 50)
    private String id;

    @Column(name = "subprueba_codigo", nullable = false, length = 50)
    private String subpruebaCodigo;

    @Column(name = "numero", nullable = false)
    private int numero;

    @Column(name = "enunciado", nullable = false, columnDefinition = "TEXT")
    private String enunciado;

    @Column(name = "recurso_multimedia", columnDefinition = "TEXT")
    private String recursoMultimedia;

    @Column(name = "tipo_respuesta", nullable = false, length = 30)
    private String tipoRespuesta;

    @Column(name = "clave_correcta")
    private String claveCorrecta;

    @Column(name = "orden", nullable = false)
    private int orden;

    @OneToMany(mappedBy = "pregunta", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden ASC")
    private List<OpcionPreguntaEntity> opciones = new ArrayList<>();

    public PreguntaCatalogoEntity() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSubpruebaCodigo() {
        return subpruebaCodigo;
    }

    public void setSubpruebaCodigo(String subpruebaCodigo) {
        this.subpruebaCodigo = subpruebaCodigo;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public String getEnunciado() {
        return enunciado;
    }

    public void setEnunciado(String enunciado) {
        this.enunciado = enunciado;
    }

    public String getRecursoMultimedia() {
        return recursoMultimedia;
    }

    public void setRecursoMultimedia(String recursoMultimedia) {
        this.recursoMultimedia = recursoMultimedia;
    }

    public String getTipoRespuesta() {
        return tipoRespuesta;
    }

    public void setTipoRespuesta(String tipoRespuesta) {
        this.tipoRespuesta = tipoRespuesta;
    }

    public String getClaveCorrecta() {
        return claveCorrecta;
    }

    public void setClaveCorrecta(String claveCorrecta) {
        this.claveCorrecta = claveCorrecta;
    }

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }

    public List<OpcionPreguntaEntity> getOpciones() {
        return opciones;
    }

    public void setOpciones(List<OpcionPreguntaEntity> opciones) {
        this.opciones = opciones;
    }
}

