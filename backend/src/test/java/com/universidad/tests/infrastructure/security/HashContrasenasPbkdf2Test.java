package com.universidad.tests.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class HashContrasenasPbkdf2Test {

    // Credencial artificial y vector obtenido independientemente con Python hashlib/OpenSSL.
    private static final String VECTOR =
            "pbkdf2-sha256$600000$AAECAwQFBgcICQoLDA0ODw==$"
                    + "Tt5UwqVgqtSN9Jbg220ij7fjHdnMFwVVIJvQOGPwteI=";
    private final HashContrasenasPbkdf2 hash = new HashContrasenasPbkdf2();

    @ParameterizedTest
    @ValueSource(ints = {0xd800, 0xdc00})
    void crear_sustitutoUnicodeAislado_rechazaLaCodificacion(int sustituto) {
        char[] entrada = {(char) sustituto};
        assertThrows(IllegalArgumentException.class, () -> hash.crear(entrada));
    }

    @ParameterizedTest
    @ValueSource(ints = {0xd800, 0xdc00})
    void verificar_sustitutoUnicodeAislado_noLoConfundeConInterrogacion(int sustituto) {
        String codificado = hash.crear(new char[] {'?'});
        assertFalse(hash.verificar(new char[] {(char) sustituto}, codificado));
    }

    @Test
    void verificar_parejaUnicodeValida_conservaLaCredencial() {
        char[] entrada = "clave-sintetica-\uD83D\uDE80".toCharArray();
        String codificado = hash.crear(entrada);
        assertTrue(hash.verificar(entrada, codificado));
        assertFalse(hash.verificar("clave-sintetica-?".toCharArray(), codificado));
    }

    @Test
    void verificar_vectorIndependiente_reconoceLaCredencialCorrecta() {
        assertTrue(hash.verificar("clave-sintetica-2026".toCharArray(), VECTOR));
        assertFalse(hash.verificar("otra-clave-sintetica".toCharArray(), VECTOR));
    }

    @Test
    void crear_mismaCredencial_generaSalesDiferentesYVerificables() {
        char[] credencial = "solo-para-pruebas-2026".toCharArray();
        String primero = hash.crear(credencial);
        String segundo = hash.crear(credencial);

        assertNotEquals(primero, segundo);
        assertFalse(primero.contains("solo-para-pruebas-2026"));
        assertTrue(hash.verificar(credencial, primero));
        assertTrue(hash.verificar(credencial, segundo));
    }

    @Test
    void crearYVerificar_noAlteranElArregloDelLlamador() {
        char[] credencial = "entrada-ficticia".toCharArray();
        char[] original = credencial.clone();
        String codificado = hash.crear(credencial);
        assertArrayEquals(original, credencial);
        assertTrue(hash.verificar(credencial, codificado));
        assertArrayEquals(original, credencial);
    }

    @Test
    void verificar_espaciosSignificativos_noNormalizaLaCredencial() {
        String codificado = hash.crear(" clave-ficticia ".toCharArray());
        assertFalse(hash.verificar("clave-ficticia".toCharArray(), codificado));
        assertTrue(hash.verificar(" clave-ficticia ".toCharArray(), codificado));
    }

    @Test
    void verificar_unicodeYEntradaLarga_noTruncaLaCredencial() {
        String credencial = "contraseña-ñ-漢字-".repeat(12);
        String codificado = hash.crear(credencial.toCharArray());
        assertTrue(hash.verificar(credencial.toCharArray(), codificado));
        assertFalse(hash.verificar((credencial + "x").toCharArray(), codificado));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(
            strings = {
                "texto-plano",
                "pbkdf2-sha256$1$sal$resumen",
                "{bcrypt}invalido",
                "pbkdf2-sha256$2147483647$sal$resumen",
                "pbkdf2-sha256$600000$sal$resumen$extra"
            })
    void verificar_formatoAjenoOMalformado_deniegaSinInterpretarElCoste(String codificado) {
        assertFalse(hash.verificar("ficticia".toCharArray(), codificado));
    }

    @Test
    void verificar_base64Invalida_deniega() {
        assertFalse(hash.verificar("ficticia".toCharArray(), VECTOR.replace('A', '!')));
    }

    @Test
    void verificar_resumenManipulado_deniega() {
        assertFalse(
                hash.verificar(
                        "clave-sintetica-2026".toCharArray(), VECTOR.replace("Tt5U", "AAAA")));
    }

    @Test
    void verificar_costeManipulado_deniegaSinDerivacionCostosa() {
        assertFalse(hash.verificar("ficticia".toCharArray(), VECTOR.replace("600000", "999999")));
    }

    @Test
    void verificar_formatoExcesivamenteLargo_deniega() {
        assertFalse(hash.verificar("ficticia".toCharArray(), VECTOR.repeat(100)));
    }

    @Test
    void verificar_credencialNula_deniega() {
        assertFalse(hash.verificar(null, VECTOR));
    }

    @Test
    void crear_credencialNula_rechazaSinExponerDatos() {
        assertThrows(NullPointerException.class, () -> hash.crear(null));
    }
}
