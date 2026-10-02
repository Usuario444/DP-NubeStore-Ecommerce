package com.utp.nubestore.dao;

import com.utp.nubestore.model.Producto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Contrato de acceso a datos para productos.
 * Los Services dependen de esta interfaz, nunca de la implementación JDBC.
 */
public interface ProductoDAO {

    /** Inserta el producto y devuelve la misma instancia con id y fecha de publicación asignados. */
    Producto insertar(Producto producto);

    Optional<Producto> buscarPorId(int idProducto);

    /**
     * Búsqueda de productos activos. Todos los filtros son opcionales (null = sin filtro).
     *
     * @param texto     coincidencia parcial en nombre o descripción
     * @param categoria coincidencia exacta (sin distinguir mayúsculas)
     * @param limite    máximo de filas a devolver
     * @param offset    filas a omitir (paginación)
     */
    List<Producto> buscar(String texto, String categoria, BigDecimal precioMin,
                          BigDecimal precioMax, int limite, int offset);
}
