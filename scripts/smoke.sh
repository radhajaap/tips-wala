#!/bin/sh
# API smoke test. Usage: ./scripts/smoke.sh https://your-domain  (or http://localhost:8080)
B="${1:-http://localhost:8080}"; fail=0
chk(){ code=$(curl -s -o /dev/null -w '%{http_code}' "$2"); [ "$code" = "$3" ] && echo "ok   $1" || { echo "FAIL $1 (got $code, want $3)"; fail=1; }; }
chk "market overview"  "$B/api/market/overview" 200
chk "market status"    "$B/api/market/status" 200
chk "movers"           "$B/api/market/movers?type=gainers&universe=NIFTY50" 200
chk "bad universe"     "$B/api/market/movers?type=gainers&universe=XYZ" 400
chk "faqs"             "$B/api/faqs" 200
chk "reviews"          "$B/api/reviews" 200
chk "posts"            "$B/api/posts" 200
chk "calendar"         "$B/api/calendar" 200
chk "news"             "$B/api/news" 200
chk "sitemap"          "$B/sitemap.xml" 200
chk "me needs login"   "$B/api/me" 403
chk "admin needs login" "$B/api/admin/stats" 403
code=$(curl -s -o /dev/null -w '%{http_code}' -X POST "$B/api/payments/webhook" -H 'Content-Type: application/json' -d '{}'); [ "$code" = "400" ] && echo "ok   webhook rejects unsigned" || { echo "FAIL webhook unsigned ($code)"; fail=1; }
exit $fail
