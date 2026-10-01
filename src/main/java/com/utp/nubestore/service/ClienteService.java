package com.utp.nubestore.service;

import com.utp.nubestore.dao.ClienteDAO;
import com.utp.nubestore.dao.factory.DAOFactory;
import com.utp.nubestore.dto.request.LoginRequest;
import com.utp.nubestore.dto.request.RegistroClienteRequest;
import com.utp.nubestore.dto.response.AuthResponse;
import com.utp.nubestore.dto.response.ClienteResponse;
import com.utp.nubestore.exception.ApiException;
import com.utp.nubestore.model.Cliente;
import com.utp.nubestore.util.PasswordUtil;
import com.utp.nubestore.util.Validador;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

/**
 * Lógica de negocio de clientes: registro, autenticación y consulta de perfil.
 */
@Service
public class ClienteService {

    /**
     * Hash usado cuando el email no existe, para que el login tarde lo mismo
     * exista o no la cuenta (evita revelar qué emails están registrados).
     */
    private static final String HASH_FICTICIO = PasswordUtil.hashear("contrasena-ficticia");

    private final ClienteDAO clienteDAO;

    public ClienteService() {
        this(DAOFactory.getClienteDAO());
    }

    public ClienteService(ClienteDAO clienteDAO) {
        this.clienteDAO = clienteDAO;
    }

    public AuthResponse registrar(RegistroClienteRequest req) {
        if (req == null) {
            throw ApiException.badRequest("El cuerpo de la solicitud es obligatorio");
        }
        String nombre = Validador.texto(req.nombre(), "nombre", 80);
        String apellido = Validador.texto(req.apellido(), "apellido", 80);
        String email = Validador.email(req.email());
        String password = validarPassword(req.password());
        String telefono = Validador.telefonoOpcional(req.telefono(), "telefono");
        String direccion = Validador.textoOpcional(req.direccion(), "direccion", 255);

        if (clienteDAO.buscarPorEmail(email).isPresent()) {
            throw ApiException.conflict("El email ya está registrado");
        }

        Cliente cliente = new Cliente();
        cliente.setNombre(nombre);
        cliente.setApellido(apellido);
        cliente.setEmail(email);
        cliente.setPasswordHash(PasswordUtil.hashear(password));
        cliente.setTelefono(telefono);
        cliente.setDireccion(direccion);

        Cliente guardado = clienteDAO.insertar(cliente);
        return new AuthResponse("Registro exitoso", ClienteResponse.desde(guardado));
    }

    public AuthResponse autenticar(LoginRequest req) {
        if (req == null) {
            throw ApiException.badRequest("El cuerpo de la solicitud es obligatorio");
        }
        String email = req.email() == null ? "" : req.email().trim().toLowerCase(Locale.ROOT);
        String password = req.password();
        if (email.isEmpty() || password == null || password.isEmpty()) {
            throw ApiException.badRequest("El email y la contraseña son obligatorios");
        }

        Optional<Cliente> encontrado = clienteDAO.buscarPorEmail(email);
        String hashAlmacenado = encontrado.map(Cliente::getPasswordHash).orElse(HASH_FICTICIO);
        boolean coincide = PasswordUtil.verificar(password, hashAlmacenado);

        if (encontrado.isEmpty() || !coincide) {
            throw ApiException.unauthorized("Email o contraseña incorrectos");
        }
        return new AuthResponse("Inicio de sesión exitoso", ClienteResponse.desde(encontrado.get()));
    }

    public ClienteResponse obtenerPorId(int idCliente) {
        Cliente cliente = clienteDAO.buscarPorId(idCliente)
                .orElseThrow(() -> ApiException.notFound("El cliente no existe"));
        return ClienteResponse.desde(cliente);
    }

    private String validarPassword(String password) {
        if (password == null || password.isEmpty()) {
            throw ApiException.badRequest("El campo 'password' es obligatorio");
        }
        if (password.length() < 8 || password.length() > 128) {
            throw ApiException.badRequest("La contraseña debe tener entre 8 y 128 caracteres");
        }
        return password; // no se recorta: los espacios forman parte de la contraseña
    }
}
