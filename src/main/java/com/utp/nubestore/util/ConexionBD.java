package com.utp.nubestore.util;

import com.utp.nubestore.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantLock;

/**
 * PATRÓN SINGLETON: centraliza una única conexión JDBC a PostgreSQL.
 *
 * - Constructor privado: nadie más puede crear instancias.
 * - getInstance(): acceso global con double-checked locking (campo volatile).
 * - getConexion(): devuelve la conexión única y la restablece si se cerró o cayó.
 * - getLock(): como la conexión es compartida entre hilos, TODA operación de los
 *   DAOs debe adquirir este lock. Así una transacción (setAutoCommit(false) ...
 *   commit/rollback) nunca se mezcla con otra petición concurrente.
 *
 * La configuración se lee de application.properties (nubestore.db.*) y puede
 * sobrescribirse con las variables de entorno NUBESTORE_DB_URL,
 * NUBESTORE_DB_USER y NUBESTORE_DB_PASSWORD.
 */
public final class ConexionBD {

    private static final Logger log = LoggerFactory.getLogger(ConexionBD.class);
    private static final String ARCHIVO_CONFIG = "application.properties";

    private static volatile ConexionBD instancia;

    private final String url;
    private final String usuario;
    private final String clave;
    private final ReentrantLock lock = new ReentrantLock(true);

    private Connection conexion;

    private ConexionBD() {
        Properties props = cargarPropiedades();
        this.url = obtener("NUBESTORE_DB_URL", "nubestore.db.url", props);
        this.usuario = obtener("NUBESTORE_DB_USER", "nubestore.db.username", props);
        this.clave = obtener("NUBESTORE_DB_PASSWORD", "nubestore.db.password", props);

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("No se encontró el driver JDBC de PostgreSQL", e);
        }

        Runtime.getRuntime().addShutdownHook(
                new Thread(this::cerrarConexion, "nubestore-db-shutdown"));
    }

    public static ConexionBD getInstance() {
        if (instancia == null) {
            synchronized (ConexionBD.class) {
                if (instancia == null) {
                    instancia = new ConexionBD();
                }
            }
        }
        return instancia;
    }

    /**
     * Devuelve la conexión única. Si no existe, se cerró o dejó de ser válida,
     * se abre una nueva.
     */
    public synchronized Connection getConexion() {
        try {
            if (conexion == null || conexion.isClosed() || !conexion.isValid(2)) {
                log.info("Abriendo conexión JDBC a PostgreSQL...");
                cerrarSilenciosamente();
                conexion = DriverManager.getConnection(url, usuario, clave);
                conexion.setAutoCommit(true);
            }
            return conexion;
        } catch (SQLException e) {
            log.error("No se pudo conectar a la base de datos (SQLState={})", e.getSQLState(), e);
            throw ApiException.internal("No se pudo establecer conexión con la base de datos", e);
        }
    }

    /** Lock que los DAOs deben adquirir en cada operación (ver Javadoc de la clase). */
    public ReentrantLock getLock() {
        return lock;
    }

    public synchronized void cerrarConexion() {
        cerrarSilenciosamente();
        log.info("Conexión JDBC cerrada");
    }

    private void cerrarSilenciosamente() {
        if (conexion != null) {
            try {
                conexion.close();
            } catch (SQLException e) {
                log.warn("Error al cerrar la conexión: {}", e.getMessage());
            } finally {
                conexion = null;
            }
        }
    }

    private static Properties cargarPropiedades() {
        Properties props = new Properties();
        try (InputStream in = ConexionBD.class.getClassLoader().getResourceAsStream(ARCHIVO_CONFIG)) {
            if (in == null) {
                throw new IllegalStateException("No se encontró " + ARCHIVO_CONFIG + " en el classpath");
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer " + ARCHIVO_CONFIG, e);
        }
        return props;
    }

    private static String obtener(String variableEntorno, String clavePropiedad, Properties props) {
        String valor = System.getenv(variableEntorno);
        if (valor == null || valor.isBlank()) {
            valor = props.getProperty(clavePropiedad);
        }
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException("Falta la configuración '" + clavePropiedad + "'");
        }
        return valor.trim();
    }
}
