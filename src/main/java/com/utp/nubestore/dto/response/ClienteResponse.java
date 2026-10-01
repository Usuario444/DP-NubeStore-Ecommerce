package com.utp.nubestore.dto.response;

import com.utp.nubestore.model.Cliente;

import java.time.LocalDateTime;

/**
 * Datos públicos del cliente. Nunca incluye el hash de la contraseña.
 */
public record ClienteResponse(
        Integer idCliente,
        String nombre,
        String apellido,
        String email,
        String telefono,
        String direccion,
        LocalDateTime fechaRegistro) {

    public static ClienteResponse desde(Cliente c) {
        return new ClienteResponse(
                c.getIdCliente(),
                c.getNombre(),
                c.getApellido(),
                c.getEmail(),
                c.getTelefono(),
                c.getDireccion(),
                c.getFechaRegistro());
    }
}
