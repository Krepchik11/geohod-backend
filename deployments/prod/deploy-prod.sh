#!/bin/bash
# Production Deployment Script

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_NAME="geohod-prod"

echo "🚀 Starting Production Deployment..."

# Load environment variables
if [ -f "$SCRIPT_DIR/.env.prod" ]; then
    export $(cat "$SCRIPT_DIR/.env.prod" | grep -v '^#' | xargs)
    echo "✅ Environment variables loaded"
else
    echo "❌ .env.prod file not found!"
    exit 1
fi

# Check if required volumes exist
echo "🔍 Checking required volumes..."
if ! docker volume inspect geohod_postgres_data_prod >/dev/null 2>&1; then
    echo "📦 Creating production database volume..."
    docker volume create geohod_postgres_data_prod
fi

# Pull latest images
echo "📥 Pulling latest images..."
docker compose -f "$SCRIPT_DIR/docker-compose.prod.yml" -p "$PROJECT_NAME" pull

# Deploy services
echo "🚀 Deploying production services..."
docker compose -f "$SCRIPT_DIR/docker-compose.prod.yml" -p "$PROJECT_NAME" up -d

# Wait for services to be healthy
echo "⏳ Waiting for services to be healthy..."
sleep 30

# Health check
echo "🏥 Performing health checks..."
if curl -f http://localhost:8080/actuator/health >/dev/null 2>&1; then
    echo "✅ Production deployment successful!"
    echo "🌐 Application available at: http://localhost:8080"
else
    echo "❌ Health check failed!"
    echo "📋 Recent logs:"
    docker compose -f "$SCRIPT_DIR/docker-compose.prod.yml" -p "$PROJECT_NAME" logs --tail=20
    exit 1
fi

echo "🎉 Production environment is ready!"