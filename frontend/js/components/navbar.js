/**
 * @file navbar.js
 * @description Componente de barra de navegación dinámica para NubeStore.
 *              Diseño Pure CSS, sin Bootstrap. Usa createElement.
 *
 * @module components/navbar
 */

import { obtenerSesion, cerrarSesion, esVendedor } from '../state/session.js';
import { obtenerCantidadTotal } from '../state/carrito.js';
import { ROLES, STORAGE_KEYS } from '../config.js';

/**
 * @param {string}   texto
 * @param {string}   href
 * @param {string[]} [clases]
 * @returns {HTMLAnchorElement}
 */
function crearNavLink(texto, href, clases = []) {
    const a = document.createElement('a');
    a.classList.add('nav-link', ...clases);
    a.href = href;
    a.textContent = texto;

    const paginaActual = window.location.pathname.split('/').pop() || 'index.html';
    if (href === paginaActual) {
        a.classList.add('active');
    }

    return a;
}

export function inicializarNavbar() {
    let contenedor = document.getElementById('navbar-container');
    if (!contenedor) return;

    // Limpiar
    while (contenedor.firstChild) {
        contenedor.removeChild(contenedor.firstChild);
    }

    const sesion = obtenerSesion();

    const nav = document.createElement('nav');
    nav.classList.add('nav-bar');

    const container = document.createElement('div');
    container.classList.add('nav-container');

    // Logo / Brand
    const brand = document.createElement('a');
    brand.classList.add('nav-brand');
    brand.href = 'index.html';
    
    // Si la imagen del logo falla, caemos en texto
    const logoImg = document.createElement('img');
    logoImg.src = 'assets/logo.jpg';
    logoImg.alt = 'Logo';
    logoImg.classList.add('nav-brand-icon');
    logoImg.onerror = () => logoImg.style.display = 'none';
    
    brand.appendChild(logoImg);
    brand.appendChild(document.createTextNode('NUBESTORE'));
    container.appendChild(brand);

    // Mobile Toggle
    const toggle = document.createElement('div');
    toggle.classList.add('nav-toggle');
    toggle.innerHTML = '<span></span><span></span><span></span>';
    container.appendChild(toggle);

    // Overlay móvil
    const overlay = document.createElement('div');
    overlay.classList.add('nav-overlay');
    document.body.appendChild(overlay);

    // Menú Central / Enlaces
    const menu = document.createElement('div');
    menu.classList.add('nav-menu');

    menu.appendChild(crearNavLink('Catálogo', 'index.html'));

    if (sesion) {
        if (sesion.rol === ROLES.VENDEDOR) {
            menu.appendChild(crearNavLink('Panel Vendedor', 'vendedor.html'));
        } else {
            menu.appendChild(crearNavLink('Mis Pedidos', 'pedidos.html'));
        }
    }
    
    // Acciones Derecha
    const actions = document.createElement('div');
    actions.classList.add('nav-actions');

    // Toggle modo oscuro — restaurar preferencia guardada
    const temaGuardado = localStorage.getItem(STORAGE_KEYS.TEMA) || 'light';
    document.documentElement.setAttribute('data-theme', temaGuardado);

    const themeToggle = document.createElement('button');
    themeToggle.classList.add('theme-toggle');
    themeToggle.textContent = temaGuardado === 'dark' ? '☀️' : '🌙';
    themeToggle.title = 'Cambiar tema';
    themeToggle.onclick = () => {
        const isDark = document.documentElement.getAttribute('data-theme') === 'dark';
        const nuevoTema = isDark ? 'light' : 'dark';
        document.documentElement.setAttribute('data-theme', nuevoTema);
        localStorage.setItem(STORAGE_KEYS.TEMA, nuevoTema);
        themeToggle.textContent = nuevoTema === 'dark' ? '☀️' : '🌙';
    };
    actions.appendChild(themeToggle);

    if (sesion) {
        if (sesion.rol === ROLES.CLIENTE) {
            const linkCarrito = document.createElement('a');
            linkCarrito.classList.add('nav-cart-count');
            linkCarrito.href = 'carrito.html';
            linkCarrito.id = 'nav-cart-link';
            
            const txt = document.createTextNode('Bolsa');
            linkCarrito.appendChild(txt);
            
            const badge = document.createElement('span');
            badge.classList.add('nav-cart-badge');
            badge.id = 'nav-cart-badge-count';
            const cant = obtenerCantidadTotal();
            badge.textContent = cant;
            badge.style.display = cant > 0 ? 'inline-flex' : 'none';
            
            linkCarrito.appendChild(badge);
            actions.appendChild(linkCarrito);
        }

        const spanUsuario = document.createElement('span');
        spanUsuario.classList.add('nav-user');
        const nombreCompleto = [sesion.nombre, sesion.apellido].filter(Boolean).join(' ');
        spanUsuario.textContent = nombreCompleto || sesion.nombre;
        actions.appendChild(spanUsuario);

        const btnLogout = document.createElement('button');
        btnLogout.classList.add('btn', 'btn-outline', 'btn-sm');
        btnLogout.textContent = 'Salir';
        btnLogout.onclick = () => {
            cerrarSesion();
            window.location.href = 'index.html';
        };
        actions.appendChild(btnLogout);

    } else {
        const linkLogin = document.createElement('a');
        linkLogin.classList.add('btn', 'btn-primary', 'btn-sm');
        linkLogin.href = 'login.html';
        linkLogin.textContent = 'Mi Cuenta';
        actions.appendChild(linkLogin);
    }

    menu.appendChild(actions);
    container.appendChild(menu);
    nav.appendChild(container);
    contenedor.appendChild(nav);

    // Lógica menú móvil
    toggle.addEventListener('click', () => {
        menu.classList.toggle('open');
        toggle.classList.toggle('open');
        overlay.classList.toggle('open');
    });

    overlay.addEventListener('click', () => {
        menu.classList.remove('open');
        toggle.classList.remove('open');
        overlay.classList.remove('open');
    });
}

export function actualizarBadgeCarrito() {
    const badge = document.getElementById('nav-cart-badge-count');
    if (badge) {
        const cant = obtenerCantidadTotal();
        badge.textContent = cant;
        badge.style.display = cant > 0 ? 'inline-flex' : 'none';
    }
}
