-- Replace broken real-products URLs with Cloudinary packshots (vimeanbaby/packshots/p-0001 … p-0100).

UPDATE categories SET
  image_url = CASE slug
    WHEN 'diapers' THEN 'https://res.cloudinary.com/vimeanbaby/image/upload/vimeanbaby/packshots/p-0001'
    WHEN 'wipes' THEN 'https://res.cloudinary.com/vimeanbaby/image/upload/vimeanbaby/packshots/p-0002'
    WHEN 'bath-care' THEN 'https://res.cloudinary.com/vimeanbaby/image/upload/vimeanbaby/packshots/p-0003'
    WHEN 'feeding' THEN 'https://res.cloudinary.com/vimeanbaby/image/upload/vimeanbaby/packshots/p-0006'
    WHEN 'baby-clothes' THEN 'https://res.cloudinary.com/vimeanbaby/image/upload/vimeanbaby/packshots/p-0008'
    WHEN 'maternity' THEN 'https://res.cloudinary.com/vimeanbaby/image/upload/vimeanbaby/packshots/p-0010'
    ELSE 'https://res.cloudinary.com/vimeanbaby/image/upload/vimeanbaby/packshots/p-' ||
         LPAD((((id - 1) % 100) + 1)::text, 4, '0')
  END,
  updated_at = NOW();

UPDATE product_images
SET
  url = 'https://res.cloudinary.com/vimeanbaby/image/upload/vimeanbaby/packshots/p-' ||
        LPAD((((product_id - 1) % 100) + 1)::text, 4, '0'),
  cloudinary_public_id = 'vimeanbaby/packshots/p-' ||
        LPAD((((product_id - 1) % 100) + 1)::text, 4, '0'),
  updated_at = NOW()
WHERE is_primary = TRUE;
