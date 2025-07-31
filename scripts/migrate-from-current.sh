#!/bin/bash
# Migration Script from Current Setup to Dev/Prod Separation

set -e

echo "🔄 Starting migration from current setup to dev/prod separation..."

# Check if current setup exists
if [ ! -f "docker-compose.yml" ] || [ ! -f ".env" ]; then
    echo "❌ Current setup files not found. Looking for docker-compose.yml and .env"
    exit 1
fi

echo "✅ Found current setup files"

# Backup current setup
echo "📦 Creating backup of current setup..."
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_DIR="./migration-backup-$TIMESTAMP"
mkdir -p "$BACKUP_DIR"

cp docker-compose.yml "$BACKUP_DIR/"
cp .env "$BACKUP_DIR/"
cp -r scripts/ "$BACKUP_DIR/" 2>/dev/null || true

echo "✅ Backup created at $BACKUP_DIR"

# Stop current services
echo "⏹️  Stopping current services..."
docker compose down 2>/dev/null || true

# Create new volumes
echo "📦 Creating new volumes for dev/prod separation..."
./scripts/setup-volumes-prod.sh
./scripts/setup-volumes-dev.sh

# Migrate production data if it exists
echo "🚚 Migrating production data..."
if docker volume inspect geohod_postgres_data >/dev/null 2>&1; then
    echo "📦 Migrating existing production data to new volume..."
    
    # Create temporary container to copy data
    docker run --rm -v geohod_postgres_data:/old-data -v geohod_postgres_data_prod:/new-data alpine sh -c "
        echo 'Copying data from old volume to new volume...'
        cd /old-data && tar cf - . | (cd /new-data && tar xf -)
        echo 'Data migration completed'
    "
    
    echo "✅ Production data migrated"
else
    echo "ℹ️  No existing production data found, new volumes will be initialized on first run"
fi

# Create production environment file
echo "📄 Creating production environment file..."
if [ -f ".env" ]; then
    # Copy and adapt current .env for production
    cp .env deployments/prod/.env.prod
    echo "✅ Production environment file created from current .env"
    echo "⚠️  IMPORTANT: Review and update deployments/prod/.env.prod with production values"
else
    cp deployments/prod/.env.prod.template deployments/prod/.env.prod
    echo "📄 Created production environment file from template"
    echo "⚠️  IMPORTANT: Update deployments/prod/.env.prod with your production values"
fi

# Show next steps
echo ""
echo "🎉 Migration preparation completed!"
echo ""
echo "Next steps:"
echo "1. Review and update deployments/prod/.env.prod with production values"
echo "2. Review and update deployments/dev/.env.dev if needed"
echo "3. Test development environment: cd deployments/dev && ./deploy-dev.sh"
echo "4. Deploy production environment: cd deployments/prod && ./deploy-prod.sh"
echo ""
echo "Backup of current setup is available at: $BACKUP_DIR"
echo ""
echo "⚠️  IMPORTANT: Test the new setup thoroughly before switching production traffic!"