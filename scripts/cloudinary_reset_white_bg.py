"""
1) Delete ALL old assets under vimeanbaby/ on Cloudinary
2) Generate new white-background product images
3) Update Neon product_images + category image_url
"""
from __future__ import annotations

import io
import os
import time
from pathlib import Path

import cloudinary
import cloudinary.api
import cloudinary.uploader
import psycopg
import requests
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
PROD = ROOT / "backend" / ".env.production"


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

CANVAS = 1000
PREFIXES = [
    "vimeanbaby/",
    "vimeanbaby/products/",
    "vimeanbaby/real-products/",
    "vimeanbaby/categories/",
]

# Real product source photos (Unsplash) by category — will be composited onto pure white
SOURCES: dict[str, list[str]] = {
    "diapers": [
        "https://images.unsplash.com/photo-1515488042361-ee00e0ddd4f8?auto=format&fit=crop&w=900&q=85",
        "https://images.unsplash.com/photo-1604881988758-f76ad2f7aac1?auto=format&fit=crop&w=900&q=85",
        "https://images.unsplash.com/photo-1596461404969-9ae70f2830c1?auto=format&fit=crop&w=900&q=85",
        "https://images.unsplash.com/photo-1566004100631-35d2661f50bf?auto=format&fit=crop&w=900&q=85",
    ],
    "wipes": [
        "https://images.unsplash.com/photo-1556228578-0d85b1a4d571?auto=format&fit=crop&w=900&q=85",
        "https://images.unsplash.com/photo-1570172619604-71b782a1e7c0?auto=format&fit=crop&w=900&q=85",
        "https://images.unsplash.com/photo-1596755389378-c31d21fd1273?auto=format&fit=crop&w=900&q=85",
    ],
    "bath-care": [
        "https://images.unsplash.com/photo-1556228720-195a672e8a03?auto=format&fit=crop&w=900&q=85",
        "https://images.unsplash.com/photo-1570194065650-d99fb4b38c15?auto=format&fit=crop&w=900&q=85",
        "https://images.unsplash.com/photo-1556228453-efd6c1ff04f6?auto=format&fit=crop&w=900&q=85",
    ],
    "feeding": [
        "https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?auto=format&fit=crop&w=900&q=85",
        "https://images.unsplash.com/photo-1555252333-9f8e92e65df9?auto=format&fit=crop&w=900&q=85",
        "https://images.unsplash.com/photo-1519689373023-dd07c7988603?auto=format&fit=crop&w=900&q=85",
    ],
    "baby-clothes": [
        "https://images.unsplash.com/photo-1522771930-78848d9293e8?auto=format&fit=crop&w=900&q=85",
        "https://images.unsplash.com/photo-1503454537195-1dcabb73ffb9?auto=format&fit=crop&w=900&q=85",
        "https://images.unsplash.com/photo-1519238263530-99bdd11df2ea?auto=format&fit=crop&w=900&q=85",
    ],
    "maternity": [
        "https://images.unsplash.com/photo-1493894473891-10fc1e36d37b?auto=format&fit=crop&w=900&q=85",
        "https://images.unsplash.com/photo-1476703993599-003599c0c1a3?auto=format&fit=crop&w=900&q=85",
        "https://images.unsplash.com/photo-1584305574647-0cc949a2bb9f?auto=format&fit=crop&w=900&q=85",
    ],
    "toys": [
        "https://images.unsplash.com/photo-1566576912321-d58ddd7a6088?auto=format&fit=crop&w=900&q=85",
        "https://images.unsplash.com/photo-1516627145497-ae6968895b74?auto=format&fit=crop&w=900&q=85",
        "https://images.unsplash.com/photo-1515488042361-ee00e0ddd4f8?auto=format&fit=crop&w=900&q=85",
    ],
    "strollers": [
        "https://images.unsplash.com/photo-1596462502278-27bfdc403348?auto=format&fit=crop&w=900&q=85",
        "https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?auto=format&fit=crop&w=900&q=85",
    ],
    "nursery": [
        "https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af?auto=format&fit=crop&w=900&q=85",
        "https://images.unsplash.com/photo-1586105251261-72a756497a11?auto=format&fit=crop&w=900&q=85",
        "https://images.unsplash.com/photo-1616486338812-3dadae4b4ace?auto=format&fit=crop&w=900&q=85",
    ],
    "health": [
        "https://images.unsplash.com/photo-1584820927498-cfe5211fd8bf?auto=format&fit=crop&w=900&q=85",
        "https://images.unsplash.com/photo-1576091160399-112ba8d25d1d?auto=format&fit=crop&w=900&q=85",
        "https://images.unsplash.com/photo-1581595220892-b245a1e6b8b9?auto=format&fit=crop&w=900&q=85",
    ],
    "formula": [
        "https://images.unsplash.com/photo-1555252333-9f8e92e65df9?auto=format&fit=crop&w=900&q=85",
        "https://images.unsplash.com/photo-1476703993599-003599c0c1a3?auto=format&fit=crop&w=900&q=85",
        "https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?auto=format&fit=crop&w=900&q=85",
    ],
    "accessories": [
        "https://images.unsplash.com/photo-1570172619604-71b782a1e7c0?auto=format&fit=crop&w=900&q=85",
        "https://images.unsplash.com/photo-1515488042361-ee00e0ddd4f8?auto=format&fit=crop&w=900&q=85",
        "https://images.unsplash.com/photo-1596461404969-9ae70f2830c1?auto=format&fit=crop&w=900&q=85",
    ],
}


def delete_all_old_cloudinary() -> int:
    deleted = 0
    # Also wipe any loose uploads from earlier tests under folder roots
    for prefix in ["vimeanbaby", "vimeanbaby/products", "vimeanbaby/real-products", "vimeanbaby/categories"]:
        print(f"Deleting Cloudinary resources with prefix: {prefix}")
        next_cursor = None
        while True:
            kwargs = {"type": "upload", "resource_type": "image", "prefix": prefix, "max_results": 500}
            if next_cursor:
                kwargs["next_cursor"] = next_cursor
            try:
                result = cloudinary.api.resources(**kwargs)
            except Exception as ex:
                print(f"  list error: {ex}")
                break
            resources = result.get("resources", [])
            if not resources:
                break
            public_ids = [r["public_id"] for r in resources]
            # delete in chunks of 100
            for i in range(0, len(public_ids), 100):
                chunk = public_ids[i : i + 100]
                cloudinary.api.delete_resources(chunk, resource_type="image", type="upload")
                deleted += len(chunk)
                print(f"  deleted {deleted}...")
            next_cursor = result.get("next_cursor")
            if not next_cursor:
                break
            time.sleep(0.3)

        # try delete empty folders
        try:
            cloudinary.api.delete_folder(prefix)
        except Exception:
            pass
    print(f"Total deleted: {deleted}")
    return deleted


def download_image(url: str) -> Image.Image | None:
    try:
        r = requests.get(url, timeout=30)
        if r.status_code >= 400:
            return None
        return Image.open(io.BytesIO(r.content)).convert("RGBA")
    except Exception:
        return None


def to_white_background(src: Image.Image, max_product_ratio: float = 0.78) -> bytes:
    """Composite product photo centered on a pure white square canvas."""
    canvas = Image.new("RGBA", (CANVAS, CANVAS), (255, 255, 255, 255))
    img = src.convert("RGBA")

    # Fit inside white canvas with margin
    max_side = int(CANVAS * max_product_ratio)
    img.thumbnail((max_side, max_side), Image.Resampling.LANCZOS)

    x = (CANVAS - img.width) // 2
    y = (CANVAS - img.height) // 2
    canvas.alpha_composite(img, (x, y))

    out = canvas.convert("RGB")
    buf = io.BytesIO()
    out.save(buf, format="JPEG", quality=90, optimize=True)
    buf.seek(0)
    buf.name = "product.jpg"
    return buf.getvalue()


def upload_bytes(data: bytes, public_id: str, folder: str) -> tuple[str, str]:
    buf = io.BytesIO(data)
    buf.name = "product.jpg"
    result = cloudinary.uploader.upload(
        buf,
        folder=folder,
        public_id=public_id,
        overwrite=True,
        resource_type="image",
        unique_filename=False,
    )
    # Force white pad delivery (already white, keeps consistent size)
    url = result["secure_url"].replace(
        "/upload/",
        "/upload/b_white,c_pad,w_800,h_800,f_auto,q_auto/",
    )
    return url, result["public_id"]


def build_white_library() -> dict[str, list[tuple[str, str]]]:
    """category -> list of (url, public_id) newly generated white-BG assets."""
    library: dict[str, list[tuple[str, str]]] = {}
    fallback_bytes: bytes | None = None

    for slug, urls in SOURCES.items():
        library[slug] = []
        for i, url in enumerate(urls):
            print(f"  generate {slug}/{i} ...")
            img = download_image(url)
            if img is None:
                print(f"    skip broken source")
                continue
            data = to_white_background(img)
            if fallback_bytes is None:
                fallback_bytes = data
            secure, pid = upload_bytes(data, f"{slug}-{i:02d}", "vimeanbaby/wb")
            library[slug].append((secure, pid))
            print(f"    -> {pid}")
            time.sleep(0.25)

    # ensure every category has at least one
    any_asset = next((v for v in library.values() if v), None)
    if not any_asset and fallback_bytes:
        secure, pid = upload_bytes(fallback_bytes, "fallback-00", "vimeanbaby/wb")
        any_asset = [(secure, pid)]
    if not any_asset:
        raise SystemExit("Failed to generate any white-BG images")

    for slug in SOURCES:
        if not library.get(slug):
            library[slug] = list(any_asset)
    return library


def update_neon(library: dict[str, list[tuple[str, str]]]) -> None:
    with psycopg.connect(DSN) as conn:
        with conn.cursor() as cur:
            cur.execute("SELECT id, slug FROM categories")
            for cat_id, slug in cur.fetchall():
                assets = library.get(slug) or next(iter(library.values()))
                url, _pid = assets[0]
                cur.execute(
                    "UPDATE categories SET image_url = %s, updated_at = NOW() WHERE id = %s",
                    (url, cat_id),
                )
            conn.commit()
            print("Updated category images")

            cur.execute(
                """
                SELECT p.id, c.slug, i.id
                FROM products p
                JOIN categories c ON c.id = p.category_id
                JOIN product_images i ON i.product_id = p.id AND i.is_primary
                WHERE p.deleted_at IS NULL
                ORDER BY p.id
                """
            )
            rows = cur.fetchall()
            print(f"Updating {len(rows)} product images...")
            for n, (pid, slug, image_id) in enumerate(rows, start=1):
                assets = library.get(slug) or next(iter(library.values()))
                url, public_id = assets[(pid - 1) % len(assets)]
                cur.execute(
                    """
                    UPDATE product_images
                    SET url = %s, cloudinary_public_id = %s, updated_at = NOW()
                    WHERE id = %s
                    """,
                    (url, public_id, image_id),
                )
                if n % 200 == 0:
                    conn.commit()
                    print(f"  {n}/{len(rows)}")
            conn.commit()
            print(f"Done. {len(rows)} products now use new white-BG images.")


def main() -> None:
    print("=== 1) Delete old Cloudinary images ===")
    delete_all_old_cloudinary()

    print("=== 2) Generate new white-background images ===")
    library = build_white_library()
    total = sum(len(v) for v in library.values())
    print(f"Generated {total} white-BG assets in folder vimeanbaby/wb")

    print("=== 3) Point Neon products at new images ===")
    update_neon(library)


if __name__ == "__main__":
    main()
