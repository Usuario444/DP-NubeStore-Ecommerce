package com.utp.nubestore.exception;

import com.utp.nubestore.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * Manejo centralizado de excepciones: ningún controller necesita try-catch
 * y el cliente siempre recibe un {@link ErrorResponse} consistente.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Errores de negocio y errores de DAO ya traducidos a ApiException. */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex, HttpServletRequest request) {
        if (ex.getStatus().is5xxServerError()) {
            log.error("Error interno en {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        } else {
            log.warn("Solicitud rechazada en {}: {}", request.getRequestURI(), ex.getMessage());
        }
        return construir(ex.getStatus(), ex.getMessage(), request);
    }

    /** Red de seguridad: una SQLException que no fue traducida en el DAO. */
    @ExceptionHandler(SQLException.class)
    public ResponseEntity<ErrorResponse> handleSqlException(SQLException ex, HttpServletRequest request) {
        log.error("SQLException (SQLState={}) en {}", ex.getSQLState(), request.getRequestURI(), ex);
        if ("23505".equals(ex.getSQLState())) { // unique_violation
            return construir(HttpStatus.CONFLICT, "Ya existe un registro con esos datos", request);
        }
        return construir(HttpStatus.INTERNAL_SERVER_ERROR,
                "Error al acceder a la base de datos", request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleBodyInvalido(HttpMessageNotReadableException ex,
                                                            HttpServletRequest request) {
        log.warn("Cuerpo JSON inválido en {}: {}", request.getRequestURI(), ex.getMessage());
        return construir(HttpStatus.BAD_REQUEST,
                "El cuerpo de la solicitud es inválido o está mal formado", request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTipoInvalido(MethodArgumentTypeMismatchException ex,
                                                            HttpServletRequest request) {
        return construir(HttpStatus.BAD_REQUEST,
                "El parámetro '" + ex.getName() + "' tiene un valor inválido", request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleParametroFaltante(MissingServletRequestParameterException ex,
                                                                 HttpServletRequest request) {
        return construir(HttpStatus.BAD_REQUEST,
                "Falta el parámetro obligatorio '" + ex.getParameterName() + "'", request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMetodoNoPermitido(HttpRequestMethodNotSupportedException ex,
                                                                 HttpServletRequest request) {
        return construir(HttpStatus.METHOD_NOT_ALLOWED,
                "Método HTTP no permitido para este recurso", request);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaType(HttpMediaTypeNotSupportedException ex,
                                                         HttpServletRequest request) {
        return construir(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Tipo de contenido no soportado; use application/json", request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoEncontrado(NoResourceFoundException ex,
                                                            HttpServletRequest request) {
        return construir(HttpStatus.NOT_FOUND, "Recurso no encontrado", request);
    }

    /** Último recurso: nunca se filtra el detalle interno al cliente. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenerico(Exception ex, HttpServletRequest request) {
        log.error("Error no controlado en {}", request.getRequestURI(), ex);
        return construir(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error inesperado. Intente nuevamente más tarde", request);
    }

    private ResponseEntity<ErrorResponse> construir(HttpStatus status, String mensaje,
                                                    HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                mensaje,
                request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}