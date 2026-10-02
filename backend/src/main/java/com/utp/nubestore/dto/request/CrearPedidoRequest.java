package com.utp.nubestore.dto.request;

import java.util.List;

/**
 * Compra del carrito completo. Si direccionEnvio es null se usa la dirección
 * registrada del cliente.
 */
public record CrearPedidoRequest(
        Integer idCliente,
        String direccionEnvio,
        List<ItemPedidoRequest> items) {
}
