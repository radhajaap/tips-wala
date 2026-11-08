#!/bin/sh
# Daily DB backup. Cron example: 0 3 * * * cd /opt/tipswala && ./scripts/backup.sh
set -e
mkdir -p backups
docker compose exec -T db pg_dump -U marketpulse marketpulse | gzip > "backups/db-$(date +%F).sql.gz"
find backups -name 'db-*.sql.gz' -mtime +14 -delete
# Copy backups/ to off-server storage as well (object storage / another machine).
