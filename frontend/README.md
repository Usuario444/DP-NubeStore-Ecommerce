# NubeStore Frontend

Frontend estático para el e-commerce universitario **NubeStore**, una tienda de moda online con diseño editorial inspirado en H&M.

## Stack Tecnológico

| Tecnología | Uso |
|---|---|
| **HTML5** | Estructura semántica de las vistas |
| **CSS3 Puro** | Diseño completo sin frameworks (Grid, Flexbox, Custom Properties) |
| **JavaScript ES Modules** | Lógica del cliente con arquitectura modular |
| **Google Fonts** | Tipografías Playfair Display + DM Sans |

> **Sin frameworks CSS ni librerías JS.** Todo el diseño, componentes (modales, toasts, navbar responsive) y lógica están construidos desde cero.

## Requisitos

- **Backend** corriendo en `http://localhost:8080` → [nubestore-backend](../nubestore-backend)
- **Servidor web local** (IntelliJ IDEA integrado en `http://localhost:63342`)
- Navegador moderno (Chrome, Firefox, Edge)

> ⚠️ No abrir los HTML con doble clic (`file://`). Los ES Modules requieren un servidor HTTP.

## Ejecución

1. Levantar el backend:
   ```bash
   cd nubestore-backend
   mvn spring-boot:run
   ```

2. Abrir el frontend en IntelliJ IDEA:
   - Clic derecho en `index.html` → **Open In → Browser**
   - Se abrirá en `http://localhost:63342/nubestore-fronted/index.html`

## Estructura del Proyecto

```
nubestore-fronted/
├── index.html              # Catálogo de productos (página principal)
├── login.html              # Inicio de sesión
├── registro.html           # Creación de cuenta
├── carrito.html            # Bolsa de compras + checkout
├── pedidos.html            # Historial y seguimiento de pedidos
├── vendedor.html           # Panel de publicación de prendas
│
├── css/
│   └── style.css           # Design System completo (900+ líneas)
│                             → Variables CSS (Light/Dark Mode)
│                             → CSS Grid + Flexbox responsive
│                             → Componentes: modal, toast, navbar, cards
│                             → Animaciones y transiciones
│
├── js/
│   ├── config.js           # URL base, endpoints, constantes, storage keys
│   │
│   ├── api/                # Capa de red (fetch wrapper)
│   │   ├── httpClient.js   # Cliente HTTP centralizado + ApiError
│   │   ├── authApi.js      # Login y registro
│   │   ├── productosApi.js # CRUD de productos
│   │   └── pedidosApi.js   # Pedidos y devoluciones
│   │
│   ├── state/              # Estado de la aplicación
│   │   ├── session.js      # Sesión en localStorage (único punto de acceso)
│   │   └── carrito.js      # Carrito en memoria + persistencia
│   │
│   ├── ui/                 # Componentes de interfaz
│   │   ├── render.js       # Construcción segura del DOM (createElement)
│   │   ├── alertas.js      # Sistema de notificaciones (toasts)
│   │   └── formato.js      # Formateo de moneda (S/) y fechas
│   │
│   ├── components/
│   │   └── navbar.js       # Navegación dinámica + toggle dark mode
│   │
│   └── pages/              # Controladores de vista (1 por HTML)
│       ├── catalogo.js
│       ├── login.js
│       ├── registro.js
│       ├── carrito.js
│       ├── pedidos.js
│       └── vendedor.js
│
└── assets/
    └── logo.jpg            # Logo NubeStore
```

## Arquitectura

```
Vista (HTML) → Controlador (pages/) → API (api/) → Backend
                                    → Estado (state/)
                                    → UI (ui/)
```

**Separación estricta de responsabilidades:**
- `pages/` — Orquesta la vista. No hace `fetch` directo ni manipula `localStorage`.
- `api/` — Única capa que hace peticiones HTTP al backend.
- `state/` — Único acceso a `localStorage` y estado en memoria.
- `ui/` — Renderizado seguro del DOM y notificaciones.

## Funcionalidades

### Cliente
- Registro y login con validación
- Catálogo con filtros (nombre, categoría, rango de precio)
- Carrito de compras con gestión de cantidades
- Checkout (creación de pedido transaccional)
- Historial de pedidos con modal de detalle
- Solicitud de devoluciones

### Vendedor
- Publicación de productos con categorías de ropa

### General
- Modo Claro / Oscuro persistente
- Diseño 100% responsive (mobile-first)
- Navegación dinámica según rol y estado de sesión

## Seguridad Frontend

- **Prevención XSS:** Cero uso de `.innerHTML` para datos de la API. Todo el renderizado dinámico usa `document.createElement()` + `.textContent`.
- **Manejo de errores:** Todas las peticiones pasan por `httpClient.js` que evalúa `res.ok` y lanza un `ApiError` con el mensaje exacto del backend.
- **Guardias de ruta:** `session.js` expone `requiereAutenticacion()` y `requiereRol()` para proteger vistas.

## Conexión con el Backend

| Frontend | Backend |
|---|---|
| `http://localhost:63342` | `http://localhost:8080` |
| `config.js → API_BASE_URL` | `CorsConfig.java` debe permitir el origen del frontend |

---

**Proyecto Universitario — UTP 2026**
