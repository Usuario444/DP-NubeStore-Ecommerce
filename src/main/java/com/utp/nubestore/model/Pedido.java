package com.utp.nubestore.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Cabecera de un pedido. Agrupa sus {@link DetallePedido} y conoce
 * sus propias reglas básicas (cálculo del total, estado de entrega).
 */
public class Pedido {

    public static final String PENDIENTE = "PENDIENTE";
    public static final String PAGADO = "PAGADO";
    public static final String PREPARANDO = "PREPARANDO";
    public static final String ENVIADO = "ENVIADO";
    public static final String ENTREGADO = "ENTREGADO";
    public static final String CANCELADO = "CANCELADO";

    private Integer idPedido;
    private Integer idCliente;
    private LocalDateTime fechaPedido;
    private LocalDateTime fechaActualizacion;
    private String estado = PENDIENTE;
    private BigDecimal total = BigDecimal.ZERO;
    private String direccionEnvio;
    private String codigoSeguimiento;
    private List<DetallePedido> detalles = new ArrayList<>();

    public Pedido() {
    }

    public void agregarDetalle(DetallePedido detalle) {
        detalles.add(detalle);
        recalcularTotal();
    }

    public BigDecimal recalcularTotal() {
        total = detalles.stream()
                .map(DetallePedido::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return total;
    }

    public boolean estaEntregado() {
        return ENTREGADO.equals(estado);
    }

    public Integer getIdPedido() { return idPedido; }
    public void setIdPedido(Integer idPedido) { this.idPedido = idPedido; }

    public Integer getIdCliente() { return idCliente; }
    public void setIdCliente(Integer idCliente) { this.idCliente = idCliente; }

    public LocalDateTime getFechaPedido() { return fechaPedido; }
    public void setFechaPedido(LocalDateTime fechaPedido) { this.fechaPedido = fechaPedido; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public String getDireccionEnvio() { return direccionEnvio; }
    public void setDireccionEnvio(String direccionEnvio) { this.direccionEnvio = direccionEnvio; }

    public String getCodigoSeguimiento() { return codigoSeguimiento; }
    public void setCodigoSeguimiento(String codigoSeguimiento) { this.codigoSeguimiento = codigoSeguimiento; }

    public List<DetallePedido> getDetalles() { return detalles; }
    public void setDetalles(List<DetallePedido> detalles) {
        this.detalles = detalles != null ? new ArrayList<>(detalles) : new ArrayList<>();
    }
}