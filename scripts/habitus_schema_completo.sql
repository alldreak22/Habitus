PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    email TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    nick TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    picture TEXT,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT
);

CREATE TABLE IF NOT EXISTS habits (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,

    title TEXT NOT NULL,
    icon TEXT,
    color TEXT,
    description TEXT,

    reminder INTEGER NOT NULL DEFAULT 0
        CHECK (reminder IN (0, 1)),

    frequency_type TEXT NOT NULL DEFAULT 'EVERY_DAY'
        CHECK (frequency_type IN ('EVERY_DAY', 'WEEKDAYS', 'WEEKENDS', 'CUSTOM')),

    status TEXT NOT NULL DEFAULT 'ACTIVE'
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ARCHIVED')),

    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT,

    FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS habit_reminder_times (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    habit_id INTEGER NOT NULL,

    reminder_time TEXT NOT NULL
        CHECK (
            reminder_time GLOB '[0-2][0-9]:[0-5][0-9]'
            AND CAST(substr(reminder_time, 1, 2) AS INTEGER) BETWEEN 0 AND 23
        ),

    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (habit_id)
        REFERENCES habits(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS habit_frequency_days (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    habit_id INTEGER NOT NULL,

    -- 1 = Segunda, 2 = Terca, 3 = Quarta, 4 = Quinta,
    -- 5 = Sexta, 6 = Sabado, 7 = Domingo
    day_of_week INTEGER NOT NULL
        CHECK (day_of_week BETWEEN 1 AND 7),

    FOREIGN KEY (habit_id)
        REFERENCES habits(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    UNIQUE (habit_id, day_of_week)
);

CREATE TABLE IF NOT EXISTS day_entries (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,

    -- Formato esperado: YYYY-MM-DD
    entry_date TEXT NOT NULL
        CHECK (
            entry_date GLOB '[0-9][0-9][0-9][0-9]-[0-1][0-9]-[0-3][0-9]'
        ),

    -- Texto livre com suporte a Markdown
    activity_description TEXT,

    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT,

    FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    UNIQUE (user_id, entry_date)
);

CREATE TABLE IF NOT EXISTS day_entry_habits (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    day_entry_id INTEGER NOT NULL,
    habit_id INTEGER NOT NULL,

    -- NULL: habito veio pela frequencia normal
    -- SELECTED: habito foi adicionado manualmente nesse dia
    -- DESELECTED: habito foi removido manualmente nesse dia
    override_action TEXT
        CHECK (override_action IN ('SELECTED', 'DESELECTED')),

    completed INTEGER NOT NULL DEFAULT 0
        CHECK (completed IN (0, 1)),

    completed_at TEXT,

    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT,

    FOREIGN KEY (day_entry_id)
        REFERENCES day_entries(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    FOREIGN KEY (habit_id)
        REFERENCES habits(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    UNIQUE (day_entry_id, habit_id)
);

CREATE INDEX IF NOT EXISTS idx_habits_user_id
    ON habits(user_id);

CREATE INDEX IF NOT EXISTS idx_habit_reminder_times_habit_id
    ON habit_reminder_times(habit_id);

CREATE INDEX IF NOT EXISTS idx_habit_frequency_days_habit_id
    ON habit_frequency_days(habit_id);

CREATE INDEX IF NOT EXISTS idx_day_entries_user_date
    ON day_entries(user_id, entry_date);

CREATE INDEX IF NOT EXISTS idx_day_entry_habits_day_entry_id
    ON day_entry_habits(day_entry_id);

CREATE INDEX IF NOT EXISTS idx_day_entry_habits_habit_id
    ON day_entry_habits(habit_id);
