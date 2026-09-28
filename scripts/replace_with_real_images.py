"""Replace geometric packshots with real product photos on a white canvas.

Uploads unique real photos once, then assigns them across all Neon products by category.
"""
from __future__ import annotations

import os
import time
from pathlib import Path
from urllib.parse import urlparse

import cloudinary
import cloudinary.uploader
import psycopg
import requests

ROOT = Path(__file__).resolve().parents[1]
env = ROOT / "backend" / ".env.production"
for line in env.read_text(encoding="utf-8").splitlines():
    line = line.strip()
    if not line or line.startswith("#") or "=" not in line:
        continue
    k, _, v = line.partition("=")
    os.environ[k.strip()] = v.strip()  # override local shell env

db_url = os.environ["DB_URL"].removeprefix("jdbc:postgresql://")
dsn = f"postgresql://{os.environ['DB_USERNAME']}:{os.environ['DB_PASSWORD']}@{db_url}"

cloudinary.config(
    cloud_name=os.environ["CLOUDINARY_CLOUD_NAME"],
    api_key=os.environ["CLOUDINARY_API_KEY"],
    api_secret=os.environ["CLOUDINARY_API_SECRET"],
    secure=True,
)

# Verified Unsplash product / baby-care photography
U = "https://images.unsplash.com/{id}?auto=format&fit=crop&w=1200&q=85"


def u(photo_id: str) -> str:
    return U.format(id=photo_id)


REAL_BY_CATEGORY: dict[str, list[str]] = {
    "diapers": [
        u("photo-1515488042361-ee00e0ddd4f8"),
        u("photo-1604881988758-f76ad2f7aac1"),
        u("photo-1596461404969-9ae70f2830c1"),
        u("photo-1584839401442-83d3a91e5f2b"),
        u("photo-1566004100631-35d2661f50bf"),
    ],
    "wipes": [
        u("photo-1556228578-0d85b1a4d571"),
        u("photo-1570172619604-71b782a1e7c0"),
        u("photo-1608248543808-ba0a7e0c4f3c"),
        u("photo-1631730486572-226b1e5bd0f4"),
    ],
    "bath-care": [
        u("photo-1556228720-195a672e8a03"),
        u("photo-1570194065650-d99fb4b38c15"),
        u("photo-1556228453-efd6c1ff04f6"),
        u("photo-1620916297397-a4a3372a4f9f"),
        u("photo-1596755389378-c31d21fd1273"),
    ],
    "feeding": [
        u("photo-1584473457409-ae85bfaeba08"),
        u("photo-1600883572812-999bc6750400"),
        u("photo-1555252333-9f8e92e65df9"),
        u("photo-1519689373023-dd07c7988603"),
        u("photo-1544367567-0f2fcb009e0b"),
    ],
    "baby-clothes": [
        u("photo-1522771930-78848d9293e8"),
        u("photo-1519689373023-dd07c7988603"),
        u("photo-1503454537195-1dcabb73ffb9"),
        u("photo-1519238263530-99bdd11df2ea"),
        u("photo-1515488042361-ee00e0ddd4f8"),
    ],
    "maternity": [
        u("photo-1493894473891-10fc1e36d37b"),
        u("photo-1555252333-9f8e92e65df9"),
        u("photo-1584305574647-0cc949a2bb9f"),
        u("photo-1476703993599-003599c0c1a3"),
    ],
    "toys": [
        u("photo-1566576912321-d58ddd7a6088"),
        u("photo-1515488042361-ee00e0ddd4f8"),
        u("photo-1596461404969-9ae70f2830c1"),
        u("photo-1516627145497-ae6968895b74"),
    ],
    "strollers": [
        u("photo-1596462502278-27bfdc403348"),
        u("photo-1544367567-0f2fcb009e0b"),
        u("photo-1515488042361-ee00e0ddd4f8"),
    ],
    "nursery": [
        u("photo-1522771739844-6a9f6d5f14af"),
        u("photo-1586105251261-72a756497a11"),
        u("photo-1616486338812-3dadae4b4ace"),
        u("photo-1503454537195-1dcabb73ffb9"),
    ],
    "health": [
        u("photo-1584820927498-cfe5211fd8bf"),
        u("photo-1584305574647-0cc949a2bb9f"),
        u("photo-1576091160399-112ba8d25d1d"),
        u("photo-1581595220892-b245a1e6b8b9"),
    ],
    "formula": [
        u("photo-1584473457409-ae85bfaeba08"),
        u("photo-1600883572812-999bc6750400"),
        u("photo-1555252333-9f8e92e65df9"),
        u("photo-1476703993599-003599c0c1a3"),
    ],
    "accessories": [
        u("photo-1608248543808-ba0a7e0c4f3c"),
        u("photo-1570172619604-71b782a1e7c0"),
        u("photo-1515488042361-ee00e0ddd4f8"),
        u("photo-1596461404969-9ae70f2830c1"),
    ],
}

FALLBACK = REAL_BY_CATEGORY["diapers"]


def white_delivery_url(secure_url: str) -> str:
    if "/upload/" in secure_url and "/b_white,c_pad," not in secure_url:
        return secure_url.replace(
            "/upload/",
            "/upload/b_white,c_pad,w_800,h_800,f_auto,q_auto/",
        )
    return secure_url


def upload_real(url: str, public_id: str) -> tuple[str, str]:
    # Preflight — skip broken Unsplash IDs
    head = requests.get(url, timeout=20, stream=True)
    if head.status_code >= 400:
        raise RuntimeError(f"HTTP {head.status_code}")
    head.close()

    result = cloudinary.uploader.upload(
        url,
        folder="vimeanbaby/real-products",
        public_id=public_id,
        overwrite=True,
        resource_type="image",
    )
    return white_delivery_url(result["secure_url"]), result["public_id"]


def main() -> None:
    all_urls = sorted({url for urls in REAL_BY_CATEGORY.values() for url in urls})
    cache: dict[str, tuple[str, str]] = {}
    print(f"Uploading up to {len(all_urls)} real product photos...")
    for i, url in enumerate(all_urls, start=1):
        photo = urlparse(url).path.strip("/").split("?")[0]
        pid = f"real-{i:03d}"
        try:
            cache[url] = upload_real(url, pid)
            print(f"  [{i}/{len(all_urls)}] OK {photo}")
        except Exception as ex:
            print(f"  [{i}/{len(all_urls)}] SKIP {photo}: {ex}")
        time.sleep(0.2)

    if len(cache) < 5:
        raise SystemExit(f"Too few uploads succeeded: {len(cache)}")

    # Drop broken sources from category maps
    for slug, urls in list(REAL_BY_CATEGORY.items()):
        REAL_BY_CATEGORY[slug] = [x for x in urls if x in cache] or list(cache.keys())[:4]

    with psycopg.connect(dsn) as conn:
        with conn.cursor() as cur:
            cur.execute("SELECT id, slug FROM categories")
            for cat_id, slug in cur.fetchall():
                sources = REAL_BY_CATEGORY.get(slug, list(cache.keys()))
                url, _pid = cache[sources[0]]
                cur.execute(
                    "UPDATE categories SET image_url = %s, updated_at = NOW() WHERE id = %s",
                    (url, cat_id),
                )
            conn.commit()
            print("Category images updated")

            cur.execute(
                """
                SELECT p.id, c.slug, i.id
                FROM products p
                JOIN categories c ON c.id = p.category_id
                JOIN product_images i ON i.product_id = p.id AND i.is_primary = TRUE
                WHERE p.deleted_at IS NULL
                ORDER BY p.id
                """
            )
            rows = cur.fetchall()
            print(f"Assigning real photos to {len(rows)} products...")
            for n, (product_id, slug, image_id) in enumerate(rows, start=1):
                sources = REAL_BY_CATEGORY.get(slug, list(cache.keys()))
                src = sources[(product_id - 1) % len(sources)]
                url, pid = cache[src]
                cur.execute(
                    """
                    UPDATE product_images
                    SET url = %s, cloudinary_public_id = %s, updated_at = NOW()
                    WHERE id = %s
                    """,
                    (url, pid, image_id),
                )
                if n % 200 == 0:
                    conn.commit()
                    print(f"  {n}/{len(rows)}")
            conn.commit()

            cur.execute(
                """
                SELECT COUNT(*) FROM product_images
                WHERE url LIKE '%vimeanbaby/real-products%'
                   OR cloudinary_public_id LIKE 'vimeanbaby/real-products%'
                """
            )
            print("products_with_real_folder", cur.fetchone()[0])
            cur.execute("SELECT url FROM product_images ORDER BY id DESC LIMIT 1")
            print("sample", cur.fetchone()[0])
            print("Done — hard-refresh the storefront to see real product photos.")


if __name__ == "__main__":
    main()
