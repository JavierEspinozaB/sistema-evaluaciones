package com.universidad.tests.domain.exception;

public class SubpruebaFueraDeOrdenException extends RuntimeException {
    private final String codigo;

    public SubpruebaFueraDeOrdenException(String codigo, String message) {
        super(message);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}

