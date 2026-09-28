# Vimean Baby: Master Prompt for Cursor AI

> How to use: save this file as `docs/MASTER_PROMPT.md` in the repo root. Copy the "Project Rules" section into `.cursor/rules/project.mdc` (or `.cursorrules`) so Cursor always follows it. Then build phase by phase using the "Phase Prompts" at the bottom.

---

## 1. Project Overview

Build a full-stack e-commerce website called **Vimean Baby**, selling mother and baby products (diapers, wipes, bath products, feeding items, baby clothes, maternity items) in **Cambodia**.

**Business needs**
- Mobile-first (most customers use phones).
- Languages: Khmer and English (Khmer default). Use a proper Khmer font (Noto Sans Khmer / Kantumruy Pro).
- Currency: USD primary, optional KHR display. Store money as `NUMERIC(12,2)`.
- Payments: Cash on Delivery (COD) and bank transfer first; ABA PayWay and KHQR later.
- Delivery: manual shipping fee by province at first; courier APIs (J&T, Flash Express, Virak Buntham) later.
- Trust is key: show product authenticity info, expiry dates, and brand.

## 2. Tech Stack (fixed, do not substitute)

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 3.x, Spring Web, Spring Data JPA, Spring Security (JWT), Validation, Lombok, MapStruct |
| Database | PostgreSQL 16, Flyway migrations |
| Customer frontend | Nuxt 3 (SSR), TypeScript, Tailwind CSS, Pinia, @nuxtjs/i18n, VueUse |
| Admin frontend | Nuxt 3 (SPA mode, `ssr: false`), TypeScript, Tailwind CSS, Pinia |
| Images | Cloudinary (backend generates signed upload or handles upload; DB stores URL + public_id only) |
| API docs | springdoc-openapi (Swagger UI) |
| Local dev | Docker Compose (PostgreSQL + backend) |
| Deployment | Railway (each folder a separate service) |

## 3. Monorepo Folder Structure

```
VimeanBaby/
├── .cursor/
│   └── rules/
│       └── project.mdc
├── backend/
│   ├── pom.xml
│   ├── Dockerfile
│   ├── .env.example
│   └── src/
│       ├── main/
│       │   ├── java/com/vimeanbaby/
│       │   │   ├── VimeanBabyApplication.java
│       │   │   ├── config/
│       │   │   │   ├── SecurityConfig.java
│       │   │   │   ├── CorsConfig.java
│       │   │   │   ├── CloudinaryConfig.java
│       │   │   │   ├── OpenApiConfig.java
│       │   │   │   └── JpaAuditingConfig.java
│       │   │   ├── security/
│       │   │   │   ├── JwtService.java
│       │   │   │   ├── JwtAuthFilter.java
│       │   │   │   └── CustomUserDetailsService.java
│       │   │   ├── common/
│       │   │   │   ├── ApiResponse.java
│       │   │   │   ├── PageResponse.java
│       │   │   │   └── BaseEntity.java
│       │   │   ├── exception/
│       │   │   │   ├── GlobalExceptionHandler.java
│       │   │   │   ├── ResourceNotFoundException.java
│       │   │   │   ├── BadRequestException.java
│       │   │   │   └── OutOfStockException.java
│       │   │   ├── user/
│       │   │   │   ├── entity/ (User, Role, Address)
│       │   │   │   ├── repository/
│       │   │   │   ├── dto/
│       │   │   │   ├── mapper/
│       │   │   │   ├── service/
│       │   │   │   └── controller/ (AuthController, ProfileController, AdminUserController)
│       │   │   ├── catalog/
│       │   │   │   ├── entity/ (Category, Brand, Product, ProductImage, ProductVariant)
│       │   │   │   ├── repository/
│       │   │   │   ├── dto/
│       │   │   │   ├── mapper/
│       │   │   │   ├── service/
│       │   │   │   └── controller/ (PublicProductController, AdminProductController, AdminCategoryController, AdminBrandController)
│       │   │   ├── cart/
│       │   │   │   ├── entity/ (Cart, CartItem)
│       │   │   │   ├── repository/
│       │   │   │   ├── dto/
│       │   │   │   ├── service/
│       │   │   │   └── controller/
│       │   │   ├── order/
│       │   │   │   ├── entity/ (Order, OrderItem, OrderStatusHistory)
│       │   │   │   ├── enums/ (OrderStatus, PaymentMethod, PaymentStatus)
│       │   │   │   ├── repository/
│       │   │   │   ├── dto/
│       │   │   │   ├── mapper/
│       │   │   │   ├── service/
│       │   │   │   └── controller/ (OrderController, AdminOrderController)
│       │   │   ├── payment/
│       │   │   │   ├── entity/ (Payment)
│       │   │   │   ├── service/ (PaymentService, CodPaymentService; later AbaPayWayService, KhqrService)
│       │   │   │   └── controller/
│       │   │   ├── shipping/
│       │   │   │   ├── entity/ (ShippingZone)
│       │   │   │   ├── service/
│       │   │   │   └── controller/
│       │   │   ├── media/
│       │   │   │   ├── service/ (CloudinaryService)
│       │   │   │   └── controller/ (AdminMediaController)
│       │   │   └── dashboard/
│       │   │       ├── service/
│       │   │       └── controller/ (AdminDashboardController)
│       │   └── resources/
│       │       ├── application.yml
│       │       ├── application-dev.yml
│       │       ├── application-prod.yml
│       │       └── db/migration/
│       │           ├── V1__init_users_roles.sql
│       │           ├── V2__catalog.sql
│       │           ├── V3__cart.sql
│       │           ├── V4__orders_payments.sql
│       │           ├── V5__shipping.sql
│       │           └── V6__seed_data.sql
│       └── test/java/com/vimeanbaby/ (service and controller tests)
│
├── user-frontend/
│   ├── nuxt.config.ts
│   ├── tailwind.config.ts
│   ├── package.json
│   ├── .env.example
│   ├── app.vue
│   ├── assets/css/main.css
│   ├── i18n/locales/ (km.json, en.json)
│   ├── layouts/ (default.vue, checkout.vue)
│   ├── pages/
│   │   ├── index.vue
│   │   ├── products/index.vue
│   │   ├── products/[slug].vue
│   │   ├── categories/[slug].vue
│   │   ├── search.vue
│   │   ├── cart.vue
│   │   ├── checkout.vue
│   │   ├── order-success/[orderNo].vue
│   │   ├── track-order.vue
│   │   ├── login.vue
│   │   ├── register.vue
│   │   └── account/ (index.vue, orders.vue, orders/[id].vue, addresses.vue)
│   ├── components/
│   │   ├── layout/ (AppHeader, AppFooter, MobileNav, LanguageSwitcher)
│   │   ├── product/ (ProductCard, ProductGrid, ProductGallery, ProductFilters, PriceTag)
│   │   ├── cart/ (CartItemRow, CartSummary, MiniCart)
│   │   ├── checkout/ (AddressForm, PaymentMethodSelect, OrderSummary)
│   │   └── ui/ (BaseButton, BaseInput, BaseModal, Toast, Skeleton, Pagination)
│   ├── composables/ (useApi.ts, useAuth.ts, useCart.ts, useCurrency.ts)
│   ├── stores/ (auth.ts, cart.ts)
│   ├── middleware/ (auth.ts)
│   ├── plugins/ (api.ts)
│   ├── types/ (product.ts, order.ts, user.ts)
│   ├── utils/ (format.ts)
│   └── public/ (favicon.ico, logo.svg)
│
├── admin-frontend/
│   ├── nuxt.config.ts            # ssr: false
│   ├── tailwind.config.ts
│   ├── package.json
│   ├── .env.example
│   ├── app.vue
│   ├── layouts/ (default.vue with sidebar, auth.vue)
│   ├── pages/
│   │   ├── login.vue
│   │   ├── index.vue             # dashboard
│   │   ├── products/ (index.vue, create.vue, [id].vue)
│   │   ├── categories/index.vue
│   │   ├── brands/index.vue
│   │   ├── orders/ (index.vue, [id].vue)
│   │   ├── customers/ (index.vue, [id].vue)
│   │   ├── shipping/index.vue
│   │   └── settings/index.vue
│   ├── components/
│   │   ├── layout/ (Sidebar, Topbar)
│   │   ├── table/ (DataTable, TablePagination, StatusBadge)
│   │   ├── product/ (ProductForm, ImageUploader, VariantEditor)
│   │   ├── order/ (OrderStatusStepper, OrderStatusSelect)
│   │   └── dashboard/ (StatCard, SalesChart, LowStockList)
│   ├── composables/ (useApi.ts, useAuth.ts, useUpload.ts)
│   ├── stores/ (auth.ts)
│   ├── middleware/ (admin-auth.ts)
│   └── types/
│
├── docs/
│   ├── MASTER_PROMPT.md
│   ├── database-diagram.md
│   └── api-endpoints.md
├── docker-compose.yml
├── .gitignore
└── README.md
```

## 4. Database Design (PostgreSQL)

Use `BIGSERIAL` primary keys, `created_at` / `updated_at` (timestamptz) on all tables, and soft delete (`is_active` or `deleted_at`) for products.

- **users**: id, full_name, email (unique), phone (unique), password_hash, role (`CUSTOMER` | `ADMIN`), is_active
- **addresses**: id, user_id, receiver_name, phone, province, district, commune, street_detail, note, is_default
- **categories**: id, parent_id, name_km, name_en, slug (unique), image_url, sort_order, is_active
- **brands**: id, name, slug, logo_url, is_active
- **products**: id, category_id, brand_id, name_km, name_en, slug (unique), description_km, description_en, age_range (e.g. 0-3 months), origin_country, is_featured, is_active
- **product_variants**: id, product_id, sku (unique), option_name (size, weight, pack), price NUMERIC(12,2), compare_at_price, stock_qty, expiry_date (nullable), is_active
- **product_images**: id, product_id, url, cloudinary_public_id, sort_order, is_primary
- **carts** / **cart_items**: cart per user (guest carts can live in the frontend store and merge on login)
- **orders**: id, order_no (unique, e.g. VB-20260101-0001), user_id (nullable for guest), receiver_name, phone, province, district, commune, street_detail, note, subtotal, shipping_fee, discount, total, payment_method (`COD` | `BANK_TRANSFER` | `ABA_PAYWAY` | `KHQR`), payment_status (`PENDING` | `PAID` | `FAILED` | `REFUNDED`), status (`PENDING` | `CONFIRMED` | `PACKING` | `SHIPPING` | `DELIVERED` | `CANCELLED`)
- **order_items**: id, order_id, variant_id, product_name_snapshot, sku_snapshot, unit_price, quantity, line_total (snapshot values so old orders never change when products change)
- **order_status_history**: id, order_id, status, note, changed_by, created_at
- **payments**: id, order_id, method, amount, status, transaction_ref, raw_response (jsonb)
- **shipping_zones**: id, province, fee, free_shipping_min, estimated_days

Add indexes on: `products.slug`, `products.category_id`, `orders.user_id`, `orders.order_no`, `order_items.order_id`, `product_variants.sku`.

## 5. API Design

Base path: `/api`. Response wrapper: `{ "success": true, "data": ..., "message": null }`. Errors: `{ "success": false, "message": "...", "errors": [...] }`.

**Public (no login)**
- `GET /api/public/categories`
- `GET /api/public/brands`
- `GET /api/public/products?category=&brand=&q=&minPrice=&maxPrice=&age=&sort=&page=&size=`
- `GET /api/public/products/{slug}`
- `POST /api/public/orders` (guest or logged-in checkout)
- `GET /api/public/orders/track?orderNo=&phone=`
- `GET /api/public/shipping/fee?province=`

**Auth**
- `POST /api/auth/register`, `POST /api/auth/login`, `POST /api/auth/refresh`, `GET /api/auth/me`

**Customer (ROLE_CUSTOMER)**
- `GET/PUT /api/me/profile`, CRUD `/api/me/addresses`, `GET /api/me/orders`, `GET /api/me/orders/{id}`, `POST /api/me/orders/{id}/cancel`

**Admin (ROLE_ADMIN)**
- CRUD `/api/admin/products`, `/api/admin/categories`, `/api/admin/brands`
- `POST /api/admin/media/upload` (Cloudinary), `DELETE /api/admin/media/{publicId}`
- `GET /api/admin/orders`, `GET /api/admin/orders/{id}`, `PATCH /api/admin/orders/{id}/status`, `PATCH /api/admin/orders/{id}/payment-status`
- `GET /api/admin/customers`
- CRUD `/api/admin/shipping-zones`
- `GET /api/admin/dashboard/summary` (today sales, orders count, low stock, top products)

## 6. Project Rules (paste into `.cursor/rules/project.mdc`)

```
You are a senior full-stack engineer building "Vimean Baby", a mother and baby e-commerce site for Cambodia.

STACK: Spring Boot 3 (Java 21), PostgreSQL + Flyway, Nuxt 3 + TypeScript, Tailwind CSS, Pinia, Cloudinary.
MONOREPO: backend/, user-frontend/, admin-frontend/. Never mix code between them.

BACKEND RULES
- Package by feature (user, catalog, cart, order, payment, shipping, media, dashboard), each with entity/repository/dto/service/controller.
- Never expose entities in controllers; use DTOs (records) and MapStruct.
- Validate all requests with Jakarta Validation. Use a GlobalExceptionHandler and the ApiResponse wrapper.
- Constructor injection only (Lombok @RequiredArgsConstructor). No field @Autowired.
- Money is BigDecimal / NUMERIC(12,2). Never float or double.
- Schema changes ONLY through new Flyway migration files. Never edit an applied migration. Set spring.jpa.hibernate.ddl-auto=validate.
- Order creation and stock decrement must be inside one @Transactional method; lock or check stock to avoid overselling.
- Copy product name, SKU, and price into order_items at order time (snapshots).
- Security: BCrypt passwords, JWT, role-based access. /api/admin/** requires ROLE_ADMIN. Never log passwords or tokens.
- Read all secrets from environment variables (DB, JWT_SECRET, CLOUDINARY_*). Never hardcode.
- Configure CORS from an env variable listing allowed frontend origins.
- Write unit tests for services (orders, stock, pricing) and use JavaDoc only where logic is non-obvious.

FRONTEND RULES
- TypeScript strict mode. Use <script setup lang="ts"> and the Composition API.
- Tailwind CSS only; mobile-first; no inline styles. Keep components small and reusable.
- All API calls go through the useApi composable (base URL from runtimeConfig). Handle loading, empty, and error states everywhere.
- user-frontend uses SSR with useSeoMeta for every public page (title, description, og:image) and lazy-loaded, optimized images.
- All user-facing text goes into i18n files (km.json, en.json). Never hardcode display text. Khmer is the default locale.
- Format currency through one utility (USD, and optional KHR).
- Store the JWT securely (httpOnly cookie preferred; otherwise a Nuxt cookie), never in plain localStorage without a reason.
- admin-frontend is SPA (ssr: false), protected by route middleware that requires an ADMIN role.

IMAGES
- Upload through the backend to Cloudinary. Store only secure_url and public_id in the DB. Use Cloudinary transformations (f_auto, q_auto, width) for thumbnails.

WORKFLOW
- Work one feature at a time. Before coding, list the files you will create or change. After coding, list how to run and test it.
- Do not add libraries outside the stack without asking.
- Keep code simple, readable, and consistent with existing patterns.
```

## 7. Phase Prompts (give these to Cursor one at a time)

**Phase 0: Scaffold**
> Read docs/MASTER_PROMPT.md. Create the monorepo folders exactly as in section 3. Initialize the Spring Boot project (Maven, Java 21, dependencies listed in section 2), a Nuxt 3 project in user-frontend and another in admin-frontend (TypeScript, Tailwind, Pinia), a docker-compose.yml with PostgreSQL 16, `.env.example` files, `.gitignore`, and a README with run instructions. Do not implement features yet.

**Phase 1: Database and catalog backend**
> Create Flyway migrations V1 and V2 for users, roles, addresses, categories, brands, products, variants, and images (section 4). Implement entities, repositories, DTOs, services, and the public catalog endpoints plus admin CRUD endpoints for categories, brands, and products. Add seed data with 3 categories and 10 sample products. Add Swagger UI.

**Phase 2: Auth**
> Implement register, login, refresh, and me with JWT and roles CUSTOMER and ADMIN. Seed one admin user from environment variables. Secure /api/admin/** with ROLE_ADMIN. Add tests for the auth service.

**Phase 3: Cloudinary media**
> Implement CloudinaryService and AdminMediaController for image upload and delete. Connect it to product images (primary image, sort order). Validate file type (jpg, png, webp) and size (max 5 MB).

**Phase 4: Customer storefront**
> In user-frontend, build the layout (header, footer, mobile nav, language switcher km/en), home page, product listing with filters and pagination, product detail page with gallery and variant selector, and category pages. Add SEO meta. Connect to the public APIs. Design palette: cream `#FFF9F0`, brand deep blue `#2F5D8A` (primary buttons with white text), soft blue `#5B9BD5`, blush `#F4A6B8` (badges only), mint `#8FD3B6`, ink `#2B3A4A`; rounded cards, large touch targets.

**Phase 5: Cart and checkout**
> Implement the cart store (guest cart in Pinia + persisted cookie, merged on login), the cart page, and checkout with address form, province-based shipping fee, and payment methods COD and bank transfer. Implement backend POST /api/public/orders with transactional stock decrement and an order number generator. Add the order success page and order tracking by order number and phone.

**Phase 6: Customer account**
> Build login, register, profile, address book, and order history pages with the auth middleware.

**Phase 7: Admin panel**
> Build admin-frontend: login, dashboard (today sales, order count, low stock), product CRUD with multi-image upload and variants, categories, brands, orders list with filters, order detail with status update and history, customers list, and shipping zones.

**Phase 8: Payments and polish**
> Add ABA PayWay and KHQR integration behind the PaymentService interface (keep COD working). Add webhook or callback handling, payment status updates, and idempotency. Then add rate limiting on auth endpoints, input sanitization, and error pages.

**Phase 9: Deployment**
> Prepare Dockerfile for backend, production configs, environment variable docs, and Railway deployment steps with three services (backend, user-frontend, admin-frontend) plus PostgreSQL. Add CORS origins, health check endpoint, and database backup notes.

## 8. Tips for Working with Cursor

1. Start each session by telling Cursor to read `docs/MASTER_PROMPT.md`.
2. Give one phase at a time and test it before continuing. Commit to Git after each phase.
3. If Cursor goes off-plan, remind it of the rules: "Follow the Project Rules; do not change the stack."
4. Review anything touching money, stock, authentication, and payments yourself. Do not accept it blindly.
5. Ask Cursor to write tests for order and stock logic.
