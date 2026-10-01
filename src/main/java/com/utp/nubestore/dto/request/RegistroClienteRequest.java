package com.utp.nubestore.dto.request;

/**
 * Datos para registrar un cliente. La validación la hace ClienteService.
 */
public record RegistroClienteRequest(
        String nombre,
        String apellido,
        String email,
        String password,
        String telefono,
        String direccion) {
}
