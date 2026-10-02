package com.utp.nubestore.util;

import com.utp.nubestore.exception.ApiException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Validaciones de entrada reutilizables por los Services. Como el proyecto solo incluye
 * Spring Web, no hay Bean Validation: cada método lanza ApiException 400 con un mensaje claro.
 */
public final class Validador {

    private static final Pattern EMAIL =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern TELEFONO = Pattern.compile("^[0-9+()\\- ]{6,20}$");
    private static final BigDecimal MAX_MONTO = new BigDecimal("99999999.99"); // NUMERIC(10,2)

    private Validador() {
    }

    /** Texto obligatorio: no nulo, no vacío, con longitud máxima. Devuelve el texto sin espacios laterales. */
    public static String texto(String valor, String campo, int max) {
        if (valor == null || valor.isBlank()) {
            throw ApiException.badRequest("El campo '" + campo + "' es obligatorio");
        }
        String limpio = valor.trim();
        if (limpio.length() > max) {
            throw ApiException.badRequest("El campo '" + campo + "' no puede superar " + max + " caracteres");
        }
        return limpio;
    }

    /** Texto opcional: devuelve null si viene vacío. */
    public static String textoOpcional(String valor, String campo, int max) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return texto(valor, campo, max);
    }

    public static String email(String valor) {
        String limpio = texto(valor, "email", 120).toLowerCase(Locale.ROOT);
        if (!EMAIL.matcher(limpio).matches()) {
            throw ApiException.badRequest("El email no tiene un formato válido");
        }
        return limpio;
    }

    public static String telefonoOpcional(String valor, String campo) {
        String limpio = textoOpcional(valor, campo, 20);
        if (limpio != null && !TELEFONO.matcher(limpio).matches()) {
            throw ApiException.badRequest("El campo '" + campo + "' no tiene un formato válido");
        }
        return limpio;
    }

    /** URL opcional: solo se aceptan http/https (evita esquemas como javascript:). */
    public static String urlOpcional(String valor, String campo) {
        String limpio = textoOpcional(valor, campo, 255);
        if (limpio == null) {
            return null;
        }
        try {
            URI uri = URI.create(limpio);
            String esquema = uri.getScheme();
            boolean esHttp = "http".equalsIgnoreCase(esquema) || "https".equalsIgnoreCase(esquema);
            if (!esHttp || uri.getHost() == null) {
                throw new IllegalArgumentException("URL no permitida");
            }
        } catch (IllegalArgumentException e) {
            throw ApiException.badRequest("El campo '" + campo + "' debe ser una URL http o https válida");
        }
        return limpio;
    }

    public static int entero(Integer valor, String campo, int min, int max) {
        if (valor == null) {
            throw ApiException.badRequest("El campo '" + campo + "' es obligatorio");
        }
        if (valor < min || valor > max) {
            throw ApiException.badRequest("El campo '" + campo + "' debe estar entre " + min + " y " + max);
        }
        return valor;
    }

    public static int enteroPositivo(Integer valor, String campo) {
        return entero(valor, campo, 1, Integer.MAX_VALUE);
    }

    /** Monto obligatorio: mayor que 0, máximo 2 decimales y dentro del rango de NUMERIC(10,2). */
    public static BigDecimal monto(BigDecimal valor, String campo) {
        if (valor == null) {
            throw ApiException.badRequest("El campo '" + campo + "' es obligatorio");
        }
        if (valor.signum() <= 0) {
            throw ApiException.badRequest("El campo '" + campo + "' debe ser mayor a 0");
        }
        if (valor.stripTrailingZeros().scale() > 2) {
            throw ApiException.badRequest("El campo '" + campo + "' admite como máximo 2 decimales");
        }
        if (valor.compareTo(MAX_MONTO) > 0) {
            throw ApiException.badRequest("El campo '" + campo + "' supera el máximo permitido");
        }
        return valor.setScale(2, RoundingMode.UNNECESSARY);
    }
}
