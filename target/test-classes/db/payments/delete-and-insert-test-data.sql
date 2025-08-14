SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE payments;
TRUNCATE TABLE rentals;
TRUNCATE TABLE users_roles;
TRUNCATE TABLE users;
TRUNCATE TABLE roles;
TRUNCATE TABLE cars;

INSERT INTO roles (id, name)
VALUES
    (1, 'MANAGER'),
    (2, 'CUSTOMER');

INSERT INTO users (id, email, first_name, last_name, password, telegram_chat_id)
VALUES
    (1, 'manager@gmail.com', 'manager', 'manager', '$2y$10$copz6yHJeSQbQav.oBK84eLY8tdOKDO2c/rebsjOdJPx8cf18VBDG', '1234567890'),
    (2, 'veronika333@gmail.com', 'Veronika', 'Verona', '$2a$12$lMMUW1ysSOJAXSsAjIgATOZgVeRuOQJUYaGcDJw99oh17iQ3aM0ju', '1234567893');

INSERT INTO users_roles (user_id, role_id)
VALUES
    (1, 1),
    (2, 2);

INSERT INTO cars (id, brand, model, type, inventory, daily_fee, is_deleted)
VALUES (1, 'Audi', 'Q8', 'SUV', 2, 700.00, false);

INSERT INTO rentals (id, rental_date, return_date, actual_return_date,
                     car_id, user_id, is_active)
VALUES (1, '2025-07-20', '2025-07-25', NULL,
        1, 2, true);

INSERT INTO payments (id, amount, status, type, rental_id, session_id, session_url)
VALUES (1, 700.00, 'PENDING', 'PAYMENT', 1,
        'sess_123456789', 'https://stripe.com/session/sess_123456789');

SET FOREIGN_KEY_CHECKS = 1;
