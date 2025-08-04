#!/bin/bash
# Production Deployment Script
# Podman-only version

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_NAME="geohod-prod"

# Check if Podman is installed
if ! command -v podman &> /dev/null; then
    echo "❌ Podman not found. Please install Podman."
    exit 1
fi

# Check if podman-compose is installed
if ! command -v podman-compose &> /dev/null; then
    echo "❌ podman-compose not found. Please install podman-compose."
    exit 1
fi

CONTAINER_RUNTIME="podman"
COMPOSE_COMMAND="podman-compose"
echo "🐳 Using Podman as container runtime"

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
if ! $CONTAINER_RUNTIME volume inspect geohod_postgres_data_prod >/dev/null 2>&1; then
    echo "📦 Creating production database volume..."
    $CONTAINER_RUNTIME volume create geohod_postgres_data_prod
fi

COMPOSE_FILE="$SCRIPT_DIR/podman-pod.prod.yml"
echo "🔧 Using pod-based configuration: $COMPOSE_FILE"

# Pull latest images
echo "📥 Pulling latest images..."
$COMPOSE_COMMAND -f "$COMPOSE_FILE" -p "$PROJECT_NAME" pull

# Deploy services
echo "🚀 Deploying production services..."
$COMPOSE_COMMAND -f "$COMPOSE_FILE" -p "$PROJECT_NAME" up -d

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
    $COMPOSE_COMMAND -f "$COMPOSE_FILE" -p "$PROJECT_NAME" logs --tail=20
    exit 1
fi

echo "🎉 Production environment is ready!"
echo "🔧 Deployed with $CONTAINER_RUNTIME using $COMPOSE_FILE"