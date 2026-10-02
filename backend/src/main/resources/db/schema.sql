-- =====================================================================
-- NubeStore - Avance 1
-- Script de creación de base de datos (PostgreSQL 12+)
-- =====================================================================
-- Ejecutar primero (conectado a la BD "postgres"):
--   CREATE DATABASE nubestore WITH ENCODING 'UTF8';
-- Luego conectarse a "nubestore" y ejecutar el resto del script.
-- =====================================================================

-- ---------------------------------------------------------------------
-- Limpieza (orden inverso a las dependencias)
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS devolucion      CASCADE;
DROP TABLE IF EXISTS detalle_pedido  CASCADE;
DROP TABLE IF EXISTS pedido          CASCADE;
DROP TABLE IF EXISTS producto        CASCADE;
DROP TABLE IF EXISTS vendedor        CASCADE;
DROP TABLE IF EXISTS cliente         CASCADE;

-- ---------------------------------------------------------------------
-- CLIENTE
-- ---------------------------------------------------------------------
CREATE TABLE cliente (
                         id_cliente      SERIAL PRIMARY KEY,
                         nombre          VARCHAR(80)  NOT NULL,
                         apellido        VARCHAR(80)  NOT NULL,
                         email           VARCHAR(120) NOT NULL UNIQUE,
                         password_hash   VARCHAR(255) NOT NULL,
                         telefono        VARCHAR(20),
                         direccion       VARCHAR(255),
                         fecha_registro  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------------
-- VENDEDOR
-- ---------------------------------------------------------------------
CREATE TABLE vendedor (
                          id_vendedor     SERIAL PRIMARY KEY,
                          nombre_tienda   VARCHAR(120) NOT NULL,
                          email           VARCHAR(120) NOT NULL UNIQUE,
                          password_hash   VARCHAR(255) NOT NULL,
                          telefono        VARCHAR(20),
                          fecha_registro  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------------
-- PRODUCTO (publicado por un vendedor)
-- ---------------------------------------------------------------------
CREATE TABLE producto (
                          id_producto        SERIAL PRIMARY KEY,
                          id_vendedor        INTEGER        NOT NULL,
                          nombre             VARCHAR(150)   NOT NULL,
                          descripcion        TEXT,
                          categoria          VARCHAR(80)    NOT NULL,
                          precio             NUMERIC(10,2)  NOT NULL,
                          stock              INTEGER        NOT NULL,
                          imagen_url         VARCHAR(255),
                          activo             BOOLEAN        NOT NULL DEFAULT TRUE,
                          fecha_publicacion  TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,

                          CONSTRAINT fk_producto_vendedor
                              FOREIGN KEY (id_vendedor) REFERENCES vendedor (id_vendedor),
                          CONSTRAINT ck_producto_precio CHECK (precio > 0),
                          CONSTRAINT ck_producto_stock  CHECK (stock >= 0)
);

CREATE INDEX idx_producto_nombre    ON producto (LOWER(nombre));
CREATE INDEX idx_producto_categoria ON producto (categoria);
CREATE INDEX idx_producto_vendedor  ON producto (id_vendedor);

-- ---------------------------------------------------------------------
-- PEDIDO (cabecera)
-- El seguimiento se maneja con "estado", "codigo_seguimiento" y
-- "fecha_actualizacion".
-- ---------------------------------------------------------------------
CREATE TABLE pedido (
                        id_pedido            SERIAL PRIMARY KEY,
                        id_cliente           INTEGER        NOT NULL,
                        fecha_pedido         TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        fecha_actualizacion  TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        estado               VARCHAR(20)    NOT NULL DEFAULT 'PENDIENTE',
                        total                NUMERIC(12,2)  NOT NULL,
                        direccion_envio      VARCHAR(255)   NOT NULL,
                        codigo_seguimiento   VARCHAR(40)    UNIQUE,

                        CONSTRAINT fk_pedido_cliente
                            FOREIGN KEY (id_cliente) REFERENCES cliente (id_cliente),
                        CONSTRAINT ck_pedido_estado CHECK (estado IN
                                                           ('PENDIENTE', 'PAGADO', 'PREPARANDO', 'ENVIADO', 'ENTREGADO', 'CANCELADO')),
                        CONSTRAINT ck_pedido_total CHECK (total >= 0)
);

CREATE INDEX idx_pedido_cliente ON pedido (id_cliente);
CREATE INDEX idx_pedido_estado  ON pedido (estado);

-- ---------------------------------------------------------------------
-- DETALLE_PEDIDO
-- precio_unitario guarda el precio al momento de la compra, así un
-- cambio posterior del precio del producto no altera pedidos históricos.
-- subtotal es una columna generada: la BD la calcula, JDBC no la inserta.
-- ---------------------------------------------------------------------
CREATE TABLE detalle_pedido (
                                id_detalle       SERIAL PRIMARY KEY,
                                id_pedido        INTEGER        NOT NULL,
                                id_producto      INTEGER        NOT NULL,
                                cantidad         INTEGER        NOT NULL,
                                precio_unitario  NUMERIC(10,2)  NOT NULL,
                                subtotal         NUMERIC(12,2)  GENERATED ALWAYS AS (cantidad * precio_unitario) STORED,

                                CONSTRAINT fk_detalle_pedido
                                    FOREIGN KEY (id_pedido)   REFERENCES pedido (id_pedido) ON DELETE CASCADE,
                                CONSTRAINT fk_detalle_producto
                                    FOREIGN KEY (id_producto) REFERENCES producto (id_producto),
                                CONSTRAINT ck_detalle_cantidad CHECK (cantidad > 0),
                                CONSTRAINT ck_detalle_precio   CHECK (precio_unitario > 0)
);

CREATE INDEX idx_detalle_pedido   ON detalle_pedido (id_pedido);
CREATE INDEX idx_detalle_producto ON detalle_pedido (id_producto);

-- ---------------------------------------------------------------------
-- DEVOLUCION (solicitada por el cliente sobre un ítem de un pedido)
-- ---------------------------------------------------------------------
CREATE TABLE devolucion (
                            id_devolucion    SERIAL PRIMARY KEY,
                            id_detalle       INTEGER      NOT NULL,
                            id_cliente       INTEGER      NOT NULL,
                            cantidad         INTEGER      NOT NULL,
                            motivo           VARCHAR(255) NOT NULL,
                            estado           VARCHAR(20)  NOT NULL DEFAULT 'SOLICITADA',
                            fecha_solicitud  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

                            CONSTRAINT fk_devolucion_detalle
                                FOREIGN KEY (id_detalle) REFERENCES detalle_pedido (id_detalle),
                            CONSTRAINT fk_devolucion_cliente
                                FOREIGN KEY (id_cliente) REFERENCES cliente (id_cliente),
                            CONSTRAINT ck_devolucion_cantidad CHECK (cantidad > 0),
                            CONSTRAINT ck_devolucion_estado CHECK (estado IN
                                                                   ('SOLICITADA', 'APROBADA', 'RECHAZADA', 'COMPLETADA'))
);

CREATE INDEX idx_devolucion_cliente ON devolucion (id_cliente);
CREATE INDEX idx_devolucion_detalle ON devolucion (id_detalle);

