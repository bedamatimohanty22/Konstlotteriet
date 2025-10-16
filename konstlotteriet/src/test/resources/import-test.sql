-- Test Data for Unit and Integration Tests

-- Insert test admin user
-- Password: admin123 (BCrypt hashed)
INSERT INTO users (id, email, password, role, member_id, active, created_at, updated_at)
VALUES (1, 'admin@konstlotteriet.se', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', NULL, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert test members
INSERT INTO members (id, first_name, last_name, email, phone_number, membership_start_year, last_win_year, active, created_at, updated_at)
VALUES (1, 'Anna', 'Andersson', 'anna@test.se', '070-1111111', 2020, NULL, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO members (id, first_name, last_name, email, phone_number, membership_start_year, last_win_year, active, created_at, updated_at)
VALUES (2, 'Björn', 'Bergström', 'bjorn@test.se', '070-2222222', 2021, 2024, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO members (id, first_name, last_name, email, phone_number, membership_start_year, last_win_year, active, created_at, updated_at)
VALUES (3, 'Cecilia', 'Carlsson', 'cecilia@test.se', '070-3333333', 2022, NULL, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert test member user
-- Password: member123 (BCrypt hashed)
INSERT INTO users (id, email, password, role, member_id, active, created_at, updated_at)
VALUES (2, 'anna@test.se', '$2a$10$rZ8.VfZ8JZxKd4vxqW0hZ.HV8KfN5F0z3vWxY9Kp5R0wZ8K8K8K8K', 'MEMBER', 1, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert test lottery
INSERT INTO lotteries (id, year, draw_date, status, created_at, updated_at)
VALUES (1, 2024, '2024-12-15', 'COMPLETED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO lotteries (id, year, draw_date, status, created_at, updated_at)
VALUES (2, 2025, '2025-12-15', 'PLANNED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert test artworks
INSERT INTO artworks (id, title, artist, year, year_id, lottery_id, estimated_value, description, created_at, updated_at)
VALUES (1, 'Sommarlandskap', 'Anders Artist', 2024, 1, 1, 15000.00, 'Vacker sommartavla', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO artworks (id, title, artist, year, year_id, lottery_id, estimated_value, description, created_at, updated_at)
VALUES (2, 'Vintervy', 'Berta Målare', 2024, 2, 1, 12000.00, 'Vinterlandskap i akvarell', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO artworks (id, title, artist, year, year_id, lottery_id, estimated_value, description, created_at, updated_at)
VALUES (3, 'Abstrakt komposition', 'Carin Konstnär', 2025, 1, 2, 18000.00, 'Modern abstrakt konst', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Set sequences to correct values
ALTER SEQUENCE users_seq RESTART WITH 10;
ALTER SEQUENCE members_seq RESTART WITH 10;
ALTER SEQUENCE lotteries_seq RESTART WITH 10;
ALTER SEQUENCE artworks_seq RESTART WITH 10;
