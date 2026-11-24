package com.universidad.tests.infrastructure.security;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Hash y comprobacion de credenciales con PBKDF2-HMAC-SHA256 de Java 17.
 *
 * <p>Usa sal aleatoria de 128 bits, 600000 iteraciones y un resumen de 256 bits. El formato interno
 * identifica el algoritmo y su coste; no es un contrato HTTP ni una politica de contrasenas. No
 * autentica cuentas, no decide roles y no persiste ni registra credenciales. El llamador conserva
 * la propiedad del arreglo de entrada y debe borrarlo cuando termine de usarlo.
 */
public final class HashContrasenasPbkdf2 {

    private static final int ITERACIONES = 600_000;
    private static final int BYTES_SAL = 16;
    private static final int BYTES_RESUMEN = 32;
    private static final String PREFIJO = "pbkdf2-sha256$600000$";
    private static final int LONGITUD_CODIFICADA = PREFIJO.length() + 24 + 1 + 44;
    private final SecureRandom aleatorio = new SecureRandom();

    public String crear(char[] contrasena) {
        Objects.requireNonNull(contrasena, "La credencial es obligatoria");
        if (!tieneCodificacionValida(contrasena)) {
            throw new IllegalArgumentException("La codificacion de la credencial no es valida");
        }
        byte[] sal = new byte[BYTES_SAL];
        aleatorio.nextBytes(sal);
        byte[] resumen = derivar(contrasena, sal);
        try {
            return PREFIJO
                    + Base64.getEncoder().encodeToString(sal)
                    + "$"
                    + Base64.getEncoder().encodeToString(resumen);
        } finally {
            Arrays.fill(resumen, (byte) 0);
        }
    }

    public boolean verificar(char[] contrasena, String codificado) {
        if (contrasena == null
                || !tieneCodificacionValida(contrasena)
                || codificado == null
                || codificado.length() != LONGITUD_CODIFICADA
                || !codificado.startsWith(PREFIJO)) {
            return false;
        }
        String[] partes = codificado.substring(PREFIJO.length()).split("\\$", -1);
        if (partes.length != 2) {
            return false;
        }
        byte[] sal;
        byte[] esperado;
        try {
            sal = Base64.getDecoder().decode(partes[0]);
            esperado = Base64.getDecoder().decode(partes[1]);
        } catch (IllegalArgumentException formatoInvalido) {
            return false;
        }
        if (sal.length != BYTES_SAL || esperado.length != BYTES_RESUMEN) {
            return false;
        }
        byte[] obtenido = derivar(contrasena, sal);
        try {
            return MessageDigest.isEqual(esperado, obtenido);
        } finally {
            Arrays.fill(obtenido, (byte) 0);
            Arrays.fill(esperado, (byte) 0);
        }
    }

    private boolean tieneCodificacionValida(char[] contrasena) {
        for (int indice = 0; indice < contrasena.length; indice++) {
            if (Character.isHighSurrogate(contrasena[indice])) {
                if (++indice == contrasena.length
                        || !Character.isLowSurrogate(contrasena[indice])) {
                    return false;
                }
            } else if (Character.isLowSurrogate(contrasena[indice])) {
                return false;
            }
        }
        return true;
    }

    private byte[] derivar(char[] contrasena, byte[] sal) {
        PBEKeySpec especificacion = new PBEKeySpec(contrasena, sal, ITERACIONES, BYTES_RESUMEN * 8);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(especificacion)
                    .getEncoded();
        } catch (GeneralSecurityException algoritmoNoDisponible) {
            throw new IllegalStateException(
                    "No esta disponible el hash de credenciales", algoritmoNoDisponible);
        } finally {
            especificacion.clearPassword();
        }
    }
}
