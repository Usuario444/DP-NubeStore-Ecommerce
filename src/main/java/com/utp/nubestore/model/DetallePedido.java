package com.utp.nubestore.model;

import java.math.BigDecimal;

/**
 * Línea de un pedido. El precio unitario es una "foto" del precio al momento
 * de la compra. El subtotal se calcula (en BD es una columna generada).
 */
public class DetallePedido {

    private Integer idDetalle;
    private Integer idPedido;
    private Integer idProducto;
    private String nombreProducto; // informativo: se llena con JOIN al consultar
    private int cantidad;
    private BigDecimal precioUnitario;

    public DetallePedido() {
    }

    public DetallePedido(Integer idProducto, int cantidad, BigDecimal precioUnitario) {
        this.idProducto = idProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    public BigDecimal getSubtotal() {
        if (precioUnitario == null) {
            return BigDecimal.ZERO;
        }
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    public Integer getIdDetalle() { return idDetalle; }
    public void setIdDetalle(Integer idDetalle) { this.idDetalle = idDetalle; }

    public Integer getIdPedido() { return idPedido; }
    public void setIdPedido(Integer idPedido) { this.idPedido = idPedido; }

    public Integer getIdProducto() { return idProducto; }
    public void setIdProducto(Integer idProducto) { this.idProducto = idProducto; }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
}