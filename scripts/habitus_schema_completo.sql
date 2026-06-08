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

CREATE TABLE IF NOT EXISTS habits (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    active boolean NOT NULL,
    color varchar(255),
    created_at varchar(255) NOT NULL,
    description varchar(1000),
    frequency_type varchar(255) NOT NULL,
    icon varchar(255),
    name varchar(255) NOT NULL,
    reminder boolean NOT NULL,
    status varchar(255) NOT NULL,
    suggested_times varchar(255),
    target_frequency varchar(255) NOT NULL,
    times_per_day INTEGER NOT NULL,
    title varchar(255) NOT NULL,
    updated_at varchar(255),
    user_id bigint NOT NULL
);

CREATE TABLE IF NOT EXISTS habit_frequency_days (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    day_of_week INTEGER NOT NULL,
    habit_id bigint NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_habit_frequency_day
    ON habit_frequency_days (habit_id, day_of_week);

CREATE TABLE IF NOT EXISTS habit_reminder_times (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    created_at varchar(255) NOT NULL,
    reminder_time varchar(255) NOT NULL,
    habit_id bigint NOT NULL
);

CREATE TABLE IF NOT EXISTS day_entries (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    activity_description TEXT,
    entry_date date NOT NULL,
    created_at varchar(255) NOT NULL,
    updated_at varchar(255),
    user_id bigint NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_day_entries_user_date
    ON day_entries (user_id, entry_date);

CREATE TABLE IF NOT EXISTS day_entry_habits (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    completed boolean NOT NULL,
    completed_at varchar(255),
    created_at varchar(255) NOT NULL,
    override_action varchar(255),
    updated_at varchar(255),
    day_entry_id bigint NOT NULL,
    habit_id bigint NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_day_entry_habits_entry_habit
    ON day_entry_habits (day_entry_id, habit_id);

CREATE TABLE IF NOT EXISTS day_entry_habit_time_completions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    completed boolean NOT NULL,
    completed_at varchar(255),
    completion_time varchar(255) NOT NULL,
    created_at varchar(255) NOT NULL,
    updated_at varchar(255),
    day_entry_habit_id bigint NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_day_entry_habit_time
    ON day_entry_habit_time_completions (day_entry_habit_id, completion_time);
