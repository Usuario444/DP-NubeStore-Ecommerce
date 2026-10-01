-- ---------------------------------------------------------------------
-- DATOS DE PRUEBA (opcional)
-- Los clientes se registran vía API (POST /api/auth/registro), así su
-- contraseña se guarda con el hash correcto. El vendedor de prueba usa un
-- hash ficticio porque en este avance el vendedor solo publica productos.
-- ---------------------------------------------------------------------
INSERT INTO vendedor (nombre_tienda, email, password_hash, telefono)
VALUES ('TechNube Store', 'ventas@technube.pe', 'HASH_DE_PRUEBA', '999111222');

INSERT INTO producto (id_vendedor, nombre, descripcion, categoria, precio, stock, imagen_url)
VALUES
    (1, 'Laptop Lenovo IdeaPad 3',  'Intel Core i5, 8GB RAM, 512GB SSD', 'Computo',     2499.90, 15, NULL),
    (1, 'Mouse Inalámbrico Logitech', 'Mouse óptico 2.4GHz',             'Accesorios',    59.90, 80, NULL),
    (1, 'Audífonos Bluetooth JBL',    'Cancelación de ruido, 30h batería', 'Audio',      189.00, 40, NULL),
    (1, 'Teclado Mecánico Redragon',  'Switch azul, retroiluminado RGB',  'Accesorios',  149.50, 25, NULL);1, 'Teclado Mecánico Redragon',  'Switch azul, retroiluminado RGB',  'Accesorios',  149.50, 25, NULL);