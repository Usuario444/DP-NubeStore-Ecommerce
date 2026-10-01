package com.utp.nubestore.model;

import java.time.LocalDateTime;

/**
 * Entidad de dominio Cliente (POJO puro, sin anotaciones de ORM).
 * El mapeo desde/hacia la tabla "cliente" se hace manualmente en ClienteDAOImpl.
 */
public class Cliente {

    private Integer idCliente;
    private String nombre;
    private String apellido;
    private String email;
    private String passwordHash;
    private String telefono;
    private String direccion;
    private LocalDateTime fechaRegistro;

    public Cliente() {
    }

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    public Integer getIdCliente() { return idCliente; }
    public void setIdCliente(Integer idCliente) { this.idCliente = idCliente; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}