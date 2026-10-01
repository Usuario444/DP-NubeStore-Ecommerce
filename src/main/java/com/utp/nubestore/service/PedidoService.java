package com.utp.nubestore.service;

import com.utp.nubestore.dao.ClienteDAO;
import com.utp.nubestore.dao.PedidoDAO;
import com.utp.nubestore.dao.factory.DAOFactory;
import com.utp.nubestore.dto.request.CrearPedidoRequest;
import com.utp.nubestore.dto.request.ItemPedidoRequest;
import com.utp.nubestore.dto.request.SolicitarDevolucionRequest;
import com.utp.nubestore.dto.response.DevolucionResponse;
import com.utp.nubestore.dto.response.PedidoResponse;
import com.utp.nubestore.exception.ApiException;
import com.utp.nubestore.model.Cliente;
import com.utp.nubestore.model.DetallePedido;
import com.utp.nubestore.model.Devolucion;
import com.utp.nubestore.model.Pedido;
import com.utp.nubestore.util.Validador;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Lógica de negocio de pedidos: compra, seguimiento y devoluciones del Cliente.
 * La atomicidad de la compra (pedido + detalles + stock) la garantiza la transacción
 * JDBC de PedidoDAO; aquí se validan los datos y se aplican las reglas de negocio.
 */
@Service
public class PedidoService {

    private static final int MAX_LINEAS_POR_PEDIDO = 50;
    private static final int MAX_UNIDADES_POR_PRODUCTO = 100;
    private static final int MIN_LONGITUD_MOTIVO = 5;

    private final PedidoDAO pedidoDAO;
    private final ClienteDAO clienteDAO;

    public PedidoService() {
        this(DAOFactory.getPedidoDAO(), DAOFactory.getClienteDAO());
    }

    public PedidoService(PedidoDAO pedidoDAO, ClienteDAO clienteDAO) {
        this.pedidoDAO = pedidoDAO;
        this.clienteDAO = clienteDAO;
    }

    // ---------------------------------------------------------------- Compra

    public PedidoResponse crearPedido(CrearPedidoRequest req) {
        if (req == null) {
            throw ApiException.badRequest("El cuerpo de la solicitud es obligatorio");
        }
        int idCliente = Validador.enteroPositivo(req.idCliente(), "idCliente");
        Map<Integer, Integer> cantidades = consolidarItems(req.items());

        Cliente cliente = clienteDAO.buscarPorId(idCliente)
                .orElseThrow(() -> ApiException.notFound("El cliente no existe"));

        String direccion = Validador.textoOpcional(req.direccionEnvio(), "direccionEnvio", 255);
        if (direccion == null) {
            direccion = cliente.getDireccion();
        }
        if (direccion == null || direccion.isBlank()) {
            throw ApiException.badRequest("Debe indicar una dirección de envío");
        }

        Pedido pedido = new Pedido();
        pedido.setIdCliente(idCliente);
        pedido.setDireccionEnvio(direccion);
        pedido.setCodigoSeguimiento(generarCodigoSeguimiento());
        cantidades.forEach((idProducto, cantidad) ->
                pedido.agregarDetalle(new DetallePedido(idProducto, cantidad, null)));

        return PedidoResponse.desde(pedidoDAO.registrarPedido(pedido));
    }

    /** Valida los ítems y suma las cantidades si un mismo producto aparece más de una vez. */
    private Map<Integer, Integer> consolidarItems(List<ItemPedidoRequest> items) {
        if (items == null || items.isEmpty()) {
            throw ApiException.badRequest("El pedido debe incluir al menos un producto");
        }
        if (items.size() > MAX_LINEAS_POR_PEDIDO) {
            throw ApiException.badRequest("Un pedido admite como máximo " + MAX_LINEAS_POR_PEDIDO + " productos");
        }
        Map<Integer, Integer> cantidades = new LinkedHashMap<>();
        for (ItemPedidoRequest item : items) {
            if (item == null) {
                throw ApiException.badRequest("La lista de items contiene un elemento vacío");
            }
            int idProducto = Validador.enteroPositivo(item.idProducto(), "items.idProducto");
            int cantidad = Validador.entero(item.cantidad(), "items.cantidad", 1, MAX_UNIDADES_POR_PRODUCTO);
            cantidades.merge(idProducto, cantidad, Integer::sum);
        }
        for (Map.Entry<Integer, Integer> entrada : cantidades.entrySet()) {
            if (entrada.getValue() > MAX_UNIDADES_POR_PRODUCTO) {
                throw ApiException.badRequest("Máximo " + MAX_UNIDADES_POR_PRODUCTO
                        + " unidades por producto (producto " + entrada.getKey() + ")");
            }
        }
        return cantidades;
    }

    /** Ejemplo: NS-20261001-A1B2C3D4 */
    private String generarCodigoSeguimiento() {
        String fecha = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String aleatorio = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "NS-" + fecha + "-" + aleatorio;
    }

    // ------------------------------------------------------- Seguimiento

    public List<PedidoResponse> listarPedidos(int idCliente) {
        verificarClienteExiste(idCliente);
        return pedidoDAO.listarPorCliente(idCliente).stream()
                .map(PedidoResponse::desde)
                .toList();
    }

    public PedidoResponse obtenerSeguimiento(int idPedido, int idCliente) {
        // Si el pedido existe pero es de otro cliente, se responde igual "no encontrado".
        Pedido pedido = pedidoDAO.buscarPorIdYCliente(idPedido, idCliente)
                .orElseThrow(() -> ApiException.notFound("El pedido no existe"));
        return PedidoResponse.desde(pedido);
    }

    // ------------------------------------------------------ Devoluciones

    public DevolucionResponse solicitarDevolucion(SolicitarDevolucionRequest req) {
        if (req == null) {
            throw ApiException.badRequest("El cuerpo de la solicitud es obligatorio");
        }
        int idCliente = Validador.enteroPositivo(req.idCliente(), "idCliente");
        int idDetalle = Validador.enteroPositivo(req.idDetalle(), "idDetalle");
        int cantidad = Validador.entero(req.cantidad(), "cantidad", 1, MAX_UNIDADES_POR_PRODUCTO);
        String motivo = Validador.texto(req.motivo(), "motivo", 255);
        if (motivo.length() < MIN_LONGITUD_MOTIVO) {
            throw ApiException.badRequest("El motivo debe tener al menos " + MIN_LONGITUD_MOTIVO + " caracteres");
        }

        Pedido pedido = pedidoDAO.buscarPorDetalle(idDetalle)
                .filter(p -> p.getIdCliente() == idCliente) // un pedido ajeno se trata como inexistente
                .orElseThrow(() -> ApiException.notFound("El producto del pedido indicado no existe"));

        if (!pedido.estaEntregado()) {
            throw ApiException.unprocessable("Solo se pueden devolver productos de pedidos entregados");
        }

        DetallePedido detalle = pedido.getDetalles().stream()
                .filter(d -> d.getIdDetalle() == idDetalle)
                .findFirst()
                .orElseThrow(() -> ApiException.notFound("El producto del pedido indicado no existe"));

        int disponibles = detalle.getCantidad() - pedidoDAO.contarUnidadesDevueltas(idDetalle);
        if (cantidad > disponibles) {
            throw ApiException.unprocessable(disponibles <= 0
                    ? "Este producto ya tiene devoluciones por todas las unidades compradas"
                    : "Solo puede devolver hasta " + disponibles + " unidad(es) de este producto");
        }

        Devolucion devolucion = new Devolucion();
        devolucion.setIdDetalle(idDetalle);
        devolucion.setIdCliente(idCliente);
        devolucion.setCantidad(cantidad);
        devolucion.setMotivo(motivo);

        // El INSERT ... SELECT del DAO vuelve a verificar las condiciones de forma atómica.
        Devolucion registrada = pedidoDAO.registrarDevolucion(devolucion)
                .orElseThrow(() -> ApiException.conflict(
                        "La devolución ya no es válida; revise el estado del pedido y las unidades disponibles"));
        return DevolucionResponse.desde(registrada);
    }

    public List<DevolucionResponse> listarDevoluciones(int idCliente) {
        verificarClienteExiste(idCliente);
        return pedidoDAO.listarDevolucionesPorCliente(idCliente).stream()
                .map(DevolucionResponse::desde)
                .toList();
    }

    private void verificarClienteExiste(int idCliente) {
        if (clienteDAO.buscarPorId(idCliente).isEmpty()) {
            throw ApiException.notFound("El cliente no existe");
        }
    }
}
