package com.utp.nubestore.controller;

import com.utp.nubestore.dto.request.LoginRequest;
import com.utp.nubestore.dto.request.RegistroClienteRequest;
import com.utp.nubestore.dto.response.AuthResponse;
import com.utp.nubestore.service.ClienteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * GRASP Controlador: recibe la solicitud HTTP, delega en el Service y devuelve el DTO.
 * No contiene lógica de negocio ni acceso a datos.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final ClienteService clienteService;

    public AuthController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    /** POST /api/auth/registro -> 201 Created */
    @PostMapping("/registro")
    public ResponseEntity<AuthResponse> registrar(@RequestBody RegistroClienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.registrar(request));
    }

    /** POST /api/auth/login -> 200 OK */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(clienteService.autenticar(request));
    }
}
