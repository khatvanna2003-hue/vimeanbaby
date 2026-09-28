"""Seed Neon catalog: categories, brands, 1000 products, white-BG Cloudinary images.

Reads credentials from backend/.env.production (or environment variables).
Usage: python scripts/seed_neon_catalog.py
"""
from __future__ import annotations

import io
import os
import random
import time
from concurrent.futures import ThreadPoolExecutor, as_completed
from pathlib import Path

import cloudinary
import cloudinary.uploader
import psycopg
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
ENV_FILE = ROOT / "backend" / ".env.production"

TARGET_PRODUCTS = 1000
IMAGE_SIZE = 800
SEED = 20260928


def load_env(path: Path) -> None:
    if not path.exists():
        return
    for line in path.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, _, val = line.partition("=")
        os.environ[key.strip()] = val.strip()  # override local shell env


load_env(ENV_FILE)

DB_URL = os.environ.get("DB_URL", "")
DB_USER = os.environ.get("DB_USERNAME", "")
DB_PASS = os.environ.get("DB_PASSWORD", "")

# Prefer JDBC-style DB_URL → libpq
if DB_URL.startswith("jdbc:postgresql://"):
    DATABASE_URL = (
        "postgresql://"
        + f"{DB_USER}:{DB_PASS}@"
        + DB_URL.removeprefix("jdbc:postgresql://")
    )
else:
    DATABASE_URL = os.environ.get(
        "DATABASE_URL",
        f"postgresql://{DB_USER}:{DB_PASS}@localhost:5432/neondb?sslmode=require",
    )

cloudinary.config(
    cloud_name=os.environ.get("CLOUDINARY_CLOUD_NAME", "vimeanbaby"),
    api_key=os.environ.get("CLOUDINARY_API_KEY", ""),
    api_secret=os.environ.get("CLOUDINARY_API_SECRET", ""),
    secure=True,
)

CATEGORIES = [
    ("diapers", "កន្ទប", "Diapers", (47, 93, 138), "pack"),
    ("wipes", "ក្រដាសជូត", "Wipes", (244, 166, 184), "pack"),
    ("bath-care", "ងូតទឹក និង ថែរក្សាស្បែក", "Bath & Care", (91, 155, 213), "bottle"),
    ("feeding", "ចំណីទារក", "Feeding", (143, 211, 182), "bottle"),
    ("baby-clothes", "សម្លៀកបំពាក់", "Baby Clothes", (232, 160, 120), "clothes"),
    ("maternity", "សម្រាប់ម៉ាក់", "Maternity", (180, 140, 200), "pack"),
    ("toys", "ប្រដាប់ក្មេងលេង", "Toys", (255, 180, 80), "toy"),
    ("strollers", "រទេះរុញ", "Strollers & Gear", (90, 110, 140), "gear"),
    ("nursery", "បន្ទប់ទារក", "Nursery", (120, 170, 160), "pack"),
    ("health", "សុខភាពទារក", "Health & Safety", (70, 150, 130), "bottle"),
    ("formula", "ទឹកដោះគោទារក", "Formula & Food", (210, 170, 100), "can"),
    ("accessories", "គ្រឿងបន្ថែម", "Accessories", (160, 120, 100), "pack"),
]

BRANDS = [
    "Pampers", "Huggies", "Merries", "Johnson Baby", "Aveeno Baby",
    "Pigeon", "Chicco", "Molfix", "MamyPoko", "Sebamed",
    "Mustela", "Philips Avent", "Medela", "Dr Browns", "Tommee Tippee",
    "Arau Baby", "Cussons", "Enfa", "Similac", "Aptamil",
]

PRODUCT_LINES = {
    "diapers": ["Premium Care", "Baby Dry", "Overnight", "Pants", "Newborn Tape", "Ultra Soft"],
    "wipes": ["Sensitive Wipes", "Natural Care Wipes", "Fresh Wipes", "Thick Soft Wipes"],
    "bath-care": ["Gentle Shampoo", "Body Wash", "Daily Lotion", "Massage Oil", "Cream"],
    "feeding": ["Feeding Bottle", "Nipple Slow Flow", "Sippy Cup", "Breast Pump", "Sterilizer"],
    "baby-clothes": ["Cotton Bodysuit", "Sleepsuit", "Romper", "Socks Pack", "Hat Set"],
    "maternity": ["Nursing Bra", "Maternity Pillow", "Belly Support", "Nursing Pad Pack"],
    "toys": ["Soft Rattle", "Teether Ring", "Play Gym Mat", "Stacking Cups"],
    "strollers": ["Travel Stroller", "Car Seat Base", "Baby Carrier", "Diaper Bag"],
    "nursery": ["Crib Sheet", "Muslin Blanket", "Night Light", "Changing Pad"],
    "health": ["Nail Care Kit", "Digital Thermometer", "Nasal Aspirator", "First Aid Kit"],
    "formula": ["Stage 1 Formula", "Stage 2 Formula", "Rice Cereal", "Fruit Puree"],
    "accessories": ["Pacifier Clip", "Bib Pack", "Burp Cloth", "Wet Bag"],
}

AGES = ["0-3 months", "0-6 months", "3-6 months", "6-12 months", "12+ months", "0+ months"]
ORIGINS = ["Japan", "Thailand", "Indonesia", "USA", "Korea", "Germany", "Vietnam", "Malaysia"]


def slugify(text: str) -> str:
    out = []
    for ch in text.lower():
        if ch.isalnum():
            out.append(ch)
        elif ch in " -_/":
            out.append("-")
    s = "".join(out)
    while "--" in s:
        s = s.replace("--", "-")
    return s.strip("-")


def draw_product_shape(draw: ImageDraw.ImageDraw, kind: str, color: tuple[int, int, int], size: int) -> None:
    cx, cy = size // 2, size // 2 + 20
    if kind == "bottle":
        draw.rounded_rectangle((cx - 70, cy - 180, cx + 70, cy + 160), radius=30, fill=color)
        draw.rectangle((cx - 35, cy - 220, cx + 35, cy - 170), fill=(220, 220, 220))
        draw.ellipse((cx - 40, cy - 240, cx + 40, cy - 210), fill=(200, 200, 200))
    elif kind == "can":
        draw.rounded_rectangle((cx - 110, cy - 140, cx + 110, cy + 150), radius=18, fill=color)
        draw.ellipse((cx - 110, cy - 165, cx + 110, cy - 115), fill=tuple(min(255, c + 30) for c in color))
    elif kind == "clothes":
        draw.polygon(
            [
                (cx - 120, cy - 40), (cx - 40, cy - 100), (cx - 20, cy - 60),
                (cx + 20, cy - 60), (cx + 40, cy - 100), (cx + 120, cy - 40),
                (cx + 90, cy + 140), (cx - 90, cy + 140),
            ],
            fill=color,
        )
    elif kind == "toy":
        draw.ellipse((cx - 120, cy - 80, cx + 120, cy + 160), fill=color)
        draw.ellipse((cx - 50, cy - 40, cx - 10, cy), fill=(255, 255, 255))
        draw.ellipse((cx + 10, cy - 40, cx + 50, cy), fill=(255, 255, 255))
    elif kind == "gear":
        draw.rounded_rectangle((cx - 140, cy - 40, cx + 140, cy + 80), radius=40, fill=color)
        draw.ellipse((cx - 130, cy + 60, cx - 50, cy + 140), outline=(80, 80, 80), width=14)
        draw.ellipse((cx + 50, cy + 60, cx + 130, cy + 140), outline=(80, 80, 80), width=14)
    else:
        draw.rounded_rectangle((cx - 150, cy - 160, cx + 150, cy + 160), radius=28, fill=color)
        draw.rounded_rectangle((cx - 120, cy - 90, cx + 120, cy + 90), radius=16, fill=(255, 255, 255))


def make_white_bg_image(title: str, accent: tuple[int, int, int], kind: str) -> bytes:
    img = Image.new("RGB", (IMAGE_SIZE, IMAGE_SIZE), (255, 255, 255))
    draw = ImageDraw.Draw(img)
    shadow = Image.new("RGBA", (IMAGE_SIZE, IMAGE_SIZE), (0, 0, 0, 0))
    sdraw = ImageDraw.Draw(shadow)
    sdraw.ellipse((220, 620, 580, 720), fill=(0, 0, 0, 28))
    img = Image.alpha_composite(img.convert("RGBA"), shadow).convert("RGB")
    draw = ImageDraw.Draw(img)
    draw_product_shape(draw, kind, accent, IMAGE_SIZE)

    try:
        font = ImageFont.truetype("arial.ttf", 28)
        small = ImageFont.truetype("arial.ttf", 18)
    except OSError:
        font = ImageFont.load_default()
        small = font

    label = title[:36]
    bbox = draw.textbbox((0, 0), label, font=font)
    tw = bbox[2] - bbox[0]
    draw.text(((IMAGE_SIZE - tw) / 2, 40), label, fill=(43, 58, 74), font=font)
    badge = "Vimean Baby"
    bbox2 = draw.textbbox((0, 0), badge, font=small)
    tw2 = bbox2[2] - bbox2[0]
    draw.text(((IMAGE_SIZE - tw2) / 2, IMAGE_SIZE - 50), badge, fill=(107, 119, 133), font=small)

    buf = io.BytesIO()
    img.save(buf, format="PNG", optimize=True)
    return buf.getvalue()


def upload_png(png_bytes: bytes, public_id: str, folder: str) -> tuple[str, str]:
    buf = io.BytesIO(png_bytes)
    buf.name = "product.png"
    result = cloudinary.uploader.upload(
        buf,
        folder=folder,
        public_id=public_id,
        overwrite=True,
        resource_type="image",
        unique_filename=False,
    )
    return result["secure_url"], result["public_id"]


def ensure_taxonomy(conn: psycopg.Connection) -> tuple[list[dict], list[dict]]:
    cats: list[dict] = []
    brands: list[dict] = []
    with conn.cursor() as cur:
        for i, (slug, name_km, name_en, color, kind) in enumerate(CATEGORIES, start=1):
            png = make_white_bg_image(name_en, color, kind)
            url, _pid = upload_png(png, f"cat-{slug}", "vimeanbaby/categories")
            cur.execute(
                """
                INSERT INTO categories (name_km, name_en, slug, image_url, sort_order, is_active)
                VALUES (%s, %s, %s, %s, %s, TRUE)
                ON CONFLICT (slug) DO UPDATE SET
                  name_km = EXCLUDED.name_km,
                  name_en = EXCLUDED.name_en,
                  image_url = EXCLUDED.image_url,
                  sort_order = EXCLUDED.sort_order,
                  is_active = TRUE,
                  updated_at = NOW()
                RETURNING id, slug, name_en
                """,
                (name_km, name_en, slug, url, i),
            )
            row = cur.fetchone()
            cats.append({"id": row[0], "slug": row[1], "name_en": row[2], "color": color, "kind": kind})
            print(f"  category {slug} ok")

        for name in BRANDS:
            slug = slugify(name)
            cur.execute(
                """
                INSERT INTO brands (name, slug, is_active)
                VALUES (%s, %s, TRUE)
                ON CONFLICT (slug) DO UPDATE SET name = EXCLUDED.name, is_active = TRUE, updated_at = NOW()
                RETURNING id, name, slug
                """,
                (name, slug),
            )
            row = cur.fetchone()
            brands.append({"id": row[0], "name": row[1], "slug": row[2]})
        conn.commit()
    return cats, brands


def count_products(conn: psycopg.Connection) -> int:
    with conn.cursor() as cur:
        cur.execute("SELECT COUNT(*) FROM products WHERE deleted_at IS NULL")
        return int(cur.fetchone()[0])


def build_product_specs(cats: list[dict], brands: list[dict], need: int, start_n: int) -> list[dict]:
    rng = random.Random(SEED)
    specs = []
    for i in range(1, need + 1):
        n = start_n + i
        cat = cats[(n - 1) % len(cats)]
        brand = brands[(n - 1) % len(brands)]
        lines = PRODUCT_LINES[cat["slug"]]
        line = lines[(n - 1) % len(lines)]
        size_tag = ["NB", "S", "M", "L", "XL", "XXL", "Pack"][(n - 1) % 7]
        name_en = f"{brand['name']} {line} {size_tag} #{n:04d}"
        name_km = f"{brand['name']} {line} {size_tag}"
        slug = slugify(f"{brand['slug']}-{line}-{size_tag}-{n}")
        price = round(rng.uniform(2.5, 49.9), 2)
        compare = round(price * rng.uniform(1.05, 1.35), 2) if rng.random() < 0.45 else None
        stock = rng.randint(5, 120)
        specs.append(
            {
                "n": n,
                "category_id": cat["id"],
                "brand_id": brand["id"],
                "brand_name": brand["name"],
                "color": cat["color"],
                "kind": cat["kind"],
                "name_en": name_en,
                "name_km": name_km,
                "slug": slug,
                "description_en": f"Authentic {brand['name']} {line} for Cambodia families.",
                "description_km": f"ផលិតផល {brand['name']} {line} ពិតប្រាកដសម្រាប់គ្រួសារកម្ពុជា។",
                "age_range": AGES[n % len(AGES)],
                "origin_country": ORIGINS[n % len(ORIGINS)],
                "is_featured": n % 17 == 0,
                "price": price,
                "compare_at_price": compare,
                "stock_qty": stock,
                "sku": f"VB-{n:05d}",
                "option_name": size_tag if cat["slug"] in ("diapers", "baby-clothes") else ("Pack" if n % 2 == 0 else "Standard"),
            }
        )
    return specs


def upload_one(spec: dict) -> dict:
    short = f"{spec['brand_name']} {spec['n']:04d}"
    png = make_white_bg_image(short, spec["color"], spec["kind"])
    url, pid = upload_png(png, f"p-{spec['n']:05d}", "vimeanbaby/products")
    return {**spec, "image_url": url, "public_id": pid}


def insert_batch(conn: psycopg.Connection, batch: list[dict]) -> None:
    with conn.cursor() as cur:
        for spec in batch:
            cur.execute(
                """
                INSERT INTO products (
                  category_id, brand_id, name_km, name_en, slug,
                  description_km, description_en, age_range, origin_country,
                  is_featured, is_active
                ) VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,TRUE)
                ON CONFLICT (slug) DO UPDATE SET
                  name_en = EXCLUDED.name_en,
                  name_km = EXCLUDED.name_km,
                  description_en = EXCLUDED.description_en,
                  description_km = EXCLUDED.description_km,
                  is_featured = EXCLUDED.is_featured,
                  is_active = TRUE,
                  updated_at = NOW()
                RETURNING id
                """,
                (
                    spec["category_id"],
                    spec["brand_id"],
                    spec["name_km"],
                    spec["name_en"],
                    spec["slug"],
                    spec["description_km"],
                    spec["description_en"],
                    spec["age_range"],
                    spec["origin_country"],
                    spec["is_featured"],
                ),
            )
            product_id = cur.fetchone()[0]

            cur.execute(
                """
                INSERT INTO product_variants (product_id, sku, option_name, price, compare_at_price, stock_qty, is_active)
                VALUES (%s, %s, %s, %s, %s, %s, TRUE)
                ON CONFLICT (sku) DO UPDATE SET
                  price = EXCLUDED.price,
                  compare_at_price = EXCLUDED.compare_at_price,
                  stock_qty = EXCLUDED.stock_qty,
                  is_active = TRUE,
                  updated_at = NOW()
                """,
                (
                    product_id,
                    spec["sku"],
                    spec["option_name"],
                    spec["price"],
                    spec["compare_at_price"],
                    spec["stock_qty"],
                ),
            )

            cur.execute("DELETE FROM product_images WHERE product_id = %s", (product_id,))
            cur.execute(
                """
                INSERT INTO product_images (product_id, url, cloudinary_public_id, sort_order, is_primary)
                VALUES (%s, %s, %s, 0, TRUE)
                """,
                (product_id, spec["image_url"], spec["public_id"]),
            )
        conn.commit()


def main() -> None:
    if not os.environ.get("CLOUDINARY_API_KEY") or not DB_USER:
        raise SystemExit("Missing credentials. Fill backend/.env.production first.")

    print("Connecting to Neon...")
    with psycopg.connect(DATABASE_URL) as conn:
        existing = count_products(conn)
        print(f"Existing products: {existing}")
        print("Ensuring categories + brands + category images (white BG)...")
        cats, brands = ensure_taxonomy(conn)
        print(f"Categories: {len(cats)}, Brands: {len(brands)}")

    need = max(0, TARGET_PRODUCTS - existing)
    if need == 0:
        print(f"Already have {existing} products. Done.")
        return

    print(f"Creating {need} products with white-BG Cloudinary images...")
    specs = build_product_specs(cats, brands, need, existing)

    batch_size = 15
    workers = 4
    done = 0
    for offset in range(0, len(specs), batch_size):
        chunk = specs[offset : offset + batch_size]
        uploaded: list[dict] = []
        retries = 0
        while True:
            try:
                with ThreadPoolExecutor(max_workers=workers) as pool:
                    futs = [pool.submit(upload_one, spec) for spec in chunk]
                    for fut in as_completed(futs):
                        uploaded.append(fut.result())
                break
            except Exception as ex:
                retries += 1
                print(f"  upload batch retry {retries}: {ex}")
                if retries >= 5:
                    raise
                time.sleep(3 * retries)
                uploaded = []

        insert_retries = 0
        while True:
            try:
                with psycopg.connect(DATABASE_URL) as conn:
                    insert_batch(conn, uploaded)
                break
            except Exception as ex:
                insert_retries += 1
                print(f"  insert retry {insert_retries}: {ex}")
                if insert_retries >= 5:
                    raise
                time.sleep(2 * insert_retries)

        done += len(uploaded)
        print(f"  inserted {done}/{need}")
        time.sleep(0.5)

    with psycopg.connect(DATABASE_URL) as conn:
        with conn.cursor() as cur:
            cur.execute("SELECT COUNT(*) FROM categories")
            print("FINAL categories", cur.fetchone()[0])
            cur.execute("SELECT COUNT(*) FROM brands")
            print("FINAL brands", cur.fetchone()[0])
            cur.execute("SELECT COUNT(*) FROM products WHERE deleted_at IS NULL")
            print("FINAL products", cur.fetchone()[0])
            cur.execute("SELECT COUNT(*) FROM product_variants")
            print("FINAL variants", cur.fetchone()[0])
            cur.execute("SELECT COUNT(*) FROM product_images")
            print("FINAL images", cur.fetchone()[0])


if __name__ == "__main__":
    main()
