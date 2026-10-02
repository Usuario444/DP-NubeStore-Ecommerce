package com.utp.nubestore.dao.impl;
import com.utp.nubestore.dao.VendedorDAO;
import com.utp.nubestore.exception.ApiException;
import com.utp.nubestore.model.Vendedor;
import com.utp.nubestore.util.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class VendedorDAOImpl implements VendedorDAO {
    @Override
    public Optional<Vendedor> buscarPorEmail(String email) {
        String sql = "SELECT id_vendedor, nombre_tienda, email, password_hash, telefono, fecha_registro FROM vendedor WHERE email = ?";
        ConexionBD.getInstance().getLock().lock();
        try (Connection conn = ConexionBD.getInstance().getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Vendedor v = new Vendedor();
                    v.setIdVendedor(rs.getInt("id_vendedor"));
                    v.setNombreTienda(rs.getString("nombre_tienda"));
                    v.setEmail(rs.getString("email"));
                    v.setPasswordHash(rs.getString("password_hash"));
                    v.setTelefono(rs.getString("telefono"));
                    v.setFechaRegistro(rs.getTimestamp("fecha_registro").toLocalDateTime());
                    return Optional.of(v);
                }
            }
        } catch (SQLException e) {
            throw ApiException.internal("Error al buscar vendedor por email", e);
        } finally {
            ConexionBD.getInstance().getLock().unlock();
        }
        return Optional.empty();
    }
}
