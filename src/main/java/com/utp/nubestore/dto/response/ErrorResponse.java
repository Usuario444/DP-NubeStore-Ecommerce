package com.utp.nubestore.dto.response;

import java.time.LocalDateTime;

/**
 * Cuerpo estándar de todas las respuestas de error de la API.
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path) {
}