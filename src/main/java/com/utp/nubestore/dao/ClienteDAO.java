package com.utp.nubestore.dao;

import com.utp.nubestore.model.Cliente;

import java.util.Optional;

/**
 * Contrato de acceso a datos para clientes.
 */
public interface ClienteDAO {

    /**
     * Inserta el cliente y devuelve la misma instancia con id y fecha de registro.
     *
     * @throws com.utp.nubestore.exception.ApiException 409 si el email ya existe
     */
    Cliente insertar(Cliente cliente);

    Optional<Cliente> buscarPorId(int idCliente);

    Optional<Cliente> buscarPorEmail(String email);
}