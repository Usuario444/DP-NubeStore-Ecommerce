/**
 * @file formato.js
 * @description Utilidades de formateo para moneda (S/) y fechas.
 *              Usa la API nativa `Intl` para formateo locale-aware.
 * @module ui/formato
 */

import { LOCALE } from '../config.js';

// ─────────────────────────────────────────────
//  Instancias de Intl (se crean una sola vez)
// ─────────────────────────────────────────────

/**
 * Formateador de moneda peruana (PEN / S/).
 * @type {Intl.NumberFormat}
 * @private
 */
const formateadorMoneda = new Intl.NumberFormat(LOCALE.CODIGO, {
    style:    'currency',
    currency: LOCALE.MONEDA,
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
});

/**
 * Formateador de fechas con hora.
 * Formato resultante: "01 oct 2026, 14:30"
 * @type {Intl.DateTimeFormat}
 * @private
 */
const formateadorFechaHora = new Intl.DateTimeFormat(LOCALE.CODIGO, {
    timeZone: LOCALE.ZONA,
    day:      '2-digit',
    month:    'short',
    year:     'numeric',
    hour:     '2-digit',
    minute:   '2-digit',
    hour12:   false,
});

/**
 * Formateador de solo fecha.
 * Formato resultante: "01 oct 2026"
 * @type {Intl.DateTimeFormat}
 * @private
 */
const formateadorFecha = new Intl.DateTimeFormat(LOCALE.CODIGO, {
    timeZone: LOCALE.ZONA,
    day:      '2-digit',
    month:    'short',
    year:     'numeric',
});

// ─────────────────────────────────────────────
//  Funciones públicas
// ─────────────────────────────────────────────

/**
 * Formatea un número como moneda peruana (S/).
 *
 * @param {number} monto - Valor numérico a formatear.
 * @returns {string} Monto formateado (ej. "S/ 2,599.90").
 *
 * @example
 * formatearMoneda(2599.9); // → "S/ 2,599.90"
 */
export function formatearMoneda(monto) {
    if (typeof monto !== 'number' || isNaN(monto)) {
        return `${LOCALE.SIMBOLO} 0.00`;
    }
    return formateadorMoneda.format(monto);
}

/**
 * Formatea una fecha ISO 8601 a formato legible con hora.
 *
 * @param {string} fechaISO - Fecha en formato ISO 8601.
 * @returns {string} Fecha formateada (ej. "01 oct 2026, 14:30").
 *
 * @example
 * formatearFechaHora('2026-10-01T14:30:00'); // → "01 oct 2026, 14:30"
 */
export function formatearFechaHora(fechaISO) {
    if (!fechaISO) return '—';
    try {
        return formateadorFechaHora.format(new Date(fechaISO));
    } catch {
        return String(fechaISO);
    }
}

/**
 * Formatea una fecha ISO 8601 a formato legible (solo fecha).
 *
 * @param {string} fechaISO - Fecha en formato ISO 8601.
 * @returns {string} Fecha formateada (ej. "01 oct 2026").
 */
export function formatearFecha(fechaISO) {
    if (!fechaISO) return '—';
    try {
        return formateadorFecha.format(new Date(fechaISO));
    } catch {
        return String(fechaISO);
    }
}

/**
 * Traduce el estado de un pedido a un texto y clase CSS de Bootstrap
 * para mostrarlo como badge visual.
 *
 * @param {string} estado - Estado del pedido tal como viene del backend.
 * @returns {{ texto: string, clase: string }} Texto legible y clase CSS del badge.
 *
 * @example
 * const { texto, clase } = obtenerBadgeEstado('PENDIENTE');
 * // → { texto: 'Pendiente', clase: 'bg-warning text-dark' }
 */
export function obtenerBadgeEstado(estado) {
    const mapa = {
        'PENDIENTE':  { texto: 'Pendiente',  clase: 'bg-warning text-dark' },
        'CONFIRMADO': { texto: 'Confirmado', clase: 'bg-info text-dark' },
        'ENVIADO':    { texto: 'Enviado',    clase: 'bg-primary' },
        'ENTREGADO':  { texto: 'Entregado',  clase: 'bg-success' },
        'CANCELADO':  { texto: 'Cancelado',  clase: 'bg-danger' },
        'DEVUELTO':   { texto: 'Devuelto',   clase: 'bg-secondary' },
    };

    return mapa[estado] || { texto: estado, clase: 'bg-secondary' };
}
