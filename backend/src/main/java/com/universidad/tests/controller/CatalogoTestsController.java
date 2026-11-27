package com.universidad.tests.controller;

import com.universidad.tests.application.service.CatalogoTestsService;
import com.universidad.tests.dto.response.ErrorResponse;
import com.universidad.tests.dto.response.TestDetalleResponse;
import com.universidad.tests.dto.response.TestResumenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tests")
public class CatalogoTestsController {

    private final CatalogoTestsService catalogoService;

    public CatalogoTestsController(CatalogoTestsService catalogoService) {
        this.catalogoService = catalogoService;
    }

    // E1
    @GetMapping
    public List<TestResumenResponse> listarTests() {
        return catalogoService.listarTests();
    }

    @Operation(
            summary = "E2 — Consultar el test y sus subpruebas",
            description =
                    "Devuelve la edición publicada más reciente del test y sus subpruebas en"
                            + " orden de aplicación. No incluye preguntas ni claves de corrección."
                            + " numeroPreguntas excluye la práctica y es 0 para Fluidez Verbal."
                            + " maximoEntradas es 75 para Fluidez Verbal y nulo para selección.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(
                responseCode = "200",
                description = "Test con sus subpruebas.",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                schema = @Schema(implementation = TestDetalleResponse.class),
                                examples =
                                        @ExampleObject(
                                                name = "Test ficticio",
                                                value =
                                                        """
                                                        {
                                                          "id": 101,
                                                          "codigo": "DEMO",
                                                          "nombre": "Test ficticio de demostración",
                                                          "versionFormularioId": 201,
                                                          "subpruebas": [
                                                            {
                                                              "codigo": "DEMO-1",
                                                              "nombre": "Subprueba ficticia",
                                                              "orden": 1,
                                                              "factor": null,
                                                              "tiempoLimiteSegundos": 120,
                                                              "tipoRespuesta": "SELECCION",
                                                              "opciones": 4,
                                                              "numeroPreguntas": 3,
                                                              "maximoEntradas": null,
                                                              "tienePractica": false
                                                            }
                                                          ]
                                                        }
                                                        """))),
        @ApiResponse(
                responseCode = "401",
                description = "Autenticación requerida: token ausente, inválido o expirado.",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
                responseCode = "403",
                description = "El usuario autenticado no tiene permiso para la operación.",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description =
                        "TEST_NO_ENCONTRADO: el test no existe, está inactivo o no tiene edición"
                                + " publicada.",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
                responseCode = "500",
                description = "Error interno del servidor.",
                content =
                        @Content(
                                mediaType = MediaType.APPLICATION_JSON_VALUE,
                                schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{testId}")
    public TestDetalleResponse obtenerTest(@PathVariable Long testId) {
        return catalogoService.obtenerTest(testId);
    }
}
