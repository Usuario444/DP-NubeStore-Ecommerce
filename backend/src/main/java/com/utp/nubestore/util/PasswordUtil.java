package com.utp.nubestore.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Hash de contraseñas utilizando BCrypt.
 * Estándar de la industria para almacenamiento seguro de credenciales.
 */
public final class PasswordUtil {

    private PasswordUtil() {
        // Evita la instanciación de la clase utilitaria
    }

    /**
     * Genera un hash seguro usando BCrypt con un factor de trabajo (log_rounds) de 12.
     */
    public static String hashear(String passwordPlana) {
        return BCrypt.hashpw(passwordPlana, BCrypt.gensalt(12));
    }

    /**
     * Verifica si la contraseña en texto plano coincide con el hash almacenado.
     */
    public static boolean verificar(String passwordPlana, String hashEnBd) {
        // Validación rápida para evitar procesar cadenas nulas o que no sean BCrypt
        if (hashEnBd == null || !hashEnBd.startsWith("$2a$")) {
            return false;
        }
        try {
            return BCrypt.checkpw(passwordPlana, hashEnBd);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}