/**
 * @file productosApi.js
 * @description Módulo de API para operaciones con productos.
 *              Delega todas las peticiones HTTP a {@link module:api/httpClient}.
 * @module api/productosApi
 */

import { get, post } from './httpClient.js';
import { ENDPOINTS } from '../config.js';

// ─────────────────────────────────────────────
//  Typedefs — DTOs del backend
// ─────────────────────────────────────────────

/**
 * Respuesta de un producto.
 * Refleja el DTO `ProductoResponse` del backend.
 *
 * @typedef  {Object} ProductoResponse
 * @property {number}  idProducto   - ID único del producto.
 * @property {string}  nombre       - Nombre del producto.
 * @property {string}  descripcion  - Descripción detallada.
 * @property {number}  precio       - Precio unitario (decimal).
 * @property {number}  stock        - Cantidad disponible en inventario.
 * @property {string}  categoria    - Categoría del producto.
 * @property {string}  imagenUrl    - URL de la imagen del producto.
 * @property {string}  nombreVendedor - Nombre del vendedor que publicó el producto.
 */

/**
 * Filtros de búsqueda de productos.
 * Refleja el DTO `FiltroProductoRequest` del backend.
 *
 * @typedef  {Object} FiltroProductoRequest
 * @property {string} [nombre]    - Filtro por nombre (búsqueda parcial).
 * @property {string} [categoria] - Filtro por categoría exacta.
 * @property {number} [precioMin] - Precio mínimo.
 * @property {number} [precioMax] - Precio máximo.
 */

/**
 * Cuerpo para publicar un producto nuevo.
 * Refleja el DTO `PublicarProductoRequest` del backend.
 *
 * @typedef  {Object} PublicarProductoRequest
 * @property {string} nombre       - Nombre del producto.
 * @property {string} descripcion  - Descripción detallada.
 * @property {number} precio       - Precio unitario.
 * @property {number} stock        - Cantidad inicial en inventario.
 * @property {string} categoria    - Categoría del producto.
 * @property {string} imagenUrl    - URL de la imagen.
 * @property {number} idVendedor   - ID del vendedor que publica.
 */

// ─────────────────────────────────────────────
//  Funciones de API
// ─────────────────────────────────────────────

/**
 * Obtiene el listado completo de productos disponibles.
 *
 * @async
 * @returns {Promise<ProductoResponse[]>} Array de productos.
 */
export function listarTodos() {
    return get(ENDPOINTS.PRODUCTOS.BASE);
}

/**
 * Obtiene un producto por su ID.
 *
 * @async
 * @param {number} idProducto - ID del producto a consultar.
 * @returns {Promise<ProductoResponse>} Datos del producto.
 * @throws {import('./httpClient.js').ApiError} Si no se encuentra (404).
 */
export function obtenerPorId(idProducto) {
    return get(`${ENDPOINTS.PRODUCTOS.POR_ID}/${idProducto}`);
}

/**
 * Busca productos aplicando filtros opcionales.
 * Los parámetros `null`, `undefined` o vacíos se omiten automáticamente
 * gracias al manejo de query params en {@link module:api/httpClient}.
 *
 * @async
 * @param {FiltroProductoRequest} filtros - Criterios de búsqueda.
 * @returns {Promise<ProductoResponse[]>} Array de productos que coinciden.
 *
 * @example
 * const laptops = await buscar({ nombre: 'laptop', precioMax: 3000 });
 */
export function buscar(filtros) {
    return get(ENDPOINTS.PRODUCTOS.BUSCAR, filtros);
}

/**
 * Publica un nuevo producto en la tienda (uso exclusivo de vendedores).
 *
 * @async
 * @param {PublicarProductoRequest} datosProducto - Datos del producto a publicar.
 * @returns {Promise<ProductoResponse>} Producto creado con su ID asignado.
 * @throws {import('./httpClient.js').ApiError} Si datos inválidos (422) o error de negocio.
 */
export function publicar(datosProducto) {
    return post(ENDPOINTS.PRODUCTOS.BASE, datosProducto);
}
