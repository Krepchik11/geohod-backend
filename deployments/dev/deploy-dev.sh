#!/bin/bash
# Development Deployment Script (CI/CD Image Pull)
# Podman-only version

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_NAME="geohod-dev"

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

echo "🚀 Starting Development Deployment (CI/CD Image Pull)..."

# Load environment variables
if [ -f "$SCRIPT_DIR/.env.dev" ]; then
    export $(cat "$SCRIPT_DIR/.env.dev" | grep -v '^#' | xargs)
    echo "✅ Environment variables loaded"
    
    # Validate IMAGE_TAG is set
    if [ -z "$IMAGE_TAG" ]; then
        echo "⚠️  IMAGE_TAG not set, using default: geohod-backend:dev-latest"
        export IMAGE_TAG="geohod-backend:dev-latest"
    else
        echo "🐳 Using image tag: $IMAGE_TAG"
    fi
else
    echo "❌ .env.dev file not found!"
    exit 1
fi

# Check if required volumes exist
echo "🔍 Checking required volumes..."
if ! $CONTAINER_RUNTIME volume inspect geohod_postgres_data_dev >/dev/null 2>&1; then
    echo "📦 Creating development database volume..."
    $CONTAINER_RUNTIME volume create geohod_postgres_data_dev
fi

# Determine which compose file to use
COMPOSE_FILE="$SCRIPT_DIR/podman-pod.dev.yml"
echo "🔧 Using pod-based configuration: $COMPOSE_FILE"

# Remove existing containers to ensure fresh deployment
echo "🧹 Cleaning up existing containers..."
$COMPOSE_COMMAND -f "$COMPOSE_FILE" -p "$PROJECT_NAME" down

# Deploy services (no build needed)
echo "🚀 Deploying development services..."
$COMPOSE_COMMAND -f "$COMPOSE_FILE" -p "$PROJECT_NAME" up -d

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
    $COMPOSE_COMMAND -f "$COMPOSE_FILE" -p "$PROJECT_NAME" logs --tail=20
    exit 1
fi

echo "🎉 Development environment is ready!"
echo "🔧 Deployed with $CONTAINER_RUNTIME using $COMPOSE_FILE"