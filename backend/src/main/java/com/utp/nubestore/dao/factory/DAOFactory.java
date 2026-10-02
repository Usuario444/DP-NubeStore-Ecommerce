package com.utp.nubestore.dao.factory;

import com.utp.nubestore.dao.ClienteDAO;
import com.utp.nubestore.dao.PedidoDAO;
import com.utp.nubestore.dao.ProductoDAO;
import com.utp.nubestore.dao.VendedorDAO;
import com.utp.nubestore.dao.impl.ClienteDAOImpl;
import com.utp.nubestore.dao.impl.PedidoDAOImpl;
import com.utp.nubestore.dao.impl.ProductoDAOImpl;
import com.utp.nubestore.dao.impl.VendedorDAOImpl;

/**
 * PATRÓN FACTORY: único lugar que conoce las clases concretas de los DAOs.
 *
 * Los Services piden un DAO a la fábrica y solo ven la interfaz; si mañana
 * cambia la tecnología de persistencia, solo se modifica esta clase.
 * Los DAOs no guardan estado (la conexión vive en el Singleton ConexionBD),
 * por lo que crear una instancia por solicitud es seguro y barato.
 */
public final class DAOFactory {

    private DAOFactory() {
        // Clase utilitaria: no se instancia.
    }

    public static ProductoDAO getProductoDAO() {
        return new ProductoDAOImpl();
    }

    public static PedidoDAO getPedidoDAO() {
        return new PedidoDAOImpl();
    }

    public static ClienteDAO getClienteDAO() {
        return new ClienteDAOImpl();
    }

    public static VendedorDAO getVendedorDAO() {
        return new VendedorDAOImpl();
    }
}