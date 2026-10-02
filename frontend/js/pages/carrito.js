/**
 * @file carrito.js (page controller)
 * @description Controlador de vista para la página del carrito (carrito.html).
 *              Renderiza los ítems del carrito, gestiona cambios de cantidad,
 *              eliminación y checkout (creación de pedido).
 * @module pages/carrito
 */

import { inicializarNavbar } from '../components/navbar.js';
import { requiereAutenticacion, obtenerIdCliente } from '../state/session.js';
import * as carrito from '../state/carrito.js';
import * as pedidosApi from '../api/pedidosApi.js';
import { crearFilaCarrito, crearMensajeVacio, limpiarContenedor } from '../ui/render.js';
import { formatearMoneda } from '../ui/formato.js';
import { mostrarExito, mostrarError, mostrarAdvertencia } from '../ui/alertas.js';

// ─────────────────────────────────────────────
//  Referencias DOM
// ─────────────────────────────────────────────

/** @type {HTMLTableSectionElement} */
let tbodyCarrito;
/** @type {HTMLElement} */
let contenedorCarrito;
/** @type {HTMLElement} */
let resumenCarrito;
/** @type {HTMLElement} */
let totalEl;
/** @type {HTMLButtonElement} */
let btnCheckout;
/** @type {HTMLButtonElement} */
let btnVaciar;

// ─────────────────────────────────────────────
//  Renderizado
// ─────────────────────────────────────────────

/**
 * Renderiza el estado actual del carrito en la tabla y actualiza el resumen.
 */
function renderizarCarrito() {
    const items = carrito.obtenerItems();

    if (items.length === 0) {
        // Ocultar tabla y resumen, mostrar mensaje vacío
        contenedorCarrito.style.display = 'none';
        resumenCarrito.style.display = 'none';

        let vacio = document.getElementById('carrito-vacio');
        if (!vacio) {
            vacio = crearMensajeVacio(
                '🛒',
                'Tu carrito está vacío',
                'Explora el catálogo y agrega productos.'
            );
            vacio.id = 'carrito-vacio';

            // Botón ir al catálogo
            const btnCatalogo = document.createElement('a');
            btnCatalogo.href = 'index.html';
            btnCatalogo.classList.add('btn', 'btn-primary', 'mt-3');
            btnCatalogo.textContent = 'Ir al catálogo';
            vacio.appendChild(btnCatalogo);

            contenedorCarrito.parentElement.appendChild(vacio);
        }
        return;
    }

    // Eliminar mensaje vacío si existe
    const vacio = document.getElementById('carrito-vacio');
    if (vacio) vacio.remove();

    contenedorCarrito.style.display = '';
    resumenCarrito.style.display = '';

    // Limpiar tbody
    while (tbodyCarrito.firstChild) {
        tbodyCarrito.removeChild(tbodyCarrito.firstChild);
    }

    // Crear filas
    items.forEach(item => {
        const fila = crearFilaCarrito(item, {
            onCambiarCantidad: manejarCambioCantidad,
            onEliminar: manejarEliminar,
        });
        tbodyCarrito.appendChild(fila);
    });

    // Actualizar total
    actualizarResumen();
}

/**
 * Actualiza el monto total y la cantidad de ítems en el resumen.
 */
function actualizarResumen() {
    if (totalEl) {
        totalEl.textContent = formatearMoneda(carrito.obtenerMontoTotal());
    }

    const cantidadEl = document.getElementById('carrito-cantidad');
    if (cantidadEl) {
        const total = carrito.obtenerCantidadTotal();
        cantidadEl.textContent = `${total} ${total === 1 ? 'producto' : 'productos'}`;
    }
}

// ─────────────────────────────────────────────
//  Handlers
// ─────────────────────────────────────────────

/**
 * @param {number} idProducto
 * @param {number} nuevaCantidad
 */
function manejarCambioCantidad(idProducto, nuevaCantidad) {
    const exito = carrito.actualizarCantidad(idProducto, nuevaCantidad);
    if (!exito) {
        mostrarAdvertencia('Cantidad no válida o excede el stock disponible.');
    }
    renderizarCarrito();
}

/**
 * @param {number} idProducto
 */
function manejarEliminar(idProducto) {
    carrito.eliminarItem(idProducto);
    renderizarCarrito();
    inicializarNavbar(); // Actualizar badge
}

/**
 * Maneja el proceso de checkout (crear pedido).
 * @async
 */
async function manejarCheckout() {
    if (carrito.estaVacio()) {
        mostrarAdvertencia('El carrito está vacío.');
        return;
    }

    const idCliente = obtenerIdCliente();
    if (!idCliente) {
        mostrarError('Sesión no válida. Por favor, inicie sesión nuevamente.');
        return;
    }

    btnCheckout.disabled = true;
    btnCheckout.textContent = 'Procesando...';

    try {
        const pedido = await pedidosApi.crearPedido({
            idCliente,
            items: carrito.obtenerItemsParaPedido(),
        });

        carrito.vaciarCarrito();
        mostrarExito(`¡Pedido #${pedido.idPedido} creado exitosamente!`);
        inicializarNavbar();

        // Redirigir a pedidos tras un delay
        setTimeout(() => {
            window.location.href = 'pedidos.html';
        }, 1500);
    } catch (error) {
        mostrarError(error.message);
        btnCheckout.disabled = false;
        btnCheckout.textContent = 'Confirmar Pedido';
    }
}

/**
 * Vacía todo el carrito con confirmación.
 */
function manejarVaciar() {
    if (carrito.estaVacio()) return;

    if (confirm('¿Está seguro de vaciar todo el carrito?')) {
        carrito.vaciarCarrito();
        renderizarCarrito();
        inicializarNavbar();
    }
}

// ─────────────────────────────────────────────
//  Inicialización
// ─────────────────────────────────────────────

function init() {
    if (!requiereAutenticacion()) return;

    inicializarNavbar();

    contenedorCarrito = document.getElementById('contenedor-carrito');
    tbodyCarrito      = document.getElementById('tbody-carrito');
    resumenCarrito    = document.getElementById('resumen-carrito');
    totalEl           = document.getElementById('carrito-total');
    btnCheckout       = document.getElementById('btn-checkout');
    btnVaciar         = document.getElementById('btn-vaciar');

    if (btnCheckout) {
        btnCheckout.addEventListener('click', manejarCheckout);
    }

    if (btnVaciar) {
        btnVaciar.addEventListener('click', manejarVaciar);
    }

    renderizarCarrito();
}

document.addEventListener('DOMContentLoaded', init);
