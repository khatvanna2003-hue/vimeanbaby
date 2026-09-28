"""Cycle the 500 studio Cloudinary images across ALL Neon products."""
from __future__ import annotations

import os
from pathlib import Path

import cloudinary
import cloudinary.api
import psycopg

ROOT = Path(__file__).resolve().parents[1]
PROD = ROOT / "backend" / ".env.production"


def load_env(path: Path) -> None:
    for line in path.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        k, _, v = line.partition("=")
        os.environ[k.strip()] = v.strip()


load_env(PROD)
host = os.environ["DB_URL"].removeprefix("jdbc:postgresql://")
DSN = f"postgresql://{os.environ['DB_USERNAME']}:{os.environ['DB_PASSWORD']}@{host}"

cloudinary.config(
    cloud_name=os.environ["CLOUDINARY_CLOUD_NAME"],
    api_key=os.environ["CLOUDINARY_API_KEY"],
    api_secret=os.environ["CLOUDINARY_API_SECRET"],
    secure=True,
)

# Collect studio assets p-0001 ... p-0500
assets: list[tuple[str, str]] = []
next_cursor = None
while True:
    kwargs = {
        "type": "upload",
        "resource_type": "image",
        "prefix": "vimeanbaby/studio/",
        "max_results": 500,
    }
    if next_cursor:
        kwargs["next_cursor"] = next_cursor
    res = cloudinary.api.resources(**kwargs)
    for r in res.get("resources", []):
        url = r["secure_url"].replace(
            "/upload/",
            "/upload/b_white,c_pad,w_800,h_800,f_auto,q_auto/",
        )
        assets.append((url, r["public_id"]))
    next_cursor = res.get("next_cursor")
    if not next_cursor:
        break

assets.sort(key=lambda x: x[1])
print(f"studio assets found: {len(assets)}")
if not assets:
    raise SystemExit("No studio assets on Cloudinary")

with psycopg.connect(DSN) as conn:
    with conn.cursor() as cur:
        cur.execute(
            """
            SELECT p.id, i.id
            FROM products p
            JOIN product_images i ON i.product_id = p.id AND i.is_primary
            WHERE p.deleted_at IS NULL
            ORDER BY p.id
            """
        )
        rows = cur.fetchall()
        for n, (product_id, image_id) in enumerate(rows):
            url, pid = assets[n % len(assets)]
            cur.execute(
                """
                UPDATE product_images
                SET url = %s, cloudinary_public_id = %s, updated_at = NOW()
                WHERE id = %s
                """,
                (url, pid, image_id),
            )
        conn.commit()
        print(f"Updated {len(rows)} products with studio white-BG images.")
