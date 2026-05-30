PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nick TEXT NOT NULL UNIQUE,
    email TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    password TEXT NOT NULL,
    picture TEXT,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT
);

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
    '$2a$10$YOrvmUShmv5NYvmkezxSnOMYNUrl0uJWja/qDz98DtKQbdFKk2qXy',
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);
