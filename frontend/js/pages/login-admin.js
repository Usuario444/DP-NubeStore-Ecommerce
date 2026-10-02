import { request } from '../api/httpClient.js';
import { ENDPOINTS, STORAGE_KEYS, ROLES } from '../config.js';
import { guardarSesion } from '../state/session.js';
import { mostrarError, mostrarExito } from '../ui/alertas.js';

document.addEventListener('DOMContentLoaded', () => {
    const formLoginAdmin = document.getElementById('form-login-admin');

    if (formLoginAdmin) {
        formLoginAdmin.addEventListener('submit', async (e) => {
            e.preventDefault();

            const email = document.getElementById('login-email').value.trim();
            const password = document.getElementById('login-password').value;

            if (!email || !password) {
                mostrarError('Por favor complete todos los campos.');
                return;
            }

            try {
                // Hacemos el POST al endpoint exclusivo del vendedor que acabamos de crear en Java
                const respuesta = await request(ENDPOINTS.AUTH.LOGIN.replace('/login', '/admin/login'), {
                    method: 'POST',
                    body: { email: email, password: password }
                });
                
                // Guardamos simulando la estructura del AuthResponse
                // respuesta.vendedor tiene {idVendedor, nombreTienda, email...}
                const authDataMock = {
                    cliente: {
                        idCliente: respuesta.vendedor.idVendedor, // Lo reusamos como ID de sesión
                        nombre: respuesta.vendedor.nombreTienda,
                        apellido: 'Store',
                        email: respuesta.vendedor.email
                    }
                };
                
                guardarSesion(authDataMock);
                
                // Sobrescribimos el ROL explícitamente a VENDEDOR
                localStorage.setItem(STORAGE_KEYS.ROL, ROLES.VENDEDOR);

                mostrarExito(respuesta.mensaje || 'Autenticación exitosa.');
                
                setTimeout(() => {
                    window.location.href = 'vendedor.html';
                }, 1000);
            } catch (error) {
                mostrarError(error.message || 'Credenciales incorrectas o error en el servidor.');
            }
        });
    }
});