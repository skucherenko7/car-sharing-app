SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM users_roles;
DELETE FROM users;
DELETE FROM roles;

SET FOREIGN_KEY_CHECKS = 1;

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
