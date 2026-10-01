package com.utp.nubestore.dto.response;

import com.utp.nubestore.model.Pedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Pedido con sus ítems. Incluye estado y código de seguimiento,
 * por lo que también se usa para el seguimiento del pedido.
 */
public record PedidoResponse(
        Integer idPedido,
        Integer idCliente,
        LocalDateTime fechaPedido,
        LocalDateTime fechaActualizacion,
        String estado,
        String codigoSeguimiento,
        String direccionEnvio,
        BigDecimal total,
        List<DetallePedidoResponse> detalles) {

    public static PedidoResponse desde(Pedido p) {
        return new PedidoResponse(
                p.getIdPedido(),
                p.getIdCliente(),
                p.getFechaPedido(),
                p.getFechaActualizacion(),
                p.getEstado(),
                p.getCodigoSeguimiento(),
                p.getDireccionEnvio(),
                p.getTotal(),
                p.getDetalles().stream().map(DetallePedidoResponse::desde).toList());
    }
}
