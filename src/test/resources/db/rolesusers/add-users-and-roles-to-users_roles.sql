DELETE FROM users_roles;

INSERT INTO users_roles (user_id, role_id)
VALUES
    ((SELECT id FROM users WHERE email = 'manager@gmail.com'), (SELECT id FROM roles WHERE name = 'MANAGER')),
    ((SELECT id FROM users WHERE email = 'veronika333@gmail.com'), (SELECT id FROM roles WHERE name = 'CUSTOMER'));


