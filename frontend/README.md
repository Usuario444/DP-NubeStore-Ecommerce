# NubeStore Frontend

Frontend estÃ¡tico para el e-commerce universitario **NubeStore**, una tienda de moda online con diseÃ±o editorial inspirado en H&M.

## Stack TecnolÃ³gico

| TecnologÃ­a | Uso |
|---|---|
| **HTML5** | Estructura semÃ¡ntica de las vistas |
| **CSS3 Puro** | DiseÃ±o completo sin frameworks (Grid, Flexbox, Custom Properties) |
| **JavaScript ES Modules** | LÃ³gica del cliente con arquitectura modular |
| **Google Fonts** | TipografÃ­as Playfair Display + DM Sans |

> **Sin frameworks CSS ni librerÃ­as JS.** Todo el diseÃ±o, componentes (modales, toasts, navbar responsive) y lÃ³gica estÃ¡n construidos desde cero.

## Requisitos

- **Backend** corriendo en `http://localhost:8080` â†’ [nubestore-backend](../nubestore-backend)
- **Servidor web local** (IntelliJ IDEA integrado en `http://localhost:63342`)
- Navegador moderno (Chrome, Firefox, Edge)

> âš ï¸ No abrir los HTML con doble clic (`file://`). Los ES Modules requieren un servidor HTTP.

## EjecuciÃ³n

1. Levantar el backend:
   ```bash
   cd nubestore-backend
   mvn spring-boot:run
   ```

2. Abrir el frontend en IntelliJ IDEA:
   - Clic derecho en `index.html` â†’ **Open In â†’ Browser**
   - Se abrirÃ¡ en `http://localhost:63342/nubestore-fronted/index.html`

## Estructura del Proyecto

```
nubestore-fronted/
â”œâ”€â”€ index.html              # CatÃ¡logo de productos (pÃ¡gina principal)
â”œâ”€â”€ login.html              # Inicio de sesiÃ³n
â”œâ”€â”€ registro.html           # CreaciÃ³n de cuenta
â”œâ”€â”€ carrito.html            # Bolsa de compras + checkout
â”œâ”€â”€ pedidos.html            # Historial y seguimiento de pedidos
â”œâ”€â”€ vendedor.html           # Panel de publicaciÃ³n de prendas
â”‚
â”œâ”€â”€ css/
â”‚   â””â”€â”€ style.css           # Design System completo (900+ lÃ­neas)
â”‚                             â†’ Variables CSS (Light/Dark Mode)
â”‚                             â†’ CSS Grid + Flexbox responsive
â”‚                             â†’ Componentes: modal, toast, navbar, cards
â”‚                             â†’ Animaciones y transiciones
â”‚
â”œâ”€â”€ js/
â”‚   â”œâ”€â”€ config.js           # URL base, endpoints, constantes, storage keys
â”‚   â”‚
â”‚   â”œâ”€â”€ api/                # Capa de red (fetch wrapper)
â”‚   â”‚   â”œâ”€â”€ httpClient.js   # Cliente HTTP centralizado + ApiError
â”‚   â”‚   â”œâ”€â”€ authApi.js      # Login y registro
â”‚   â”‚   â”œâ”€â”€ productosApi.js # CRUD de productos
â”‚   â”‚   â””â”€â”€ pedidosApi.js   # Pedidos y devoluciones
â”‚   â”‚
â”‚   â”œâ”€â”€ state/              # Estado de la aplicaciÃ³n
â”‚   â”‚   â”œâ”€â”€ session.js      # SesiÃ³n en localStorage (Ãºnico punto de acceso)
â”‚   â”‚   â””â”€â”€ carrito.js      # Carrito en memoria + persistencia
â”‚   â”‚
â”‚   â”œâ”€â”€ ui/                 # Componentes de interfaz
â”‚   â”‚   â”œâ”€â”€ render.js       # ConstrucciÃ³n segura del DOM (createElement)
â”‚   â”‚   â”œâ”€â”€ alertas.js      # Sistema de notificaciones (toasts)
â”‚   â”‚   â””â”€â”€ formato.js      # Formateo de moneda (S/) y fechas
â”‚   â”‚
â”‚   â”œâ”€â”€ components/
â”‚   â”‚   â””â”€â”€ navbar.js       # NavegaciÃ³n dinÃ¡mica + toggle dark mode
â”‚   â”‚
â”‚   â””â”€â”€ pages/              # Controladores de vista (1 por HTML)
â”‚       â”œâ”€â”€ catalogo.js
â”‚       â”œâ”€â”€ login.js
â”‚       â”œâ”€â”€ registro.js
â”‚       â”œâ”€â”€ carrito.js
â”‚       â”œâ”€â”€ pedidos.js
â”‚       â””â”€â”€ vendedor.js
â”‚
â””â”€â”€ assets/
    â””â”€â”€ logo.jpg            # Logo NubeStore
```

## Arquitectura

```
Vista (HTML) â†’ Controlador (pages/) â†’ API (api/) â†’ Backend
                                    â†’ Estado (state/)
                                    â†’ UI (ui/)
```

**SeparaciÃ³n estricta de responsabilidades:**
- `pages/` â€” Orquesta la vista. No hace `fetch` directo ni manipula `localStorage`.
- `api/` â€” Ãšnica capa que hace peticiones HTTP al backend.
- `state/` â€” Ãšnico acceso a `localStorage` y estado en memoria.
- `ui/` â€” Renderizado seguro del DOM y notificaciones.

## Funcionalidades

### Cliente
- Registro y login con validaciÃ³n
- CatÃ¡logo con filtros (nombre, categorÃ­a, rango de precio)
- Carrito de compras con gestiÃ³n de cantidades
- Checkout (creaciÃ³n de pedido transaccional)
- Historial de pedidos con modal de detalle
- Solicitud de devoluciones

### Vendedor
- PublicaciÃ³n de productos con categorÃ­as de ropa

### General
- Modo Claro / Oscuro persistente
- DiseÃ±o 100% responsive (mobile-first)
- NavegaciÃ³n dinÃ¡mica segÃºn rol y estado de sesiÃ³n

## Seguridad Frontend

- **PrevenciÃ³n XSS:** Cero uso de `.innerHTML` para datos de la API. Todo el renderizado dinÃ¡mico usa `document.createElement()` + `.textContent`.
- **Manejo de errores:** Todas las peticiones pasan por `httpClient.js` que evalÃºa `res.ok` y lanza un `ApiError` con el mensaje exacto del backend.
- **Guardias de ruta:** `session.js` expone `requiereAutenticacion()` y `requiereRol()` para proteger vistas.

## ConexiÃ³n con el Backend

| Frontend | Backend |
|---|---|
| `http://localhost:63342` | `http://localhost:8080` |
| `config.js â†’ API_BASE_URL` | `CorsConfig.java` debe permitir el origen del frontend |

---

**Proyecto Universitario â€” UTP 2026**

