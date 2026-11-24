package com.universidad.tests.dto.response;

/** Salida publica E13: nunca transporta la contrasena ni su resumen. */
public record CuentaResponse(Long id, String correo, String rol, String estado) {}
