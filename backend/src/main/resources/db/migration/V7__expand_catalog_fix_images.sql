-- Fix images with stable placeholders hosted by the storefront public folder paths.
-- Frontend serves these from user-frontend/public/catalog/*

UPDATE categories SET image_url = '/catalog/cat-diapers.svg', updated_at = NOW() WHERE slug = 'diapers';
UPDATE categories SET image_url = '/catalog/cat-wipes.svg', updated_at = NOW() WHERE slug = 'wipes';
UPDATE categories SET image_url = '/catalog/cat-bath.svg', updated_at = NOW() WHERE slug = 'bath-care';

INSERT INTO categories (id, parent_id, name_km, name_en, slug, image_url, sort_order, is_active) VALUES
 (4, NULL, 'ចំណីទារក', 'Feeding', 'feeding', '/catalog/cat-feeding.svg', 4, TRUE),
 (5, NULL, 'សម្លៀកបំពាក់', 'Baby Clothes', 'baby-clothes', '/catalog/cat-clothes.svg', 5, TRUE),
 (6, NULL, 'សម្រាប់ម៉ាក់', 'Maternity', 'maternity', '/catalog/cat-maternity.svg', 6, TRUE)
ON CONFLICT (id) DO UPDATE SET
  name_km = EXCLUDED.name_km,
  name_en = EXCLUDED.name_en,
  slug = EXCLUDED.slug,
  image_url = EXCLUDED.image_url,
  sort_order = EXCLUDED.sort_order,
  is_active = TRUE,
  updated_at = NOW();

SELECT setval('categories_id_seq', (SELECT MAX(id) FROM categories));

INSERT INTO brands (id, name, slug, logo_url, is_active) VALUES
 (6, 'Pigeon', 'pigeon', NULL, TRUE),
 (7, 'Chicco', 'chicco', NULL, TRUE)
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, slug = EXCLUDED.slug, is_active = TRUE, updated_at = NOW();

SELECT setval('brands_id_seq', (SELECT MAX(id) FROM brands));

-- Replace existing product images with local catalog assets
UPDATE product_images SET url = '/catalog/p-pampers-nb.svg', updated_at = NOW()
WHERE product_id = 1 AND is_primary = TRUE;
UPDATE product_images SET url = '/catalog/p-pampers-m.svg', updated_at = NOW()
WHERE product_id = 2 AND is_primary = TRUE;
UPDATE product_images SET url = '/catalog/p-huggies-l.svg', updated_at = NOW()
WHERE product_id = 3 AND is_primary = TRUE;
UPDATE product_images SET url = '/catalog/p-merries-s.svg', updated_at = NOW()
WHERE product_id = 4 AND is_primary = TRUE;
UPDATE product_images SET url = '/catalog/p-huggies-wipes.svg', updated_at = NOW()
WHERE product_id = 5 AND is_primary = TRUE;
UPDATE product_images SET url = '/catalog/p-pampers-wipes.svg', updated_at = NOW()
WHERE product_id = 6 AND is_primary = TRUE;
UPDATE product_images SET url = '/catalog/p-johnson-shampoo.svg', updated_at = NOW()
WHERE product_id = 7 AND is_primary = TRUE;
UPDATE product_images SET url = '/catalog/p-johnson-oil.svg', updated_at = NOW()
WHERE product_id = 8 AND is_primary = TRUE;
UPDATE product_images SET url = '/catalog/p-aveeno-lotion.svg', updated_at = NOW()
WHERE product_id = 9 AND is_primary = TRUE;
UPDATE product_images SET url = '/catalog/p-aveeno-wash.svg', updated_at = NOW()
WHERE product_id = 10 AND is_primary = TRUE;

-- Extra products
INSERT INTO products (id, category_id, brand_id, name_km, name_en, slug, description_km, description_en, age_range, origin_country, is_featured, is_active) VALUES
 (11, 4, 6, 'Pigeon ដបទឹកដោះគោ Wide Neck', 'Pigeon Wide Neck Baby Bottle', 'pigeon-wide-neck-bottle',
  'ដបទឹកដោះគោទន់ ងាយស្រូប។', 'Soft wide-neck bottle designed for comfortable feeding.', '0+ months', 'Japan', TRUE, TRUE),
 (12, 4, 6, 'Pigeon ស៊ីលីកូន ដើមបបរ', 'Pigeon Silicone Spoon', 'pigeon-silicone-spoon',
  'ស្លាបព្រាស៊ីលីកូនទន់សម្រាប់ទារក។', 'Soft silicone weaning spoon for first foods.', '4+ months', 'Japan', FALSE, TRUE),
 (13, 5, 7, 'Chicco Body Suit ២ ឈុត', 'Chicco Soft Bodysuit 2-Pack', 'chicco-bodysuit-2pack',
  'ឈុតទារកទន់ ស្រួលពាក់។', 'Soft cotton bodysuits for everyday comfort.', '0-6 months', 'Italy', TRUE, TRUE),
 (14, 5, 7, 'Chicco អាវទារក', 'Chicco Baby Romper', 'chicco-baby-romper',
  'អាវទារកស្រួលចល័ត។', 'Breathable romper for active little ones.', '3-9 months', 'Italy', FALSE, TRUE),
 (15, 6, 5, 'Aveeno Mama Belly Balm', 'Aveeno Mama Belly Balm', 'aveeno-mama-belly-balm',
  'បាល់ម៉ាសម្រាប់ស្បែកម៉ាក់។', 'Soothing balm for maternity skin care.', 'Maternity', 'USA', TRUE, TRUE),
 (16, 6, 4, 'Johnson Mama Lotion', 'Johnson Mama Body Lotion', 'johnson-mama-lotion',
  'ឡេសំណើមសម្រាប់ម៉ាក់។', 'Moisturizing lotion made for moms.', 'Maternity', 'Indonesia', FALSE, TRUE)
ON CONFLICT (id) DO UPDATE SET
  category_id = EXCLUDED.category_id,
  brand_id = EXCLUDED.brand_id,
  name_km = EXCLUDED.name_km,
  name_en = EXCLUDED.name_en,
  slug = EXCLUDED.slug,
  description_km = EXCLUDED.description_km,
  description_en = EXCLUDED.description_en,
  age_range = EXCLUDED.age_range,
  origin_country = EXCLUDED.origin_country,
  is_featured = EXCLUDED.is_featured,
  is_active = TRUE,
  updated_at = NOW();

SELECT setval('products_id_seq', (SELECT MAX(id) FROM products));

INSERT INTO product_variants (product_id, sku, option_name, price, compare_at_price, stock_qty, is_active)
SELECT * FROM (VALUES
 (11, 'PIG-BN-160', '160 ml', 7.50::numeric, 8.50::numeric, 22, TRUE),
 (12, 'PIG-SP-01', '1 pc', 3.20::numeric, NULL::numeric, 40, TRUE),
 (13, 'CHI-BS-2P', '2-Pack', 11.90::numeric, 13.50::numeric, 18, TRUE),
 (14, 'CHI-RP-01', 'Size 3-6M', 9.80::numeric, NULL::numeric, 15, TRUE),
 (15, 'AVE-MB-100', '100 ml', 12.50::numeric, 14.00::numeric, 9, TRUE),
 (16, 'JNB-ML-200', '200 ml', 6.40::numeric, NULL::numeric, 26, TRUE)
) AS v(product_id, sku, option_name, price, compare_at_price, stock_qty, is_active)
WHERE NOT EXISTS (SELECT 1 FROM product_variants pv WHERE pv.sku = v.sku);

INSERT INTO product_images (product_id, url, cloudinary_public_id, sort_order, is_primary)
SELECT * FROM (VALUES
 (11, '/catalog/p-pigeon-bottle.svg', NULL, 0, TRUE),
 (12, '/catalog/p-pigeon-spoon.svg', NULL, 0, TRUE),
 (13, '/catalog/p-chicco-bodysuit.svg', NULL, 0, TRUE),
 (14, '/catalog/p-chicco-romper.svg', NULL, 0, TRUE),
 (15, '/catalog/p-aveeno-mama.svg', NULL, 0, TRUE),
 (16, '/catalog/p-johnson-mama.svg', NULL, 0, TRUE)
) AS v(product_id, url, cloudinary_public_id, sort_order, is_primary)
WHERE NOT EXISTS (
  SELECT 1 FROM product_images pi WHERE pi.product_id = v.product_id AND pi.is_primary = TRUE
);
