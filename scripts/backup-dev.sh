#!/bin/bash
# Development Backup Script

set -e

BACKUP_DIR="./backups/dev"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_PATH="$BACKUP_DIR/$TIMESTAMP"

echo "📦 Starting development backup..."

# Create backup directory
mkdir -p "$BACKUP_PATH"

# Backup database
echo "🗄️  Backing up development database..."
docker exec geohod-postgres-dev pg_dump -U geohod_dev_user geohod_dev > "$BACKUP_PATH/database.sql"

# Backup volumes
echo "💾 Backing up development volumes..."
docker run --rm -v geohod_postgres_data_dev:/data -v "$(pwd)/$BACKUP_PATH":/backup alpine tar czf /backup/postgres_data.tar.gz -C /data .

echo "✅ Development backup completed: $BACKUP_PATH"