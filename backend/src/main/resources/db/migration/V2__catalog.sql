CREATE TABLE categories (
    id          BIGSERIAL PRIMARY KEY,
    parent_id   BIGINT REFERENCES categories(id) ON DELETE SET NULL,
    name_km     VARCHAR(150) NOT NULL,
    name_en     VARCHAR(150) NOT NULL,
    slug        VARCHAR(180) NOT NULL UNIQUE,
    image_url   VARCHAR(500),
    sort_order  INT          NOT NULL DEFAULT 0,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE brands (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    slug        VARCHAR(180) NOT NULL UNIQUE,
    logo_url    VARCHAR(500),
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE products (
    id              BIGSERIAL PRIMARY KEY,
    category_id     BIGINT       NOT NULL REFERENCES categories(id),
    brand_id        BIGINT       REFERENCES brands(id),
    name_km         VARCHAR(255) NOT NULL,
    name_en         VARCHAR(255) NOT NULL,
    slug            VARCHAR(220) NOT NULL UNIQUE,
    description_km  TEXT,
    description_en  TEXT,
    age_range       VARCHAR(50),
    origin_country  VARCHAR(100),
    is_featured     BOOLEAN      NOT NULL DEFAULT FALSE,
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    deleted_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_products_slug ON products(slug);
CREATE INDEX idx_products_category_id ON products(category_id);
CREATE INDEX idx_products_brand_id ON products(brand_id);
CREATE INDEX idx_products_featured ON products(is_featured) WHERE is_active = TRUE AND deleted_at IS NULL;

CREATE TABLE product_variants (
    id               BIGSERIAL PRIMARY KEY,
    product_id       BIGINT         NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    sku              VARCHAR(80)    NOT NULL UNIQUE,
    option_name      VARCHAR(120)   NOT NULL,
    price            NUMERIC(12, 2) NOT NULL,
    compare_at_price NUMERIC(12, 2),
    stock_qty        INT            NOT NULL DEFAULT 0,
    expiry_date      DATE,
    is_active        BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_product_variants_sku ON product_variants(sku);
CREATE INDEX idx_product_variants_product_id ON product_variants(product_id);

CREATE TABLE product_images (
    id                   BIGSERIAL PRIMARY KEY,
    product_id           BIGINT       NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    url                  VARCHAR(500) NOT NULL,
    cloudinary_public_id VARCHAR(255),
    sort_order           INT          NOT NULL DEFAULT 0,
    is_primary           BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_product_images_product_id ON product_images(product_id);
