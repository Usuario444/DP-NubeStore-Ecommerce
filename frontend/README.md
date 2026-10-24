# 🎨 NubeStore Frontend

Frontend estático para el e-commerce universitario **NubeStore**, una tienda de moda online con diseño editorial premium inspirado en marcas contemporáneas.

## 🛠️ Stack Tecnológico

| Tecnología | Uso |
|---|---|
| **HTML5** | Estructura semántica de las vistas |
| **CSS3 Puro** | Diseño completo sin frameworks (Grid, Flexbox, Custom Properties) |
| **JavaScript ES Modules** | Lógica del cliente con arquitectura modular nativa |
| **Google Fonts** | Tipografías Playfair Display + DM Sans |

> **Sin frameworks CSS ni librerías JS.** Todo el diseño, componentes (modales, toasts, navbar responsive) y lógica están construidos desde cero con Vanilla JS y CSS puro.

---

## 📋 Requisitos

- **Backend** corriendo en `http://localhost:8080` → [backend](../backend)
- **Servidor web local** (IntelliJ IDEA integrado en `http://localhost:63342`, Live Server en VS Code o similar)
- Navegador moderno (Chrome, Firefox, Edge)

> ⚠️ **Nota:** No abrir los archivos HTML con doble clic (`file://`). Los módulos ES (`import`/`export`) requieren ser servidos a través de un servidor HTTP local para evitar bloqueos de políticas CORS del navegador.

---

## 🚀 Ejecución

1. **Levantar el backend:**
   ```bash
   cd backend
   mvn spring-boot:run
   ```

2. **Abrir el frontend en IntelliJ IDEA:**
   - Clic derecho en `index.html` → **Open In** → **Browser** (o abrir con Live Server en VS Code).
   - Se abrirá en `http://localhost:63342/DP-NubeStore-Ecommerce/frontend/index.html` (o la ruta correspondiente de tu servidor local).

---

## 📁 Estructura del Proyecto

```text
frontend/
├── index.html              # Catálogo de productos (página principal)
├── login.html              # Inicio de sesión de clientes
├── login-admin.html        # Inicio de sesión de vendedores / administración
├── registro.html           # Creación de cuenta de clientes
├── carrito.html            # Bolsa de compras + checkout
├── pedidos.html            # Historial y seguimiento de pedidos
├── vendedor.html           # Panel de publicación de prendas
│
├── css/
│   └── style.css           # Design System completo (900+ líneas)
│                           # - Variables CSS (Light/Dark Mode)
│                           # - CSS Grid + Flexbox responsive
│                           # - Componentes: modal, toast, navbar, cards
│                           # - Animaciones y transiciones
│
├── js/
│   ├── config.js           # URL base, endpoints, constantes, storage keys
│   │
│   ├── api/                # Capa de red (fetch wrapper)
│   │   ├── httpClient.js   # Cliente HTTP centralizado + ApiError
│   │   ├── authApi.js      # Login y registro
│   │   ├── productosApi.js # CRUD y búsqueda de productos
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
│       ├── login-admin.js
│       ├── registro.js
│       ├── carrito.js
│       ├── pedidos.js
│       └── vendedor.js
│
└── assets/
    └── logo.jpg            # Logo NubeStore
```

---

## 🏛️ Arquitectura

```text
Vista (HTML) ──► Controlador (pages/) ──► API (api/) ──► Backend
                                       ──► Estado (state/)
                                       ──► UI (ui/)
```

**Separación estricta de responsabilidades:**
- `pages/` → Orquesta la vista. No hace `fetch` directo ni manipula `localStorage`.
- `api/` → Única capa que realiza peticiones HTTP al backend.
- `state/` → Único punto de acceso a `localStorage` y estado en memoria.
- `ui/` → Renderizado seguro del DOM y sistema de notificaciones.

---

## ✨ Funcionalidades

### 👤 Cliente
- Registro y login con validación en cliente y servidor.
- Catálogo con filtros (nombre, categoría, rango de precio).
- Carrito de compras reactivo con gestión de cantidades.
- Checkout con creación de pedido transaccional.
- Historial de pedidos con modal de detalle y seguimiento en tiempo real.
- Solicitud de devoluciones para pedidos entregados.

### 🏪 Vendedor
- Login independiente para vendedores (`login-admin.html`).
- Publicación de productos con categorías de moda, precios e inventario.

### 🌐 General
- Modo Claro / Oscuro persistente mediante CSS Custom Properties.
- Diseño 100% responsive (mobile-first).
- Barra de navegación dinámica según el rol y estado de la sesión.

---

## 🔒 Seguridad Frontend

- **Prevención XSS:** Cero uso de `.innerHTML` para datos provenientes de la API. Todo el renderizado dinámico usa `document.createElement()` y `.textContent`.
- **Manejo centralizado de errores:** Todas las peticiones pasan por `httpClient.js`, el cual evalúa `res.ok` y lanza un `ApiError` capturando el mensaje exacto enviado por el backend.
- **Guardias de ruta:** `session.js` expone funciones como `requiereAutenticacion()` y `requiereRol()` para proteger vistas que requieren sesión iniciada.

---

## 🔌 Conexión con el Backend

| Frontend | Backend |
|---|---|
| `http://localhost:63342` | `http://localhost:8080` |
| `config.js` → `API_BASE_URL` | `CorsConfig.java` permite el origen del frontend |

---

**Proyecto Universitario — UTP 2026**
