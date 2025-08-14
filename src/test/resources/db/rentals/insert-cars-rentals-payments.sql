INSERT INTO rentals (id, rental_date, return_date, actual_return_date, car_id, user_id, is_active)
VALUES (1, '2025-07-20', '2025-07-25', NULL, 1, 2, TRUE);

INSERT INTO payments (id, amount, status, type, rental_id, session_id, session_url)
VALUES (1, 700.00, 'PENDING', 'PAYMENT', 1, 'sess_123456789', 'https://stripe.com/session/sess_123456789');
