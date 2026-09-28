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

db_url = os.environ["DB_URL"].removeprefix("jdbc:postgresql://")
dsn = f"postgresql://{os.environ['DB_USERNAME']}:{os.environ['DB_PASSWORD']}@{db_url}"

with psycopg.connect(dsn) as conn:
    with conn.cursor() as cur:
        cur.execute(
            """
            SELECT COUNT(*) FROM products p
            LEFT JOIN product_images i ON i.product_id = p.id AND i.is_primary
            WHERE i.id IS NULL AND p.deleted_at IS NULL
            """
        )
        print("missing_primary_image", cur.fetchone()[0])
        cur.execute(
            """
            SELECT COUNT(*) FROM product_images
            WHERE url LIKE '%res.cloudinary.com%'
            """
        )
        print("cloudinary_images", cur.fetchone()[0])
        cur.execute(
            """
            SELECT COUNT(*) FROM product_images
            WHERE url NOT LIKE '%res.cloudinary.com%'
            """
        )
        print("non_cloudinary_images", cur.fetchone()[0])
        cur.execute("SELECT url, cloudinary_public_id FROM product_images ORDER BY id DESC LIMIT 2")
        for r in cur.fetchall():
            print("sample", r[0])
            print("  pid", r[1])
