# Deploy backend on Railway (Dockerfile + Neon + Cloudinary)

## 1. Push code to GitHub

Ensure `backend/Dockerfile`, `backend/railway.toml`, and migrations are committed.
Do **not** commit `.env` or `.env.production`.

## 2. Create Railway project

1. [railway.app](https://railway.app) → New Project → Deploy from GitHub repo  
2. Select this repo  
3. Add a service → choose the repo again if needed  
4. **Settings → Root Directory** = `backend`  
5. **Settings → Builder** = Dockerfile (or leave auto; `railway.toml` sets it)

## 3. Variables (Variables tab)

Copy from `backend/.env.production.example` / your Neon + Cloudinary values:

| Variable | Example |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `DB_URL` | `jdbc:postgresql://ep-….neon.tech/neondb?sslmode=require` |
| `DB_USERNAME` | `neondb_owner` |
| `DB_PASSWORD` | *(Neon password)* |
| `JWT_SECRET` | *(long random string ≥ 32 chars)* |
| `CORS_ALLOWED_ORIGINS` | `https://vimeanbaby.vercel.app,https://vimeanbaby-dashboard.vercel.app` |
| `CLOUDINARY_CLOUD_NAME` | `vimeanbaby` |
| `CLOUDINARY_API_KEY` | *(from Cloudinary)* |
| `CLOUDINARY_API_SECRET` | *(from Cloudinary)* |
| `ADMIN_EMAIL` | `admin@vimeanbaby.com` |
| `ADMIN_PASSWORD` | *(strong password)* |
| `ADMIN_FULL_NAME` | `Vimean Baby Admin` |

Railway injects `PORT` automatically — do not hardcode `SERVER_PORT` unless you need to.

## 4. Networking

Settings → Networking → **Generate Domain**  
Health check path: `/actuator/health` (already in `railway.toml`).

## 5. First boot

Flyway runs against Neon on startup. Then open:

- `https://<your-domain>/` → API info  
- `https://<your-domain>/actuator/health` → `{"status":"UP"}`  
- `https://<your-domain>/swagger-ui.html` → docs  

## 6. Local Docker smoke test (optional)

```bash
cd backend
docker build -t vimeanbaby-api .
docker run --rm -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e PORT=8080 \
  -e DB_URL="jdbc:postgresql://…" \
  -e DB_USERNAME=neondb_owner \
  -e DB_PASSWORD=… \
  -e JWT_SECRET=change-me-to-a-long-random-secret-at-least-256-bits-long \
  -e CLOUDINARY_CLOUD_NAME=vimeanbaby \
  -e CLOUDINARY_API_KEY=… \
  -e CLOUDINARY_API_SECRET=… \
  vimeanbaby-api
```

## Notes

- Database = **Neon** (external). No Railway Postgres needed for this setup.  
- Images = **Cloudinary** (same cloud as local).  
- After frontend URLs exist, update `CORS_ALLOWED_ORIGINS` and redeploy.

Current frontends:
- Store: https://vimeanbaby.vercel.app  
- Admin: https://vimeanbaby-dashboard.vercel.app  

On Vercel, set `NUXT_PUBLIC_API_BASE=https://<your-railway-backend-domain>/api` for both apps.
