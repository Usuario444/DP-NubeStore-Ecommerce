package com.utp.nubestore.service;
import com.utp.nubestore.dao.VendedorDAO;
import com.utp.nubestore.dao.factory.DAOFactory;
import com.utp.nubestore.dto.request.LoginRequest;
import com.utp.nubestore.dto.response.AuthVendedorResponse;
import com.utp.nubestore.dto.response.VendedorResponse;
import com.utp.nubestore.exception.ApiException;
import com.utp.nubestore.model.Vendedor;
import com.utp.nubestore.util.PasswordUtil;
import com.utp.nubestore.util.Validador;
import org.springframework.stereotype.Service;
import java.util.Locale;
import java.util.Optional;

@Service
public class VendedorService {
    private static final String HASH_FICTICIO = PasswordUtil.hashear("contrasena-ficticia");
    private final VendedorDAO vendedorDAO;

    public VendedorService() {
        this(DAOFactory.getVendedorDAO());
    }

    public VendedorService(VendedorDAO vendedorDAO) {
        this.vendedorDAO = vendedorDAO;
    }

    public AuthVendedorResponse autenticar(LoginRequest req) {
        if (req == null) throw ApiException.badRequest("Cuerpo de la solicitud obligatorio");
        String email = Validador.email(req.email());
        String password = req.password();
        if (password == null || password.isBlank()) {
            throw ApiException.badRequest("La contraseña es obligatoria");
        }
        Optional<Vendedor> optVendedor = vendedorDAO.buscarPorEmail(email.toLowerCase(Locale.ROOT));
        String hashAVerificar = optVendedor.map(Vendedor::getPasswordHash).orElse(HASH_FICTICIO);
        boolean pwdValido = PasswordUtil.verificar(password, hashAVerificar);
        if (optVendedor.isEmpty() || !pwdValido) {
            throw ApiException.unauthorized("Credenciales incorrectas");
        }
        return new AuthVendedorResponse("Login de vendedor exitoso", VendedorResponse.desde(optVendedor.get()));
    }
}
