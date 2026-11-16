-- ============================================================
-- Sistema de Biblioteca - Esquema MySQL 8
-- Script idempotente: NO borra ni sobrescribe datos existentes.
-- Uso:  mysql -uUSUARIO -p < biblioteca.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS biblioteca
    CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE biblioteca;

-- ------------------------------------------------------------
-- categorias(id, nombre)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS categorias (
    id     INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- libros: catálogo con control de ejemplares
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS libros (
    id                    INT AUTO_INCREMENT PRIMARY KEY,
    categoria_id          INT NOT NULL,
    titulo                VARCHAR(200) NOT NULL,
    autor                 VARCHAR(150) NOT NULL,
    isbn                  VARCHAR(20)  NULL UNIQUE,
    portada               VARCHAR(255) NULL,
    ejemplares_total      INT NOT NULL DEFAULT 1,
    ejemplares_disponibles INT NOT NULL DEFAULT 1,
    created_at            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_libros_categoria FOREIGN KEY (categoria_id)
        REFERENCES categorias (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_ejemplares CHECK (ejemplares_disponibles >= 0
                                  AND ejemplares_disponibles <= ejemplares_total)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- usuarios: clientes y administradores
-- rol en MAYÚSCULAS para coincidir con el enum Java Rol (EnumType.STRING)
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuarios (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    nombre        VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    rol           ENUM('CLIENTE','ADMIN') NOT NULL DEFAULT 'CLIENTE',
    activo        TINYINT(1) NOT NULL DEFAULT 1,
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- prestamos: estado en MAYÚSCULAS para el enum EstadoPrestamo
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS prestamos (
    id                      INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id              INT NOT NULL,
    libro_id                INT NOT NULL,
    fecha_prestamo          DATE NOT NULL,
    fecha_devolucion_prevista DATE NOT NULL,
    fecha_devolucion_real   DATE NULL,
    estado                  ENUM('ACTIVO','DEVUELTO','ATRASADO') NOT NULL DEFAULT 'ACTIVO',
    CONSTRAINT fk_prestamos_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_prestamos_libro FOREIGN KEY (libro_id)
        REFERENCES libros (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    KEY idx_prestamos_usuario (usuario_id),
    KEY idx_prestamos_libro (libro_id)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- multas: generadas al devolver con atraso
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS multas (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    prestamo_id  INT NOT NULL,
    dias_atraso  INT NOT NULL DEFAULT 0,
    monto        DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    pagada       TINYINT(1) NOT NULL DEFAULT 0,
    fecha_pago   DATE NULL,
    CONSTRAINT fk_multas_prestamo FOREIGN KEY (prestamo_id)
        REFERENCES prestamos (id) ON DELETE CASCADE ON UPDATE CASCADE,
    KEY idx_multas_prestamo (prestamo_id)
) ENGINE=InnoDB;

-- ============================================================
-- Migración para bases creadas por versiones anteriores del
-- proyecto (agrega columnas faltantes y pasa rol/estado a
-- MAYÚSCULAS). No elimina ninguna fila.
-- ============================================================

-- libros.created_at / usuarios.created_at
SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = 'biblioteca' AND TABLE_NAME = 'libros'
        AND COLUMN_NAME = 'created_at') = 0,
    'ALTER TABLE libros ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = 'biblioteca' AND TABLE_NAME = 'usuarios'
        AND COLUMN_NAME = 'created_at') = 0,
    'ALTER TABLE usuarios ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- usuarios.rol -> ENUM en mayúsculas (primero normaliza valores viejos)
UPDATE usuarios SET rol = CASE WHEN UPPER(rol) = 'ADMIN' THEN 'ADMIN' ELSE 'CLIENTE' END;
SET @sql := IF(
    (SELECT DATA_TYPE FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = 'biblioteca' AND TABLE_NAME = 'usuarios'
        AND COLUMN_NAME = 'rol') <> 'enum',
    "ALTER TABLE usuarios MODIFY COLUMN rol ENUM('CLIENTE','ADMIN') NOT NULL DEFAULT 'CLIENTE'",
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- prestamos.estado -> ENUM en mayúsculas (primero normaliza valores viejos)
UPDATE prestamos SET estado = CASE
    WHEN UPPER(estado) = 'DEVUELTO' THEN 'DEVUELTO'
    WHEN UPPER(estado) = 'ATRASADO' THEN 'ATRASADO'
    ELSE 'ACTIVO' END;
SET @sql := IF(
    (SELECT DATA_TYPE FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = 'biblioteca' AND TABLE_NAME = 'prestamos'
        AND COLUMN_NAME = 'estado') <> 'enum',
    "ALTER TABLE prestamos MODIFY COLUMN estado ENUM('ACTIVO','DEVUELTO','ATRASADO') NOT NULL DEFAULT 'ACTIVO'",
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ============================================================
-- Datos de ejemplo (INSERT IGNORE: no duplica ni pisa lo existente)
-- ============================================================

INSERT IGNORE INTO categorias (nombre) VALUES
    ('Novela'),
    ('Ciencia'),
    ('Historia'),
    ('Infantil');

INSERT IGNORE INTO libros
    (categoria_id, titulo, autor, isbn, portada, ejemplares_total, ejemplares_disponibles)
SELECT c.id, 'Cien años de soledad', 'Gabriel García Márquez', '9780307474728', '', 3, 3
    FROM categorias c WHERE c.nombre = 'Novela'
    AND NOT EXISTS (SELECT 1 FROM libros l WHERE l.isbn = '9780307474728');

INSERT IGNORE INTO libros
    (categoria_id, titulo, autor, isbn, portada, ejemplares_total, ejemplares_disponibles)
SELECT c.id, 'El principito', 'Antoine de Saint-Exupéry', '9788437604947', '', 4, 4
    FROM categorias c WHERE c.nombre = 'Infantil'
    AND NOT EXISTS (SELECT 1 FROM libros l WHERE l.isbn = '9788437604947');

INSERT IGNORE INTO libros
    (categoria_id, titulo, autor, isbn, portada, ejemplares_total, ejemplares_disponibles)
SELECT c.id, 'Breve historia del tiempo', 'Stephen Hawking', '9788489233690', '', 2, 2
    FROM categorias c WHERE c.nombre = 'Ciencia'
    AND NOT EXISTS (SELECT 1 FROM libros l WHERE l.isbn = '9788489233690');

INSERT IGNORE INTO libros
    (categoria_id, titulo, autor, isbn, portada, ejemplares_total, ejemplares_disponibles)
SELECT c.id, 'Sapiens', 'Yuval Noah Harari', '9788499926223', '', 2, 2
    FROM categorias c WHERE c.nombre = 'Historia'
    AND NOT EXISTS (SELECT 1 FROM libros l WHERE l.isbn = '9788499926223');

INSERT IGNORE INTO libros
    (categoria_id, titulo, autor, isbn, portada, ejemplares_total, ejemplares_disponibles)
SELECT c.id, 'El túnel', 'Ernesto Sabato', '9789871138753', '', 1, 1
    FROM categorias c WHERE c.nombre = 'Novela'
    AND NOT EXISTS (SELECT 1 FROM libros l WHERE l.isbn = '9789871138753');

-- Dos usuarios de prueba (contraseñas hasheadas con BCrypt):
--   admin@biblioteca.com   / admin123   (ADMIN)
--   cliente@biblioteca.com / cliente123 (CLIENTE)
INSERT IGNORE INTO usuarios (nombre, email, password_hash, rol, activo) VALUES
    ('Administrador', 'admin@biblioteca.com',
     '$2a$10$AhE90QXhBVeA0IluePupRerSV3GCpxA9I1ltwOtT2ukBf6oGew.qG', 'ADMIN', 1),
    ('Juan Pérez', 'cliente@biblioteca.com',
     '$2a$10$zWYb9k3c7FHvdONRZhAyR.S2Iyv3Ka0J3y1N.pWCUOoblhL26acE.', 'CLIENTE', 1);
