package com.utp.nubestore.dto.request;

import java.math.BigDecimal;

/**
 * Datos para que un Vendedor publique un producto nuevo.
 */
public record PublicarProductoRequest(
        Integer idVendedor,
        String nombre,
        String descripcion,
        String categoria,
        BigDecimal precio,
        Integer stock,
        String imagenUrl) {
}
