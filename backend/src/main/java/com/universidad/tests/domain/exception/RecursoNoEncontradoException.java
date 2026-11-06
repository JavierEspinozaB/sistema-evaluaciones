package com.universidad.tests.domain.exception;

public class RecursoNoEncontradoException extends RuntimeException {
    private final String codigo;

    public RecursoNoEncontradoException(String codigo, String message) {
        super(message);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
