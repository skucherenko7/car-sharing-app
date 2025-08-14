SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM users_roles;
DELETE FROM users;
DELETE FROM roles;

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO roles (id, name) VALUES
                                 (1, 'MANAGER'),
                                 (2, 'CUSTOMER');

INSERT INTO users (id, email, first_name, last_name, password, telegram_chat_id) VALUES
                                                                                     (1, 'manager@gmail.com', 'manager', 'manager', '$2y$10$copz6yHJeSQbQav.oBK84eLY8tdOKDO2c/rebsjOdJPx8cf18VBDG', '1234567890'),
                                                                                     (2, 'veronika333@gmail.com', 'Veronika', 'Verona', '$2a$12$lMMUW1ysSOJAXSsAjIgATOZgVeRuOQJUYaGcDJw99oh17iQ3aM0ju', '1234567893'),
                                                                                     (3, 'max222@gmail.com', 'Max', 'Maxi', '$2a$12$QFa70zsjdnCLK0.JEO2rv.Voci4e8aTBiIp1L5aDqT7g7AfcmGUM2', '1234567892'),
                                                                                     (4, 'john444@gmail.com', 'John', 'Jo', '$2a$12$DKTKHWN0hq/Bssg.gucQ6e0o4sDyxkNVfOAgQUqceaDzCPB.AT9yO', '1234567894');

INSERT INTO users_roles (user_id, role_id) VALUES
                                               (1, 1),
                                               (2, 2),
                                               (3, 2),
                                               (4, 2);
