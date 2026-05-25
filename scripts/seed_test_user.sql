-- Usuario de teste temporario para desenvolvimento local.
-- Token mock atual do frontend resolve para user_id = 1.

INSERT OR IGNORE INTO users (
    id,
    nick,
    email,
    name,
    password,
    picture,
    created_at,
    updated_at
) VALUES (
    1,
    'teste',
    'teste@habitus.local',
    'Usuario Teste',
    '$2a$10$7EqJtq98hPqEX7fNZaFWoOa5l9wY9qf2Qf4l7f4bL8nL0q8V5y8yW',
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);
