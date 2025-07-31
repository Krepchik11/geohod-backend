#!/bin/bash
# Production Backup Script

set -e

BACKUP_DIR="./backups/prod"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_PATH="$BACKUP_DIR/$TIMESTAMP"

echo "📦 Starting production backup..."

# Create backup directory
mkdir -p "$BACKUP_PATH"

# Backup database
echo "🗄️  Backing up production database..."
docker exec geohod-postgres-prod pg_dump -U geohod_prod_user geohod_prod > "$BACKUP_PATH/database.sql"

# Backup volumes
echo "💾 Backing up production volumes..."
docker run --rm -v geohod_postgres_data_prod:/data -v "$(pwd)/$BACKUP_PATH":/backup alpine tar czf /backup/postgres_data.tar.gz -C /data .

echo "✅ Production backup completed: $BACKUP_PATH"