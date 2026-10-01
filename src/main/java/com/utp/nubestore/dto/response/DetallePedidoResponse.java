package com.utp.nubestore.dto.response;

import com.utp.nubestore.model.DetallePedido;

import java.math.BigDecimal;

public record DetallePedidoResponse(
        Integer idDetalle,
        Integer idProducto,
        String nombreProducto,
        int cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal) {

    public static DetallePedidoResponse desde(DetallePedido d) {
        return new DetallePedidoResponse(
                d.getIdDetalle(),
                d.getIdProducto(),
                d.getNombreProducto(),
                d.getCantidad(),
                d.getPrecioUnitario(),
                d.getSubtotal());
    }
}
