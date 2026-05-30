import sqlite3
import sys


DB_PATH = "backend/habitus-api/data/habitus.db"
ALLOWED_PREFIXES = ("select ", "pragma ")


def main():
    if len(sys.argv) != 2:
        raise SystemExit("Uso: python scripts/python/sqlite_query.py \"select ...\"")

    sql = sys.argv[1].strip()
    if not sql.lower().startswith(ALLOWED_PREFIXES):
        raise SystemExit("Somente consultas SELECT ou PRAGMA sao permitidas por este utilitario.")

    conn = sqlite3.connect(DB_PATH)
    try:
        cur = conn.execute(sql)
        print(cur.fetchall())
    finally:
        conn.close()


if __name__ == "__main__":
    main()
