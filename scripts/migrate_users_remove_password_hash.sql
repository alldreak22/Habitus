PRAGMA foreign_keys = OFF;

BEGIN TRANSACTION;

ALTER TABLE users RENAME TO users_old;

CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nick TEXT NOT NULL UNIQUE,
    email TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    password TEXT NOT NULL,
    picture TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT
);

INSERT INTO users (id, nick, email, name, password, picture, created_at, updated_at)
SELECT
    id,
    COALESCE(NULLIF(TRIM(nick), ''), LOWER(SUBSTR(email, 1, INSTR(email, '@') - 1))),
    email,
    name,
    COALESCE(NULLIF(password, ''), password_hash),
    picture,
    created_at,
    updated_at
FROM users_old;

DROP TABLE users_old;

COMMIT;

PRAGMA foreign_keys = ON;
