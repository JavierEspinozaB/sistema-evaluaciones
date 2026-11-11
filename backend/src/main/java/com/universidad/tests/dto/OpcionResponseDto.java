package com.universidad.tests.dto;

public record OpcionResponseDto(
        String letra, String texto, String id, String etiqueta, String recursoUrl) {

    public OpcionResponseDto(String letra, String texto, String id, String etiqueta) {
        this(letra, texto, id, etiqueta, null);
    }

    public OpcionResponseDto(String letra, String texto) {
        this(letra, texto, letra, letra, null);
    }
}
