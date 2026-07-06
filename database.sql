CREATE DATABASE IF NOT EXISTS fintrack;
USE fintrack;

-- Table users
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(120) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Table roles
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(20) NOT NULL UNIQUE
);

-- Table user_roles
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Table income
CREATE TABLE IF NOT EXISTS income (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    category VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    transaction_date DATE NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Table expense
CREATE TABLE IF NOT EXISTS expense (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    category VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    transaction_date DATE NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Table budget
CREATE TABLE IF NOT EXISTS budget (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category VARCHAR(50) NOT NULL,
    limit_amount DECIMAL(15, 2) NOT NULL,
    spent_amount DECIMAL(15, 2) DEFAULT 0.00 NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Table recurring_transactions
CREATE TABLE IF NOT EXISTS recurring_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(10) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    category VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    frequency VARCHAR(15) NOT NULL,
    last_execution_date DATE,
    next_execution_date DATE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Table notifications
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    message VARCHAR(500) NOT NULL,
    is_read BOOLEAN DEFAULT FALSE NOT NULL,
    type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Table audit_logs
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    action VARCHAR(100) NOT NULL,
    details VARCHAR(1000),
    ip_address VARCHAR(45),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Indexes for optimal lookup performance
CREATE INDEX idx_income_user_date ON income(user_id, transaction_date);
CREATE INDEX idx_expense_user_date ON expense(user_id, transaction_date);
CREATE INDEX idx_budget_user_active ON budget(user_id, is_deleted, start_date, end_date);
CREATE INDEX idx_notifications_user_unread ON notifications(user_id, is_read);

-- Seed Initial Roles & Test User
INSERT INTO roles (id, name) VALUES (1, 'ROLE_USER') ON DUPLICATE KEY UPDATE name=name;
INSERT INTO roles (id, name) VALUES (2, 'ROLE_ADMIN') ON DUPLICATE KEY UPDATE name=name;

-- Seed default user (password is 'password')
INSERT INTO users (id, username, email, password, full_name, is_active, created_at, updated_at) 
VALUES (1, 'user', 'user@fintrack.com', '$2a$10$8.g2tkN.A19fE963yK/64e/95d03W83sH45Q0/5mG1gD8sH45Q0/5', 'Test User', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE id=id;

INSERT INTO user_roles (user_id, role_id) VALUES (1, 1) ON DUPLICATE KEY UPDATE user_id=user_id;
