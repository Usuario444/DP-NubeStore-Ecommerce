package com.utp.nubestore.controller;

import com.utp.nubestore.dto.request.CrearPedidoRequest;
import com.utp.nubestore.dto.request.SolicitarDevolucionRequest;
import com.utp.nubestore.dto.response.DevolucionResponse;
import com.utp.nubestore.dto.response.PedidoResponse;
import com.utp.nubestore.service.PedidoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * GRASP Controlador para los casos de uso del Cliente sobre pedidos:
 * comprar, hacer seguimiento y solicitar devoluciones.
 */
@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    /** POST /api/pedidos -> 201 Created (compra del carrito en una transacción) */
    @PostMapping
    public ResponseEntity<PedidoResponse> crearPedido(@RequestBody CrearPedidoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.crearPedido(request));
    }

    /** GET /api/pedidos/cliente/{idCliente} -> pedidos del cliente, del más reciente al más antiguo */
    @GetMapping("/cliente/{idCliente}")
    public ResponseEntity<List<PedidoResponse>> listarPedidos(@PathVariable("idCliente") int idCliente) {
        return ResponseEntity.ok(pedidoService.listarPedidos(idCliente));
    }

    /** GET /api/pedidos/{idPedido}?idCliente=1 -> seguimiento de un pedido */
    @GetMapping("/{idPedido}")
    public ResponseEntity<PedidoResponse> obtenerSeguimiento(
            @PathVariable("idPedido") int idPedido,
            @RequestParam("idCliente") int idCliente) {
        return ResponseEntity.ok(pedidoService.obtenerSeguimiento(idPedido, idCliente));
    }

    /** POST /api/pedidos/devoluciones -> 201 Created */
    @PostMapping("/devoluciones")
    public ResponseEntity<DevolucionResponse> solicitarDevolucion(
            @RequestBody SolicitarDevolucionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.solicitarDevolucion(request));
    }

    /** GET /api/pedidos/devoluciones/cliente/{idCliente} */
    @GetMapping("/devoluciones/cliente/{idCliente}")
    public ResponseEntity<List<DevolucionResponse>> listarDevoluciones(
            @PathVariable("idCliente") int idCliente) {
        return ResponseEntity.ok(pedidoService.listarDevoluciones(idCliente));
    }
}
