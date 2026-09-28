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
    os.environ[k.strip()] = v.strip()

db_url = os.environ["DB_URL"].removeprefix("jdbc:postgresql://")
dsn = f"postgresql://{os.environ['DB_USERNAME']}:{os.environ['DB_PASSWORD']}@{db_url}"

with psycopg.connect(dsn) as conn:
    with conn.cursor() as cur:
        cur.execute("SELECT tablename FROM pg_tables WHERE schemaname='public' ORDER BY 1")
        print("tables:", [r[0] for r in cur.fetchall()])
        for t in ("categories", "brands", "products", "product_variants", "product_images", "flyway_schema_history"):
            try:
                cur.execute(f"SELECT COUNT(*) FROM {t}")
                print(t, cur.fetchone()[0])
            except Exception as e:
                print(t, "ERR", e)
                conn.rollback()
