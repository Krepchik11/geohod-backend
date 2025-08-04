#!/bin/bash
set -e

ENVIRONMENT=${1:-"prod"}
HEALTHCHECK_TIMEOUT=${2:-30}

echo "🏥 Performing health check for $ENVIRONMENT environment..."

case $ENVIRONMENT in
  "prod")
    PORT=8080
    PROJECT_NAME="geohod-prod"
    ;;
  "dev")
    PORT=8081
    PROJECT_NAME="geohod-dev"
    ;;
  *)
    echo "❌ Invalid environment. Use 'prod' or 'dev'"
    exit 1
    ;;
esac

# Wait for services to be ready
echo "⏳ Waiting for services to be ready (max $HEALTHCHECK_TIMEOUT seconds)..."
SECONDS=0
while [ $SECONDS -lt $HEALTHCHECK_TIMEOUT ]; do
  if curl -f http://localhost:$PORT/actuator/health >/dev/null 2>&1; then
    echo "✅ $ENVIRONMENT environment is healthy!"
    echo "🌐 Application available at: http://localhost:$PORT"
    
    # Show service status
    echo "📋 Service status:"
    podman-compose -p $PROJECT_NAME ps
    
    exit 0
  fi
  sleep 5
  echo "⏳ Still waiting... ($SECONDS seconds elapsed)"
done

echo "❌ Health check failed after $HEALTHCHECK_TIMEOUT seconds!"
echo "📋 Recent logs:"
podman-compose -p $PROJECT_NAME logs --tail=20
exit 1