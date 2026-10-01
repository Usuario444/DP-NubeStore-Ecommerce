package com.utp.nubestore.dao.impl;

import com.utp.nubestore.dao.ProductoDAO;
import com.utp.nubestore.exception.ApiException;
import com.utp.nubestore.model.Producto;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implementación JDBC pura de {@link ProductoDAO}.
 * Todas las consultas usan PreparedStatement con parámetros enlazados (anti SQL injection)
 * y el ResultSet se convierte manualmente a objetos {@link Producto}.
 */
public class ProductoDAOImpl extends BaseDAO implements ProductoDAO {

    static final String COLUMNAS =
            "id_producto, id_vendedor, nombre, descripcion, categoria, precio, stock, "
                    + "imagen_url, activo, fecha_publicacion";

    private static final String SQL_INSERTAR = """
            INSERT INTO producto
                (id_vendedor, nombre, descripcion, categoria, precio, stock, imagen_url, activo)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING id_producto, fecha_publicacion
            """;

    private static final String SQL_BUSCAR_POR_ID =
            "SELECT " + COLUMNAS + " FROM producto WHERE id_producto = ?";

    @Override
    public Producto insertar(Producto producto) {
        ReentrantLock lock = conexionBD.getLock();
        lock.lock();
        try (PreparedStatement ps = conexionBD.getConexion().prepareStatement(SQL_INSERTAR)) {
            ps.setInt(1, producto.getIdVendedor());
            ps.setString(2, producto.getNombre());
            ps.setString(3, producto.getDescripcion());
            ps.setString(4, producto.getCategoria());
            ps.setBigDecimal(5, producto.getPrecio());
            ps.setInt(6, producto.getStock());
            ps.setString(7, producto.getImagenUrl());
            ps.setBoolean(8, producto.isActivo());

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("INSERT ... RETURNING no devolvió filas");
                }
                producto.setIdProducto(rs.getInt("id_producto"));
                producto.setFechaPublicacion(toLocalDateTime(rs.getTimestamp("fecha_publicacion")));
            }
            return producto;
        } catch (SQLException e) {
            if (FK_VIOLATION.equals(e.getSQLState())) {
                log.warn("Intento de publicar producto con vendedor inexistente (id={})",
                        producto.getIdVendedor());
                throw ApiException.badRequest("El vendedor indicado no existe");
            }
            throw traducir(e, "insertar el producto");
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Optional<Producto> buscarPorId(int idProducto) {
        ReentrantLock lock = conexionBD.getLock();
        lock.lock();
        try (PreparedStatement ps = conexionBD.getConexion().prepareStatement(SQL_BUSCAR_POR_ID)) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw traducir(e, "buscar el producto por id");
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<Producto> buscar(String texto, String categoria, BigDecimal precioMin,
                                 BigDecimal precioMax, int limite, int offset) {
        // El SQL se arma solo con fragmentos constantes; todo valor del usuario va como parámetro (?).
        StringBuilder sql = new StringBuilder("SELECT " + COLUMNAS + " FROM producto WHERE activo = TRUE");
        List<Object> parametros = new ArrayList<>();

        if (texto != null && !texto.isBlank()) {
            sql.append(" AND (LOWER(nombre) LIKE ? ESCAPE '!' OR LOWER(descripcion) LIKE ? ESCAPE '!')");
            String patron = "%" + escaparLike(texto.trim().toLowerCase(Locale.ROOT)) + "%";
            parametros.add(patron);
            parametros.add(patron);
        }
        if (categoria != null && !categoria.isBlank()) {
            sql.append(" AND LOWER(categoria) = ?");
            parametros.add(categoria.trim().toLowerCase(Locale.ROOT));
        }
        if (precioMin != null) {
            sql.append(" AND precio >= ?");
            parametros.add(precioMin);
        }
        if (precioMax != null) {
            sql.append(" AND precio <= ?");
            parametros.add(precioMax);
        }
        sql.append(" ORDER BY fecha_publicacion DESC, id_producto DESC LIMIT ? OFFSET ?");
        parametros.add(limite);
        parametros.add(offset);

        ReentrantLock lock = conexionBD.getLock();
        lock.lock();
        try (PreparedStatement ps = conexionBD.getConexion().prepareStatement(sql.toString())) {
            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }
            List<Producto> resultado = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapear(rs));
                }
            }
            return resultado;
        } catch (SQLException e) {
            throw traducir(e, "buscar productos");
        } finally {
            lock.unlock();
        }
    }

    /** Mapeo manual ResultSet -> Producto (reutilizado por PedidoDAOImpl). */
    static Producto mapear(ResultSet rs) throws SQLException {
        Producto p = new Producto();
        p.setIdProducto(rs.getInt("id_producto"));
        p.setIdVendedor(rs.getInt("id_vendedor"));
        p.setNombre(rs.getString("nombre"));
        p.setDescripcion(rs.getString("descripcion"));
        p.setCategoria(rs.getString("categoria"));
        p.setPrecio(rs.getBigDecimal("precio"));
        p.setStock(rs.getInt("stock"));
        p.setImagenUrl(rs.getString("imagen_url"));
        p.setActivo(rs.getBoolean("activo"));
        p.setFechaPublicacion(toLocalDateTime(rs.getTimestamp("fecha_publicacion")));
        return p;
    }

    /** Neutraliza los comodines de LIKE (%, _) que escriba el usuario. */
    private static String escaparLike(String texto) {
        return texto.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }
}