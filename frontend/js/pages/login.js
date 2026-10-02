/**
 * @file login.js
 * @description Controlador de vista para login.html (solo inicio de sesión).
 * @module pages/login
 */

import { inicializarNavbar } from '../components/navbar.js';
import * as authApi from '../api/authApi.js';
import { guardarSesion, estaAutenticado } from '../state/session.js';
import { mostrarExito, mostrarError } from '../ui/alertas.js';

// ─────────────────────────────────────────────

/** @type {HTMLFormElement} */
let formLogin;
/** @type {HTMLButtonElement} */
let btnLogin;

/**
 * @param {SubmitEvent} e
 * @async
 */
async function manejarLogin(e) {
    e.preventDefault();

    const email    = document.getElementById('login-email').value.trim();
    const password = document.getElementById('login-password').value;

    if (!email || !password) {
        mostrarError('Por favor, complete todos los campos.');
        return;
    }

    btnLogin.disabled = true;
    btnLogin.textContent = 'Ingresando...';

    try {
        const respuesta = await authApi.login({ email, password });
        guardarSesion(respuesta);
        mostrarExito(respuesta.mensaje || '¡Bienvenido!');

        setTimeout(() => {
            window.location.href = 'index.html';
        }, 800);
    } catch (error) {
        mostrarError(error.message);
        btnLogin.disabled = false;
        btnLogin.textContent = 'Iniciar Sesión';
    }
}

function init() {
    if (estaAutenticado()) {
        window.location.href = 'index.html';
        return;
    }

    inicializarNavbar();

    formLogin = document.getElementById('form-login');
    btnLogin  = document.getElementById('btn-login');

    if (formLogin) {
        formLogin.addEventListener('submit', manejarLogin);
    }
}

document.addEventListener('DOMContentLoaded', init);
