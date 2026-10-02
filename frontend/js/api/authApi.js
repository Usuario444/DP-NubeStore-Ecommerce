/**
 * @file authApi.js
 * @description Módulo de API para autenticación (login y registro).
 *              Delega todas las peticiones HTTP a {@link module:api/httpClient}.
 * @module api/authApi
 */

import { post } from './httpClient.js';
import { ENDPOINTS } from '../config.js';

// ─────────────────────────────────────────────
//  Typedefs — DTOs del backend
// ─────────────────────────────────────────────

/**
 * Cuerpo de la petición de login.
 * Refleja el DTO `LoginRequest` del backend.
 *
 * @typedef  {Object} LoginRequest
 * @property {string} email    - Correo electrónico del usuario.
 * @property {string} password - Contraseña en texto plano.
 */

/**
 * Cuerpo de la petición de registro.
 * Refleja el DTO `RegistroClienteRequest` del backend.
 *
 * @typedef  {Object} RegistroClienteRequest
 * @property {string} nombre    - Nombre del cliente.
 * @property {string} apellido  - Apellido del cliente.
 * @property {string} email     - Correo electrónico (único).
 * @property {string} password  - Contraseña (mínimo requerido por el backend).
 * @property {string} telefono  - Número de teléfono.
 * @property {string} direccion - Dirección de envío.
 */

/**
 * Respuesta exitosa del login.
 * Refleja el DTO `AuthResponse` del backend.
 *
 * @typedef  {Object} AuthResponse
 * @property {number} idCliente - ID del cliente autenticado.
 * @property {string} nombre    - Nombre completo.
 * @property {string} email     - Correo electrónico.
 * @property {string} rol       - Rol asignado ('CLIENTE' | 'VENDEDOR').
 * @property {string} mensaje   - Mensaje de bienvenida.
 */

// ─────────────────────────────────────────────
//  Funciones de API
// ─────────────────────────────────────────────

/**
 * Inicia sesión con credenciales de email y contraseña.
 *
 * @async
 * @param {LoginRequest} credenciales - Email y password del usuario.
 * @returns {Promise<AuthResponse>} Datos del usuario autenticado.
 * @throws {import('./httpClient.js').ApiError} Si las credenciales son inválidas (401/404).
 *
 * @example
 * const auth = await login({ email: 'user@utp.edu.pe', password: 'miClave123' });
 * guardarSesion(auth);
 */
export function login(credenciales) {
    return post(ENDPOINTS.AUTH.LOGIN, credenciales);
}

/**
 * Registra un nuevo cliente en el sistema.
 *
 * @async
 * @param {RegistroClienteRequest} datosRegistro - Datos completos del nuevo cliente.
 * @returns {Promise<AuthResponse>} Datos del cliente recién registrado.
 * @throws {import('./httpClient.js').ApiError} Si el email ya existe (409) o datos inválidos (422).
 *
 * @example
 * const auth = await registro({
 *     nombre: 'Juan Pérez',
 *     email: 'juan@utp.edu.pe',
 *     password: 'segura123',
 *     telefono: '987654321',
 *     direccion: 'Av. Universitaria 123'
 * });
 */
export function registro(datosRegistro) {
    return post(ENDPOINTS.AUTH.REGISTRO, datosRegistro);
}
