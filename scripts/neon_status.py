"""Force-load .env.production and report Neon product counts. Re-seed if < 1000."""
from __future__ import annotations

import os
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PROD = ROOT / "backend" / ".env.production"


def load_env_override(path: Path) -> dict[str, str]:
    data: dict[str, str] = {}
    for line in path.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        k, _, v = line.partition("=")
        data[k.strip()] = v.strip()
        os.environ[k.strip()] = v.strip()  # override
    return data


env = load_env_override(PROD)
print("DB_USERNAME", env.get("DB_USERNAME"))
print("DB_URL", env.get("DB_URL"))

import psycopg

host_part = env["DB_URL"].removeprefix("jdbc:postgresql://")
dsn = f"postgresql://{env['DB_USERNAME']}:{env['DB_PASSWORD']}@{host_part}"
print("Connecting...")

with psycopg.connect(dsn) as conn:
    with conn.cursor() as cur:
        cur.execute("SELECT current_database(), current_user")
        print("connected as", cur.fetchone())
        cur.execute("SELECT COUNT(*) FROM products")
        products = cur.fetchone()[0]
        cur.execute("SELECT COUNT(*) FROM categories")
        cats = cur.fetchone()[0]
        cur.execute("SELECT COUNT(*) FROM brands")
        brands = cur.fetchone()[0]
        cur.execute("SELECT COUNT(*) FROM product_images WHERE url LIKE '%cloudinary%'")
        imgs = cur.fetchone()[0]
        print(f"products={products} categories={cats} brands={brands} cloudinary_images={imgs}")
        cur.execute("SELECT id, name_en FROM products ORDER BY id DESC LIMIT 3")
        print("latest:", cur.fetchall())

# Also list databases
with psycopg.connect(dsn) as conn:
    with conn.cursor() as cur:
        cur.execute("SELECT datname FROM pg_database WHERE datistemplate = false ORDER BY 1")
        print("databases:", [r[0] for r in cur.fetchall()])
