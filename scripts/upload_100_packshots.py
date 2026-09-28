"""Upload generated AI packshots to Cloudinary and cycle them across all Neon products."""
from __future__ import annotations

import os
import time
from concurrent.futures import ThreadPoolExecutor, as_completed
from pathlib import Path

import cloudinary
import cloudinary.uploader
import psycopg

ROOT = Path(__file__).resolve().parents[1]
PROD = ROOT / "backend" / ".env.production"
PACKSHOTS = ROOT / "generated-packshots"
FOLDER = "vimeanbaby/packshots"


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


def delivery_url(secure_url: str) -> str:
    if "/upload/" in secure_url and "b_white" not in secure_url:
        return secure_url.replace(
            "/upload/",
            "/upload/b_white,c_pad,w_800,h_800,f_auto,q_auto/",
        )
    return secure_url


def upload_one(path: Path) -> tuple[str, str, str]:
    # p-0001-diapers-newborn.png -> public_id p-0001
    stem = path.stem  # p-0001-diapers-newborn
    public_id = "-".join(stem.split("-")[:2])  # p-0001
    result = cloudinary.uploader.upload(
        str(path),
        folder=FOLDER,
        public_id=public_id,
        overwrite=True,
        resource_type="image",
        unique_filename=False,
    )
    return public_id, delivery_url(result["secure_url"]), result["public_id"]


def main() -> None:
    files = sorted(PACKSHOTS.glob("p-*.png"))
    if len(files) != 100:
        raise SystemExit(f"Expected 100 packshots, found {len(files)} in {PACKSHOTS}")

    print(f"Uploading {len(files)} packshots to Cloudinary folder {FOLDER}/ ...")
    uploaded: dict[str, tuple[str, str]] = {}  # key p-0001 -> (url, full_public_id)

    def work(path: Path) -> tuple[str, str, str]:
        return upload_one(path)

    batch_size = 10
    for start in range(0, len(files), batch_size):
        batch = files[start : start + batch_size]
        retries = 0
        while True:
            try:
                with ThreadPoolExecutor(max_workers=5) as ex:
                    futs = [ex.submit(work, p) for p in batch]
                    for fut in as_completed(futs):
                        key, url, pid = fut.result()
                        uploaded[key] = (url, pid)
                break
            except Exception as exn:
                retries += 1
                print(f"  batch {start + 1}-{start + len(batch)} retry {retries}: {exn}")
                if retries >= 4:
                    raise
                time.sleep(2 * retries)
        print(f"  uploaded {len(uploaded)}/{len(files)}")
        time.sleep(0.3)

    # Stable order p-0001 .. p-0100
    ordered = [uploaded[f"p-{i:04d}"] for i in range(1, 101)]
    print(f"Cloudinary assets ready: {len(ordered)}")

    print("Linking to all Neon products (cycling 100 images)...")
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
            for n, (_product_id, image_id) in enumerate(rows):
                url, pid = ordered[n % len(ordered)]
                cur.execute(
                    """
                    UPDATE product_images
                    SET url = %s, cloudinary_public_id = %s, updated_at = NOW()
                    WHERE id = %s
                    """,
                    (url, pid, image_id),
                )
                if (n + 1) % 200 == 0:
                    conn.commit()
                    print(f"  linked {n + 1}/{len(rows)}")
            conn.commit()

            # Refresh category images from first N packshots
            cur.execute("SELECT id FROM categories ORDER BY id")
            cats = cur.fetchall()
            for i, (cat_id,) in enumerate(cats):
                url, _pid = ordered[i % len(ordered)]
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
            print("neon_products_with_packshots", cur.fetchone()[0])
            cur.execute(
                "SELECT url FROM product_images WHERE cloudinary_public_id LIKE %s LIMIT 1",
                (f"{FOLDER}/%",),
            )
            print("sample", cur.fetchone()[0])

    print(f"Done: {len(ordered)} packshots on Cloudinary, cycled across {len(rows)} products.")


if __name__ == "__main__":
    main()
