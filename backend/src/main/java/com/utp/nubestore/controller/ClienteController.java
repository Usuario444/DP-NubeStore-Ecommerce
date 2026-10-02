package com.utp.nubestore.controller;

import com.utp.nubestore.dto.response.ClienteResponse;
import com.utp.nubestore.service.ClienteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * GRASP Controlador para datos del cliente.
 */
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    /** GET /api/clientes/{idCliente} */
    @GetMapping("/{idCliente}")
    public ResponseEntity<ClienteResponse> obtenerPorId(@PathVariable("idCliente") int idCliente) {
        return ResponseEntity.ok(clienteService.obtenerPorId(idCliente));
    }
}
