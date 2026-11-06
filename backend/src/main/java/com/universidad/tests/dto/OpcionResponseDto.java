package com.universidad.tests.dto;

public record OpcionResponseDto(String letra, String texto, String id, String etiqueta) {

    public OpcionResponseDto(String letra, String texto) {
        this(letra, texto, letra, letra);
    }
}
