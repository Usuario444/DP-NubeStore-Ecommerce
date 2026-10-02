/**
 * @file httpClient.js
 * @description Cliente HTTP centralizado para NubeStore.
 *              Envuelve la Fetch API con:
 *              - Construcción automática de URLs absolutas.
 *              - Headers JSON por defecto.
 *              - Timeout configurable vía AbortController.
 *              - Parseo de la respuesta JSON.
 *              - Manejo de errores estandarizado que captura el formato
 *                {@link ErrorResponse} del backend y lanza {@link ApiError}.
 * @module api/httpClient
 */

import { API_BASE_URL, TIEMPOS } from '../config.js';

// ─────────────────────────────────────────────
//  Clase de Error Personalizada
// ─────────────────────────────────────────────

/**
 * Error personalizado para respuestas no exitosas de la API.
 * Extiende Error nativo para mantener compatibilidad con try/catch.
 *
 * @extends Error
 *
 * @typedef  {Object} ErrorResponseBody
 * @property {number} status    - Código HTTP (400, 404, 409, 422, etc.)
 * @property {string} error     - Tipo de error HTTP (ej. "Bad Request")
 * @property {string} message   - Mensaje descriptivo del backend
 * @property {string} timestamp - Fecha/hora del error (ISO 8601)
 * @property {string} path      - Ruta del endpoint que falló
 */
export class ApiError extends Error {

    /**
     * @param {number}              status  - Código de estado HTTP.
     * @param {string}              message - Mensaje legible del error.
     * @param {ErrorResponseBody}   body    - Cuerpo completo de la respuesta de error.
     */
    constructor(status, message, body = null) {
        super(message);
        this.name   = 'ApiError';
        this.status = status;
        this.body   = body;
    }
}

// ─────────────────────────────────────────────
//  Headers por defecto
// ─────────────────────────────────────────────

/**
 * Genera los headers base para peticiones JSON.
 * @returns {HeadersInit}
 */
function defaultHeaders() {
    return {
        'Content-Type': 'application/json',
        'Accept':       'application/json',
    };
}

// ─────────────────────────────────────────────
//  Función principal: request
// ─────────────────────────────────────────────

/**
 * Realiza una petición HTTP genérica al backend.
 *
 * @async
 * @param {string}  endpoint            - Ruta relativa del endpoint (ej. `/api/productos`).
 * @param {Object}  [opciones={}]       - Opciones de configuración.
 * @param {string}  [opciones.method='GET']  - Método HTTP.
 * @param {Object}  [opciones.body]          - Cuerpo de la petición (se serializa a JSON).
 * @param {Object}  [opciones.headers]       - Headers adicionales (se fusionan con los por defecto).
 * @param {Object}  [opciones.params]        - Query params como objeto clave-valor.
 * @param {number}  [opciones.timeout]       - Timeout en ms (por defecto {@link TIEMPOS.HTTP_TIMEOUT}).
 * @returns {Promise<any>} Datos parseados de la respuesta JSON, o `null` si no hay cuerpo.
 * @throws  {ApiError}      Si la respuesta no es exitosa (`!res.ok`).
 * @throws  {ApiError}      Si la petición excede el timeout.
 * @throws  {ApiError}      Si ocurre un error de red.
 *
 * @example
 * // GET con query params
 * const productos = await request(ENDPOINTS.PRODUCTOS.BUSCAR, {
 *     params: { nombre: 'laptop', precioMax: 3000 }
 * });
 *
 * @example
 * // POST con body
 * const auth = await request(ENDPOINTS.AUTH.LOGIN, {
 *     method: 'POST',
 *     body: { email: 'user@utp.edu.pe', password: '1234' }
 * });
 */
export async function request(endpoint, opciones = {}) {
    const {
        method  = 'GET',
        body    = undefined,
        headers = {},
        params  = undefined,
        timeout = TIEMPOS.HTTP_TIMEOUT,
    } = opciones;

    // — Construir URL absoluta con query params —
    let url = `${API_BASE_URL}${endpoint}`;

    if (params) {
        const searchParams = new URLSearchParams();
        for (const [clave, valor] of Object.entries(params)) {
            if (valor !== undefined && valor !== null && valor !== '') {
                searchParams.append(clave, String(valor));
            }
        }
        const queryString = searchParams.toString();
        if (queryString) {
            url += `?${queryString}`;
        }
    }

    // — Configurar AbortController para timeout —
    const controller = new AbortController();
    const timeoutId  = setTimeout(() => controller.abort(), timeout);

    // — Construir opciones del fetch —
    /** @type {RequestInit} */
    const fetchOptions = {
        method,
        headers: { ...defaultHeaders(), ...headers },
        signal:  controller.signal,
    };

    if (body !== undefined) {
        fetchOptions.body = JSON.stringify(body);
    }

    // — Ejecutar petición —
    let res;
    try {
        res = await fetch(url, fetchOptions);
    } catch (error) {
        clearTimeout(timeoutId);

        // Timeout disparado por AbortController
        if (error.name === 'AbortError') {
            throw new ApiError(
                0,
                `La petición a ${endpoint} excedió el tiempo límite de ${timeout}ms.`
            );
        }

        // Error de red (servidor caído, sin conexión, CORS, etc.)
        throw new ApiError(
            0,
            `Error de conexión al servidor. Verifique que el backend esté en ejecución. (${error.message})`
        );
    } finally {
        clearTimeout(timeoutId);
    }

    // — Parsear cuerpo de la respuesta —
    let data = null;
    const contentType = res.headers.get('Content-Type') || '';

    if (contentType.includes('application/json')) {
        try {
            data = await res.json();
        } catch {
            // Respuesta marcada como JSON pero cuerpo vacío o malformado
            data = null;
        }
    }

    // — Evaluar si la respuesta fue exitosa —
    if (!res.ok) {
        /*
         * El backend devuelve un ErrorResponse con esta forma:
         * { status, error, message, timestamp, path }
         *
         * Extraemos el `message` para mostrarlo al usuario.
         * Si no hay cuerpo JSON, generamos un mensaje genérico.
         */
        const mensajeError = (data && data.message)
            ? data.message
            : `Error ${res.status}: ${res.statusText}`;

        throw new ApiError(res.status, mensajeError, data);
    }

    return data;
}

// ─────────────────────────────────────────────
//  Métodos de conveniencia (atajos semánticos)
// ─────────────────────────────────────────────

/**
 * Atajo para peticiones GET.
 * @param {string} endpoint - Ruta relativa del endpoint.
 * @param {Object} [params] - Query params opcionales.
 * @returns {Promise<any>}
 */
export function get(endpoint, params) {
    return request(endpoint, { method: 'GET', params });
}

/**
 * Atajo para peticiones POST.
 * @param {string} endpoint - Ruta relativa del endpoint.
 * @param {Object} body     - Cuerpo de la petición.
 * @returns {Promise<any>}
 */
export function post(endpoint, body) {
    return request(endpoint, { method: 'POST', body });
}

/**
 * Atajo para peticiones PUT.
 * @param {string} endpoint - Ruta relativa del endpoint.
 * @param {Object} body     - Cuerpo de la petición.
 * @returns {Promise<any>}
 */
export function put(endpoint, body) {
    return request(endpoint, { method: 'PUT', body });
}

/**
 * Atajo para peticiones DELETE.
 * @param {string} endpoint - Ruta relativa del endpoint.
 * @returns {Promise<any>}
 */
export function del(endpoint) {
    return request(endpoint, { method: 'DELETE' });
}
