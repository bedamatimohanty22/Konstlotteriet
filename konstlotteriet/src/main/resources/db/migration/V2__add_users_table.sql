-- Users table for authentication
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    member_id BIGINT REFERENCES members(id) ON DELETE SET NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_member_id ON users(member_id);
CREATE INDEX idx_users_active ON users(active);

-- Default admin user (password: admin123)
-- Bcrypt hash for "admin123" generated using BcryptUtil.bcryptHash("admin123")
INSERT INTO users (email, password, role, active, created_at, updated_at)
VALUES ('admin@konstlotteriet.se', '$2a$10$N8D7qQjPhpLB7WAc2aGRJOzDbzG049qbKCDZOes5u.EEPLaJyLYYm', 'ADMIN', true, NOW(), NOW());
