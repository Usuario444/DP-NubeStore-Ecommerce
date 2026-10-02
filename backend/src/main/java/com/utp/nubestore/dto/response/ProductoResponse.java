package com.utp.nubestore.dto.response;

import com.utp.nubestore.model.Producto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductoResponse(
        Integer idProducto,
        Integer idVendedor,
        String nombre,
        String descripcion,
        String categoria,
        BigDecimal precio,
        int stock,
        boolean disponible,
        String imagenUrl,
        LocalDateTime fechaPublicacion) {

    public static ProductoResponse desde(Producto p) {
        return new ProductoResponse(
                p.getIdProducto(),
                p.getIdVendedor(),
                p.getNombre(),
                p.getDescripcion(),
                p.getCategoria(),
                p.getPrecio(),
                p.getStock(),
                p.isActivo() && p.getStock() > 0,
                p.getImagenUrl(),
                p.getFechaPublicacion());
    }
}
