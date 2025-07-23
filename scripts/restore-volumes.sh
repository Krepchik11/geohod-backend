#!/bin/bash

# Script to restore Docker volumes for Geohod application
# Usage: ./restore-volumes.sh [backup_directory]

set -e

if [ $# -eq 0 ]; then
    echo "Usage: $0 <backup_directory>"
    echo "Example: $0 ./backups/20240723_143000"
    exit 1
fi

BACKUP_PATH="$1"

if [ ! -d "${BACKUP_PATH}" ]; then
    echo "Error: Backup directory ${BACKUP_PATH} does not exist"
    exit 1
fi

echo "Restoring Geohod application volumes from backup..."
echo "Backup source: ${BACKUP_PATH}"

# Stop services if running
echo "Stopping services..."
docker compose down || true

# Restore postgres data
if [ -f "${BACKUP_PATH}/geohod_postgres_data.tar.gz" ]; then
    echo "Restoring PostgreSQL data..."
    docker run --rm \
      -v geohod_postgres_data:/data \
      -v "${BACKUP_PATH}:/backup" \
      alpine:latest \
      sh -c "rm -rf /data/* && tar xzf /backup/geohod_postgres_data.tar.gz -C /data"
else
    echo "Warning: PostgreSQL backup not found, skipping..."
fi

echo "✅ Restore completed successfully!"
echo "You can now start the services with: docker compose up -d"
