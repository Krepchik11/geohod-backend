#!/bin/bash
# Production Volume Setup Script

set -e

echo "📦 Setting up production volumes..."

# Create production database volume
if ! docker volume inspect geohod_postgres_data_prod >/dev/null 2>&1; then
    docker volume create geohod_postgres_data_prod
    echo "✅ Created geohod_postgres_data_prod volume"
else
    echo "ℹ️  Production database volume already exists"
fi

echo "🎉 Production volumes setup complete!"