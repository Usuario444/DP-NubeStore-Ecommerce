# ☁️ NubeStore API — Backend (Avance 1)

<div align="center">
  <img src="https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring_Boot-4.1.1-6DB33F?style=for-the-badge&logo=spring&logoColor=white" />
  <img src="https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white" />
  <img src="https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" />
</div>

API REST de e-commerce construida con JDBC puro, arquitecturas limpias y patrones de diseño.

## 🚀 Características base

1. **JDBC puro, cero ORM.** Sin JPA, Hibernate ni Spring Data. Acceso a datos con `java.sql.*`, `PreparedStatement` en todas las consultas y mapeo manual de `ResultSet` a objetos. Las transacciones son manuales (`setAutoCommit(false)` → `commit()` / `rollback()`) en `PedidoDAOImpl.registrarPedido`, con `SELECT ... FOR UPDATE` para bloquear el stock.
2. **Seguridad de contraseñas.** Migración estricta a **BCrypt** (`jbcrypt` de Mindrot) con factor de costo 12 (`$2a$12$`). Validación en tiempo constante. El login responde con el mismo retardo exista o no el email para mitigar ataques de enumeración.
3. **Lógica en PostgreSQL.** `detalle_pedido.subtotal` es `GENERATED ALWAYS AS (cantidad * precio_unitario) STORED`. Foreign Keys estrictas, `CHECK` (precio, stock, cantidades, estados válidos) y `UNIQUE` (email, código de seguimiento). La devolución se registra con un `INSERT ... SELECT` atómico que valida propiedad, estado y cantidad disponible.

## 📊 Alcance del Avance 1

| Actor | Cobertura | Casos de uso |
|---|---|---|
| Cliente | 100% | Registro/login, buscar productos, comprar (transacción pedido + detalle), seguimiento de pedidos, solicitar devoluciones |
| Vendedor | 25% | Publicar productos |

## 🛠️ Requisitos

- JDK 21
- Dependencias clave: `spring-boot-starter-web`, `org.mindrot:jbcrypt`, `org.postgresql:postgresql`.
- Maven 3.9+
- PostgreSQL 12+

## ⚙️ Ejecutar

`ash
# 1. Crear la base de datos
psql -U postgres -c "CREATE DATABASE nubestore WITH ENCODING 'UTF8';"

# 2. Crear tablas y datos semilla (¡el script ejecuta DROP TABLE de las tablas existentes!)
psql -U postgres -d nubestore -f src/main/resources/db/schema.sql

# 3. Levantar la API (http://localhost:8080)
mvn spring-boot:run
`

`schema.sql` **no** se ejecuta automáticamente: el proyecto no tiene DataSource de Spring.

**Configuración** (`src/main/resources/application.properties`). La conexión JDBC la lee `ConexionBD`, no Spring, y se puede sobrescribir con variables de entorno:

| Propiedad | Variable de entorno | Valor por defecto |
|---|---|---|
| `nubestore.db.url` | `NUBESTORE_DB_URL` | `jdbc:postgresql://localhost:5432/nubestore` |
| `nubestore.db.username` | `NUBESTORE_DB_USER` | `postgres` |
| `nubestore.db.password` | `NUBESTORE_DB_PASSWORD` | `postgres` |
| `nubestore.cors.allowed-origins` | — | `http://localhost:63342, http://127.0.0.1:63342` |

## 👥 Cuentas semilla

`data.sql` inserta:

| Entidad | Datos |
|---|---|
| Vendedor `id=1` | `NubeStore Official` · `ventas@nubestore.pe` · hash BCrypt real (Permite login en panel administrativo) |
| Productos `id=1..4` | Polo de Algodón (S/ 49.90), Pantalón Jean Slim (129.90), Zapatillas Urban (199.00), Casaca Puffer (249.50) |
| Clientes | Ninguno. Se crean con `POST /api/auth/registro` (contraseña mínima: 8 caracteres) |

## 📡 Rutas de la API

| Método | Ruta | Actor | Descripción | Éxito |
|---|---|---|---|---|
| POST | `/api/auth/registro` | Cliente | Registra un cliente | 201 |
| POST | `/api/auth/login` | Cliente | Valida credenciales | 200 |
| POST | `/api/auth/admin/login` | Administrador | Valida credenciales del dueño/vendedor | 200 |
| GET | `/api/clientes/{idCliente}` | Cliente | Perfil del cliente | 200 |
| GET | `/api/productos` | Cliente | Búsqueda. Query opcionales: `texto`, `categoria`, `precioMin`, `precioMax`, `pagina` (desde 1), `tamanio` (def. 20, máx. 100) | 200 |
| GET | `/api/productos/{id}` | Cliente | Detalle de producto activo | 200 |
| POST | `/api/productos` | Vendedor | Publica un producto | 201 |
| POST | `/api/pedidos` | Cliente | Compra (pedido + detalles + descuento de stock en una transacción) | 201 |
| GET | `/api/pedidos/cliente/{idCliente}` | Cliente | Pedidos del cliente, más reciente primero | 200 |
| GET | `/api/pedidos/{idPedido}?idCliente=` | Cliente | Seguimiento: estado, código, ítems | 200 |
| POST | `/api/pedidos/devoluciones` | Cliente | Solicita devolución de un ítem | 201 |
| GET | `/api/pedidos/devoluciones/cliente/{idCliente}` | Cliente | Devoluciones del cliente | 200 |

### 📖 Ejemplos

`ash
# Registro
curl -X POST localhost:8080/api/auth/registro -H "Content-Type: application/json" \
  -d '{"nombre":"Ana","apellido":"Ruiz","email":"ana@mail.com","password":"clave1234","telefono":"999888777","direccion":"Av. Grau 123, Ica"}'

# Buscar
curl "localhost:8080/api/productos?texto=polo&precioMax=100"

# Publicar producto (Vendedor)
curl -X POST localhost:8080/api/productos -H "Content-Type: application/json" \
  -d '{"idVendedor":1,"nombre":"Gorra Deportiva","categoria":"Accesorios","precio":39.90,"stock":15,"imagenUrl":"https://ejemplo.com/gorra.jpg"}'

# Comprar (el precio siempre se toma de la BD)
curl -X POST localhost:8080/api/pedidos -H "Content-Type: application/json" \
  -d '{"idCliente":1,"items":[{"idProducto":2,"cantidad":2},{"idProducto":3,"cantidad":1}]}'

# Seguimiento
curl "localhost:8080/api/pedidos/1?idCliente=1"

# Devolución
curl -X POST localhost:8080/api/pedidos/devoluciones -H "Content-Type: application/json" \
  -d '{"idCliente":1,"idDetalle":1,"cantidad":1,"motivo":"Talla incorrecta"}'
`

### ⚖️ Reglas de negocio

- Estado inicial del pedido: `PENDIENTE`. Estados válidos: `PENDIENTE`, `PAGADO`, `PREPARANDO`, `ENVIADO`, `ENTREGADO`, `CANCELADO`. **Ningún endpoint del Avance 1 cambia el estado.** Para probar devoluciones: `UPDATE pedido SET estado = 'ENTREGADO' WHERE id_pedido = 1;`
- Compra: máx. 50 líneas por pedido y 100 unidades por producto; las líneas repetidas se consolidan. Si no se envía `direccionEnvio` se usa la del cliente.
- Devolución: solo ítems de pedidos propios en estado `ENTREGADO`, y cantidad ≤ comprada − ya devuelta (las `RECHAZADA` no cuentan).
- Código de seguimiento: `NS-yyyyMMdd-XXXXXXXX`.

### ❌ Errores

Formato único (`ErrorResponse`):

`json
{ "timestamp": "2026-XX-XXT10:15:30", "status": 409, "error": "Conflict",
  "message": "Stock insuficiente o producto no disponible: Polo de Algodón Básico",
  "path": "/api/pedidos" }
`

| Código | Causa |
|---|---|
| 400 | Validación, JSON mal formado, parámetro inválido |
| 401 | Credenciales incorrectas |
| 404 | Recurso inexistente (o ajeno al cliente) |
| 409 | Email duplicado, stock insuficiente |
| 422 | Regla de negocio (devolución no permitida) |
| 500 | Error interno (detalle solo en el log del servidor) |

## 📁 Estructura del proyecto

`	ext
nubestore-backend/
├── pom.xml
└── src/main/
    ├── java/com/utp/nubestore/
    │   ├── NubestoreApplication.java
    │   ├── config/        CorsConfig
    │   ├── controller/    AuthController, ClienteController, PedidoController, ProductoController
    │   ├── dao/           ClienteDAO, PedidoDAO, ProductoDAO (interfaces)
    │   │   ├── factory/   DAOFactory
    │   │   └── impl/      BaseDAO, ClienteDAOImpl, PedidoDAOImpl, ProductoDAOImpl
    │   ├── dto/
    │   │   ├── request/   CrearPedidoRequest, FiltroProductoRequest, ItemPedidoRequest, LoginRequest, PublicarProductoRequest, RegistroClienteRequest, SolicitarDevolucionRequest
    │   │   └── response/  AuthResponse, ClienteResponse, DetallePedidoResponse, DevolucionResponse, ErrorResponse, PedidoResponse, ProductoResponse
    │   ├── exception/     ApiException, GlobalExceptionHandler
    │   ├── model/         Cliente, DetallePedido, Devolucion, Pedido, Producto, Usuario, Vendedor
    │   ├── service/       ClienteService, PedidoService, ProductoService
    │   └── util/          ConexionBD, PasswordUtil, Validador
    └── resources/
        ├── application.properties
        └── db/            data.sql, schema.sql
`

### 🏗️ Arquitectura, Patrones y Principios SOLID

Arquitectura de 3 capas estricta: **Controller → Service → DAO (interfaz) → JDBC / PostgreSQL**. Los controllers solo manejan DTOs; las reglas de negocio viven en los Services; el SQL, solo en los DAOs.

**Manejo de excepciones.** Cada método de `*DAOImpl` captura `SQLException` y la traduce a `ApiException` según el `SQLState` (`23505` → 409, `23503`/`23514`/`23502` → 400, otro → 500). `GlobalExceptionHandler` (`@RestControllerAdvice`) convierte toda excepción en un `ErrorResponse` sin filtrar detalles internos.

#### 🎨 Patrones GoF aplicados

| Patrón | Clase | Aplicación |
|---|---|---|
| **Singleton** | `ConexionBD` | Constructor privado y `getInstance()` con double-checked locking (`volatile`). Mantiene **una única** `Connection` a PostgreSQL y la reabre si se cierra o deja de ser válida (`getConexion()`). Expone un `ReentrantLock` (`getLock()`) que todos los DAOs adquieren en cada operación, para que una transacción nunca se mezcle con otra petición concurrente. |
| **Factory** | `DAOFactory` | Único lugar que conoce las clases `*DAOImpl`. `getProductoDAO()`, `getPedidoDAO()` y `getClienteDAO()` devuelven las **interfaces**; los Services nunca importan una implementación concreta. |

#### 🧠 Patrones GRASP aplicados

| Patrón | Clases | Aplicación |
|---|---|---|
| **Controlador** | `AuthController`, `ClienteController`, `ProductoController`, `PedidoController` (`@RestController`) | Reciben la petición HTTP como DTO, delegan en el Service y devuelven el DTO. Sin lógica de negocio ni acceso a datos. |
| **Experto** | `Producto`, `Pedido`, `DetallePedido` | La clase que tiene los datos hace el cálculo: `Producto.tieneStockPara(cantidad)`, `Pedido.recalcularTotal()`, `Pedido.estaEntregado()`, `DetallePedido.getSubtotal()`. |

#### 💎 Principios SOLID

| Principio | Cómo se respeta |
|---|---|
| **S** — Responsabilidad Única | Cada clase tiene un solo motivo de cambio: controllers (HTTP), services (reglas), DAOs (SQL), `Validador` (entradas), `PasswordUtil` (hash), `GlobalExceptionHandler` (errores), `ConexionBD` (conexión). |
| **O** — Abierto/Cerrado | Se agrega una nueva persistencia implementando las interfaces DAO, sin modificar Services ni Controllers; el único cambio queda confinado a `DAOFactory`. |
| **L** — Sustitución de Liskov | `ProductoDAOImpl`, `PedidoDAOImpl` y `ClienteDAOImpl` cumplen el contrato de sus interfaces; los Services funcionan con cualquier implementación (constructor alternativo que recibe los DAOs, útil para pruebas). |
| **I** — Segregación de Interfaces | Interfaces DAO pequeñas por entidad (`ProductoDAO`, `PedidoDAO`, `ClienteDAO`); cada Service depende solo de las que usa (`ProductoService` solo de `ProductoDAO`). |
| **D** — Inversión de Dependencias | Los Services dependen de abstracciones (interfaces DAO), no de clases JDBC; los Controllers dependen de los Services. Las implementaciones se obtienen vía `DAOFactory`. |

#### ⏳ Pendientes (no incluidos en el Avance 1)

Los siguientes patrones no están implementados. El estado del pedido se maneja hoy con constantes `String` en `Pedido`.

- **Facade** (`SistemaFacade`): interfaz simplificada sobre los Services.
- **Observer** (`NotificadorPedido`): aviso de cambios de estado del pedido.
- **State** (`EstadoPedido`): transiciones de estado sin condicionales encadenados.

## 🚧 Limitaciones conocidas

- **Sin autenticación por token.** El login valida credenciales pero no emite sesión ni JWT (no hay Spring Security en las dependencias); los endpoints reciben `idCliente` en el body o la ruta. Cualquier llamador podría enviar el id de otro cliente.
- **Conexión única.** El Singleton con un lock global serializa el acceso a la BD: es correcto y seguro para transacciones, pero limita la concurrencia.
- **Vendedor con Login Propio.** Cuenta con su propio endpoint aislado (`/api/auth/admin/login`) por seguridad.
- **Estado del pedido fijo.** Ningún endpoint lo avanza (ver "Reglas de negocio").