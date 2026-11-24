package com.universidad.tests.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Primitiva para tokens opacos: 256 bits aleatorios y resumen SHA-256 para su verificacion.
 *
 * <p>No incorpora identidad, roles ni datos personales al token. El consumidor debera comprobar
 * unicidad al persistir, almacenar solo el resumen y gestionar vigencia/revocacion segun el flujo
 * aprobado. Esta clase no crea sesiones ni autoriza solicitudes por si sola.
 */
public final class TokensSesionSeguros {

    private static final int BYTES_TOKEN = 32;
    private static final int LONGITUD_TOKEN = 43;
    private final SecureRandom aleatorio = new SecureRandom();

    public String generar() {
        byte[] bytes = new byte[BYTES_TOKEN];
        aleatorio.nextBytes(bytes);
        try {
            return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        } finally {
            Arrays.fill(bytes, (byte) 0);
        }
    }

    public String resumir(String token) {
        if (!tieneFormatoValido(token)) {
            throw new IllegalArgumentException("El formato del token no es valido");
        }
        byte[] resumen = derivar(token);
        try {
            return HexFormat.of().formatHex(resumen);
        } finally {
            Arrays.fill(resumen, (byte) 0);
        }
    }

    public boolean verificar(String token, String resumen) {
        if (!tieneFormatoValido(token) || resumen == null || resumen.length() != 64) {
            return false;
        }
        byte[] esperado;
        try {
            esperado = HexFormat.of().parseHex(resumen);
        } catch (IllegalArgumentException formatoInvalido) {
            return false;
        }
        byte[] obtenido = derivar(token);
        try {
            return MessageDigest.isEqual(esperado, obtenido);
        } finally {
            Arrays.fill(esperado, (byte) 0);
            Arrays.fill(obtenido, (byte) 0);
        }
    }

    private boolean tieneFormatoValido(String token) {
        if (token == null || token.length() != LONGITUD_TOKEN) {
            return false;
        }
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(token);
            try {
                return bytes.length == BYTES_TOKEN
                        && Base64.getUrlEncoder()
                                .withoutPadding()
                                .encodeToString(bytes)
                                .equals(token);
            } finally {
                Arrays.fill(bytes, (byte) 0);
            }
        } catch (IllegalArgumentException formatoInvalido) {
            return false;
        }
    }

    private byte[] derivar(String token) {
        try {
            return MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.US_ASCII));
        } catch (NoSuchAlgorithmException algoritmoNoDisponible) {
            throw new IllegalStateException(
                    "No esta disponible el resumen de tokens", algoritmoNoDisponible);
        }
    }
}
