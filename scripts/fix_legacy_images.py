"""Replace remaining non-Cloudinary product images with white-BG Cloudinary packshots."""
from __future__ import annotations

import io
import os
from pathlib import Path

import cloudinary
import cloudinary.uploader
import psycopg
from PIL import Image, ImageDraw, ImageFont

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

cloudinary.config(
    cloud_name=os.environ["CLOUDINARY_CLOUD_NAME"],
    api_key=os.environ["CLOUDINARY_API_KEY"],
    api_secret=os.environ["CLOUDINARY_API_SECRET"],
    secure=True,
)

SIZE = 800
COLORS = [
    (47, 93, 138), (244, 166, 184), (91, 155, 213), (143, 211, 182),
    (232, 160, 120), (180, 140, 200),
]


def make_image(title: str, color: tuple[int, int, int]) -> bytes:
    img = Image.new("RGB", (SIZE, SIZE), (255, 255, 255))
    draw = ImageDraw.Draw(img)
    cx, cy = SIZE // 2, SIZE // 2 + 20
    draw.rounded_rectangle((cx - 150, cy - 160, cx + 150, cy + 160), radius=28, fill=color)
    draw.rounded_rectangle((cx - 120, cy - 90, cx + 120, cy + 90), radius=16, fill=(255, 255, 255))
    try:
        font = ImageFont.truetype("arial.ttf", 26)
    except OSError:
        font = ImageFont.load_default()
    label = title[:34]
    bbox = draw.textbbox((0, 0), label, font=font)
    tw = bbox[2] - bbox[0]
    draw.text(((SIZE - tw) / 2, 48), label, fill=(43, 58, 74), font=font)
    buf = io.BytesIO()
    img.save(buf, format="PNG", optimize=True)
    buf.seek(0)
    buf.name = "product.png"
    return buf.getvalue()


with psycopg.connect(dsn) as conn:
    with conn.cursor() as cur:
        cur.execute(
            """
            SELECT p.id, p.name_en, i.id
            FROM products p
            JOIN product_images i ON i.product_id = p.id AND i.is_primary
            WHERE i.url NOT LIKE '%res.cloudinary.com%'
            ORDER BY p.id
            """
        )
        rows = cur.fetchall()
        print(f"Updating {len(rows)} legacy images...")
        for idx, (pid, name, img_id) in enumerate(rows):
            color = COLORS[idx % len(COLORS)]
            png = make_image(name, color)
            buf = io.BytesIO(png)
            buf.name = "product.png"
            result = cloudinary.uploader.upload(
                buf,
                folder="vimeanbaby/products",
                public_id=f"legacy-{pid}",
                overwrite=True,
                resource_type="image",
            )
            cur.execute(
                """
                UPDATE product_images
                SET url = %s, cloudinary_public_id = %s, updated_at = NOW()
                WHERE id = %s
                """,
                (result["secure_url"], result["public_id"], img_id),
            )
            print(f"  product {pid} ok")
        conn.commit()
        cur.execute("SELECT COUNT(*) FROM product_images WHERE url LIKE '%res.cloudinary.com%'")
        print("cloudinary_images", cur.fetchone()[0])
