/**
 * @file vendedor.js
 * @description Controlador de vista para el panel del vendedor (vendedor.html).
 *              Permite publicar nuevos productos en la tienda.
 *              Solo accesible para usuarios con rol VENDEDOR.
 * @module pages/vendedor
 */

import { inicializarNavbar } from '../components/navbar.js';
import { requiereRol, obtenerIdCliente } from '../state/session.js';
import { ROLES } from '../config.js';
import * as productosApi from '../api/productosApi.js';
import { mostrarExito, mostrarError } from '../ui/alertas.js';
import { formatearMoneda } from '../ui/formato.js';

// ─────────────────────────────────────────────
//  Referencias DOM
// ─────────────────────────────────────────────

/** @type {HTMLFormElement} */
let formProducto;
/** @type {HTMLButtonElement} */
let btnPublicar;
/** @type {HTMLElement} */
let listaProductos;

// ─────────────────────────────────────────────
//  Publicar Producto
// ─────────────────────────────────────────────

/**
 * Maneja el envío del formulario de publicación de producto.
 * @param {SubmitEvent} e
 * @async
 */
async function manejarPublicar(e) {
    e.preventDefault();

    const nombre      = document.getElementById('prod-nombre').value.trim();
    const descripcion = document.getElementById('prod-descripcion').value.trim();
    const precio      = parseFloat(document.getElementById('prod-precio').value);
    const stock       = parseInt(document.getElementById('prod-stock').value, 10);
    const categoria   = document.getElementById('prod-categoria').value.trim();
    const imagenUrl   = document.getElementById('prod-imagen').value.trim();

    // Validaciones básicas
    if (!nombre || !descripcion || !categoria) {
        mostrarError('Por favor, complete todos los campos obligatorios.');
        return;
    }

    if (isNaN(precio) || precio <= 0) {
        mostrarError('El precio debe ser un número mayor a 0.');
        return;
    }

    if (isNaN(stock) || stock < 0) {
        mostrarError('El stock debe ser un número igual o mayor a 0.');
        return;
    }

    const idVendedor = obtenerIdCliente();
    if (!idVendedor) {
        mostrarError('Sesión no válida.');
        return;
    }

    btnPublicar.disabled = true;
    btnPublicar.textContent = 'Publicando...';

    try {
        const producto = await productosApi.publicar({
            nombre,
            descripcion,
            precio,
            stock,
            categoria,
            imagenUrl,
            idVendedor,
        });

        mostrarExito(`Producto "${producto.nombre}" publicado exitosamente.`);
        formProducto.reset();
        cargarProductosVendedor();
    } catch (error) {
        mostrarError(error.message);
    } finally {
        btnPublicar.disabled = false;
        btnPublicar.textContent = 'Publicar Producto';
    }
}

// ─────────────────────────────────────────────
//  Listar Productos del Vendedor
// ─────────────────────────────────────────────

/**
 * Carga y muestra los productos publicados (catálogo completo como referencia).
 * @async
 */
async function cargarProductosVendedor() {
    if (!listaProductos) return;

    while (listaProductos.firstChild) {
        listaProductos.removeChild(listaProductos.firstChild);
    }

    try {
        const productos = await productosApi.listarTodos();

        if (!productos || productos.length === 0) {
            const p = document.createElement('p');
            p.classList.add('text-muted', 'text-center', 'py-3');
            p.textContent = 'No hay productos publicados aún.';
            listaProductos.appendChild(p);
            return;
        }

        // Crear tabla de productos
        const table = document.createElement('table');
        table.classList.add('table', 'table-hover', 'align-middle');

        const thead = document.createElement('thead');
        thead.classList.add('table-dark');
        const trHead = document.createElement('tr');
        ['ID', 'Nombre', 'Categoría', 'Precio', 'Stock'].forEach(txt => {
            const th = document.createElement('th');
            th.textContent = txt;
            trHead.appendChild(th);
        });
        thead.appendChild(trHead);
        table.appendChild(thead);

        const tbody = document.createElement('tbody');

        productos.forEach(p => {
            const tr = document.createElement('tr');

            const tdId = document.createElement('td');
            tdId.textContent = String(p.idProducto);
            tr.appendChild(tdId);

            const tdNombre = document.createElement('td');
            tdNombre.classList.add('fw-semibold');
            tdNombre.textContent = p.nombre;
            tr.appendChild(tdNombre);

            const tdCat = document.createElement('td');
            const badge = document.createElement('span');
            badge.classList.add('badge', 'bg-secondary');
            badge.textContent = p.categoria || '—';
            tdCat.appendChild(badge);
            tr.appendChild(tdCat);

            const tdPrecio = document.createElement('td');
            tdPrecio.textContent = formatearMoneda(p.precio);
            tr.appendChild(tdPrecio);

            const tdStock = document.createElement('td');
            tdStock.textContent = String(p.stock);
            tdStock.classList.add(p.stock > 0 ? 'text-success' : 'text-danger');
            tr.appendChild(tdStock);

            tbody.appendChild(tr);
        });

        table.appendChild(tbody);
        listaProductos.appendChild(table);
    } catch (error) {
        mostrarError(error.message);
    }
}

// ─────────────────────────────────────────────
//  Inicialización
// ─────────────────────────────────────────────

function init() {
    if (!requiereRol(ROLES.VENDEDOR)) return;

    inicializarNavbar();

    formProducto   = document.getElementById('form-producto');
    btnPublicar    = document.getElementById('btn-publicar');
    listaProductos = document.getElementById('lista-productos');

    if (formProducto) {
        formProducto.addEventListener('submit', manejarPublicar);
    }

    cargarProductosVendedor();
}

document.addEventListener('DOMContentLoaded', init);
