USE office_equipment;

INSERT INTO departments (name, room) VALUES
    ('Бухгалтерия', '101'),
    ('Отдел кадров', '102'),
    ('IT-отдел', '205'),
    ('Отдел продаж', '210');

INSERT INTO employees (full_name, position, phone, department_id) VALUES
    ('Иванов Иван Иванович', 'Главный бухгалтер', '+7 900 111-11-11', 1),
    ('Петрова Анна Сергеевна', 'Бухгалтер', '+7 900 222-22-22', 1),
    ('Сидоров Пётр Алексеевич', 'Начальник отдела кадров', '+7 900 333-33-33', 2),
    ('Кузнецов Алексей Дмитриевич', 'Системный администратор', '+7 900 444-44-44', 3),
    ('Смирнова Мария Олеговна', 'Менеджер по продажам', '+7 900 555-55-55', 4);

INSERT INTO equipment_types (name) VALUES
    ('Персональный компьютер'),
    ('Ноутбук'),
    ('Монитор'),
    ('Принтер'),
    ('МФУ'),
    ('Сетевое оборудование');

INSERT INTO equipment
    (inventory_number, name, type_id, serial_number, purchase_date, status, ip_address, mac_address, owner_id, location, specs)
VALUES
    ('INV-0001', 'Dell OptiPlex 7090', 1, 'SN-DL-70901', '2023-03-15', 'IN_USE', '192.168.1.11', 'AA:BB:CC:00:00:01', 1, 'Каб. 101, стол у окна', 'Core i5-11500, 16 ГБ, SSD 512 ГБ'),
    ('INV-0002', 'HP ProDesk 400 G7', 1, 'SN-HP-40072', '2022-09-01', 'IN_USE', '192.168.1.12', 'AA:BB:CC:00:00:02', 2, 'Каб. 101, стол у двери', 'Core i3-10100, 8 ГБ, SSD 256 ГБ'),
    ('INV-0003', 'Lenovo ThinkPad E15', 2, 'SN-LN-E1503', '2024-01-20', 'IN_USE', '192.168.1.21', 'AA:BB:CC:00:00:03', 5, 'Каб. 210', 'Ryzen 5 5500U, 16 ГБ, SSD 512 ГБ'),
    ('INV-0004', 'Samsung S24R350', 3, 'SN-SM-35004', '2022-09-01', 'IN_USE', NULL, NULL, 2, 'Каб. 101, стол у двери', '24", Full HD, IPS'),
    ('INV-0005', 'HP LaserJet Pro M404', 4, 'SN-HP-LJ005', '2021-06-10', 'IN_USE', '192.168.1.50', 'AA:BB:CC:00:00:05', 1, 'Каб. 101, тумба', 'Лазерный, ч/б, A4'),
    ('INV-0006', 'Canon i-SENSYS MF445dw', 5, 'SN-CN-MF006', '2023-11-05', 'IN_REPAIR', '192.168.1.51', 'AA:BB:CC:00:00:06', 3, 'Каб. 102', 'МФУ, лазерный, Wi-Fi'),
    ('INV-0007', 'MikroTik hAP ac2', 6, 'SN-MT-AC207', '2023-02-14', 'IN_USE', '192.168.1.1', 'AA:BB:CC:00:00:07', 4, 'Каб. 205, серверная стойка', 'Маршрутизатор, Wi-Fi'),
    ('INV-0008', 'Dell OptiPlex 3080', 1, 'SN-DL-30808', '2020-05-12', 'IN_STOCK', NULL, NULL, NULL, 'Склад', 'Core i3-10100, 8 ГБ, HDD 1 ТБ');