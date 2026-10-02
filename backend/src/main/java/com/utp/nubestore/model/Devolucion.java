package com.utp.nubestore.model;

import java.time.LocalDateTime;

/**
 * Solicitud de devolución de un ítem (detalle) de un pedido entregado.
 */
public class Devolucion {

    public static final String SOLICITADA = "SOLICITADA";
    public static final String APROBADA = "APROBADA";
    public static final String RECHAZADA = "RECHAZADA";
    public static final String COMPLETADA = "COMPLETADA";

    private Integer idDevolucion;
    private Integer idDetalle;
    private Integer idCliente;
    private int cantidad;
    private String motivo;
    private String estado = SOLICITADA;
    private LocalDateTime fechaSolicitud;

    public Devolucion() {
    }

    public Integer getIdDevolucion() { return idDevolucion; }
    public void setIdDevolucion(Integer idDevolucion) { this.idDevolucion = idDevolucion; }

    public Integer getIdDetalle() { return idDetalle; }
    public void setIdDetalle(Integer idDetalle) { this.idDetalle = idDetalle; }

    public Integer getIdCliente() { return idCliente; }
    public void setIdCliente(Integer idCliente) { this.idCliente = idCliente; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDateTime fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }
}