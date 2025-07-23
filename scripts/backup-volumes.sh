#!/bin/bash

# Script to backup Docker volumes for Geohod application
# Usage: ./backup-volumes.sh [backup_directory]

set -e

BACKUP_DIR=${1:-./backups}
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_PATH="${BACKUP_DIR}/${TIMESTAMP}"

echo "Creating backup of Geohod application volumes..."

# Create backup directory
mkdir -p "${BACKUP_PATH}"

# Backup postgres data
echo "Backing up PostgreSQL data..."
docker run --rm \
  -v geohod_postgres_data:/data:ro \
  -v "${BACKUP_PATH}:/backup" \
  alpine:latest \
  tar czf /backup/geohod_postgres_data.tar.gz -C /data .

# Create backup metadata
cat > "${BACKUP_PATH}/backup_info.txt" << EOF
Backup created: $(date)
Volumes backed up:
- geohod_postgres_data

To restore:
1. Stop services: docker compose down
2. Run restore script: ./scripts/restore-volumes.sh ${BACKUP_PATH}
EOF

echo "✅ Backup completed successfully!"
echo "Backup location: ${BACKUP_PATH}"
ls -la "${BACKUP_PATH}"
