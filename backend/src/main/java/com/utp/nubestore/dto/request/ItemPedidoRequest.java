package com.utp.nubestore.dto.request;

/**
 * Un producto del carrito con su cantidad. El precio NO se envía:
 * siempre se toma de la base de datos al comprar.
 */
public record ItemPedidoRequest(
        Integer idProducto,
        Integer cantidad) {
}
