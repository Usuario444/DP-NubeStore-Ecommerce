/**
 * @file render.js
 * @description Funciones de renderizado DOM seguro sin Bootstrap.
 *              Usa createElement + textContent. Estética premium pure CSS.
 *
 * @module ui/render
 */

import { formatearMoneda, formatearFechaHora } from './formato.js';

function crearElemento(tag, clases = [], texto = '') {
    const el = document.createElement(tag);
    if (clases.length > 0) el.classList.add(...clases);
    if (texto) el.textContent = texto;
    return el;
}

function crearImagen(src, alt, clases = []) {
    const img = document.createElement('img');
    img.src = src || '';
    img.alt = alt;
    if (clases.length > 0) img.classList.add(...clases);
    img.onerror = () => {
        img.onerror = null; // Evita loop infinito si el logo también falla
        img.src = 'assets/logo.jpg';
        img.classList.add('fallback-img');
    };
    return img;
}

// ─────────────────────────────────────────────
//  Tarjeta de Producto
// ─────────────────────────────────────────────

export function crearTarjetaProducto(producto, onAgregarCarrito) {
    const card = crearElemento('article', ['product-card']);

    // Contenedor Imagen
    const imgContainer = crearElemento('div', ['product-card-image']);
    const img = crearImagen(producto.imagenUrl, producto.nombre, ['product-card-img']);
    imgContainer.appendChild(img);

    // Tags de producto (ej. Agotado, Últimas unidades)
    if (producto.stock <= 0) {
        const tag = crearElemento('span', ['product-card-tag', 'tag-out'], 'Agotado');
        imgContainer.appendChild(tag);
    } else if (producto.stock <= 5) {
        const tag = crearElemento('span', ['product-card-tag', 'tag-alert'], 'Últimas');
        imgContainer.appendChild(tag);
    }

    // Overlay (Hover Action)
    const overlay = crearElemento('div', ['product-card-overlay']);
    const btnAgregar = crearElemento('button', ['btn']);
    btnAgregar.textContent = producto.stock > 0 ? 'Añadir a la bolsa' : 'Sin Stock';
    btnAgregar.disabled = producto.stock <= 0;
    btnAgregar.addEventListener('click', (e) => {
        e.stopPropagation(); // Evitar click en la card si hacemos que la card sea un link luego
        onAgregarCarrito(producto);
    });
    overlay.appendChild(btnAgregar);
    imgContainer.appendChild(overlay);

    card.appendChild(imgContainer);

    // Body (Info)
    const body = crearElemento('div', ['product-card-body']);
    
    if (producto.categoria) {
        const cat = crearElemento('p', ['product-card-category'], producto.categoria);
        body.appendChild(cat);
    }
    
    const titulo = crearElemento('h3', ['product-card-name'], producto.nombre);
    body.appendChild(titulo);

    const precio = crearElemento('p', ['product-card-price'], formatearMoneda(producto.precio));
    body.appendChild(precio);

    card.appendChild(body);

    return card;
}

// ─────────────────────────────────────────────
//  Fila de Carrito (Data Table)
// ─────────────────────────────────────────────

export function crearFilaCarrito(item, { onCambiarCantidad, onEliminar }) {
    const tr = document.createElement('tr');

    const tdImg = document.createElement('td');
    const imgMini = crearImagen(item.imagenUrl, item.nombre, ['data-table-img']);
    tdImg.appendChild(imgMini);
    tr.appendChild(tdImg);

    const tdNombre = crearElemento('td', ['fw-semi'], item.nombre);
    tr.appendChild(tdNombre);

    const tdPrecio = crearElemento('td', [], formatearMoneda(item.precio));
    tr.appendChild(tdPrecio);

    const tdCantidad = document.createElement('td');
    const inputCantidad = document.createElement('input');
    inputCantidad.type = 'number';
    inputCantidad.classList.add('quantity-input');
    inputCantidad.min = '1';
    inputCantidad.max = String(item.stock);
    inputCantidad.value = String(item.cantidad);
    inputCantidad.addEventListener('change', () => {
        const val = parseInt(inputCantidad.value, 10);
        if (!isNaN(val)) onCambiarCantidad(item.idProducto, val);
    });
    tdCantidad.appendChild(inputCantidad);
    tr.appendChild(tdCantidad);

    const subtotal = item.precio * item.cantidad;
    const tdSubtotal = crearElemento('td', ['fw-bold'], formatearMoneda(subtotal));
    tr.appendChild(tdSubtotal);

    const tdAcciones = crearElemento('td', ['text-right']);
    const btnEliminar = crearElemento('button', ['btn', 'btn-ghost', 'text-danger'], 'Eliminar');
    btnEliminar.addEventListener('click', () => onEliminar(item.idProducto));
    tdAcciones.appendChild(btnEliminar);
    tr.appendChild(tdAcciones);

    return tr;
}

// ─────────────────────────────────────────────
//  Empty States & Spinner
// ─────────────────────────────────────────────

export function crearMensajeVacio(icono, titulo, subtitulo = '') {
    const wrapper = crearElemento('div', ['empty-state']);

    const iconoEl = crearElemento('div', ['empty-state-icon'], icono);
    wrapper.appendChild(iconoEl);

    const tituloEl = crearElemento('h3', ['empty-state-title'], titulo);
    wrapper.appendChild(tituloEl);

    if (subtitulo) {
        const subEl = crearElemento('p', ['empty-state-text'], subtitulo);
        wrapper.appendChild(subEl);
    }

    return wrapper;
}

export function crearSpinner(mensaje = 'Cargando...') {
    const wrapper = crearElemento('div', ['spinner-wrap']);
    const spinner = crearElemento('div', ['spinner']);
    wrapper.appendChild(spinner);

    const textoEl = crearElemento('p', ['spinner-text'], mensaje);
    wrapper.appendChild(textoEl);

    return wrapper;
}

export function limpiarContenedor(contenedor, conSpinner = false) {
    while (contenedor.firstChild) {
        contenedor.removeChild(contenedor.firstChild);
    }
    if (conSpinner) {
        contenedor.appendChild(crearSpinner());
    }
}
