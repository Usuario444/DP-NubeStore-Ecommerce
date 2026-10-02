/**
 * @file config.js
 * @description Módulo central de configuración para NubeStore Frontend.
 *              Contiene la URL base de la API, rutas de endpoints y constantes globales.
 *              Importar desde aquí para evitar cadenas mágicas dispersas en el proyecto.
 * @module config
 */

// ─────────────────────────────────────────────
//  URL Base de la API
// ─────────────────────────────────────────────

/**
 * URL base del backend Spring Boot.
 * En desarrollo apunta a localhost:8080.
 * @constant {string}
 */
export const API_BASE_URL = 'http://localhost:8080';

// ─────────────────────────────────────────────
//  Endpoints de la API (relativos a API_BASE_URL)
// ─────────────────────────────────────────────

/**
 * Mapa de endpoints agrupados por dominio.
 * Cada propiedad corresponde a un controller del backend.
 * @constant {Object}
 */
export const ENDPOINTS = Object.freeze({

    /** Autenticación — AuthController */
    AUTH: {
        LOGIN:    '/api/auth/login',
        REGISTRO: '/api/auth/registro',
    },

    /** Productos — ProductoController */
    PRODUCTOS: {
        BASE:      '/api/productos',           // GET (listado) | POST (publicar)
        POR_ID:    '/api/productos',            // GET /:id
        BUSCAR:    '/api/productos/buscar',     // GET ?nombre=&categoria=&precioMin=&precioMax=
    },

    /** Pedidos — PedidoController */
    PEDIDOS: {
        BASE:      '/api/pedidos',             // POST (crear pedido)
        POR_ID:    '/api/pedidos',             // GET /:id
        CLIENTE:   '/api/pedidos/cliente',      // GET /:idCliente
    },

    /** Clientes — ClienteController */
    CLIENTES: {
        BASE:      '/api/clientes',            // GET | POST
        POR_ID:    '/api/clientes',            // GET /:id
    },

    /** Devoluciones */
    DEVOLUCIONES: {
        SOLICITAR: '/api/pedidos',             // POST /:idPedido/devolucion
    },
});

// ─────────────────────────────────────────────
//  Constantes de la Aplicación
// ─────────────────────────────────────────────

/**
 * Claves utilizadas en localStorage para la sesión del usuario.
 * Centralizar aquí evita errores por typos en otras partes del código.
 * @constant {Object}
 */
export const STORAGE_KEYS = Object.freeze({
    ID_CLIENTE: 'nubestore_idCliente',
    NOMBRE:     'nubestore_nombre',
    APELLIDO:   'nubestore_apellido',
    EMAIL:      'nubestore_email',
    ROL:        'nubestore_rol',
    CARRITO:    'nubestore_carrito',
    TEMA:       'nubestore_tema',
});

/**
 * Roles de usuario reconocidos por la aplicación.
 * @constant {Object}
 */
export const ROLES = Object.freeze({
    CLIENTE:  'CLIENTE',
    VENDEDOR: 'VENDEDOR',
});

/**
 * Configuración regional para formateo de moneda y fechas.
 * @constant {Object}
 */
export const LOCALE = Object.freeze({
    CODIGO:   'es-PE',
    MONEDA:   'PEN',
    SIMBOLO:  'S/',
    ZONA:     'America/Lima',
});

/**
 * Tiempos de espera y duraciones (en milisegundos).
 * @constant {Object}
 */
export const TIEMPOS = Object.freeze({
    /** Duración por defecto de toasts/alertas (ms) */
    TOAST_DURACION: 4000,
    /** Timeout máximo para peticiones HTTP (ms) */
    HTTP_TIMEOUT:   15000,
});
