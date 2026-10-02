-- ---------------------------------------------------------------------
-- DATOS DE PRUEBA
-- Los clientes se registran vía API (POST /api/auth/registro), así su
-- contraseña se guarda con el hash correcto.
-- ---------------------------------------------------------------------
INSERT INTO vendedor (nombre_tienda, email, password_hash, telefono)
VALUES ('NubeStore Official', 'ventas@nubestore.pe', '$2a$12$drvXQqKRojqEGljf60ckV.Ir4yhMgXCAwkcq8.0s9.1ycphzKpJG.', '999111222');

INSERT INTO producto (id_vendedor, nombre, descripcion, categoria, precio, stock, imagen_url)
VALUES
    (1, 'Polo de Algodón Básico',  'Polo 100% algodón, cuello redondo, varios colores', 'Ropa Superior', 49.90, 50, NULL),
    (1, 'Pantalón Jean Slim Fit',  'Pantalón de mezclilla corte slim, color azul clásico', 'Ropa Inferior', 129.90, 40, NULL),
    (1, 'Zapatillas Urban Blancas', 'Zapatillas urbanas de cuero sintético, suela de goma', 'Calzado', 199.00, 25, NULL),
    (1, 'Casaca de Invierno Puffer', 'Casaca térmica acolchada, resistente al agua', 'Abrigos', 249.50, 15, NULL);