package com.universidad.tests.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class TokensSesionSegurosTest {

    // Vector sintetico de 32 bytes cero; resumen contrastado con Python hashlib/OpenSSL.
    private static final String TOKEN = "A".repeat(43);
    private static final String RESUMEN =
            "0f007385b6f9d4b7eeb2748605afe1a984a0a3bfa3f014d09e2a784ce9e5cd1a";
    private final TokensSesionSeguros tokens = new TokensSesionSeguros();

    @Test
    void generar_entrega256BitsConCodificacionUrlSinRelleno() {
        String token = tokens.generar();
        assertEquals(43, token.length());
        assertEquals(32, Base64.getUrlDecoder().decode(token).length);
        assertTrue(token.matches("[A-Za-z0-9_-]{43}"));
        assertNotEquals(token, tokens.generar());
        assertTrue(tokens.verificar(token, tokens.resumir(token)));
    }

    @Test
    void resumir_vectorIndependiente_coincideConSha256() {
        assertEquals(RESUMEN, tokens.resumir(TOKEN));
        assertTrue(tokens.verificar(TOKEN, RESUMEN));
    }

    @Test
    void verificar_tokenDiferente_deniega() {
        assertFalse(tokens.verificar("B" + TOKEN.substring(1), RESUMEN));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"token-ficticio", "Bearer token", "a.b.c"})
    void verificar_tokenMalformado_deniega(String token) {
        assertFalse(tokens.verificar(token, RESUMEN));
        assertThrows(IllegalArgumentException.class, () -> tokens.resumir(token));
    }

    @Test
    void verificar_codificacionAlternativaDeLosMismosBytes_deniega() {
        String noCanonico = TOKEN.substring(0, 42) + "B";
        assertFalse(tokens.verificar(noCanonico, RESUMEN));
        assertThrows(IllegalArgumentException.class, () -> tokens.resumir(noCanonico));
    }

    @Test
    void verificar_tokenConCaracterAjeno_deniega() {
        assertFalse(tokens.verificar("!" + TOKEN.substring(1), RESUMEN));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(
            strings = {
                "resumen-invalido",
                "g0007385b6f9d4b7eeb2748605afe1a984a0a3bfa3f014d09e2a784ce9e5cd1a"
            })
    void verificar_resumenMalformado_deniega(String resumen) {
        assertFalse(tokens.verificar(TOKEN, resumen));
    }

    @Test
    void verificar_resumenManipulado_deniega() {
        assertFalse(tokens.verificar(TOKEN, "1" + RESUMEN.substring(1)));
    }

    @Test
    void verificar_entradaExcesivamenteLarga_deniega() {
        assertFalse(tokens.verificar(TOKEN.repeat(100), RESUMEN));
        assertFalse(tokens.verificar(TOKEN, RESUMEN.repeat(100)));
    }
}
