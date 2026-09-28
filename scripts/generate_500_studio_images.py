"""
Generate 500 studio white-background product images (e-commerce packshot style)
and attach them to Neon products.
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
import requests
from PIL import Image, ImageFilter, ImageEnhance

ROOT = Path(__file__).resolve().parents[1]
PROD = ROOT / "backend" / ".env.production"
TARGET = 500
CANVAS = 1000
FOLDER = "vimeanbaby/studio"
SEED = 42


def load_env(path: Path) -> dict[str, str]:
    data: dict[str, str] = {}
    for line in path.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        k, _, v = line.partition("=")
        data[k.strip()] = v.strip()
        os.environ[k.strip()] = v.strip()
    return data


env = load_env(PROD)
host = env["DB_URL"].removeprefix("jdbc:postgresql://")
DSN = f"postgresql://{env['DB_USERNAME']}:{env['DB_PASSWORD']}@{host}"

cloudinary.config(
    cloud_name=env["CLOUDINARY_CLOUD_NAME"],
    api_key=env["CLOUDINARY_API_KEY"],
    api_secret=env["CLOUDINARY_API_SECRET"],
    secure=True,
)

# Large pool of baby / mother / care product photos
PHOTO_IDS = [
    "photo-1515488042361-ee00e0ddd4f8",
    "photo-1604881988758-f76ad2f7aac1",
    "photo-1596461404969-9ae70f2830c1",
    "photo-1566004100631-35d2661f50bf",
    "photo-1556228578-0d85b1a4d571",
    "photo-1570172619604-71b782a1e7c0",
    "photo-1596755389378-c31d21fd1273",
    "photo-1556228720-195a672e8a03",
    "photo-1570194065650-d99fb4b38c15",
    "photo-1556228453-efd6c1ff04f6",
    "photo-1544367567-0f2fcb009e0b",
    "photo-1555252333-9f8e92e65df9",
    "photo-1519689373023-dd07c7988603",
    "photo-1522771930-78848d9293e8",
    "photo-1503454537195-1dcabb73ffb9",
    "photo-1519238263530-99bdd11df2ea",
    "photo-1493894473891-10fc1e36d37b",
    "photo-1476703993599-003599c0c1a3",
    "photo-1584305574647-0cc949a2bb9f",
    "photo-1566576912321-d58ddd7a6088",
    "photo-1516627145497-ae6968895b74",
    "photo-1596462502278-27bfdc403348",
    "photo-1522771739844-6a9f6d5f14af",
    "photo-1586105251261-72a756497a11",
    "photo-1616486338812-3dadae4b4ace",
    "photo-1584820927498-cfe5211fd8bf",
    "photo-1576091160399-112ba8d25d1d",
    "photo-1581595220892-b245a1e6b8b9",
    "photo-1519689373023-dd07c7988603",
    "photo-1600883572812-999bc6750400",
    "photo-1584473457409-ae85bfaeba08",
    "photo-1631730486572-226b1e5bd0f4",
    "photo-1608248543808-ba0a7e0c4f3c",
    "photo-1620916297397-a4a3372a4f9f",
    "photo-1515488042361-ee00e0ddd4f8",
    "photo-1522771739844-6a9f6d5f14af",
    "photo-1503454537195-1dcabb73ffb9",
    "photo-1556228720-195a672e8a03",
    "photo-1570194065650-d99fb4b38c15",
    "photo-1566576912321-d58ddd7a6088",
]


def src_url(photo_id: str) -> str:
    return f"https://images.unsplash.com/{photo_id}?auto=format&fit=crop&w=1200&q=85"


def download(url: str) -> Image.Image | None:
    try:
        r = requests.get(url, timeout=25)
        if r.status_code >= 400:
            return None
        return Image.open(io.BytesIO(r.content)).convert("RGBA")
    except Exception:
        return None


def studio_white(src: Image.Image, rng: random.Random) -> bytes:
    """
    Packshot style like the mockup:
    - pure white canvas
    - product centered with margin
    - subtle soft contact shadow under product
    """
    canvas = Image.new("RGBA", (CANVAS, CANVAS), (255, 255, 255, 255))

    # Random crop zoom for uniqueness
    w, h = src.size
    zoom = rng.uniform(0.82, 1.0)
    cw, ch = int(w * zoom), int(h * zoom)
    left = rng.randint(0, max(0, w - cw))
    top = rng.randint(0, max(0, h - ch))
    cropped = src.crop((left, top, left + cw, top + ch))

    # Fit product ~70-80% of canvas
    ratio = rng.uniform(0.68, 0.80)
    max_side = int(CANVAS * ratio)
    cropped.thumbnail((max_side, max_side), Image.Resampling.LANCZOS)

    # Slight brightness/contrast for clean studio look
    rgb = cropped.convert("RGB")
    rgb = ImageEnhance.Brightness(rgb).enhance(rng.uniform(1.02, 1.08))
    rgb = ImageEnhance.Contrast(rgb).enhance(rng.uniform(1.02, 1.12))
    product = rgb.convert("RGBA")

    pw, ph = product.size
    x = (CANVAS - pw) // 2
    y = (CANVAS - ph) // 2 - 10

    # Soft elliptical contact shadow
    shadow = Image.new("RGBA", (CANVAS, CANVAS), (0, 0, 0, 0))
    from PIL import ImageDraw

    draw = ImageDraw.Draw(shadow)
    sx1 = x + int(pw * 0.15)
    sx2 = x + int(pw * 0.85)
    sy1 = y + ph - 8
    sy2 = min(CANVAS - 40, sy1 + 36)
    draw.ellipse((sx1, sy1, sx2, sy2), fill=(0, 0, 0, 40))
    shadow = shadow.filter(ImageFilter.GaussianBlur(12))

    canvas = Image.alpha_composite(canvas, shadow)
    canvas.alpha_composite(product, (x, y))

    out = canvas.convert("RGB")
    buf = io.BytesIO()
    out.save(buf, format="JPEG", quality=92, optimize=True)
    buf.seek(0)
    buf.name = "studio.jpg"
    return buf.getvalue()


def upload_studio(index: int, jpeg: bytes) -> tuple[int, str, str]:
    buf = io.BytesIO(jpeg)
    buf.name = "studio.jpg"
    result = cloudinary.uploader.upload(
        buf,
        folder=FOLDER,
        public_id=f"p-{index:04d}",
        overwrite=True,
        resource_type="image",
        unique_filename=False,
    )
    url = result["secure_url"]
    # Keep delivery on white square
    if "/upload/" in url and "b_white" not in url:
        url = url.replace("/upload/", "/upload/b_white,c_pad,w_800,h_800,f_auto,q_auto/")
    return index, url, result["public_id"]


def load_source_pool() -> list[Image.Image]:
    print("Downloading source photos...")
    pool: list[Image.Image] = []
    seen = set()
    for pid in PHOTO_IDS:
        if pid in seen:
            continue
        seen.add(pid)
        img = download(src_url(pid))
        if img is not None:
            pool.append(img)
            print(f"  OK {pid} ({len(pool)})")
        else:
            print(f"  skip {pid}")
        time.sleep(0.15)
    if len(pool) < 5:
        raise SystemExit("Not enough source images downloaded")
    print(f"Source pool: {len(pool)}")
    return pool


def main() -> None:
    rng = random.Random(SEED)
    pool = load_source_pool()

    print(f"Generating + uploading {TARGET} studio white-BG images...")
    uploaded: dict[int, tuple[str, str]] = {}

    # Generate in memory then upload with limited workers
    def work(i: int) -> tuple[int, str, str]:
        src = pool[(i - 1) % len(pool)]
        local_rng = random.Random(SEED + i * 997)
        jpeg = studio_white(src, local_rng)
        return upload_studio(i, jpeg)

    batch = 20
    for start in range(1, TARGET + 1, batch):
        end = min(TARGET, start + batch - 1)
        indexes = list(range(start, end + 1))
        retries = 0
        while True:
            try:
                with ThreadPoolExecutor(max_workers=5) as ex:
                    futs = [ex.submit(work, i) for i in indexes]
                    for fut in as_completed(futs):
                        idx, url, pid = fut.result()
                        uploaded[idx] = (url, pid)
                break
            except Exception as exn:
                retries += 1
                print(f"  batch {start}-{end} retry {retries}: {exn}")
                if retries >= 4:
                    raise
                time.sleep(2 * retries)
        print(f"  uploaded {len(uploaded)}/{TARGET}")
        time.sleep(0.4)

    print("Connecting images to Neon (first 500 products by id)...")
    with psycopg.connect(DSN) as conn:
        with conn.cursor() as cur:
            cur.execute(
                """
                SELECT p.id, i.id
                FROM products p
                JOIN product_images i ON i.product_id = p.id AND i.is_primary
                WHERE p.deleted_at IS NULL
                ORDER BY p.id
                LIMIT %s
                """,
                (TARGET,),
            )
            rows = cur.fetchall()
            if len(rows) < TARGET:
                print(f"Warning: only {len(rows)} products available")

            for n, (product_id, image_id) in enumerate(rows, start=1):
                url, public_id = uploaded[n]
                cur.execute(
                    """
                    UPDATE product_images
                    SET url = %s, cloudinary_public_id = %s, updated_at = NOW()
                    WHERE id = %s
                    """,
                    (url, public_id, image_id),
                )
                if n % 100 == 0:
                    conn.commit()
                    print(f"  linked {n}/{len(rows)}")
            conn.commit()

            # Also refresh category images from first studio assets per category feel
            cur.execute("SELECT id, slug FROM categories ORDER BY id")
            cats = cur.fetchall()
            for i, (cat_id, _slug) in enumerate(cats, start=1):
                url, _pid = uploaded[((i - 1) % TARGET) + 1]
                cur.execute(
                    "UPDATE categories SET image_url = %s, updated_at = NOW() WHERE id = %s",
                    (url, cat_id),
                )
            conn.commit()

            cur.execute(
                """
                SELECT COUNT(*) FROM product_images
                WHERE cloudinary_public_id LIKE %s
                """,
                (f"{FOLDER}/%",),
            )
            print("neon_products_with_studio_images", cur.fetchone()[0])
            cur.execute("SELECT url FROM product_images WHERE cloudinary_public_id LIKE %s LIMIT 1", (f"{FOLDER}/%",))
            print("sample", cur.fetchone()[0])

    print(f"Done: {TARGET} studio white-BG images on Cloudinary + Neon.")


if __name__ == "__main__":
    main()
