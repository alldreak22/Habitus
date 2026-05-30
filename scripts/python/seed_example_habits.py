import argparse
import sqlite3
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
DB_PATH = ROOT / "backend" / "habitus-api" / "data" / "habitus.db"
SCHEMA_PATH = ROOT / "scripts" / "habitus_schema_completo.sql"

EXAMPLE_HABITS = [
    {
        "title": "Beber agua",
        "icon": "water_drop",
        "color": "#2F80ED",
        "description": "Beber agua ao longo do dia para manter energia e foco.",
        "frequency_type": "EVERY_DAY",
        "target_frequency": "DAILY",
        "reminder_times": ["08:00", "14:00", "20:00"],
        "frequency_days": [],
    },
    {
        "title": "Caminhada leve",
        "icon": "directions_walk",
        "color": "#2F8F6B",
        "description": "Fazer uma caminhada curta para movimentar o corpo.",
        "frequency_type": "CUSTOM",
        "target_frequency": "CUSTOM",
        "reminder_times": ["18:00"],
        "frequency_days": [1, 3, 5],
    },
    {
        "title": "Leitura diaria",
        "icon": "menu_book",
        "color": "#8B5CF6",
        "description": "Ler algumas paginas sem pressa e sem notificacoes.",
        "frequency_type": "EVERY_DAY",
        "target_frequency": "DAILY",
        "reminder_times": ["21:00"],
        "frequency_days": [],
    },
    {
        "title": "Planejar o dia",
        "icon": "event_note",
        "color": "#F59E0B",
        "description": "Definir prioridades antes de comecar as tarefas.",
        "frequency_type": "EVERY_DAY",
        "target_frequency": "DAILY",
        "reminder_times": ["07:30"],
        "frequency_days": [],
    },
    {
        "title": "Estudar ingles",
        "icon": "translate",
        "color": "#14B8A6",
        "description": "Praticar vocabulario, escuta ou conversacao.",
        "frequency_type": "CUSTOM",
        "target_frequency": "CUSTOM",
        "reminder_times": ["19:30"],
        "frequency_days": [2, 4, 6],
    },
    {
        "title": "Organizar ambiente",
        "icon": "cleaning_services",
        "color": "#64748B",
        "description": "Arrumar mesa, arquivos e pendencias pequenas.",
        "frequency_type": "CUSTOM",
        "target_frequency": "CUSTOM",
        "reminder_times": ["17:45"],
        "frequency_days": [1, 5],
    },
    {
        "title": "Meditar",
        "icon": "spa",
        "color": "#944343",
        "description": "Fazer uma pausa curta para respirar e reduzir ruido mental.",
        "frequency_type": "EVERY_DAY",
        "target_frequency": "DAILY",
        "reminder_times": ["06:45"],
        "frequency_days": [],
    },
    {
        "title": "Treino de forca",
        "icon": "fitness_center",
        "color": "#D64545",
        "description": "Sessao objetiva de exercicios de forca.",
        "frequency_type": "CUSTOM",
        "target_frequency": "CUSTOM",
        "reminder_times": ["18:30"],
        "frequency_days": [2, 4, 6],
    },
    {
        "title": "Revisar financas",
        "icon": "account_balance_wallet",
        "color": "#0EA5E9",
        "description": "Conferir gastos e manter o planejamento financeiro em dia.",
        "frequency_type": "CUSTOM",
        "target_frequency": "CUSTOM",
        "reminder_times": ["10:00"],
        "frequency_days": [7],
    },
    {
        "title": "Sono sem telas",
        "icon": "bedtime",
        "color": "#6366F1",
        "description": "Desligar telas antes de dormir para melhorar o descanso.",
        "frequency_type": "EVERY_DAY",
        "target_frequency": "DAILY",
        "reminder_times": ["22:30"],
        "frequency_days": [],
    },
]


def main():
    parser = argparse.ArgumentParser(description="Cria 10 habitos exemplares no SQLite local.")
    parser.add_argument("--db", default=str(DB_PATH), help="Caminho do arquivo SQLite.")
    parser.add_argument("--user-id", type=int, default=1, help="Usuario dono dos habitos.")
    args = parser.parse_args()

    db_path = Path(args.db)
    db_path.parent.mkdir(parents=True, exist_ok=True)

    conn = sqlite3.connect(db_path)
    try:
        conn.execute("PRAGMA foreign_keys = ON")
        ensure_users(conn)
        ensure_habit_tables(conn)
        ensure_user_exists(conn, args.user_id)
        remove_existing_examples(conn, args.user_id)
        insert_examples(conn, args.user_id)
        conn.commit()
        total = conn.execute("select count(*) from habits where user_id = ?", (args.user_id,)).fetchone()[0]
    finally:
        conn.close()

    print(f"Habitos exemplares criados para user_id={args.user_id}: {len(EXAMPLE_HABITS)}")
    print(f"Total de habitos do usuario: {total}")


def ensure_users(conn):
    user_table_exists = conn.execute(
        "select 1 from sqlite_master where type = 'table' and name = 'users'"
    ).fetchone()
    if user_table_exists:
        return
    schema_sql = SCHEMA_PATH.read_text(encoding="utf-8")
    conn.executescript(schema_sql)


def ensure_user_exists(conn, user_id):
    user = conn.execute("select id from users where id = ?", (user_id,)).fetchone()
    if user is None:
        raise SystemExit(f"Usuario nao encontrado: {user_id}")


def ensure_habit_tables(conn):
    conn.executescript(
        """
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
        """
    )


def remove_existing_examples(conn, user_id):
    titles = [habit["title"] for habit in EXAMPLE_HABITS]
    placeholders = ",".join("?" for _ in titles)
    params = [user_id, *titles]
    habit_ids = [
        row[0]
        for row in conn.execute(
            f"select id from habits where user_id = ? and title in ({placeholders})",
            params,
        ).fetchall()
    ]
    if not habit_ids:
        return

    habit_placeholders = ",".join("?" for _ in habit_ids)
    conn.execute(f"delete from habit_reminder_times where habit_id in ({habit_placeholders})", habit_ids)
    conn.execute(f"delete from habit_frequency_days where habit_id in ({habit_placeholders})", habit_ids)
    conn.execute(f"delete from habits where id in ({habit_placeholders})", habit_ids)


def insert_examples(conn, user_id):
    now = "2026-05-30 09:00:00"
    for habit in EXAMPLE_HABITS:
        reminder_times = habit["reminder_times"]
        suggested_times = ",".join(reminder_times)
        cursor = conn.execute(
            """
            INSERT INTO habits (
                active, color, created_at, description, frequency_type, icon, name,
                reminder, status, suggested_times, target_frequency, times_per_day,
                title, updated_at, user_id
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
            (
                1,
                habit["color"],
                now,
                habit["description"],
                habit["frequency_type"],
                habit["icon"],
                habit["title"],
                1 if reminder_times else 0,
                "ACTIVE",
                suggested_times,
                habit["target_frequency"],
                max(1, len(reminder_times)),
                habit["title"],
                now,
                user_id,
            ),
        )
        habit_id = cursor.lastrowid
        for reminder_time in reminder_times:
            conn.execute(
                """
                INSERT INTO habit_reminder_times (created_at, reminder_time, habit_id)
                VALUES (?, ?, ?)
                """,
                (now, reminder_time, habit_id),
            )
        for day in habit["frequency_days"]:
            conn.execute(
                """
                INSERT INTO habit_frequency_days (day_of_week, habit_id)
                VALUES (?, ?)
                """,
                (day, habit_id),
            )


if __name__ == "__main__":
    main()
