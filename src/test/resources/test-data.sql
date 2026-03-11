-- Clean and insert test users
DELETE FROM user WHERE username IN ('customer', 'admin');
INSERT INTO user (username, password, email, name, role) VALUES
('customer', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'customer@test.com', 'Test Customer', 'CUSTOMER'),
('admin', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'admin@test.com', 'Administrator', 'ADMIN');