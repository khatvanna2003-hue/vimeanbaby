# Environments — Database & media

| | Local | Production |
|---|---|---|
| Profile | `dev` (`SPRING_PROFILES_ACTIVE=dev`) | `prod` |
| Database | Docker Postgres / pgAdmin → `vimeanbaby_db` on `localhost:5432` | **Neon** Postgres |
| Env file | `backend/.env` | `backend/.env.production` or Railway Variables |
| Images | **Cloudinary** `vimeanbaby` (same cloud) | **Cloudinary** `vimeanbaby` (same cloud) |

## Local database

```bash
docker compose up -d postgres
# pgAdmin → localhost:5432 / postgres / postgres123 / vimeanbaby_db
cd backend && .\mvnw.cmd spring-boot:run
```

`backend/.env` keeps `DB_URL=jdbc:postgresql://localhost:5432/vimeanbaby_db`.

## Production database (Neon)

Neon gives a URI like:

`postgresql://USER:PASSWORD@HOST/neondb?sslmode=require&channel_binding=require`

Spring needs **JDBC**:

```text
DB_URL=jdbc:postgresql://HOST/neondb?sslmode=require
DB_USERNAME=USER
DB_PASSWORD=PASSWORD
SPRING_PROFILES_ACTIVE=prod
```

Copy `backend/.env.production.example` → `.env.production` (gitignored), or set the same keys on Railway.

On first deploy, Flyway runs migrations against Neon automatically when the app starts with `prod`.

## Railway (backend Dockerfile)

See **`docs/railway-backend.md`**.

Short version: Root Directory = `backend`, Builder = Dockerfile, set Neon + Cloudinary + JWT env vars, generate a public domain, health = `/actuator/health`.

## Shared Cloudinary

Set the same `CLOUDINARY_CLOUD_NAME` / `API_KEY` / `API_SECRET` in both local `.env` and production. Uploads go to one Media Library for local + production.
