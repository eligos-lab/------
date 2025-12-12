-- Таблица категорий
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL, -- 'INCOME' или 'EXPENSE'
    color VARCHAR(20)
);

-- Таблица транзакций
CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(20) NOT NULL, -- 'INCOME' или 'EXPENSE'
    amount DECIMAL(15, 2) NOT NULL,
    category_id BIGINT,
    transaction_date TIMESTAMP NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
);

-- Вставка начальных категорий, если их нет
MERGE INTO categories (name, type, color) KEY(name, type) VALUES
('Зарплата', 'INCOME', '#4CAF50'),
('Фриланс', 'INCOME', '#8BC34A'),
('Инвестиции', 'INCOME', '#CDDC39'),
('Продукты', 'EXPENSE', '#FF9800'),
('Транспорт', 'EXPENSE', '#2196F3'),
('Развлечения', 'EXPENSE', '#E91E63'),
('Жилье', 'EXPENSE', '#9C27B0'),
('Здоровье', 'EXPENSE', '#00BCD4'),
('Образование', 'EXPENSE', '#795548'),
('Прочее', 'EXPENSE', '#9E9E9E');