package com.universidad.tests.domain.exception;

public class AccesoEvaluacionException extends RuntimeException {
    private final boolean autenticado;

    public AccesoEvaluacionException(boolean autenticado) {
        super(
                autenticado
                        ? "No tiene permiso sobre esta evaluación."
                        : "Se requiere autenticación.");
        this.autenticado = autenticado;
    }

    public boolean isAutenticado() {
        return autenticado;
    }
}
