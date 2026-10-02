/**
 * @file pedidos.js
 * @description Controlador de vista para pedidos.html.
 * @module pages/pedidos
 */

import { inicializarNavbar, actualizarBadgeCarrito } from '../components/navbar.js';
import * as pedidosApi from '../api/pedidosApi.js';
import { requiereAutenticacion, obtenerSesion } from '../state/session.js';
import { mostrarExito, mostrarError } from '../ui/alertas.js';
import { limpiarContenedor, crearSpinner, crearMensajeVacio, crearTarjetaPedido } from '../ui/render.js';
import { formatearMoneda } from '../ui/formato.js';

// ─────────────────────────────────────────────
//  Variables Globales
// ─────────────────────────────────────────────

/** @type {HTMLElement} */
let contenedorPedidos;

/** @type {HTMLElement} */
let modalOverlay;
/** @type {HTMLElement} */
let modalDetalleBody;

// ─────────────────────────────────────────────
//  Funciones Principales
// ─────────────────────────────────────────────

/**
 * Carga el historial de pedidos del cliente autenticado.
 * @async
 */
async function cargarPedidos() {
    limpiarContenedor(contenedorPedidos, true);

    const sesion = obtenerSesion();
    if (!sesion) return;

    try {
        const respuesta = await pedidosApi.listarPedidosPorCliente(sesion.idCliente);
        
        limpiarContenedor(contenedorPedidos);

        if (!respuesta || respuesta.length === 0) {
            contenedorPedidos.appendChild(crearMensajeVacio(
                '📦', 
                'Aún no tienes pedidos', 
                '¡Explora nuestro catálogo y realiza tu primera compra!'
            ));
            return;
        }

        // Renderizar cada pedido
        respuesta.forEach(pedido => {
            const card = crearTarjetaPedido(pedido, verDetallePedido, solicitarDevolucion);
            contenedorPedidos.appendChild(card);
        });

    } catch (error) {
        limpiarContenedor(contenedorPedidos);
        mostrarError('Error al cargar pedidos: ' + error.message);
    }
}

/**
 * Muestra el modal con los detalles completos del pedido.
 * @param {number} idPedido 
 * @async
 */
async function verDetallePedido(idPedido) {
    try {
        const pedido = await pedidosApi.obtenerDetallePedido(idPedido);
        
        // Limpiar cuerpo del modal
        limpiarContenedor(modalDetalleBody);

        // Construir tabla de detalles a mano (pura DOM)
        if (pedido.detalles && pedido.detalles.length > 0) {
            const table = document.createElement('table');
            table.classList.add('data-table');

            const thead = document.createElement('thead');
            thead.innerHTML = `
                <tr>
                    <th>Producto</th>
                    <th>Cant.</th>
                    <th>P. Unit.</th>
                    <th>Subtotal</th>
                </tr>
            `;
            table.appendChild(thead);

            const tbody = document.createElement('tbody');
            pedido.detalles.forEach(det => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>${det.nombreProducto}</td>
                    <td>${det.cantidad}</td>
                    <td>${formatearMoneda(det.precioUnitario)}</td>
                    <td class="fw-bold">${formatearMoneda(det.subtotal)}</td>
                `;
                tbody.appendChild(tr);
            });
            table.appendChild(tbody);
            modalDetalleBody.appendChild(table);
        } else {
            modalDetalleBody.textContent = 'No hay detalles disponibles.';
        }

        // Mostrar Modal Custom
        modalOverlay.classList.add('open');

    } catch (error) {
        mostrarError('No se pudo obtener el detalle del pedido: ' + error.message);
    }
}

/**
 * Solicita una devolución para un pedido entregado.
 * @param {number} idPedido 
 * @async
 */
async function solicitarDevolucion(idPedido) {
    const motivo = prompt('Por favor, indica el motivo de la devolución:');
    
    if (!motivo) return; // Cancelado por el usuario
    if (motivo.trim().length < 10) {
        mostrarError('El motivo debe tener al menos 10 caracteres.');
        return;
    }

    try {
        const respuesta = await pedidosApi.solicitarDevolucion({
            idPedido: idPedido,
            motivo: motivo.trim()
        });

        mostrarExito(respuesta.mensaje || 'Devolución solicitada correctamente.');
        cargarPedidos(); // Recargar la lista para ver el cambio de estado
    } catch (error) {
        mostrarError('Error al procesar devolución: ' + error.message);
    }
}

// ─────────────────────────────────────────────
//  Inicialización
// ─────────────────────────────────────────────

function init() {
    if (!requiereAutenticacion('CLIENTE')) return;

    inicializarNavbar();
    actualizarBadgeCarrito();

    contenedorPedidos = document.getElementById('contenedor-pedidos');
    modalOverlay      = document.getElementById('modal-detalle-overlay');
    modalDetalleBody  = document.getElementById('modal-detalle-body');

    // Configuración del modal custom
    const btnCerrar = document.getElementById('btn-cerrar-modal');
    const btnCerrarFooter = document.getElementById('btn-cerrar-modal-footer');

    const cerrarModal = () => {
        if (modalOverlay) modalOverlay.classList.remove('open');
    };

    if (btnCerrar) btnCerrar.addEventListener('click', cerrarModal);
    if (btnCerrarFooter) btnCerrarFooter.addEventListener('click', cerrarModal);
    
    if (modalOverlay) {
        modalOverlay.addEventListener('click', (e) => {
            if (e.target === modalOverlay) cerrarModal();
        });
    }

    // Cargar data
    if (contenedorPedidos) {
        cargarPedidos();
    }
}

document.addEventListener('DOMContentLoaded', init);
