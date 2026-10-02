/**
 * @file registro.js
 * @description Controlador de vista para registro.html (creación de cuenta).
 * @module pages/registro
 */

import { inicializarNavbar } from '../components/navbar.js';
import * as authApi from '../api/authApi.js';
import { guardarSesion, estaAutenticado } from '../state/session.js';
import { mostrarExito, mostrarError } from '../ui/alertas.js';

// ─────────────────────────────────────────────

/** @type {HTMLFormElement} */
let formRegistro;
/** @type {HTMLButtonElement} */
let btnRegistro;

/**
 * @param {SubmitEvent} e
 * @async
 */
async function manejarRegistro(e) {
    e.preventDefault();

    const nombre    = document.getElementById('registro-nombre').value.trim();
    const apellido  = document.getElementById('registro-apellido').value.trim();
    const email     = document.getElementById('registro-email').value.trim();
    const password  = document.getElementById('registro-password').value;
    const confirmar = document.getElementById('registro-confirmar').value;
    const telefono  = document.getElementById('registro-telefono').value.trim();
    const direccion = document.getElementById('registro-direccion').value.trim();

    if (!nombre || !apellido || !email || !password || !telefono || !direccion) {
        mostrarError('Por favor, complete todos los campos.');
        return;
    }

    if (password !== confirmar) {
        mostrarError('Las contraseñas no coinciden.');
        return;
    }

    if (password.length < 8) {
        mostrarError('La contraseña debe tener al menos 8 caracteres.');
        return;
    }

    btnRegistro.disabled = true;
    btnRegistro.textContent = 'Creando cuenta...';

    try {
        const respuesta = await authApi.registro({
            nombre,
            apellido,
            email,
            password,
            telefono,
            direccion,
        });

        guardarSesion(respuesta);
        mostrarExito(respuesta.mensaje || '¡Cuenta creada exitosamente!');

        setTimeout(() => {
            window.location.href = 'index.html';
        }, 800);
    } catch (error) {
        mostrarError(error.message);
        btnRegistro.disabled = false;
        btnRegistro.textContent = 'Crear Cuenta';
    }
}

function init() {
    if (estaAutenticado()) {
        window.location.href = 'index.html';
        return;
    }

    inicializarNavbar();

    formRegistro = document.getElementById('form-registro');
    btnRegistro  = document.getElementById('btn-registro');

    if (formRegistro) {
        formRegistro.addEventListener('submit', manejarRegistro);
    }
}

document.addEventListener('DOMContentLoaded', init);
