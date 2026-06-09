-- Script de creación y población de la base de datos de Subastas

CREATE DATABASE IF NOT EXISTS bd_subastas;
USE bd_subastas;

-- Desactivar llaves foráneas para reinicio limpio
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS ofertas;
DROP TABLE IF EXISTS articulos;
DROP TABLE IF EXISTS categorias;
DROP TABLE IF EXISTS usuarios;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. Tabla de Usuarios
CREATE TABLE usuarios (
    id VARCHAR(36) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    direccion VARCHAR(200) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL
);

-- 2. Tabla de Categorías (Organización Jerárquica)
CREATE TABLE categorias (
    id VARCHAR(36) PRIMARY KEY,
    nombre VARCHAR(100) UNIQUE NOT NULL,
    padre_id VARCHAR(36) NULL,
    FOREIGN KEY (padre_id) REFERENCES categorias(id) ON DELETE SET NULL
);

-- 3. Tabla de Artículos
CREATE TABLE articulos (
    id VARCHAR(36) PRIMARY KEY,
    vendedor_id VARCHAR(36) NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    estado VARCHAR(20) NOT NULL, -- NUEVO, USADO, etc.
    precio_inicial DECIMAL(10,2) NOT NULL,
    fecha_limite TIMESTAMP NOT NULL,
    categoria_id VARCHAR(36) NOT NULL,
    estado_subasta VARCHAR(30) NOT NULL, -- ACTIVA, ADJUDICADA, CANCELADA_SIN_PISO
    adjudicado_a_id VARCHAR(36) NULL,
    precio_final DECIMAL(10,2) NULL,
    FOREIGN KEY (vendedor_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (categoria_id) REFERENCES categorias(id) ON DELETE CASCADE,
    FOREIGN KEY (adjudicado_a_id) REFERENCES usuarios(id) ON DELETE SET NULL
);

-- 4. Tabla de Ofertas (Pujas)
CREATE TABLE ofertas (
    id VARCHAR(36) PRIMARY KEY,
    articulo_id VARCHAR(36) NOT NULL,
    ofertante_id VARCHAR(36) NOT NULL,
    precio_propuesto DECIMAL(10,2) NOT NULL,
    momento TIMESTAMP NOT NULL,
    FOREIGN KEY (articulo_id) REFERENCES articulos(id) ON DELETE CASCADE,
    FOREIGN KEY (ofertante_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

-- ==========================================
-- INSERCIÓN DE DATOS DE PRUEBA (SEMILLA)
-- ==========================================

-- Usuarios de prueba
INSERT INTO usuarios (id, nombre, apellidos, direccion, email) VALUES
('u1111111-1111-1111-1111-111111111111', 'Andres', 'Cantillo', 'Calle 123 #45-67, Cartagena', 'andres@correo.com'),
('u2222222-2222-2222-2222-222222222222', 'Maria', 'Gomez', 'Carrera 15 #30-40, Bogota', 'maria.gomez@correo.com'),
('u3333333-3333-3333-3333-333333333333', 'Carlos', 'Perez', 'Avenida Principal #10-20, Medellin', 'carlos.perez@correo.com'),
('u4444444-4444-4444-4444-444444444444', 'Ana', 'Rodriguez', 'Calle Falsa 123, Cartagena', 'ana.rod@correo.com'),
('u5555555-5555-5555-5555-555555555555', 'Luis', 'Martinez', 'Manzana 4 Lote 5, Turbaco', 'luis.martinez@correo.com');

-- Categorías
-- Categoría principal: Electrónica
INSERT INTO categorias (id, nombre, padre_id) VALUES
('c1000000-0000-0000-0000-000000000000', 'Electrónica', NULL);

-- Subcategorías de Electrónica
INSERT INTO categorias (id, nombre, padre_id) VALUES
('c1100000-0000-0000-0000-000000000000', 'Celulares y Smartphones', 'c1000000-0000-0000-0000-000000000000'),
('c1200000-0000-0000-0000-000000000000', 'Laptops y Computadores', 'c1000000-0000-0000-0000-000000000000');

-- Otras categorías independientes
INSERT INTO categorias (id, nombre, padre_id) VALUES
('c2000000-0000-0000-0000-000000000000', 'Libros', NULL),
('c3000000-0000-0000-0000-000000000000', 'Hogar y Cocina', NULL);

-- Subcategorías de Libros
INSERT INTO categorias (id, nombre, padre_id) VALUES
('c2100000-0000-0000-0000-000000000000', 'Ficción', 'c2000000-0000-0000-0000-000000000000');

-- Artículos
-- Artículos en "Celulares y Smartphones" (pertenecientes indirectamente a Electrónica) e independientes
INSERT INTO articulos (id, vendedor_id, nombre, descripcion, estado, precio_inicial, fecha_limite, categoria_id, estado_subasta, adjudicado_a_id, precio_final) VALUES
-- 1. Celular en subasta activa
('a1111111-1111-1111-1111-111111111111', 'u1111111-1111-1111-1111-111111111111', 'iPhone 13 Pro', 'Usado en excelente estado, 128GB, batería al 85%', 'USADO', 500.00, '2026-06-15 18:00:00', 'c1100000-0000-0000-0000-000000000000', 'ACTIVA', NULL, NULL),

-- 2. Laptop en subasta adjudicada por encima del precio inicial (salida: 800, vendido: 950)
('a2222222-2222-2222-2222-222222222222', 'u2222222-2222-2222-2222-222222222222', 'MacBook Air M1', 'Nueva sellada de fabrica, 8GB RAM, 256GB SSD', 'NUEVO', 800.00, '2026-06-08 14:00:00', 'c1200000-0000-0000-0000-000000000000', 'ADJUDICADA', 'u3333333-3333-3333-3333-333333333333', 950.00),

-- 3. Celular adjudicado al mismo precio de salida (salida: 150, vendido: 150)
('a3333333-3333-3333-3333-333333333333', 'u3333333-3333-3333-3333-333333333333', 'Xiaomi Redmi Note 10', 'Usado con detalles de uso en pantalla, incluye cargador', 'USADO', 150.00, '2026-06-09 10:00:00', 'c1100000-0000-0000-0000-000000000000', 'ADJUDICADA', 'u4444444-4444-4444-4444-444444444444', 150.00),

-- 4. Libro en subasta activa
('a4444444-4444-4444-4444-444444444444', 'u4444444-4444-4444-4444-444444444444', 'Cien Años de Soledad - Firmado', 'Primera edicion firmada por Gabriel Garcia Marquez', 'USADO', 300.00, '2026-06-18 20:00:00', 'c2100000-0000-0000-0000-000000000000', 'ACTIVA', NULL, NULL),

-- 5. Licuadora adjudicada por encima del precio inicial (salida: 50, vendido: 75)
('a5555555-5555-5555-5555-555555555555', 'u1111111-1111-1111-1111-111111111111', 'Licuadora Osterizer', 'Licuadora clásica de 3 velocidades, seminueva', 'USADO', 50.00, '2026-06-07 16:00:00', 'c3000000-0000-0000-0000-000000000000', 'ADJUDICADA', 'u2222222-2222-2222-2222-222222222222', 75.00);

-- Ofertas realizadas
-- Ofertas para el iPhone 13 Pro (subasta activa)
INSERT INTO ofertas (id, articulo_id, ofertante_id, precio_propuesto, momento) VALUES
('o1111111-1111-1111-1111-111111111111', 'a1111111-1111-1111-1111-111111111111', 'u2222222-2222-2222-2222-222222222222', 510.00, '2026-06-09 15:30:00'),
('o2222222-2222-2222-2222-222222222222', 'a1111111-1111-1111-1111-111111111111', 'u3333333-3333-3333-3333-333333333333', 550.00, '2026-06-10 10:15:00');

-- Ofertas para el MacBook Air M1 (adjudicada)
INSERT INTO ofertas (id, articulo_id, ofertante_id, precio_propuesto, momento) VALUES
('o3333333-3333-3333-3333-333333333333', 'a2222222-2222-2222-2222-222222222222', 'u1111111-1111-1111-1111-111111111111', 850.00, '2026-06-07 10:00:00'),
('o4444444-4444-4444-4444-444444444444', 'a2222222-2222-2222-2222-222222222222', 'u3333333-3333-3333-3333-333333333333', 950.00, '2026-06-08 12:30:00');

-- Ofertas para el Xiaomi (adjudicada al mismo precio de salida)
INSERT INTO ofertas (id, articulo_id, ofertante_id, precio_propuesto, momento) VALUES
('o5555555-5555-5555-5555-555555555555', 'a3333333-3333-3333-3333-333333333333', 'u4444444-4444-4444-4444-444444444444', 150.00, '2026-06-08 09:00:00');

-- Ofertas para la licuadora (adjudicada)
INSERT INTO ofertas (id, articulo_id, ofertante_id, precio_propuesto, momento) VALUES
('o6666666-6666-6666-6666-666666666666', 'a5555555-5555-5555-5555-555555555555', 'u2222222-2222-2222-2222-222222222222', 75.00, '2026-06-06 14:15:00');
