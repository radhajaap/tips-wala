# Tips Wala – Stock Market Info Platform (Website + Android)

Phases 1-3 (foundation, market data + website wiring, content/legal/support). Status: **not yet compiled or run** – run `mvn verify` locally first.

## Run backend
1. Start PostgreSQL + Redis, copy `.env.example` to `.env`, export the variables.
2. `cd backend && mvn spring-boot:run` (Flyway creates the schema).

## Website
Open `frontend/index.html` (static; API wiring is next phase).

## Android (Capacitor)
`npm install && npx cap add android && npm run apk:debug`
Release signing: keep the keystore + passwords OUTSIDE git (see `.gitignore`).

## Done in Phase 1
Register/login (BCrypt + JWT), RBAC skeleton, server-side plan prices, Razorpay order + HMAC signature verification, 10-business-day free demo (one per user), market API that never fakes prices, Flyway schema, CI.

## Also done (Phases 2-3)
Market service (cache, dedupe, stale detection, WebSocket `/ws/market`), BTC provider, movers API, website wired to API (live cards, login/register, free demo, Razorpay checkout, FAQ, reviews, contact), content APIs (posts with premium gate + compliance switch, FAQ, moderated reviews, contact inbox), 5 legal pages (drafts), rate limiting, first Super Admin from env.
Open `frontend/index.html?demo=1` for the offline design demo. Set `API_BASE` in `frontend/js/config.js`.

## Still to build
Admin dashboard UI, refunds module, charts with real history, email/OTP, push notifications, uploads, post pages UI, user dashboard.

## Old list
Market-data provider + WebSocket, gainers/losers, charts, admin CMS, refunds, reviews/FAQ/contact, legal pages, push notifications, email/OTP verification.

## Legal
Platform is NOT SEBI registered: no buy/sell calls, targets or stop-loss in paid content. Get legal text reviewed by a securities lawyer.

## Branding
App name: **Tips Wala**, package ID `com.tipswala.app`. Logo: `frontend/assets/logo.svg`. Source art for icon/splash is in `resources/` (convert to `icon.png` 1024px and `splash.png` 2732px, then `npm run assets`).
