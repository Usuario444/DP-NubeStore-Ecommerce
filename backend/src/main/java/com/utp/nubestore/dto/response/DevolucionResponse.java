package com.utp.nubestore.dto.response;

import com.utp.nubestore.model.Devolucion;

import java.time.LocalDateTime;

public record DevolucionResponse(
        Integer idDevolucion,
        Integer idDetalle,
        int cantidad,
        String motivo,
        String estado,
        LocalDateTime fechaSolicitud) {

    public static DevolucionResponse desde(Devolucion d) {
        return new DevolucionResponse(
                d.getIdDevolucion(),
                d.getIdDetalle(),
                d.getCantidad(),
                d.getMotivo(),
                d.getEstado(),
                d.getFechaSolicitud());
    }
}
