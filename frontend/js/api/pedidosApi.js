/**
 * @file pedidosApi.js
 * @description Módulo de API para operaciones con pedidos y devoluciones.
 *              Delega todas las peticiones HTTP a {@link module:api/httpClient}.
 * @module api/pedidosApi
 */

import { get, post } from './httpClient.js';
import { ENDPOINTS } from '../config.js';

// ─────────────────────────────────────────────
//  Typedefs — DTOs del backend
// ─────────────────────────────────────────────

/**
 * Ítem individual dentro de un pedido.
 * Refleja el DTO `ItemPedidoRequest` del backend.
 *
 * @typedef  {Object} ItemPedidoRequest
 * @property {number} idProducto - ID del producto a comprar.
 * @property {number} cantidad   - Cantidad solicitada.
 */

/**
 * Cuerpo para crear un pedido nuevo.
 * Refleja el DTO `CrearPedidoRequest` del backend.
 *
 * @typedef  {Object} CrearPedidoRequest
 * @property {number}              idCliente - ID del cliente que realiza el pedido.
 * @property {ItemPedidoRequest[]} items     - Lista de ítems del pedido.
 */

/**
 * Detalle de un producto dentro de un pedido (respuesta).
 * Refleja el DTO `DetallePedidoResponse` del backend.
 *
 * @typedef  {Object} DetallePedidoResponse
 * @property {number} idProducto     - ID del producto.
 * @property {string} nombreProducto - Nombre del producto.
 * @property {number} cantidad       - Cantidad comprada.
 * @property {number} precioUnitario - Precio por unidad al momento de la compra.
 * @property {number} subtotal       - Subtotal (cantidad × precioUnitario).
 */

/**
 * Respuesta de un pedido completo.
 * Refleja el DTO `PedidoResponse` del backend.
 *
 * @typedef  {Object} PedidoResponse
 * @property {number}                   idPedido      - ID único del pedido.
 * @property {number}                   idCliente     - ID del cliente.
 * @property {string}                   nombreCliente - Nombre del cliente.
 * @property {string}                   estado        - Estado del pedido (PENDIENTE, ENVIADO, ENTREGADO, etc.).
 * @property {number}                   total         - Monto total del pedido.
 * @property {string}                   fechaCreacion - Fecha de creación (ISO 8601).
 * @property {DetallePedidoResponse[]}  detalles      - Lista de ítems del pedido.
 */

/**
 * Cuerpo para solicitar una devolución.
 * Refleja el DTO `SolicitarDevolucionRequest` del backend.
 *
 * @typedef  {Object} SolicitarDevolucionRequest
 * @property {string} motivo - Motivo de la devolución.
 */

/**
 * Respuesta de una devolución.
 * Refleja el DTO `DevolucionResponse` del backend.
 *
 * @typedef  {Object} DevolucionResponse
 * @property {number} idDevolucion  - ID de la devolución.
 * @property {number} idPedido      - ID del pedido asociado.
 * @property {string} motivo        - Motivo proporcionado.
 * @property {string} estado        - Estado de la devolución.
 * @property {string} fechaSolicitud - Fecha de la solicitud (ISO 8601).
 */

// ─────────────────────────────────────────────
//  Funciones de API — Pedidos
// ─────────────────────────────────────────────

/**
 * Crea un nuevo pedido a partir de los ítems del carrito.
 *
 * @async
 * @param {CrearPedidoRequest} datosPedido - ID del cliente e ítems.
 * @returns {Promise<PedidoResponse>} Pedido creado con su ID y detalles.
 * @throws {import('./httpClient.js').ApiError} Si stock insuficiente (422), datos inválidos (400), etc.
 *
 * @example
 * const pedido = await crearPedido({
 *     idCliente: 1,
 *     items: [
 *         { idProducto: 5, cantidad: 2 },
 *         { idProducto: 12, cantidad: 1 }
 *     ]
 * });
 */
export function crearPedido(datosPedido) {
    return post(ENDPOINTS.PEDIDOS.BASE, datosPedido);
}

/**
 * Obtiene un pedido específico por su ID.
 *
 * @async
 * @param {number} idPedido - ID del pedido a consultar.
 * @returns {Promise<PedidoResponse>} Datos completos del pedido con detalles.
 * @throws {import('./httpClient.js').ApiError} Si no se encuentra (404).
 */
export function obtenerPorId(idPedido) {
    return get(`${ENDPOINTS.PEDIDOS.POR_ID}/${idPedido}`);
}

/**
 * Lista todos los pedidos de un cliente específico.
 *
 * @async
 * @param {number} idCliente - ID del cliente.
 * @returns {Promise<PedidoResponse[]>} Array de pedidos del cliente.
 * @throws {import('./httpClient.js').ApiError} Si el cliente no existe (404).
 */
export function listarPorCliente(idCliente) {
    return get(`${ENDPOINTS.PEDIDOS.CLIENTE}/${idCliente}`);
}

// ─────────────────────────────────────────────
//  Funciones de API — Devoluciones
// ─────────────────────────────────────────────

/**
 * Solicita la devolución de un pedido.
 *
 * @async
 * @param {number}                      idPedido - ID del pedido a devolver.
 * @param {SolicitarDevolucionRequest}  datos    - Motivo de la devolución.
 * @returns {Promise<DevolucionResponse>} Datos de la devolución creada.
 * @throws {import('./httpClient.js').ApiError} Si el pedido no existe (404) o no es elegible (422).
 *
 * @example
 * const devolucion = await solicitarDevolucion(15, { motivo: 'Producto defectuoso' });
 */
export function solicitarDevolucion(idPedido, datos) {
    return post(`${ENDPOINTS.DEVOLUCIONES.SOLICITAR}/${idPedido}/devolucion`, datos);
}
