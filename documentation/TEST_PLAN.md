# Test plan (tick each before launch). Use Razorpay TEST keys first.
Automated: `mvn verify` (unit tests) and `./scripts/smoke.sh https://YOUR-DOMAIN` (API smoke test).
## Website
- [ ] Home loads; nav + mobile drawer; light/dark; cards show LIVE/DELAYED/UNAVAILABLE honestly
- [ ] Gainers/Losers: universe filter, search, sort, row click → chart; closed-market label
- [ ] Chart: timeframes, candle/line, MA, RSI, MACD, zoom, fullscreen, history note
- [ ] Register (bad inputs rejected), email verify link, login, logout, forgot/reset password (link works once)
- [ ] Free demo: once per user; ends after 10 business days; premium post opens during demo, 403 after
- [ ] Payment (test mode): order → pay → membership active; close tab right after paying → webhook activates; replayed verify does not double-extend
- [ ] Refund: request inside window OK, outside window rejected, admin approve/reject/mark refunded, emails sent, membership ends on REFUNDED
- [ ] Reviews: submit with photo → pending → approve in admin → visible; edit; delete; verified badge only for paid users
- [ ] FAQ, contact form (email arrives, honeypot field blocks bots), legal pages, footer SEBI line
- [ ] Posts: create text/image/video, premium gate, likes, bookmark, share links, comments (if enabled), SEO tags
- [ ] Calendar/News empty states and added items; watchlist add/remove/search
## Admin
- [ ] Only admin roles can open /admin; Support cannot see payments/revenue; Content cannot change roles
- [ ] Every admin change appears in Audit log; restricted post categories refused while RESTRICTED_CONTENT_ENABLED=false
## Android
- [ ] Install APK; login; back button; deep link https://DOMAIN/post.html?slug=…; offline notice; payment; push received and deep link opens; image upload via file chooser
## Backend/ops
- [ ] WebSocket reconnects after backend restart; backups restore; HTTPS grade; rate-limit returns 429 on abuse
