BEGIN;

INSERT INTO roles (id, name)
VALUES (1, 'MANAGER')
    ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO users (id, email, first_name, last_name, password, telegram_chat_id)
VALUES (2, 'manager1@gmail.com', 'manager', 'manager', 'Password111', 1234567890)
    ON DUPLICATE KEY UPDATE email = VALUES(email);

INSERT INTO users_roles (user_id, role_id)
VALUES (2, 1)
    ON DUPLICATE KEY UPDATE user_id = VALUES(user_id);

INSERT INTO cars (id, brand, model, type, inventory, daily_fee, is_deleted)
VALUES (1, 'Audi', 'Q8', 'SUV', 2, 700.00, false)
    ON DUPLICATE KEY UPDATE model = VALUES(model);

INSERT INTO rentals (id, rental_date, return_date, actual_return_date, car_id, user_id, is_active)
VALUES
    (1, '2025-07-27', '2099-12-31', NULL, 1, 2, TRUE),
    (2, '2025-07-28', '2099-12-31', NULL, 1, 2, TRUE)
    ON DUPLICATE KEY UPDATE rental_date = VALUES(rental_date);

INSERT INTO payments (id, amount, status, type, rental_id, session_id, session_url)
VALUES (1, 700.00, 'PENDING', 'PAYMENT', 1, 'sess_123456789', 'https://stripe.com/session/sess_123456789')
    ON DUPLICATE KEY UPDATE session_id = VALUES(session_id);

COMMIT;
