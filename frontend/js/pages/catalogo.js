/**
 * @file catalogo.js
 * @description Controlador de vista para la página principal (index.html).
 *              Carga y muestra el catálogo de productos con búsqueda y filtros.
 *              Delega red a `productosApi`, renderizado a `render`, estado a `carrito`.
 * @module pages/catalogo
 */

import { inicializarNavbar, actualizarBadgeCarrito } from '../components/navbar.js';
import * as productosApi from '../api/productosApi.js';
import * as carrito from '../state/carrito.js';
import { crearTarjetaProducto, limpiarContenedor, crearMensajeVacio } from '../ui/render.js';
import { mostrarExito, mostrarError, mostrarAdvertencia } from '../ui/alertas.js';

// ─────────────────────────────────────────────
//  Referencias DOM
// ─────────────────────────────────────────────

/** @type {HTMLElement} */
let contenedorProductos;
/** @type {HTMLInputElement} */
let inputBusqueda;
/** @type {HTMLSelectElement} */
let selectCategoria;
/** @type {HTMLInputElement} */
let inputPrecioMin;
/** @type {HTMLInputElement} */
let inputPrecioMax;
/** @type {HTMLFormElement} */
let formFiltros;

// ─────────────────────────────────────────────
//  Carga de Productos
// ─────────────────────────────────────────────

/**
 * Carga todos los productos desde la API y los renderiza como tarjetas.
 * @async
 */
async function cargarProductos() {
    limpiarContenedor(contenedorProductos, true);

    try {
        const productos = await productosApi.listarTodos();
        renderizarProductos(productos);
    } catch (error) {
        mostrarError(error.message);
        limpiarContenedor(contenedorProductos);
        contenedorProductos.appendChild(
            crearMensajeVacio('⚠️', 'Error al cargar productos', error.message)
        );
    }
}

/**
 * Busca productos con los filtros actuales del formulario.
 * @async
 */
async function buscarProductos() {
    limpiarContenedor(contenedorProductos, true);

    const filtros = {
        nombre:    inputBusqueda.value.trim(),
        categoria: selectCategoria.value,
        precioMin: inputPrecioMin.value || undefined,
        precioMax: inputPrecioMax.value || undefined,
    };

    try {
        const productos = await productosApi.buscar(filtros);
        renderizarProductos(productos);
    } catch (error) {
        mostrarError(error.message);
        limpiarContenedor(contenedorProductos);
        contenedorProductos.appendChild(
            crearMensajeVacio('🔍', 'Error en la búsqueda', error.message)
        );
    }
}

/**
 * Renderiza un array de productos como tarjetas en el grid.
 * @param {import('../api/productosApi.js').ProductoResponse[]} productos
 */
function renderizarProductos(productos) {
    limpiarContenedor(contenedorProductos);

    if (!productos || productos.length === 0) {
        contenedorProductos.appendChild(
            crearMensajeVacio('📦', 'No se encontraron productos', 'Intenta con otros filtros de búsqueda.')
        );
        return;
    }

    productos.forEach(producto => {
        const tarjeta = crearTarjetaProducto(producto, manejarAgregarCarrito);
        contenedorProductos.appendChild(tarjeta);
    });
}

// ─────────────────────────────────────────────
//  Acciones
// ─────────────────────────────────────────────

/**
 * Maneja el clic en "Agregar al carrito" de una tarjeta de producto.
 * @param {import('../api/productosApi.js').ProductoResponse} producto
 */
function manejarAgregarCarrito(producto) {
    const exito = carrito.agregarItem({
        idProducto: producto.idProducto,
        nombre:     producto.nombre,
        precio:     producto.precio,
        cantidad:   1,
        imagenUrl:  producto.imagenUrl,
        stock:      producto.stock,
    });

    if (exito) {
        mostrarExito(`"${producto.nombre}" agregado al carrito.`);
        actualizarBadgeCarrito();
    } else {
        mostrarAdvertencia(`No se puede agregar más unidades de "${producto.nombre}". Stock máximo alcanzado.`);
    }
}

// ─────────────────────────────────────────────
//  Inicialización
// ─────────────────────────────────────────────

/**
 * Punto de entrada del controlador. Se ejecuta al cargar la página.
 */
function init() {
    inicializarNavbar();

    contenedorProductos = document.getElementById('productos-grid');
    inputBusqueda       = document.getElementById('input-busqueda');
    selectCategoria     = document.getElementById('select-categoria');
    inputPrecioMin      = document.getElementById('input-precio-min');
    inputPrecioMax      = document.getElementById('input-precio-max');
    formFiltros         = document.getElementById('form-filtros');

    // Evento de búsqueda
    if (formFiltros) {
        formFiltros.addEventListener('submit', (e) => {
            e.preventDefault();
            buscarProductos();
        });
    }

    // Botón limpiar filtros
    const btnLimpiar = document.getElementById('btn-limpiar-filtros');
    if (btnLimpiar) {
        btnLimpiar.addEventListener('click', () => {
            formFiltros.reset();
            cargarProductos();
        });
    }

    // Carga inicial
    cargarProductos();
}

document.addEventListener('DOMContentLoaded', init);
