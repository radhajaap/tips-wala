# Deployment (single VPS, Docker Compose)
1. **Server:** Ubuntu VPS (2 GB RAM min), install Docker + Compose. Point your domain's DNS A record to the server IP.
2. **Code:** `git clone` the repo to `/opt/tipswala`. Copy `.env.example` to `.env` and fill every value (strong `DB_PASSWORD`, 32+ char `JWT_SECRET`, `DOMAIN`, `PUBLIC_URL=https://DOMAIN`, `CORS_ORIGINS=https://DOMAIN,capacitor://localhost,https://localhost`, `ADMIN_EMAIL`, `ADMIN_PASSWORD` 12+ chars).
3. **Frontend config:** set `window.API_BASE = "https://DOMAIN"` in `frontend/js/config.js` (needed for the Android app; same origin works for the website). Replace `YOUR-DOMAIN` in `frontend/sitemap.xml` and `robots.txt`.
4. **Start:** `docker compose up -d --build`. Caddy gets the HTTPS certificate automatically. Check `https://DOMAIN/actuator/health` is not exposed publicly (only `/api` and `/ws` are proxied).
5. **First admin:** log in at `https://DOMAIN/admin/` with `ADMIN_EMAIL` / `ADMIN_PASSWORD`; then remove `ADMIN_PASSWORD` from `.env`.
6. **Razorpay:** put live `RAZORPAY_KEY_ID/SECRET` in `.env` (test keys first). Complete Razorpay KYC. Add a webhook for `payment.captured` as a safety net (not built yet) so a closed browser tab never loses a paid order.
7. **Email:** set `MAIL_*` (any SMTP, e.g. your provider). Without it, verification/reset/receipt emails are silently skipped.
8. **Push:** create a Firebase project, download the service-account JSON to the server, set `FIREBASE_CREDENTIALS_FILE` (mount it into the backend container).
9. **Backups:** cron `scripts/backup.sh` daily and copy backups off the server. Test a restore.
10. **Updates:** `git pull && docker compose up -d --build`. Flyway applies new migrations automatically.
Costs (indicative, ₹/month): VPS 500–1,500 · domain ~100 · email/SMS provider usage-based · market-data licence: **the biggest item**, get quotes from NSE/MCX-authorized vendors.
