package com.utp.nubestore.dto.request;

/**
 * Solicitud de devolución de un ítem (detalle) de un pedido entregado.
 */
public record SolicitarDevolucionRequest(
        Integer idCliente,
        Integer idDetalle,
        Integer cantidad,
        String motivo) {
}
