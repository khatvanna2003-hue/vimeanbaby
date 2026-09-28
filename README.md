# Vimean Baby

Mother and baby e-commerce platform for Cambodia.

## Monorepo

| Folder | Stack | Port |
|---|---|---|
| `backend/` | Java 21, Spring Boot 3, PostgreSQL (`vimeanbaby_db`), Flyway | 8080 |
| `user-frontend/` | Nuxt 3 (SSR), TypeScript, Tailwind, Pinia, i18n | 3000 |
| `admin-frontend/` | Nuxt 3 (SPA), TypeScript, Tailwind, Pinia | 3001 |

See `docs/MASTER_PROMPT.md` for full product and phase plan.

## Prerequisites

- Java 21+
- Maven 3.9+ (or use Maven Wrapper once added)
- Node.js 20+
- Docker Desktop (for PostgreSQL)

## Quick start

### 1. Database

```bash
docker compose up -d postgres
```

### 2. Backend

```bash
cd backend
cp .env.example .env
# Fill CLOUDINARY_CLOUD_NAME / API_KEY / API_SECRET (same for local + production)
mvn spring-boot:run
```

`spring-dotenv` loads `backend/.env` automatically.
API: http://localhost:8080  
Swagger UI: http://localhost:8080/swagger-ui.html  
Health: http://localhost:8080/actuator/health

### 3. Customer storefront

```bash
cd user-frontend
cp .env.example .env
npm install
npm run dev
```

http://localhost:3000

### 4. Admin panel

```bash
cd admin-frontend
cp .env.example .env
npm install
npm run dev
```

http://localhost:3001

### Full stack via Docker (Postgres + backend)

```bash
docker compose up --build
```

### Deploy backend to Railway

See **`docs/railway-backend.md`**.

1. Root Directory = `backend`
2. Builder = Dockerfile
3. Set Neon `DB_*`, Cloudinary, `JWT_SECRET`, `SPRING_PROFILES_ACTIVE=prod`
4. Generate domain → check `/actuator/health`

## Phase status

- [x] Phase 0: Scaffold
- [x] Phase 1: Database and catalog backend
- [ ] Phase 2: Auth (JWT)
- [x] Phase 3: Cloudinary media
- [x] Phase 4: Customer storefront (MVP)
- [ ] Phase 5: Cart and checkout
- [ ] Phase 6: Customer account
- [ ] Phase 7: Admin panel
- [ ] Phase 8: Payments and polish
- [ ] Phase 9: Deployment (Railway)

## Notes

- Khmer is the default storefront locale.
- Money is stored as `NUMERIC(12,2)` / `BigDecimal` — never float.
- Schema changes only via Flyway migrations.
- **Database:** local = Docker/pgAdmin Postgres; production = **Neon**. See `docs/environments.md`.
- **Images:** all uploads go through `POST /api/admin/media/upload` → Cloudinary (same cloud for local + production). Set `CLOUDINARY_*` in `backend/.env` and on Railway. Never commit secrets.
