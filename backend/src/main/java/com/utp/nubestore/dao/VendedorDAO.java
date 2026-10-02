package com.utp.nubestore.dao;
import com.utp.nubestore.model.Vendedor;
import java.util.Optional;
public interface VendedorDAO {
    Optional<Vendedor> buscarPorEmail(String email);
}
