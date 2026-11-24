package com.universidad.tests.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.universidad.tests.dto.request.InicioSesionRequest;
import com.universidad.tests.dto.request.RegistroRequest;
import com.universidad.tests.dto.response.CuentaResponse;
import com.universidad.tests.dto.response.SesionResponse;
import jakarta.validation.Validation;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class AccesoDtoTest {

    private final ObjectMapper json =
            JsonMapper.builder()
                    .addModule(new JavaTimeModule())
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                    .build();

    @Test
    void registro_leeCamposExactosYOcultaLaContrasenaAlSerializar() throws Exception {
        var request =
                json.readValue(
                        """
                {"correo":"persona@example.invalid","contrasena":" clave-ficticia ",
                 "nombres":"Persona","apellidos":"Sintetica","rol":"SUJETO"}
                """,
                        RegistroRequest.class);

        assertThat(request.correo()).isEqualTo("persona@example.invalid");
        assertThat(request.contrasena()).isEqualTo(" clave-ficticia ");
        assertThat(request.nombres()).isEqualTo("Persona");
        assertThat(request.apellidos()).isEqualTo("Sintetica");
        assertThat(request.rol()).isEqualTo("SUJETO");
        assertThat(json.readTree(json.writeValueAsString(request)).has("contrasena")).isFalse();
        assertThat(request.toString()).doesNotContain(request.contrasena(), request.correo());
    }

    @Test
    void inicioSesion_leeLaCredencialSinNormalizarlaYLaProtegeEnSalidas() throws Exception {
        var request =
                json.readValue(
                        """
                {"correo":"persona@example.invalid","contrasena":"ñ-漢字-ficticia "}
                """,
                        InicioSesionRequest.class);

        assertThat(request.contrasena()).isEqualTo("ñ-漢字-ficticia ");
        assertThat(json.readTree(json.writeValueAsString(request)).has("contrasena")).isFalse();
        assertThat(request.toString()).doesNotContain(request.contrasena(), request.correo());
    }

    @Test
    void solicitudes_camposObligatoriosAusentes_producenViolacionesDeValidacion() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            var registro = validator.validate(new RegistroRequest(null, null, null, null, null));
            assertThat(registro)
                    .extracting(error -> error.getPropertyPath().toString())
                    .contains("correo", "contrasena", "nombres", "apellidos", "rol");
            var sesion = validator.validate(new InicioSesionRequest(null, null));
            assertThat(sesion)
                    .extracting(error -> error.getPropertyPath().toString())
                    .contains("correo", "contrasena");
        }
    }

    @Test
    void solicitudes_correoInvalido_noSuperaValidacion() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var errores =
                    factory.getValidator()
                            .validate(new InicioSesionRequest("sin-arroba", "ficticia"));
            assertThat(errores)
                    .extracting(error -> error.getPropertyPath().toString())
                    .containsOnly("correo");
        }
    }

    @Test
    void solicitudes_conDatosSinteticos_conservanElContratoValido() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            assertThat(
                            validator.validate(
                                    new RegistroRequest(
                                            "persona@example.invalid",
                                            "ficticia",
                                            "Persona",
                                            "Sintetica",
                                            "SUJETO")))
                    .isEmpty();
            assertThat(
                            validator.validate(
                                    new InicioSesionRequest("persona@example.invalid", "ficticia")))
                    .isEmpty();
        }
    }

    @Test
    void cuentaResponse_tieneSoloLosCamposPublicosDelContrato() throws Exception {
        var nodo =
                json.readTree(
                        json.writeValueAsString(
                                new CuentaResponse(
                                        7L, "persona@example.invalid", "SUJETO", "ACTIVA")));
        assertThat(nodo.size()).isEqualTo(4);
        assertThat(nodo.get("id").longValue()).isEqualTo(7L);
        assertThat(nodo.get("correo").textValue()).isEqualTo("persona@example.invalid");
        assertThat(nodo.get("rol").textValue()).isEqualTo("SUJETO");
        assertThat(nodo.get("estado").textValue()).isEqualTo("ACTIVA");
        assertThat(nodo.has("contrasena")).isFalse();
    }

    @Test
    void sesionResponse_exponeTokenFechaUtcRolYPermisos_exactamenteComoE14() throws Exception {
        var response =
                new SesionResponse(
                        "token-ficticio",
                        Instant.parse("2026-11-23T12:00:00Z"),
                        "SUJETO",
                        List.of("PRACTICA_REALIZAR"));
        var nodo = json.readTree(json.writeValueAsString(response));

        assertThat(nodo.size()).isEqualTo(4);
        assertThat(nodo.get("token").textValue()).isEqualTo("token-ficticio");
        assertThat(nodo.get("expiraEn").textValue()).isEqualTo("2026-11-23T12:00:00Z");
        assertThat(nodo.get("rolActivo").textValue()).isEqualTo("SUJETO");
        assertThat(nodo.get("permisos").get(0).textValue()).isEqualTo("PRACTICA_REALIZAR");
        assertThat(response.toString()).doesNotContain("token-ficticio");
    }

    @Test
    void sesionResponse_copiaLosPermisosParaEvitarMutacionesPosteriores() {
        var permisos = new ArrayList<>(List.of("PRACTICA_REALIZAR"));
        var response = new SesionResponse("ficticio", Instant.EPOCH, "SUJETO", permisos);
        permisos.add("OTRO_PERMISO_FICTICIO");

        assertThat(response.permisos()).containsExactly("PRACTICA_REALIZAR");
        assertThrows(UnsupportedOperationException.class, () -> response.permisos().clear());
    }
}
