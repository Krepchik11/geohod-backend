#!/bin/bash
# Development Deployment Script

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_NAME="geohod-dev"

echo "🚀 Starting Development Deployment..."

# Load environment variables
if [ -f "$SCRIPT_DIR/.env.dev" ]; then
    export $(cat "$SCRIPT_DIR/.env.dev" | grep -v '^#' | xargs)
    echo "✅ Environment variables loaded"
else
    echo "❌ .env.dev file not found!"
    exit 1
fi

# Check if required volumes exist
echo "🔍 Checking required volumes..."
if ! docker volume inspect geohod_postgres_data_dev >/dev/null 2>&1; then
    echo "📦 Creating development database volume..."
    docker volume create geohod_postgres_data_dev
fi

# Build and deploy services
echo "🔨 Building and deploying development services..."
docker compose -f "$SCRIPT_DIR/docker-compose.dev.yml" -p "$PROJECT_NAME" up -d --build

# Wait for services to be healthy
echo "⏳ Waiting for services to be healthy..."
sleep 45

# Health check
echo "🏥 Performing health checks..."
if curl -f http://localhost:8081/actuator/health >/dev/null 2>&1; then
    echo "✅ Development deployment successful!"
    echo "🌐 Application available at: http://localhost:8081"
    echo "🐛 Debug port available at: localhost:5005"
    echo "🗄️  Database available at: localhost:5433"
else
    echo "❌ Health check failed!"
    echo "📋 Recent logs:"
    docker compose -f "$SCRIPT_DIR/docker-compose.dev.yml" -p "$PROJECT_NAME" logs --tail=20
    exit 1
fi

echo "🎉 Development environment is ready!"