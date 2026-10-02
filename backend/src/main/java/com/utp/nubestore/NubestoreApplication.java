package com.utp.nubestore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de NubeStore.
 * No hay DataSource ni EntityManager de Spring: el acceso a datos
 * se hace con JDBC puro a través de {@code ConexionBD} (Singleton).
 */
@SpringBootApplication
public class NubestoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(NubestoreApplication.class, args);
    }
}