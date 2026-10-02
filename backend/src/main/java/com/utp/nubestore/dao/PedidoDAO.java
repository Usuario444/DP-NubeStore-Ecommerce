package com.utp.nubestore.dao;

import com.utp.nubestore.model.Devolucion;
import com.utp.nubestore.model.Pedido;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de acceso a datos para pedidos y devoluciones
 * (la devolución es parte del ciclo de vida del pedido).
 */
public interface PedidoDAO {

    /**
     * Registra un pedido completo en UNA transacción JDBC:
     * bloquea y valida cada producto, descuenta stock, inserta la cabecera
     * y todos los detalles. Si algo falla, hace rollback de todo.
     * Cada detalle debe traer idProducto y cantidad; el precio lo toma de la BD.
     *
     * @throws com.utp.nubestore.exception.ApiException 404 si un producto no existe,
     *         409 si no hay stock suficiente
     */
    Pedido registrarPedido(Pedido pedido);

    /** Pedido (con detalles) que pertenece al cliente indicado. */
    Optional<Pedido> buscarPorIdYCliente(int idPedido, int idCliente);

    /** Pedidos del cliente (con detalles), del más reciente al más antiguo. */
    List<Pedido> listarPorCliente(int idCliente);

    /** Pedido (con detalles) al que pertenece el detalle indicado. */
    Optional<Pedido> buscarPorDetalle(int idDetalle);

    /** Unidades de un detalle con devolución solicitada, aprobada o completada. */
    int contarUnidadesDevueltas(int idDetalle);

    /**
     * Inserta la devolución solo si, en el mismo instante, el pedido es del cliente,
     * está ENTREGADO y la cantidad no excede lo comprado menos lo ya devuelto
     * (un único INSERT ... SELECT atómico).
     *
     * @return la devolución registrada, o vacío si ya no cumple las condiciones
     */
    Optional<Devolucion> registrarDevolucion(Devolucion devolucion);

    List<Devolucion> listarDevolucionesPorCliente(int idCliente);
}