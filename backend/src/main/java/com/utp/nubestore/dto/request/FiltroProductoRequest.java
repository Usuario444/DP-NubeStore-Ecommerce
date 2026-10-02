package com.utp.nubestore.dto.request;

import java.math.BigDecimal;

/**
 * Filtros opcionales para buscar productos (todos pueden ser null).
 * pagina empieza en 1; tamanio por defecto 20 (máximo 100).
 */
public record FiltroProductoRequest(
        String texto,
        String categoria,
        BigDecimal precioMin,
        BigDecimal precioMax,
        Integer pagina,
        Integer tamanio) {
}
