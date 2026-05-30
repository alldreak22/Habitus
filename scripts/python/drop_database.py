import argparse
import sqlite3
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
DB_PATH = ROOT / "backend" / "habitus-api" / "data" / "habitus.db"
SCHEMA_PATH = ROOT / "scripts" / "habitus_schema_completo.sql"


def main():
    parser = argparse.ArgumentParser(description="Dropa e recria o banco SQLite local do Habitus.")
    parser.add_argument(
        "--db",
        default=str(DB_PATH),
        help="Caminho do arquivo SQLite. Padrao: backend/habitus-api/data/habitus.db",
    )
    parser.add_argument(
        "--schema",
        default=str(SCHEMA_PATH),
        help="Arquivo SQL usado para recriar o banco.",
    )
    args = parser.parse_args()

    db_path = Path(args.db)
    schema_path = Path(args.schema)

    if not schema_path.exists():
        raise SystemExit(f"Schema nao encontrado: {schema_path}")

    if db_path.exists():
        try:
            db_path.unlink()
        except PermissionError as exc:
            raise SystemExit(
                "Nao foi possivel remover o banco. Pare o backend Habitus e execute novamente."
            ) from exc

    db_path.parent.mkdir(parents=True, exist_ok=True)
    schema_sql = schema_path.read_text(encoding="utf-8")

    conn = sqlite3.connect(db_path)
    try:
        conn.executescript(schema_sql)
        conn.commit()
        users = conn.execute("select id, nick, email from users order by id").fetchall()
    finally:
        conn.close()

    print(f"Banco recriado: {db_path}")
    print(f"Usuarios: {users}")


if __name__ == "__main__":
    main()
