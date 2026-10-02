/**
 * @file alertas.js
 * @description Sistema de notificaciones (Toasts) customizado sin Bootstrap.
 * @module ui/alertas
 */

/**
 * Muestra un toast en pantalla.
 * @param {string} mensaje 
 * @param {'success' | 'error' | 'warning' | 'info'} tipo 
 */
function mostrarToast(mensaje, tipo = 'info') {
    const container = document.getElementById('toast-container');
    if (!container) return;

    const toast = document.createElement('div');
    toast.classList.add('toast', `toast-${tipo}`);

    const iconBox = document.createElement('div');
    iconBox.classList.add('toast-icon');
    
    // Íconos simples
    const iconos = {
        success: '✓',
        error: '✕',
        warning: '⚠',
        info: 'ℹ'
    };
    iconBox.textContent = iconos[tipo] || iconos.info;

    const messageBox = document.createElement('div');
    messageBox.classList.add('toast-message');
    messageBox.textContent = mensaje;

    const closeBtn = document.createElement('button');
    closeBtn.classList.add('toast-close');
    closeBtn.textContent = '✕';

    const remover = () => {
        if (!toast.classList.contains('removing')) {
            toast.classList.add('removing');
            setTimeout(() => {
                if (toast.parentNode) toast.parentNode.removeChild(toast);
            }, 300); // duración de la animación css
        }
    };

    closeBtn.onclick = remover;

    toast.appendChild(iconBox);
    toast.appendChild(messageBox);
    toast.appendChild(closeBtn);

    container.appendChild(toast);

    // Auto-remover
    setTimeout(remover, 4000);
}

export function mostrarExito(mensaje) {
    mostrarToast(mensaje, 'success');
}

export function mostrarError(mensaje) {
    mostrarToast(mensaje, 'error');
}

export function mostrarInfo(mensaje) {
    mostrarToast(mensaje, 'info');
}

export function mostrarAdvertencia(mensaje) {
    mostrarToast(mensaje, 'warning');
}
