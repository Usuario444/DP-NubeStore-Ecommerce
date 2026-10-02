/**
 * @file session.js
 * @description Módulo de gestión de sesión para NubeStore.
 *              Actúa como ÚNICO punto de acceso a localStorage para los datos
 *              del usuario autenticado. Ningún otro módulo debe leer/escribir
 *              directamente en localStorage para datos de sesión.
 *
 *              Datos almacenados tras login exitoso:
 *              - idCliente (number)
 *              - nombre    (string)
 *              - email     (string)
 *              - rol       (string: 'CLIENTE' | 'VENDEDOR')
 *
 * @module state/session
 */

import { STORAGE_KEYS, ROLES } from '../config.js';

// ─────────────────────────────────────────────
//  Typedefs (documentación de la forma de datos)
// ─────────────────────────────────────────────

/**
 * Respuesta del endpoint de autenticación.
 * Refleja el DTO `AuthResponse` del backend.
 *
 * @typedef  {Object} AuthResponse
 * @property {number} idCliente - ID del cliente autenticado.
 * @property {string} nombre    - Nombre completo del cliente.
 * @property {string} email     - Correo electrónico del cliente.
 * @property {string} rol       - Rol asignado ('CLIENTE' o 'VENDEDOR').
 * @property {string} mensaje   - Mensaje de bienvenida del backend.
 */

/**
 * Datos de sesión almacenados localmente.
 *
 * @typedef  {Object} DatosSesion
 * @property {number} idCliente - ID del cliente.
 * @property {string} nombre    - Nombre completo.
 * @property {string} email     - Correo electrónico.
 * @property {string} rol       - Rol del usuario.
 */

// ─────────────────────────────────────────────
//  Guardar / Eliminar Sesión
// ─────────────────────────────────────────────

/**
 * Persiste los datos de sesión en localStorage tras un login exitoso.
 * Debe invocarse únicamente desde el controlador de login (`pages/login.js`)
 * después de recibir un {@link AuthResponse} exitoso de la API.
 *
 * @param {AuthResponse} authResponse - Respuesta del endpoint `/api/auth/login`.
 * @returns {void}
 *
 * @example
 * import { guardarSesion } from '../state/session.js';
 * const respuesta = await authApi.login(credenciales);
 * guardarSesion(respuesta);
 */
export function guardarSesion(authResponse) {
    const cliente = authResponse.cliente;
    localStorage.setItem(STORAGE_KEYS.ID_CLIENTE, String(cliente.idCliente));
    localStorage.setItem(STORAGE_KEYS.NOMBRE,     cliente.nombre);
    localStorage.setItem(STORAGE_KEYS.APELLIDO,   cliente.apellido || '');
    localStorage.setItem(STORAGE_KEYS.EMAIL,      cliente.email);
    localStorage.setItem(STORAGE_KEYS.ROL,        ROLES.CLIENTE);
}

/**
 * Elimina todos los datos de sesión de localStorage.
 * Incluye también la limpieza del carrito almacenado.
 *
 * @returns {void}
 *
 * @example
 * import { cerrarSesion } from '../state/session.js';
 * cerrarSesion();
 * window.location.href = 'login.html';
 */
export function cerrarSesion() {
    localStorage.removeItem(STORAGE_KEYS.ID_CLIENTE);
    localStorage.removeItem(STORAGE_KEYS.NOMBRE);
    localStorage.removeItem(STORAGE_KEYS.APELLIDO);
    localStorage.removeItem(STORAGE_KEYS.EMAIL);
    localStorage.removeItem(STORAGE_KEYS.ROL);
    localStorage.removeItem(STORAGE_KEYS.CARRITO);
}

// ─────────────────────────────────────────────
//  Consultar Estado de Sesión
// ─────────────────────────────────────────────

/**
 * Verifica si hay una sesión activa (existe un idCliente en storage).
 *
 * @returns {boolean} `true` si el usuario está autenticado.
 */
export function estaAutenticado() {
    return localStorage.getItem(STORAGE_KEYS.ID_CLIENTE) !== null;
}

/**
 * Obtiene todos los datos de la sesión actual.
 *
 * @returns {DatosSesion | null} Datos de sesión o `null` si no hay sesión activa.
 *
 * @example
 * const sesion = obtenerSesion();
 * if (sesion) {
 *     console.log(`Bienvenido, ${sesion.nombre}`);
 * }
 */
export function obtenerSesion() {
    const idCliente = localStorage.getItem(STORAGE_KEYS.ID_CLIENTE);

    if (idCliente === null) {
        return null;
    }

    return {
        idCliente: Number(idCliente),
        nombre:    localStorage.getItem(STORAGE_KEYS.NOMBRE)    || '',
        apellido:  localStorage.getItem(STORAGE_KEYS.APELLIDO)  || '',
        email:     localStorage.getItem(STORAGE_KEYS.EMAIL)     || '',
        rol:       localStorage.getItem(STORAGE_KEYS.ROL)       || ROLES.CLIENTE,
    };
}

// ─────────────────────────────────────────────
//  Getters Individuales
// ─────────────────────────────────────────────

/**
 * Obtiene el ID del cliente autenticado.
 * @returns {number | null} ID numérico o `null` si no hay sesión.
 */
export function obtenerIdCliente() {
    const valor = localStorage.getItem(STORAGE_KEYS.ID_CLIENTE);
    return valor !== null ? Number(valor) : null;
}

/**
 * Obtiene el nombre del usuario autenticado.
 * @returns {string | null} Nombre completo o `null` si no hay sesión.
 */
export function obtenerNombre() {
    return localStorage.getItem(STORAGE_KEYS.NOMBRE);
}

/**
 * Obtiene el email del usuario autenticado.
 * @returns {string | null} Email o `null` si no hay sesión.
 */
export function obtenerEmail() {
    return localStorage.getItem(STORAGE_KEYS.EMAIL);
}

/**
 * Obtiene el rol del usuario autenticado.
 * @returns {string | null} Rol ('CLIENTE' | 'VENDEDOR') o `null` si no hay sesión.
 */
export function obtenerRol() {
    return localStorage.getItem(STORAGE_KEYS.ROL);
}

// ─────────────────────────────────────────────
//  Verificación de Roles
// ─────────────────────────────────────────────

/**
 * Verifica si el usuario actual tiene el rol VENDEDOR.
 * @returns {boolean}
 */
export function esVendedor() {
    return obtenerRol() === ROLES.VENDEDOR;
}

/**
 * Verifica si el usuario actual tiene el rol CLIENTE.
 * @returns {boolean}
 */
export function esCliente() {
    return obtenerRol() === ROLES.CLIENTE;
}

// ─────────────────────────────────────────────
//  Guardias de Navegación
// ─────────────────────────────────────────────

/**
 * Redirige al login si el usuario no está autenticado.
 * Invocar al inicio de páginas que requieren sesión activa
 * (carrito.html, pedidos.html, vendedor.html).
 *
 * @param {string} [urlLogin='login.html'] - URL de la página de login.
 * @returns {boolean} `true` si el usuario está autenticado y puede continuar.
 *
 * @example
 * // Al inicio de pages/pedidos.js
 * import { requiereAutenticacion } from '../state/session.js';
 * if (!requiereAutenticacion()) {
 *     // La redirección ya se ejecutó, no continuar.
 * }
 */
export function requiereAutenticacion(urlLogin = 'login.html') {
    if (!estaAutenticado()) {
        window.location.href = urlLogin;
        return false;
    }
    return true;
}

/**
 * Redirige al inicio si el usuario no tiene el rol requerido.
 * Útil para proteger la vista de vendedor.
 *
 * @param {string} rolRequerido        - Rol necesario (usar constantes de {@link ROLES}).
 * @param {string} [urlRedireccion='index.html'] - URL de redirección si no tiene el rol.
 * @returns {boolean} `true` si el usuario tiene el rol y puede continuar.
 *
 * @example
 * // Al inicio de pages/vendedor.js
 * import { requiereRol } from '../state/session.js';
 * import { ROLES } from '../config.js';
 * if (!requiereRol(ROLES.VENDEDOR)) {
 *     // La redirección ya se ejecutó.
 * }
 */
export function requiereRol(rolRequerido, urlRedireccion = 'index.html') {
    if (!requiereAutenticacion()) {
        return false;
    }

    if (obtenerRol() !== rolRequerido) {
        window.location.href = urlRedireccion;
        return false;
    }

    return true;
}
