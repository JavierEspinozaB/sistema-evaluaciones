package com.universidad.tests.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Entrada E13 conforme a RegistroRequest del contrato aprobado. */
public record RegistroRequest(
        @NotBlank @Email String correo,
        @NotNull @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) String contrasena,
        @NotNull String nombres,
        @NotNull String apellidos,
        @NotNull String rol) {

    @Override
    public String toString() {
        return "RegistroRequest[datos protegidos]";
    }
}
