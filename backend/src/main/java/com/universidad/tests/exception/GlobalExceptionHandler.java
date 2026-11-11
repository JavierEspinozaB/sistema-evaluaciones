package com.universidad.tests.exception;

import com.universidad.tests.domain.exception.RecursoNoEncontradoException;
import com.universidad.tests.domain.exception.RespuestaInvalidaException;
import com.universidad.tests.domain.exception.SolicitudInvalidaException;
import com.universidad.tests.domain.exception.SubpruebaCerradaException;
import com.universidad.tests.domain.exception.SubpruebaFueraDeOrdenException;
import com.universidad.tests.domain.exception.SubpruebaNoActivaException;
import com.universidad.tests.domain.exception.TiempoAgotadoException;
import com.universidad.tests.dto.DetalleErrorDto;
import com.universidad.tests.dto.ErrorResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(com.universidad.tests.domain.exception.LetraNoConfiguradaException.class)
    public ResponseEntity<ErrorResponseDto> handleLetraNoConfigurada(
            com.universidad.tests.domain.exception.LetraNoConfiguradaException ex,
            HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(
                        new ErrorResponseDto(
                                Instant.now(),
                                409,
                                "LETRA_NO_CONFIGURADA",
                                ex.getMessage(),
                                request.getRequestURI(),
                                List.of(new DetalleErrorDto("letraInicial", ex.getMessage()))));
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponseDto> handleRecursoNoEncontrado(
            RecursoNoEncontradoException ex, HttpServletRequest request) {
        ErrorResponseDto error =
                new ErrorResponseDto(
                        Instant.now(),
                        HttpStatus.NOT_FOUND.value(),
                        ex.getCodigo(),
                        ex.getMessage(),
                        request.getRequestURI(),
                        null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(SubpruebaFueraDeOrdenException.class)
    public ResponseEntity<ErrorResponseDto> handleSubpruebaFueraDeOrden(
            SubpruebaFueraDeOrdenException ex, HttpServletRequest request) {
        ErrorResponseDto error =
                new ErrorResponseDto(
                        Instant.now(),
                        HttpStatus.CONFLICT.value(),
                        ex.getCodigo(),
                        ex.getMessage(),
                        request.getRequestURI(),
                        null);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(SubpruebaCerradaException.class)
    public ResponseEntity<ErrorResponseDto> handleSubpruebaCerrada(
            SubpruebaCerradaException ex, HttpServletRequest request) {
        ErrorResponseDto error =
                new ErrorResponseDto(
                        Instant.now(),
                        HttpStatus.CONFLICT.value(),
                        "SUBPRUEBA_CERRADA",
                        ex.getMessage(),
                        request.getRequestURI(),
                        null);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(SubpruebaNoActivaException.class)
    public ResponseEntity<ErrorResponseDto> handleSubpruebaNoActiva(
            SubpruebaNoActivaException ex, HttpServletRequest request) {
        ErrorResponseDto error =
                new ErrorResponseDto(
                        Instant.now(),
                        HttpStatus.CONFLICT.value(),
                        "SUBPRUEBA_NO_ACTIVA",
                        ex.getMessage(),
                        request.getRequestURI(),
                        null);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(TiempoAgotadoException.class)
    public ResponseEntity<ErrorResponseDto> handleTiempoAgotado(
            TiempoAgotadoException ex, HttpServletRequest request) {
        ErrorResponseDto error =
                new ErrorResponseDto(
                        Instant.now(),
                        HttpStatus.CONFLICT.value(),
                        "TIEMPO_AGOTADO",
                        ex.getMessage(),
                        request.getRequestURI(),
                        null);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(RespuestaInvalidaException.class)
    public ResponseEntity<ErrorResponseDto> handleRespuestaInvalida(
            RespuestaInvalidaException ex, HttpServletRequest request) {
        String campo = ex.getCampo() != null ? ex.getCampo() : "opcion";
        List<DetalleErrorDto> detalles = List.of(new DetalleErrorDto(campo, ex.getMessage()));

        ErrorResponseDto error =
                new ErrorResponseDto(
                        Instant.now(),
                        HttpStatus.UNPROCESSABLE_ENTITY.value(),
                        "RESPUESTA_INVALIDA",
                        ex.getMessage(),
                        request.getRequestURI(),
                        detalles);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }

    @ExceptionHandler(SolicitudInvalidaException.class)
    public ResponseEntity<ErrorResponseDto> handleSolicitudInvalida(
            SolicitudInvalidaException ex, HttpServletRequest request) {
        List<DetalleErrorDto> detalles =
                ex.getDetalles().stream()
                        .map(d -> new DetalleErrorDto(d.campo(), d.mensaje()))
                        .toList();
        if (detalles.isEmpty()) {
            detalles = List.of(new DetalleErrorDto("cuerpo", ex.getMessage()));
        }

        ErrorResponseDto error =
                new ErrorResponseDto(
                        Instant.now(),
                        HttpStatus.BAD_REQUEST.value(),
                        "SOLICITUD_INVALIDA",
                        ex.getMessage(),
                        request.getRequestURI(),
                        detalles);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<DetalleErrorDto> detalles =
                ex.getBindingResult().getFieldErrors().stream()
                        .map(
                                error ->
                                        new DetalleErrorDto(
                                                error.getField(), error.getDefaultMessage()))
                        .toList();

        ErrorResponseDto error =
                new ErrorResponseDto(
                        Instant.now(),
                        HttpStatus.BAD_REQUEST.value(),
                        "SOLICITUD_INVALIDA",
                        "Validación fallida en los datos enviados.",
                        request.getRequestURI(),
                        detalles);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDto> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        ErrorResponseDto error =
                new ErrorResponseDto(
                        Instant.now(),
                        HttpStatus.BAD_REQUEST.value(),
                        "SOLICITUD_INVALIDA",
                        "El formato del cuerpo JSON es inválido.",
                        request.getRequestURI(),
                        List.of(new DetalleErrorDto("cuerpo", "JSON mal formado")));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGeneralException(
            Exception ex, HttpServletRequest request) {
        ErrorResponseDto error =
                new ErrorResponseDto(
                        Instant.now(),
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "ERROR_INTERNO",
                        "Ha ocurrido un error interno en el servidor.",
                        request.getRequestURI(),
                        null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
