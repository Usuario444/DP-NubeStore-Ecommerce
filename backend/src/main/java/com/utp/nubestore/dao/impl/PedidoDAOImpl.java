package com.utp.nubestore.dao.impl;

import com.utp.nubestore.dao.PedidoDAO;
import com.utp.nubestore.exception.ApiException;
import com.utp.nubestore.model.DetallePedido;
import com.utp.nubestore.model.Devolucion;
import com.utp.nubestore.model.Pedido;
import com.utp.nubestore.model.Producto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implementación JDBC pura de {@link PedidoDAO}.
 * Incluye la transacción de compra (commit/rollback manual).
 */
public class PedidoDAOImpl extends BaseDAO implements PedidoDAO {

    // ------------------------------------------------------------------ SQL

    private static final String SQL_BLOQUEAR_PRODUCTO =
            "SELECT " + ProductoDAOImpl.COLUMNAS + " FROM producto WHERE id_producto = ? FOR UPDATE";

    private static final String SQL_DESCONTAR_STOCK =
            "UPDATE producto SET stock = stock - ? WHERE id_producto = ? AND stock >= ?";

    private static final String SQL_INSERTAR_PEDIDO = """
            INSERT INTO pedido (id_cliente, estado, total, direccion_envio, codigo_seguimiento)
            VALUES (?, ?, ?, ?, ?)
            RETURNING id_pedido, fecha_pedido, fecha_actualizacion
            """;

    private static final String SQL_INSERTAR_DETALLE = """
            INSERT INTO detalle_pedido (id_pedido, id_producto, cantidad, precio_unitario)
            VALUES (?, ?, ?, ?)
            RETURNING id_detalle
            """;

    /** Pedido + detalles + nombre del producto en una sola consulta (evita el problema N+1). */
    private static final String SQL_SELECT_PEDIDOS = """
            SELECT p.id_pedido, p.id_cliente, p.fecha_pedido, p.fecha_actualizacion, p.estado,
                   p.total, p.direccion_envio, p.codigo_seguimiento,
                   d.id_detalle, d.id_producto, d.cantidad, d.precio_unitario,
                   pr.nombre AS nombre_producto
            FROM pedido p
            LEFT JOIN detalle_pedido d ON d.id_pedido = p.id_pedido
            LEFT JOIN producto pr      ON pr.id_producto = d.id_producto
            """;

    private static final String SQL_ORDEN_PEDIDOS =
            " ORDER BY p.fecha_pedido DESC, p.id_pedido DESC, d.id_detalle ASC";

    private static final String SQL_PEDIDOS_POR_CLIENTE =
            SQL_SELECT_PEDIDOS + "WHERE p.id_cliente = ?" + SQL_ORDEN_PEDIDOS;

    private static final String SQL_PEDIDO_POR_ID_Y_CLIENTE =
            SQL_SELECT_PEDIDOS + "WHERE p.id_pedido = ? AND p.id_cliente = ?" + SQL_ORDEN_PEDIDOS;

    private static final String SQL_PEDIDO_POR_DETALLE =
            SQL_SELECT_PEDIDOS
                    + "WHERE p.id_pedido = (SELECT id_pedido FROM detalle_pedido WHERE id_detalle = ?)"
                    + SQL_ORDEN_PEDIDOS;

    private static final String SQL_UNIDADES_DEVUELTAS = """
            SELECT COALESCE(SUM(cantidad), 0)
            FROM devolucion
            WHERE id_detalle = ? AND estado <> 'RECHAZADA'
            """;

    /** INSERT atómico: solo inserta si el pedido es del cliente, está ENTREGADO y hay cantidad disponible. */
    private static final String SQL_INSERTAR_DEVOLUCION = """
            INSERT INTO devolucion (id_detalle, id_cliente, cantidad, motivo, estado)
            SELECT d.id_detalle, p.id_cliente, CAST(? AS INTEGER), CAST(? AS VARCHAR), 'SOLICITADA'
            FROM detalle_pedido d
            JOIN pedido p ON p.id_pedido = d.id_pedido
            WHERE d.id_detalle = ?
              AND p.id_cliente = ?
              AND p.estado = 'ENTREGADO'
              AND d.cantidad - COALESCE((SELECT SUM(dv.cantidad)
                                         FROM devolucion dv
                                         WHERE dv.id_detalle = d.id_detalle
                                           AND dv.estado <> 'RECHAZADA'), 0) >= CAST(? AS INTEGER)
            RETURNING id_devolucion, fecha_solicitud
            """;

    private static final String SQL_DEVOLUCIONES_POR_CLIENTE = """
            SELECT id_devolucion, id_detalle, id_cliente, cantidad, motivo, estado, fecha_solicitud
            FROM devolucion
            WHERE id_cliente = ?
            ORDER BY fecha_solicitud DESC, id_devolucion DESC
            """;

    // ------------------------------------------------- Transacción de compra

    @Override
    public Pedido registrarPedido(Pedido pedido) {
        if (pedido.getDetalles().isEmpty()) {
            throw ApiException.badRequest("El pedido debe tener al menos un producto");
        }

        ReentrantLock lock = conexionBD.getLock();
        lock.lock();
        Connection con = null;
        try {
            con = conexionBD.getConexion();
            con.setAutoCommit(false); // inicio de la transacción

            // 1) Bloquear cada producto (FOR UPDATE), validar disponibilidad y descontar stock
            for (DetallePedido detalle : pedido.getDetalles()) {
                Producto producto = bloquearProducto(con, detalle.getIdProducto());
                if (producto == null) {
                    throw ApiException.notFound("El producto con id " + detalle.getIdProducto() + " no existe");
                }
                if (!producto.tieneStockPara(detalle.getCantidad())) {
                    throw ApiException.conflict("Stock insuficiente o producto no disponible: "
                            + producto.getNombre());
                }
                detalle.setPrecioUnitario(producto.getPrecio()); // precio vigente en BD, no el del cliente
                detalle.setNombreProducto(producto.getNombre());
                descontarStock(con, detalle.getIdProducto(), detalle.getCantidad());
            }
            pedido.recalcularTotal();

            // 2) Insertar la cabecera del pedido
            insertarCabecera(con, pedido);

            // 3) Insertar cada detalle
            for (DetallePedido detalle : pedido.getDetalles()) {
                insertarDetalle(con, pedido.getIdPedido(), detalle);
            }

            con.commit(); // todo salió bien
            log.info("Pedido {} registrado para el cliente {}", pedido.getIdPedido(), pedido.getIdCliente());
            return pedido;

        } catch (SQLException e) {
            rollback(con);
            throw traducir(e, "registrar el pedido");
        } catch (RuntimeException e) { // incluye ApiException de negocio (stock, producto inexistente)
            rollback(con);
            throw e;
        } finally {
            restaurarAutoCommit(con);
            lock.unlock();
        }
    }

    private Producto bloquearProducto(Connection con, int idProducto) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(SQL_BLOQUEAR_PRODUCTO)) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? ProductoDAOImpl.mapear(rs) : null;
            }
        }
    }

    private void descontarStock(Connection con, int idProducto, int cantidad) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(SQL_DESCONTAR_STOCK)) {
            ps.setInt(1, cantidad);
            ps.setInt(2, idProducto);
            ps.setInt(3, cantidad);
            if (ps.executeUpdate() != 1) {
                throw ApiException.conflict("No se pudo reservar el stock del producto " + idProducto);
            }
        }
    }

    private void insertarCabecera(Connection con, Pedido pedido) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(SQL_INSERTAR_PEDIDO)) {
            ps.setInt(1, pedido.getIdCliente());
            ps.setString(2, pedido.getEstado());
            ps.setBigDecimal(3, pedido.getTotal());
            ps.setString(4, pedido.getDireccionEnvio());
            ps.setString(5, pedido.getCodigoSeguimiento());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("INSERT de pedido no devolvió filas");
                }
                pedido.setIdPedido(rs.getInt("id_pedido"));
                pedido.setFechaPedido(toLocalDateTime(rs.getTimestamp("fecha_pedido")));
                pedido.setFechaActualizacion(toLocalDateTime(rs.getTimestamp("fecha_actualizacion")));
            }
        }
    }

    private void insertarDetalle(Connection con, int idPedido, DetallePedido detalle) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(SQL_INSERTAR_DETALLE)) {
            ps.setInt(1, idPedido);
            ps.setInt(2, detalle.getIdProducto());
            ps.setInt(3, detalle.getCantidad());
            ps.setBigDecimal(4, detalle.getPrecioUnitario());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("INSERT de detalle no devolvió filas");
                }
                detalle.setIdDetalle(rs.getInt("id_detalle"));
                detalle.setIdPedido(idPedido);
            }
        }
    }

    // ------------------------------------------------------- Consultas

    @Override
    public Optional<Pedido> buscarPorIdYCliente(int idPedido, int idCliente) {
        ReentrantLock lock = conexionBD.getLock();
        lock.lock();
        try {
            List<Pedido> pedidos = consultarPedidos(
                    conexionBD.getConexion(), SQL_PEDIDO_POR_ID_Y_CLIENTE, idPedido, idCliente);
            return pedidos.isEmpty() ? Optional.empty() : Optional.of(pedidos.get(0));
        } catch (SQLException e) {
            throw traducir(e, "buscar el pedido del cliente");
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<Pedido> listarPorCliente(int idCliente) {
        ReentrantLock lock = conexionBD.getLock();
        lock.lock();
        try {
            return consultarPedidos(conexionBD.getConexion(), SQL_PEDIDOS_POR_CLIENTE, idCliente);
        } catch (SQLException e) {
            throw traducir(e, "listar los pedidos del cliente");
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Optional<Pedido> buscarPorDetalle(int idDetalle) {
        ReentrantLock lock = conexionBD.getLock();
        lock.lock();
        try {
            List<Pedido> pedidos = consultarPedidos(
                    conexionBD.getConexion(), SQL_PEDIDO_POR_DETALLE, idDetalle);
            return pedidos.isEmpty() ? Optional.empty() : Optional.of(pedidos.get(0));
        } catch (SQLException e) {
            throw traducir(e, "buscar el pedido por detalle");
        } finally {
            lock.unlock();
        }
    }

    /**
     * Ejecuta la consulta de pedidos y agrupa las filas (una por detalle) en objetos Pedido.
     * Debe invocarse con el lock de ConexionBD ya adquirido.
     */
    private List<Pedido> consultarPedidos(Connection con, String sql, Object... parametros)
            throws SQLException {
        Map<Integer, Pedido> pedidos = new LinkedHashMap<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                ps.setObject(i + 1, parametros[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int idPedido = rs.getInt("id_pedido");
                    Pedido pedido = pedidos.get(idPedido);
                    if (pedido == null) {
                        pedido = mapearPedido(rs);
                        pedidos.put(idPedido, pedido);
                    }
                    int idDetalle = rs.getInt("id_detalle");
                    if (!rs.wasNull()) { // LEFT JOIN: puede no haber detalle
                        pedido.getDetalles().add(mapearDetalle(rs, idDetalle, idPedido));
                    }
                }
            }
        }
        return new ArrayList<>(pedidos.values());
    }

    // ------------------------------------------------------ Devoluciones

    @Override
    public int contarUnidadesDevueltas(int idDetalle) {
        ReentrantLock lock = conexionBD.getLock();
        lock.lock();
        try (PreparedStatement ps = conexionBD.getConexion().prepareStatement(SQL_UNIDADES_DEVUELTAS)) {
            ps.setInt(1, idDetalle);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw traducir(e, "contar unidades devueltas");
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Optional<Devolucion> registrarDevolucion(Devolucion devolucion) {
        ReentrantLock lock = conexionBD.getLock();
        lock.lock();
        try (PreparedStatement ps = conexionBD.getConexion().prepareStatement(SQL_INSERTAR_DEVOLUCION)) {
            ps.setInt(1, devolucion.getCantidad());
            ps.setString(2, devolucion.getMotivo());
            ps.setInt(3, devolucion.getIdDetalle());
            ps.setInt(4, devolucion.getIdCliente());
            ps.setInt(5, devolucion.getCantidad());

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty(); // no cumple las condiciones del INSERT ... SELECT
                }
                devolucion.setIdDevolucion(rs.getInt("id_devolucion"));
                devolucion.setEstado(Devolucion.SOLICITADA);
                devolucion.setFechaSolicitud(toLocalDateTime(rs.getTimestamp("fecha_solicitud")));
                return Optional.of(devolucion);
            }
        } catch (SQLException e) {
            throw traducir(e, "registrar la devolución");
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<Devolucion> listarDevolucionesPorCliente(int idCliente) {
        ReentrantLock lock = conexionBD.getLock();
        lock.lock();
        try (PreparedStatement ps = conexionBD.getConexion().prepareStatement(SQL_DEVOLUCIONES_POR_CLIENTE)) {
            ps.setInt(1, idCliente);
            List<Devolucion> resultado = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(mapearDevolucion(rs));
                }
            }
            return resultado;
        } catch (SQLException e) {
            throw traducir(e, "listar las devoluciones del cliente");
        } finally {
            lock.unlock();
        }
    }

    // ------------------------------------------------ Mapeo ResultSet -> objetos

    private static Pedido mapearPedido(ResultSet rs) throws SQLException {
        Pedido p = new Pedido();
        p.setIdPedido(rs.getInt("id_pedido"));
        p.setIdCliente(rs.getInt("id_cliente"));
        p.setFechaPedido(toLocalDateTime(rs.getTimestamp("fecha_pedido")));
        p.setFechaActualizacion(toLocalDateTime(rs.getTimestamp("fecha_actualizacion")));
        p.setEstado(rs.getString("estado"));
        p.setTotal(rs.getBigDecimal("total"));
        p.setDireccionEnvio(rs.getString("direccion_envio"));
        p.setCodigoSeguimiento(rs.getString("codigo_seguimiento"));
        return p;
    }

    private static DetallePedido mapearDetalle(ResultSet rs, int idDetalle, int idPedido) throws SQLException {
        DetallePedido d = new DetallePedido();
        d.setIdDetalle(idDetalle);
        d.setIdPedido(idPedido);
        d.setIdProducto(rs.getInt("id_producto"));
        d.setNombreProducto(rs.getString("nombre_producto"));
        d.setCantidad(rs.getInt("cantidad"));
        d.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
        return d;
    }

    private static Devolucion mapearDevolucion(ResultSet rs) throws SQLException {
        Devolucion d = new Devolucion();
        d.setIdDevolucion(rs.getInt("id_devolucion"));
        d.setIdDetalle(rs.getInt("id_detalle"));
        d.setIdCliente(rs.getInt("id_cliente"));
        d.setCantidad(rs.getInt("cantidad"));
        d.setMotivo(rs.getString("motivo"));
        d.setEstado(rs.getString("estado"));
        d.setFechaSolicitud(toLocalDateTime(rs.getTimestamp("fecha_solicitud")));
        return d;
    }
}