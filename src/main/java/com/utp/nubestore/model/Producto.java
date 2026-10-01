package com.utp.nubestore.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad de dominio Producto, publicado por un Vendedor.
 */
public class Producto {

    private Integer idProducto;
    private Integer idVendedor;
    private String nombre;
    private String descripcion;
    private String categoria;
    private BigDecimal precio;
    private int stock;
    private String imagenUrl;
    private boolean activo = true;
    private LocalDateTime fechaPublicacion;

    public Producto() {
    }

    /** Regla de dominio: ¿se puede vender esta cantidad del producto? */
    public boolean tieneStockPara(int cantidad) {
        return activo && cantidad > 0 && stock >= cantidad;
    }

    public Integer getIdProducto() { return idProducto; }
    public void setIdProducto(Integer idProducto) { this.idProducto = idProducto; }

    public Integer getIdVendedor() { return idVendedor; }
    public void setIdVendedor(Integer idVendedor) { this.idVendedor = idVendedor; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public LocalDateTime getFechaPublicacion() { return fechaPublicacion; }
    public void setFechaPublicacion(LocalDateTime fechaPublicacion) { this.fechaPublicacion = fechaPublicacion; }
}