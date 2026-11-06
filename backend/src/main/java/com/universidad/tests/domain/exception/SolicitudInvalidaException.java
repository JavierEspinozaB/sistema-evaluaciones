package com.universidad.tests.domain.exception;

import java.util.Collections;
import java.util.List;

public class SolicitudInvalidaException extends RuntimeException {
    public record Detalle(String campo, String mensaje) {}

    private final List<Detalle> detalles;

    public SolicitudInvalidaException(String message) {
        super(message);
        this.detalles = Collections.emptyList();
    }

    public SolicitudInvalidaException(String message, String campo, String detalleMensaje) {
        super(message);
        this.detalles = List.of(new Detalle(campo, detalleMensaje));
    }

    public SolicitudInvalidaException(String message, List<Detalle> detalles) {
        super(message);
        this.detalles = detalles == null ? Collections.emptyList() : List.copyOf(detalles);
    }

    public List<Detalle> getDetalles() {
        return detalles;
    }
}
