package com.utp.nubestore.dao.impl;

import com.utp.nubestore.dao.ClienteDAO;
import com.utp.nubestore.exception.ApiException;
import com.utp.nubestore.model.Cliente;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implementación JDBC pura de {@link ClienteDAO}.
 */
public class ClienteDAOImpl extends BaseDAO implements ClienteDAO {

    private static final String COLUMNAS =
            "id_cliente, nombre, apellido, email, password_hash, telefono, direccion, fecha_registro";

    private static final String SQL_INSERTAR = """
            INSERT INTO cliente (nombre, apellido, email, password_hash, telefono, direccion)
            VALUES (?, ?, ?, ?, ?, ?)
            RETURNING id_cliente, fecha_registro
            """;

    private static final String SQL_BUSCAR_POR_ID =
            "SELECT " + COLUMNAS + " FROM cliente WHERE id_cliente = ?";

    private static final String SQL_BUSCAR_POR_EMAIL =
            "SELECT " + COLUMNAS + " FROM cliente WHERE email = ?";

    @Override
    public Cliente insertar(Cliente cliente) {
        ReentrantLock lock = conexionBD.getLock();
        lock.lock();
        try (PreparedStatement ps = conexionBD.getConexion().prepareStatement(SQL_INSERTAR)) {
            ps.setString(1, cliente.getNombre());
            ps.setString(2, cliente.getApellido());
            ps.setString(3, cliente.getEmail());
            ps.setString(4, cliente.getPasswordHash());
            ps.setString(5, cliente.getTelefono());
            ps.setString(6, cliente.getDireccion());

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("INSERT ... RETURNING no devolvió filas");
                }
                cliente.setIdCliente(rs.getInt("id_cliente"));
                cliente.setFechaRegistro(toLocalDateTime(rs.getTimestamp("fecha_registro")));
            }
            return cliente;
        } catch (SQLException e) {
            if (UNIQUE_VIOLATION.equals(e.getSQLState())) {
                log.warn("Intento de registrar un email ya existente");
                throw ApiException.conflict("El email ya está registrado");
            }
            throw traducir(e, "insertar el cliente");
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Optional<Cliente> buscarPorId(int idCliente) {
        ReentrantLock lock = conexionBD.getLock();
        lock.lock();
        try (PreparedStatement ps = conexionBD.getConexion().prepareStatement(SQL_BUSCAR_POR_ID)) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw traducir(e, "buscar el cliente por id");
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Optional<Cliente> buscarPorEmail(String email) {
        ReentrantLock lock = conexionBD.getLock();
        lock.lock();
        try (PreparedStatement ps = conexionBD.getConexion().prepareStatement(SQL_BUSCAR_POR_EMAIL)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw traducir(e, "buscar el cliente por email");
        } finally {
            lock.unlock();
        }
    }

    /** Mapeo manual ResultSet -> Cliente. */
    private static Cliente mapear(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setIdCliente(rs.getInt("id_cliente"));
        c.setNombre(rs.getString("nombre"));
        c.setApellido(rs.getString("apellido"));
        c.setEmail(rs.getString("email"));
        c.setPasswordHash(rs.getString("password_hash"));
        c.setTelefono(rs.getString("telefono"));
        c.setDireccion(rs.getString("direccion"));
        c.setFechaRegistro(toLocalDateTime(rs.getTimestamp("fecha_registro")));
        return c;
    }
}
