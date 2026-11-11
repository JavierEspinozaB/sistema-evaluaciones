package com.universidad.tests.domain.model;

public class Opcion {
    private String recursoUrl;

    public String getRecursoUrl() {
        return recursoUrl;
    }

    public Opcion(String id, String etiqueta, String texto, int orden, String recursoUrl) {
        this(id, etiqueta, texto, orden);
        this.recursoUrl = recursoUrl;
    }

    private final String id;
    private final String etiqueta;
    private final String texto;
    private final int orden;

    public Opcion(String id, String etiqueta, String texto, int orden) {
        this.id = id;
        this.etiqueta = etiqueta;
        this.texto = texto;
        this.orden = orden;
    }

    public String getId() {
        return id;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public String getTexto() {
        return texto;
    }

    public int getOrden() {
        return orden;
    }
}
