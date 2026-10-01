package com.utp.nubestore.dao.impl;

import com.utp.nubestore.exception.ApiException;
import com.utp.nubestore.util.ConexionBD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Utilidades compartidas por los DAOs JDBC. NO contiene lógica de acceso a datos:
 * cada DAO escribe sus propios PreparedStatement y try-catch.
 */
abstract class BaseDAO {

    // Códigos SQLState de PostgreSQL
    protected static final String UNIQUE_VIOLATION = "23505";
    protected static final String FK_VIOLATION = "23503";
    protected static final String CHECK_VIOLATION = "23514";
    protected static final String NOT_NULL_VIOLATION = "23502";

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final ConexionBD conexionBD = ConexionBD.getInstance();

    /**
     * Convierte una SQLException en una ApiException con mensaje seguro para el cliente.
     * El detalle técnico solo se escribe en el log.
     */
    protected ApiException traducir(SQLException e, String operacion) {
        log.error("Error SQL al {} (SQLState={}): {}", operacion, e.getSQLState(), e.getMessage(), e);
        String estado = e.getSQLState();
        if (UNIQUE_VIOLATION.equals(estado)) {
            return ApiException.conflict("Ya existe un registro con esos datos");
        }
        if (FK_VIOLATION.equals(estado)) {
            return ApiException.badRequest("La operación referencia un registro que no existe");
        }
        if (CHECK_VIOLATION.equals(estado) || NOT_NULL_VIOLATION.equals(estado)) {
            return ApiException.badRequest("Los datos enviados no cumplen las reglas de la base de datos");
        }
        return ApiException.internal("Error al acceder a la base de datos", e);
    }

    protected void rollback(Connection con) {
        if (con == null) {
            return;
        }
        try {
            if (!con.isClosed()) {
                con.rollback();
                log.warn("Transacción revertida (rollback)");
            }
        } catch (SQLException e) {
            log.error("No se pudo hacer rollback (SQLState={})", e.getSQLState(), e);
        }
    }

    /** Deja la conexión compartida en modo autocommit para las siguientes operaciones. */
    protected void restaurarAutoCommit(Connection con) {
        if (con == null) {
            return;
        }
        try {
            if (!con.isClosed()) {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            log.error("No se pudo restaurar el autocommit", e);
        }
    }

    protected static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }
}