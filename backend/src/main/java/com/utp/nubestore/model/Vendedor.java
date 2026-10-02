package com.utp.nubestore.model;

import java.time.LocalDateTime;

/**
 * Entidad de dominio Vendedor.
 * En el Avance 1 solo se usa como dueño de los productos publicados.
 */
public class Vendedor {

    private Integer idVendedor;
    private String nombreTienda;
    private String email;
    private String passwordHash;
    private String telefono;
    private LocalDateTime fechaRegistro;

    public Vendedor() {
    }

    public Integer getIdVendedor() { return idVendedor; }
    public void setIdVendedor(Integer idVendedor) { this.idVendedor = idVendedor; }

    public String getNombreTienda() { return nombreTienda; }
    public void setNombreTienda(String nombreTienda) { this.nombreTienda = nombreTienda; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}