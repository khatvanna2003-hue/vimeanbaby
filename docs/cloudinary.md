# Cloudinary (local + production — same cloud)

Set on both `backend/.env` (local) and Railway / `.env.production`:

```
CLOUDINARY_CLOUD_NAME=vimeanbaby
CLOUDINARY_API_KEY=<from Cloudinary console>
CLOUDINARY_API_SECRET=<from Cloudinary console>
```

Or: `CLOUDINARY_URL=cloudinary://API_KEY:API_SECRET@vimeanbaby`

Database setup (local Postgres vs Neon) is in `docs/environments.md`.
Do not commit real secrets. Rotate keys if they were ever shared in chat/git.
