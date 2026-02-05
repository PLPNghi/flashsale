-- Drop tables if exist (for clean restart)
DROP TABLE IF EXISTS inventory_sync_logs;
DROP TABLE IF EXISTS flash_sale_orders;
DROP TABLE IF EXISTS flash_sale_configs;
DROP TABLE IF EXISTS otp_verifications;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS users;

-- Create USERS table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) UNIQUE,
    phone VARCHAR(20) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    balance DECIMAL(15, 2) DEFAULT 0.00,
    email_verified BOOLEAN DEFAULT FALSE,
    phone_verified BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create OTP_VERIFICATIONS table
CREATE TABLE otp_verifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    otp_code VARCHAR(10) NOT NULL,
    verification_type VARCHAR(20) NOT NULL COMMENT 'EMAIL or PHONE',
    contact_info VARCHAR(255) NOT NULL COMMENT 'Email or phone number',
    expires_at DATETIME NOT NULL,
    is_used BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_contact_expires (contact_info, expires_at, is_used)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create PRODUCTS table
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    regular_price DECIMAL(15, 2) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create FLASH_SALE_CONFIGS table
CREATE TABLE flash_sale_configs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    flash_price DECIMAL(15, 2) NOT NULL,
    flash_quantity INT NOT NULL,
    sold_quantity INT DEFAULT 0,
    sale_date DATE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0 COMMENT 'For optimistic locking',
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    INDEX idx_product_date (product_id, sale_date),
    INDEX idx_sale_date_time (sale_date, start_time, end_time, is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create FLASH_SALE_ORDERS table
CREATE TABLE flash_sale_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    flash_sale_config_id BIGINT NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    status VARCHAR(20) DEFAULT 'COMPLETED' COMMENT 'COMPLETED, CANCELLED',
    ordered_at DATETIME NOT NULL,
    order_date DATE AS (DATE(ordered_at)) STORED,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (flash_sale_config_id) REFERENCES flash_sale_configs(id) ON DELETE CASCADE,
    INDEX idx_user_date (user_id, ordered_at),
    INDEX idx_flash_sale_config (flash_sale_config_id),
    UNIQUE KEY uk_user_order_date (user_id, order_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create INVENTORY_SYNC_LOGS table
CREATE TABLE inventory_sync_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    quantity_change INT NOT NULL COMMENT 'Negative for decrease, positive for increase',
    stock_before INT NOT NULL,
    stock_after INT NOT NULL,
    sync_type VARCHAR(50) NOT NULL COMMENT 'FLASH_SALE_ORDER, MANUAL_ADJUSTMENT, etc.',
    reference_id VARCHAR(100) COMMENT 'Order ID or other reference',
    synced_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    INDEX idx_product_id (product_id),
    INDEX idx_reference (reference_id),
    UNIQUE KEY uk_sync_reference (sync_type, reference_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;