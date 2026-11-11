package com.universidad.tests.domain.exception;

public class LetraNoConfiguradaException extends RuntimeException {
    public LetraNoConfiguradaException() {
        super("FV requiere una letra inicial asignada antes de iniciar.");
    }
}
