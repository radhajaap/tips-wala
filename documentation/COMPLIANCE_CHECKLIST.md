# Security + legal checklist (tick before launch)
## Security
- [ ] HTTPS everywhere (Caddy), HSTS on
- [ ] `JWT_SECRET`, DB and admin passwords strong; `.env` never committed; `ADMIN_PASSWORD` removed after first login
- [ ] Razorpay live keys only on server; signature verified (done); add webhook (todo)
- [ ] Admin accounts use unique passwords; roles reviewed (Super Admin only for money/security)
- [ ] Daily DB backups copied off-server; restore tested
- [ ] Rate limiting is per-instance: move to Redis/proxy level if you run more than one backend
- [ ] Add CSP header and move inline scripts to files (todo hardening)
- [ ] JWT is kept in browser storage: keep XSS risk low (all user text is escaped; post HTML is sanitised)
- [ ] File upload endpoints are NOT built yet; when added: type/size checks, virus scan, private bucket
- [ ] Penetration/security review before accepting real payments
## Legal (India)
- [ ] Securities-law professional reviews every legal page, the brand name "Tips Wala", and the content plan
- [ ] No buy/sell calls, targets, stop-loss, or personal portfolio advice unless SEBI registration is in place (`RESTRICTED_CONTENT_ENABLED=false` until then)
- [ ] Refund policy window/eligibility filled in; same values in `REFUND_WINDOW_DAYS` and shown before payment
- [ ] Governing law/jurisdiction filled in Terms
- [ ] Market-data licence covers redistribution to your users
- [ ] Review Razorpay, Play Store and DPDP Act (data deletion, consent, grievance contact) requirements
- [ ] GST/invoicing: receipts are not tax invoices; confirm GST registration needs with a CA
