package com.universidad.tests.exception;

import com.universidad.tests.domain.exception.EvaluacionNoEncontradaException;
import com.universidad.tests.domain.exception.TestNoEncontradoException;
import com.universidad.tests.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ManejadorErroresApi {

    @ExceptionHandler(TestNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> testNoEncontrado(
            TestNoEncontradoException ex, HttpServletRequest request) {

        return error(HttpStatus.NOT_FOUND, "TEST_NO_ENCONTRADO", ex.getMessage(), request);
    }

    @ExceptionHandler(EvaluacionNoEncontradaException.class)
    public ResponseEntity<ErrorResponse> evaluacionNoEncontrada(
            EvaluacionNoEncontradaException ex, HttpServletRequest request) {

        return error(HttpStatus.NOT_FOUND, "EVALUACION_NO_ENCONTRADA", ex.getMessage(), request);
    }

    private ResponseEntity<ErrorResponse> error(
            HttpStatus status, String codigo, String mensaje, HttpServletRequest request) {

        return ResponseEntity.status(status)
                .body(
                        new ErrorResponse(
                                Instant.now(),
                                status.value(),
                                codigo,
                                mensaje,
                                request.getRequestURI()));
    }
}
