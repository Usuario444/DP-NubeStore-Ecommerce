package com.utp.nubestore.dto.response;

public record AuthResponse(
        String mensaje,
        ClienteResponse cliente) {
}
