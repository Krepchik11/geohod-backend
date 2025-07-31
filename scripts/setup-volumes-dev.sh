#!/bin/bash
# Development Volume Setup Script

set -e

echo "📦 Setting up development volumes..."

# Create development database volume
if ! docker volume inspect geohod_postgres_data_dev >/dev/null 2>&1; then
    docker volume create geohod_postgres_data_dev
    echo "✅ Created geohod_postgres_data_dev volume"
else
    echo "ℹ️  Development database volume already exists"
fi

echo "🎉 Development volumes setup complete!"