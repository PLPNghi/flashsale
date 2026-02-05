-- Insert dummy products
INSERT INTO products (name, description, regular_price, stock_quantity) VALUES
('iPhone 15 Pro Max', 'Latest Apple flagship phone with A17 Pro chip', 29990000, 100),
('Samsung Galaxy S24 Ultra', 'Premium Android phone with S Pen', 27990000, 80),
('MacBook Pro M3', '14-inch laptop with M3 chip, 16GB RAM', 45990000, 50),
('Sony WH-1000XM5', 'Premium noise-cancelling headphones', 8990000, 150),
('iPad Air M2', '10.9-inch tablet with M2 chip', 16990000, 75),
('Apple Watch Series 9', 'Smartwatch with health tracking', 10990000, 120),
('AirPods Pro 2', 'Wireless earbuds with ANC', 6490000, 200),
('PlayStation 5', 'Latest gaming console from Sony', 13990000, 60),
('Nintendo Switch OLED', 'Portable gaming console with OLED screen', 8990000, 90),
('Dyson V15 Detect', 'Cordless vacuum cleaner with laser detection', 17990000, 40);

-- Insert flash sale configs for today
-- Morning slot: 9:00 - 12:00
INSERT INTO flash_sale_configs (product_id, start_time, end_time, flash_price, flash_quantity, sold_quantity, sale_date, is_active) VALUES
(1, '09:00:00', '11:00:00', 24990000, 10, 0, CURRENT_DATE, TRUE),
(2, '09:00:00', '10:00:00', 22990000, 8, 0, CURRENT_DATE, TRUE),
(4, '09:00:00', '12:00:00', 6990000, 20, 0, CURRENT_DATE, TRUE);

-- Afternoon slot: 13:00 - 16:00
INSERT INTO flash_sale_configs (product_id, start_time, end_time, flash_price, flash_quantity, sold_quantity, sale_date, is_active) VALUES
(3, '13:00:00', '16:00:00', 39990000, 5, 0, CURRENT_DATE, TRUE),
(5, '14:00:00', '15:00:00', 14990000, 15, 0, CURRENT_DATE, TRUE),
(7, '14:00:00', '16:00:00', 4990000, 30, 0, CURRENT_DATE, TRUE);

-- Evening slot: 17:00 - 22:00
INSERT INTO flash_sale_configs (product_id, start_time, end_time, flash_price, flash_quantity, sold_quantity, sale_date, is_active) VALUES
(6, '17:00:00', '19:00:00', 8990000, 12, 0, CURRENT_DATE, TRUE),
(8, '18:00:00', '22:00:00', 11990000, 10, 0, CURRENT_DATE, TRUE),
(9, '20:00:00', '22:00:00', 6990000, 15, 0, CURRENT_DATE, TRUE),
(10, '20:00:00', '22:00:00', 14990000, 8, 0, CURRENT_DATE, TRUE);

-- Insert demo users with balance (password is "Password123!" for all)
-- Password hash generated using BCrypt with strength 10
INSERT INTO users (email, phone, password_hash, balance, email_verified, phone_verified) VALUES
('user1@example.com', '0901234567', '$2a$10$7eSIXzdNLUw8QHT3z4sEd.Cut.c8Os7aaNSvrzM6NRgkvR/oKdTL6', 50000000, TRUE, TRUE),
('user2@example.com', '0901234568', '$2a$10$7eSIXzdNLUw8QHT3z4sEd.Cut.c8Os7aaNSvrzM6NRgkvR/oKdTL6', 30000000, TRUE, FALSE),
('user3@example.com', '0901234569', '$2a$10$7eSIXzdNLUw8QHT3z4sEd.Cut.c8Os7aaNSvrzM6NRgkvR/oKdTL6', 100000000, TRUE, TRUE),
('user4@example.com', NULL, '$2a$10$7eSIXzdNLUw8QHT3z4sEd.Cut.c8Os7aaNSvrzM6NRgkvR/oKdTL6', 20000000, TRUE, FALSE),
(NULL, '0901234570', '$2a$10$7eSIXzdNLUw8QHT3z4sEd.Cut.c8Os7aaNSvrzM6NRgkvR/oKdTL6', 15000000, FALSE, TRUE);
