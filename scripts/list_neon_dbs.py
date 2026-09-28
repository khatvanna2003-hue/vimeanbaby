"""List Neon databases and product counts on neondb + vimeanbaby_db if present."""
import os
from pathlib import Path
import psycopg

ROOT = Path(__file__).resolve().parents[1]
env = ROOT / "backend" / ".env.production"
for line in env.read_text(encoding="utf-8").splitlines():
    line = line.strip()
    if not line or line.startswith("#") or "=" not in line:
        continue
    k, _, v = line.partition("=")
    os.environ.setdefault(k.strip(), v.strip())

host = "ep-bold-heart-azq8jamc.c-3.ap-southeast-1.aws.neon.tech"
user = os.environ["DB_USERNAME"]
password = os.environ["DB_PASSWORD"]

# Connect to default neondb to list DBs
admin_dsn = f"postgresql://{user}:{password}@{host}/neondb?sslmode=require"
with psycopg.connect(admin_dsn) as conn:
    with conn.cursor() as cur:
        cur.execute("SELECT datname FROM pg_database WHERE datistemplate = false ORDER BY 1")
        dbs = [r[0] for r in cur.fetchall()]
        print("databases:", dbs)

for dbname in dbs:
    dsn = f"postgresql://{user}:{password}@{host}/{dbname}?sslmode=require"
    try:
        with psycopg.connect(dsn) as conn:
            with conn.cursor() as cur:
                cur.execute(
                    """
                    SELECT EXISTS (
                      SELECT 1 FROM information_schema.tables
                      WHERE table_schema='public' AND table_name='products'
                    )
                    """
                )
                if not cur.fetchone()[0]:
                    print(f"{dbname}: no products table")
                    continue
                cur.execute("SELECT COUNT(*) FROM products")
                print(f"{dbname}: products =", cur.fetchone()[0])
                cur.execute("SELECT COUNT(*) FROM categories")
                print(f"{dbname}: categories =", cur.fetchone()[0])
    except Exception as e:
        print(f"{dbname}: ERR {e}")
