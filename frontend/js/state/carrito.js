/**
 * @file carrito.js
 * @description Módulo de estado del carrito de compras para NubeStore.
 *              Gestiona los ítems del carrito en memoria y los sincroniza
 *              con localStorage para persistencia entre recargas de página.
 *
 *              Cada ítem almacena los datos mínimos necesarios para renderizar
 *              la vista del carrito y para construir el `CrearPedidoRequest`.
 *
 * @module state/carrito
 */

import { STORAGE_KEYS } from '../config.js';

// ─────────────────────────────────────────────
//  Typedefs
// ─────────────────────────────────────────────

/**
 * Ítem almacenado en el carrito.
 *
 * @typedef  {Object} ItemCarrito
 * @property {number} idProducto  - ID del producto.
 * @property {string} nombre      - Nombre del producto (para renderizar sin re-consultar la API).
 * @property {number} precio      - Precio unitario al momento de agregar.
 * @property {number} cantidad    - Cantidad seleccionada por el usuario.
 * @property {string} imagenUrl   - URL de la imagen del producto.
 * @property {number} stock       - Stock disponible (para validar cantidad máxima en frontend).
 */

// ─────────────────────────────────────────────
//  Estado interno
// ─────────────────────────────────────────────

/**
 * Array de ítems en el carrito. Se carga desde localStorage al iniciar.
 * @type {ItemCarrito[]}
 */
let items = cargarDesdeStorage();

// ─────────────────────────────────────────────
//  Persistencia (privada)
// ─────────────────────────────────────────────

/**
 * Lee el carrito desde localStorage.
 * @returns {ItemCarrito[]}
 * @private
 */
function cargarDesdeStorage() {
    try {
        const datos = localStorage.getItem(STORAGE_KEYS.CARRITO);
        return datos ? JSON.parse(datos) : [];
    } catch {
        // Si el JSON está corrupto, reiniciar el carrito
        localStorage.removeItem(STORAGE_KEYS.CARRITO);
        return [];
    }
}

/**
 * Guarda el estado actual del carrito en localStorage.
 * @private
 */
function guardarEnStorage() {
    localStorage.setItem(STORAGE_KEYS.CARRITO, JSON.stringify(items));
}

// ─────────────────────────────────────────────
//  Operaciones del Carrito
// ─────────────────────────────────────────────

/**
 * Agrega un producto al carrito. Si el producto ya existe,
 * incrementa la cantidad (sin exceder el stock disponible).
 *
 * @param {ItemCarrito} producto - Datos del producto a agregar.
 * @returns {boolean} `true` si se agregó/incrementó exitosamente,
 *                    `false` si no se pudo (stock agotado).
 *
 * @example
 * import { agregarItem } from '../state/carrito.js';
 * const exito = agregarItem({
 *     idProducto: 5,
 *     nombre: 'Laptop ASUS',
 *     precio: 2599.90,
 *     cantidad: 1,
 *     imagenUrl: 'https://...',
 *     stock: 10
 * });
 */
export function agregarItem(producto) {
    const existente = items.find(item => item.idProducto === producto.idProducto);

    if (existente) {
        const nuevaCantidad = existente.cantidad + (producto.cantidad || 1);

        if (nuevaCantidad > existente.stock) {
            return false;
        }

        existente.cantidad = nuevaCantidad;
    } else {
        items.push({
            idProducto: producto.idProducto,
            nombre:     producto.nombre,
            precio:     producto.precio,
            cantidad:   producto.cantidad || 1,
            imagenUrl:  producto.imagenUrl || '',
            stock:      producto.stock,
        });
    }

    guardarEnStorage();
    return true;
}

/**
 * Actualiza la cantidad de un producto en el carrito.
 *
 * @param {number} idProducto    - ID del producto a actualizar.
 * @param {number} nuevaCantidad - Nueva cantidad deseada (debe ser >= 1).
 * @returns {boolean} `true` si se actualizó, `false` si el producto no está en el carrito
 *                    o la cantidad excede el stock.
 */
export function actualizarCantidad(idProducto, nuevaCantidad) {
    const item = items.find(i => i.idProducto === idProducto);

    if (!item) {
        return false;
    }

    if (nuevaCantidad < 1 || nuevaCantidad > item.stock) {
        return false;
    }

    item.cantidad = nuevaCantidad;
    guardarEnStorage();
    return true;
}

/**
 * Elimina un producto del carrito.
 *
 * @param {number} idProducto - ID del producto a eliminar.
 * @returns {void}
 */
export function eliminarItem(idProducto) {
    items = items.filter(item => item.idProducto !== idProducto);
    guardarEnStorage();
}

/**
 * Vacía completamente el carrito.
 * Se invoca tras completar un pedido exitoso.
 *
 * @returns {void}
 */
export function vaciarCarrito() {
    items = [];
    guardarEnStorage();
}

// ─────────────────────────────────────────────
//  Consultas (sin modificar estado)
// ─────────────────────────────────────────────

/**
 * Obtiene una copia del array de ítems del carrito.
 * Retorna una copia para evitar mutaciones externas accidentales.
 *
 * @returns {ItemCarrito[]} Copia del array de ítems.
 */
export function obtenerItems() {
    return [...items];
}

/**
 * Obtiene la cantidad total de unidades en el carrito.
 * Útil para mostrar el badge en la navbar.
 *
 * @returns {number} Suma de las cantidades de todos los ítems.
 */
export function obtenerCantidadTotal() {
    return items.reduce((total, item) => total + item.cantidad, 0);
}

/**
 * Calcula el monto total del carrito.
 *
 * @returns {number} Suma de (precio × cantidad) de todos los ítems.
 */
export function obtenerMontoTotal() {
    return items.reduce((total, item) => total + (item.precio * item.cantidad), 0);
}

/**
 * Verifica si el carrito está vacío.
 *
 * @returns {boolean} `true` si no hay ítems.
 */
export function estaVacio() {
    return items.length === 0;
}

/**
 * Obtiene la cantidad de un producto específico en el carrito.
 * Retorna 0 si el producto no está en el carrito.
 *
 * @param {number} idProducto - ID del producto a consultar.
 * @returns {number} Cantidad actual en el carrito.
 */
export function obtenerCantidadProducto(idProducto) {
    const item = items.find(i => i.idProducto === idProducto);
    return item ? item.cantidad : 0;
}

/**
 * Construye el array de ítems en el formato que espera el backend
 * para la creación de un pedido (`ItemPedidoRequest[]`).
 *
 * @returns {Array<{idProducto: number, cantidad: number}>}
 *
 * @example
 * import { obtenerItemsParaPedido } from '../state/carrito.js';
 * const pedidoRequest = {
 *     idCliente: sesion.idCliente,
 *     items: obtenerItemsParaPedido()
 * };
 */
export function obtenerItemsParaPedido() {
    return items.map(item => ({
        idProducto: item.idProducto,
        cantidad:   item.cantidad,
    }));
}
