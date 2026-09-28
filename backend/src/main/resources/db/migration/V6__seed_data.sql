-- Seed brands
INSERT INTO brands (id, name, slug, logo_url, is_active) VALUES
 (1, 'Pampers', 'pampers', NULL, TRUE),
 (2, 'Huggies', 'huggies', NULL, TRUE),
 (3, 'Merries', 'merries', NULL, TRUE),
 (4, 'Johnson Baby', 'johnson-baby', NULL, TRUE),
 (5, 'Aveeno Baby', 'aveeno-baby', NULL, TRUE);

SELECT setval('brands_id_seq', (SELECT MAX(id) FROM brands));

-- Seed categories
INSERT INTO categories (id, parent_id, name_km, name_en, slug, image_url, sort_order, is_active) VALUES
 (1, NULL, 'កន្ទប', 'Diapers', 'diapers',
  'https://images.unsplash.com/photo-1515488042361-ee00e0ddd4f8?auto=format&fit=crop&w=600&q=80', 1, TRUE),
 (2, NULL, 'ក្រដាសជូត', 'Wipes', 'wipes',
  'https://images.unsplash.com/photo-1584839401442-83d3a91e5f2b?auto=format&fit=crop&w=600&q=80', 2, TRUE),
 (3, NULL, 'ងូតទឹក និង ថែរក្សាស្បែក', 'Bath & Care', 'bath-care',
  'https://images.unsplash.com/photo-1556228578-0d85b1a4d571?auto=format&fit=crop&w=600&q=80', 3, TRUE);

SELECT setval('categories_id_seq', (SELECT MAX(id) FROM categories));

-- Seed products
INSERT INTO products (id, category_id, brand_id, name_km, name_en, slug, description_km, description_en, age_range, origin_country, is_featured, is_active) VALUES
 (1, 1, 1, 'Pampers Premium Care ទំហំ NB', 'Pampers Premium Care Newborn', 'pampers-premium-care-nb',
  'កន្ទបទន់សម្រាប់ទារកទើបកើត ជួយការពារស្បែករសើប។', 'Soft diapers for newborns, gentle on sensitive skin.', '0-3 months', 'Japan', TRUE, TRUE),
 (2, 1, 1, 'Pampers Baby Dry ទំហំ M', 'Pampers Baby Dry Size M', 'pampers-baby-dry-m',
  'កន្ទបស្រូបទឹកល្អ សម្រាប់ទារកចល័ត។', 'High absorbency diapers for active babies.', '6-12 months', 'Thailand', TRUE, TRUE),
 (3, 1, 2, 'Huggies Ultra Comfort ទំហំ L', 'Huggies Ultra Comfort Size L', 'huggies-ultra-comfort-l',
  'កន្ទបស្រួលពាក់ មានខ្យល់ចេញចូលល្អ។', 'Breathable and comfortable fit for growing babies.', '9-14 months', 'Indonesia', FALSE, TRUE),
 (4, 1, 3, 'Merries Tape ទំហំ S', 'Merries Tape Size S', 'merries-tape-s',
  'កន្ទបជប៉ុន ស្រូបទឹកលឿន និងស្ងួត។', 'Japanese diapers with fast absorption and dry feel.', '3-6 months', 'Japan', TRUE, TRUE),
 (5, 2, 2, 'Huggies Natural Care Wipes', 'Huggies Natural Care Wipes', 'huggies-natural-care-wipes',
  'ក្រដាសជូតគ្មានក្លិនសម្រាប់ស្បែករសើប។', 'Fragrance-free wipes for sensitive baby skin.', '0+ months', 'USA', TRUE, TRUE),
 (6, 2, 1, 'Pampers Sensitive Wipes', 'Pampers Sensitive Wipes', 'pampers-sensitive-wipes',
  'ក្រដាសជូតទន់ បន្ថែមសំណើម។', 'Extra soft moisturizing wipes for delicate skin.', '0+ months', 'Thailand', FALSE, TRUE),
 (7, 3, 4, 'Johnson Baby Shampoo', 'Johnson Baby Shampoo', 'johnson-baby-shampoo',
  'សាប៊ូកក់សក់ទន់ មិនឈឺភ្នែក។', 'No more tears baby shampoo for gentle cleansing.', '0+ months', 'Indonesia', TRUE, TRUE),
 (8, 3, 4, 'Johnson Baby Oil', 'Johnson Baby Oil', 'johnson-baby-oil',
  'ប្រេងទន់សម្រាប់ម៉ាស្សាទារក។', 'Gentle baby oil for massage and moisturizing.', '0+ months', 'Indonesia', FALSE, TRUE),
 (9, 3, 5, 'Aveeno Baby Daily Moisture Lotion', 'Aveeno Baby Daily Moisture Lotion', 'aveeno-baby-daily-moisture',
  'ឡេសំណើមសម្រាប់ស្បែកស្ងួត និងរសើប។', 'Daily moisture lotion for dry, sensitive baby skin.', '0+ months', 'USA', TRUE, TRUE),
 (10, 3, 5, 'Aveeno Baby Wash & Shampoo', 'Aveeno Baby Wash & Shampoo', 'aveeno-baby-wash-shampoo',
  'សាប៊ូងូតទឹក និងកក់សក់ ២ ក្នុង ១។', '2-in-1 baby wash and shampoo with oat extract.', '0+ months', 'USA', FALSE, TRUE);

SELECT setval('products_id_seq', (SELECT MAX(id) FROM products));

-- Seed variants
INSERT INTO product_variants (product_id, sku, option_name, price, compare_at_price, stock_qty, expiry_date, is_active) VALUES
 (1, 'PAM-PC-NB-24', 'Pack 24', 8.50, 9.90, 40, '2027-06-30', TRUE),
 (1, 'PAM-PC-NB-48', 'Pack 48', 15.90, 17.50, 25, '2027-06-30', TRUE),
 (2, 'PAM-BD-M-46', 'Pack 46', 12.50, 14.00, 8, '2027-03-15', TRUE),
 (3, 'HUG-UC-L-40', 'Pack 40', 13.20, NULL, 30, '2027-08-01', TRUE),
 (4, 'MER-TP-S-30', 'Pack 30', 14.80, 16.00, 5, '2027-05-20', TRUE),
 (5, 'HUG-NC-W56', 'Pack 56', 4.90, 5.50, 60, '2027-12-01', TRUE),
 (5, 'HUG-NC-W112', 'Pack 112', 8.90, 9.90, 35, '2027-12-01', TRUE),
 (6, 'PAM-SW-56', 'Pack 56', 4.50, NULL, 45, '2027-11-15', TRUE),
 (7, 'JNB-SH-200', '200 ml', 5.20, 5.90, 50, NULL, TRUE),
 (8, 'JNB-OIL-200', '200 ml', 4.80, NULL, 42, NULL, TRUE),
 (9, 'AVE-DM-227', '227 ml', 9.50, 10.90, 7, NULL, TRUE),
 (10, 'AVE-WS-236', '236 ml', 8.20, NULL, 28, NULL, TRUE);

-- Seed images (primary)
INSERT INTO product_images (product_id, url, cloudinary_public_id, sort_order, is_primary) VALUES
 (1, 'https://images.unsplash.com/photo-1515488042361-ee00e0ddd4f8?auto=format&fit=crop&w=800&q=80', NULL, 0, TRUE),
 (2, 'https://images.unsplash.com/photo-1604881988758-f76ad2f7aac1?auto=format&fit=crop&w=800&q=80', NULL, 0, TRUE),
 (3, 'https://images.unsplash.com/photo-1596461404969-9ae70f2830c1?auto=format&fit=crop&w=800&q=80', NULL, 0, TRUE),
 (4, 'https://images.unsplash.com/photo-1584839401442-83d3a91e5f2b?auto=format&fit=crop&w=800&q=80', NULL, 0, TRUE),
 (5, 'https://images.unsplash.com/photo-1556228578-0d85b1a4d571?auto=format&fit=crop&w=800&q=80', NULL, 0, TRUE),
 (6, 'https://images.unsplash.com/photo-1570172619604-71b782a1e7c0?auto=format&fit=crop&w=800&q=80', NULL, 0, TRUE),
 (7, 'https://images.unsplash.com/photo-1556228720-195a672e8a03?auto=format&fit=crop&w=800&q=80', NULL, 0, TRUE),
 (8, 'https://images.unsplash.com/photo-1608248543808-ba0a7e0c4f3c?auto=format&fit=crop&w=800&q=80', NULL, 0, TRUE),
 (9, 'https://images.unsplash.com/photo-1570194065650-d99fb4b38c15?auto=format&fit=crop&w=800&q=80', NULL, 0, TRUE),
 (10, 'https://images.unsplash.com/photo-1620916567722-6a1a8e1c2d6a?auto=format&fit=crop&w=800&q=80', NULL, 0, TRUE);
