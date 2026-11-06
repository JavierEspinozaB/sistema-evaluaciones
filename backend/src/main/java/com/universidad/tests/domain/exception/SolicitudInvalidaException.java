package com.universidad.tests.domain.exception;

import java.util.Collections;
import java.util.List;

public class SolicitudInvalidaException extends RuntimeException {
    private final List<String> detalles;

    public SolicitudInvalidaException(String message) {
        super(message);
        this.detalles = Collections.emptyList();
    }

    public SolicitudInvalidaException(String message, List<String> detalles) {
        super(message);
        this.detalles = detalles == null ? Collections.emptyList() : List.copyOf(detalles);
    }

    public List<String> getDetalles() {
        return detalles;
    }
}

