package com.utp.nubestore.dto.response;
import com.utp.nubestore.model.Vendedor;
import java.time.LocalDateTime;
public record VendedorResponse(Integer idVendedor, String nombreTienda, String email, String telefono, LocalDateTime fechaRegistro) {
    public static VendedorResponse desde(Vendedor v) {
        return new VendedorResponse(v.getIdVendedor(), v.getNombreTienda(), v.getEmail(), v.getTelefono(), v.getFechaRegistro());
    }
}
