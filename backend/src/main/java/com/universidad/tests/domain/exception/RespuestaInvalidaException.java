package com.universidad.tests.domain.exception;

public class RespuestaInvalidaException extends RuntimeException {
    private final String campo;

    public RespuestaInvalidaException(String message) {
        super(message);
        this.campo = null;
    }

    public RespuestaInvalidaException(String campo, String message) {
        super(message);
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}
