CREATE DATABASE IF NOT EXISTS office_equipment
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE office_equipment;

DROP TABLE IF EXISTS assignments;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS equipment;
DROP TABLE IF EXISTS equipment_types;
DROP TABLE IF EXISTS employees;
DROP TABLE IF EXISTS departments;

CREATE TABLE departments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    room VARCHAR(20)
);

CREATE TABLE employees (
    id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    position VARCHAR(100),
    phone VARCHAR(30),
    department_id INT NOT NULL,
    FOREIGN KEY (department_id) REFERENCES departments(id)
);

CREATE TABLE roles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(30) NOT NULL UNIQUE,
    description VARCHAR(200)
);

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role_id INT NOT NULL,
    employee_id INT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES roles(id),
    FOREIGN KEY (employee_id) REFERENCES employees(id)
);

CREATE TABLE equipment_types (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE equipment (
    id INT AUTO_INCREMENT PRIMARY KEY,
    inventory_number VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    type_id INT NOT NULL,
    serial_number VARCHAR(60),
    purchase_date DATE,
    status ENUM('IN_STOCK', 'IN_USE', 'IN_REPAIR', 'WRITTEN_OFF') NOT NULL DEFAULT 'IN_STOCK',
    ip_address VARCHAR(45),
    mac_address VARCHAR(17) UNIQUE,
    owner_id INT,
    location VARCHAR(150),
    specs TEXT,
    FOREIGN KEY (type_id) REFERENCES equipment_types(id),
    FOREIGN KEY (owner_id) REFERENCES employees(id)
);

INSERT INTO roles (name, description) VALUES
    ('ADMIN', 'Полный доступ, управление пользователями'),
    ('MANAGER', 'Добавление и редактирование техники и сотрудников'),
    ('VIEWER', 'Только просмотр');