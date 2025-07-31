# Dev/Production Deployment Implementation Plan

## Implementation Overview

This document provides the complete implementation plan for separating development and production environments using the separate directories structure approach.

## 1. Directory Structure

```
geohod-backend/
├── deployments/
│   ├── prod/
│   │   ├── docker-compose.prod.yml
│   │   ├── .env.prod
│   │   ├── deploy-prod.sh
│   │   └── README.md
│   └── dev/
│       ├── docker-compose.dev.yml
│       ├── .env.dev
│       ├── deploy-dev.sh
│       └── README.md
├── scripts/
│   ├── setup-volumes-prod.sh
│   ├── setup-volumes-dev.sh
│   ├── backup-prod.sh
│   ├── backup-dev.sh
│   └── health-check.sh
├── .github/
│   └── workflows/
│       ├── deploy-prod.yml
│       └── deploy-dev.yml
├── docs/
│   └── DEPLOYMENT_SEPARATION_GUIDE.md
└── (existing files...)
```

## 2. Production Configuration Files

### 2.1 deployments/prod/docker-compose.prod.yml

```yaml
# Production Environment Docker Compose Configuration
# Project: geohod-prod

services:
  geohod-app:
    image: geohod-backend:latest
    container_name: geohod-backend-prod
    restart: unless-stopped
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-prod:5432/${POSTGRES_DB}
      - SPRING_DATASOURCE_USERNAME=${POSTGRES_USER}
      - SPRING_DATASOURCE_PASSWORD=${POSTGRES_PASSWORD}
      - GEOHOD_TELEGRAM_BOT_TOKEN=${TELEGRAM_BOT_TOKEN}
      - GEOHOD_TELEGRAM_BOT_USERNAME=${TELEGRAM_BOT_USERNAME}
      - GEOHOD_CREATED_EVENT_LINK_TEMPLATE=${GEOHOD_CREATED_EVENT_LINK_TEMPLATE}
      - GEOHOD_REVIEW_LINK_TEMPLATE=${GEOHOD_REVIEW_LINK_TEMPLATE}
      - CORS_ALLOWED_ORIGINS=${CORS_ALLOWED_ORIGINS}
      - SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE=20
      - LOGGING_LEVEL_ROOT=INFO
      - MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics
    depends_on:
      postgres-prod:
        condition: service_healthy
    networks:
      - geohod-prod-network
    healthcheck:
      test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 30s
    deploy:
      resources:
        limits:
          memory: 2G
          cpus: '1.5'
        reservations:
          memory: 1G
          cpus: '0.5'

  postgres-prod:
    image: postgres:17-alpine
    container_name: geohod-postgres-prod
    restart: unless-stopped
    environment:
      - POSTGRES_DB=${POSTGRES_DB}
      - POSTGRES_USER=${POSTGRES_USER}
      - POSTGRES_PASSWORD=${POSTGRES_PASSWORD}
      - POSTGRES_INITDB_ARGS=--auth-host=scram-sha-256
    volumes:
      - geohod_postgres_data_prod:/var/lib/postgresql/data
    ports:
      - "5432:5432"
    networks:
      - geohod-prod-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${POSTGRES_USER} -d ${POSTGRES_DB}"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 30s
    deploy:
      resources:
        limits:
          memory: 1G
          cpus: '1.0'
        reservations:
          memory: 512M
          cpus: '0.3'

volumes:
  geohod_postgres_data_prod:
    external: true

networks:
  geohod-prod-network:
    name: geohod-prod-network
    driver: bridge
```

### 2.2 deployments/prod/.env.prod

```bash
# Production Environment Variables
# WARNING: This file contains production secrets - never commit to version control

# Database Configuration
POSTGRES_DB=geohod_prod
POSTGRES_USER=geohod_prod_user
POSTGRES_PASSWORD=SECURE_PRODUCTION_PASSWORD_HERE

# Application Configuration
TELEGRAM_BOT_TOKEN=PRODUCTION_TELEGRAM_BOT_TOKEN_HERE
TELEGRAM_BOT_USERNAME=PRODUCTION_BOT_USERNAME_HERE

# CORS Configuration
CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://www.yourdomain.com

# Link Templates
GEOHOD_CREATED_EVENT_LINK_TEMPLATE=https://yourdomain.com/event/{eventId}
GEOHOD_REVIEW_LINK_TEMPLATE=https://yourdomain.com/review/{userId}
```

### 2.3 deployments/prod/deploy-prod.sh

```bash
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
```

## 3. Development Configuration Files

### 3.1 deployments/dev/docker-compose.dev.yml

```yaml
# Development Environment Docker Compose Configuration
# Project: geohod-dev

services:
  geohod-app-dev:
    build:
      context: ../../
      dockerfile: Dockerfile.dev
    image: geohod-backend:dev
    container_name: geohod-backend-dev
    restart: unless-stopped
    ports:
      - "8081:8080"
      - "5005:5005"  # Debug port
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-dev:5432/${POSTGRES_DB}
      - SPRING_DATASOURCE_USERNAME=${POSTGRES_USER}
      - SPRING_DATASOURCE_PASSWORD=${POSTGRES_PASSWORD}
      - GEOHOD_TELEGRAM_BOT_TOKEN=${TELEGRAM_BOT_TOKEN}
      - GEOHOD_TELEGRAM_BOT_USERNAME=${TELEGRAM_BOT_USERNAME}
      - GEOHOD_CREATED_EVENT_LINK_TEMPLATE=${GEOHOD_CREATED_EVENT_LINK_TEMPLATE}
      - GEOHOD_REVIEW_LINK_TEMPLATE=${GEOHOD_REVIEW_LINK_TEMPLATE}
      - CORS_ALLOWED_ORIGINS=${CORS_ALLOWED_ORIGINS}
      - SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE=5
      - LOGGING_LEVEL_ROOT=DEBUG
      - LOGGING_LEVEL_ME_GEOHOD=TRACE
      - MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=*
      - JAVA_TOOL_OPTIONS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005
    volumes:
      - ../../src:/app/src
      - ../../build.gradle:/app/build.gradle
      - ../../settings.gradle:/app/settings.gradle
      - gradle-cache-dev:/root/.gradle
    depends_on:
      postgres-dev:
        condition: service_healthy
    networks:
      - geohod-dev-network
    healthcheck:
      test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    deploy:
      resources:
        limits:
          memory: 1G
          cpus: '1.0'
        reservations:
          memory: 512M
          cpus: '0.2'

  postgres-dev:
    image: postgres:17-alpine
    container_name: geohod-postgres-dev
    restart: unless-stopped
    environment:
      - POSTGRES_DB=${POSTGRES_DB}
      - POSTGRES_USER=${POSTGRES_USER}
      - POSTGRES_PASSWORD=${POSTGRES_PASSWORD}
    volumes:
      - geohod_postgres_data_dev:/var/lib/postgresql/data
    ports:
      - "5433:5432"
    networks:
      - geohod-dev-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${POSTGRES_USER} -d ${POSTGRES_DB}"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 30s
    deploy:
      resources:
        limits:
          memory: 512M
          cpus: '0.5'
        reservations:
          memory: 256M
          cpus: '0.2'

volumes:
  geohod_postgres_data_dev:
    external: true
  gradle-cache-dev:

networks:
  geohod-dev-network:
    name: geohod-dev-network
    driver: bridge
```

### 3.2 deployments/dev/.env.dev

```bash
# Development Environment Variables
# Safe to commit - no production secrets

# Database Configuration
POSTGRES_DB=geohod_dev
POSTGRES_USER=geohod_dev_user
POSTGRES_PASSWORD=dev_password_123

# Application Configuration (Development Bot)
TELEGRAM_BOT_TOKEN=DEV_TELEGRAM_BOT_TOKEN_HERE
TELEGRAM_BOT_USERNAME=DEV_BOT_USERNAME_HERE

# CORS Configuration
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:3001,http://127.0.0.1:3000

# Link Templates
GEOHOD_CREATED_EVENT_LINK_TEMPLATE=http://localhost:3000/event/{eventId}
GEOHOD_REVIEW_LINK_TEMPLATE=http://localhost:3000/review/{userId}
```

### 3.3 deployments/dev/deploy-dev.sh

```bash
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
```

## 4. Enhanced Scripts

### 4.1 scripts/setup-volumes-prod.sh

```bash
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
```

### 4.2 scripts/setup-volumes-dev.sh

```bash
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
```

### 4.3 scripts/backup-prod.sh

```bash
#!/bin/bash
# Production Backup Script

set -e

BACKUP_DIR="./backups/prod"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_PATH="$BACKUP_DIR/$TIMESTAMP"

echo "📦 Starting production backup..."

# Create backup directory
mkdir -p "$BACKUP_PATH"

# Backup database
echo "🗄️  Backing up production database..."
docker exec geohod-postgres-prod pg_dump -U geohod_prod_user geohod_prod > "$BACKUP_PATH/database.sql"

# Backup volumes
echo "💾 Backing up production volumes..."
docker run --rm -v geohod_postgres_data_prod:/data -v "$(pwd)/$BACKUP_PATH":/backup alpine tar czf /backup/postgres_data.tar.gz -C /data .

echo "✅ Production backup completed: $BACKUP_PATH"
```

### 4.4 scripts/backup-dev.sh

```bash
#!/bin/bash
# Development Backup Script

set -e

BACKUP_DIR="./backups/dev"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_PATH="$BACKUP_DIR/$TIMESTAMP"

echo "📦 Starting development backup..."

# Create backup directory
mkdir -p "$BACKUP_PATH"

# Backup database
echo "🗄️  Backing up development database..."
docker exec geohod-postgres-dev pg_dump -U geohod_dev_user geohod_dev > "$BACKUP_PATH/database.sql"

# Backup volumes
echo "💾 Backing up development volumes..."
docker run --rm -v geohod_postgres_data_dev:/data -v "$(pwd)/$BACKUP_PATH":/backup alpine tar czf /backup/postgres_data.tar.gz -C /data .

echo "✅ Development backup completed: $BACKUP_PATH"
```

## 5. GitHub Actions Workflows

### 5.1 .github/workflows/deploy-prod.yml

```yaml
name: Deploy to Production

on:
  push:
    branches: [ main ]
  workflow_dispatch:

env:
  REGISTRY: ghcr.io
  IMAGE_NAME: ${{ github.repository }}

jobs:
  deploy-production:
    runs-on: ubuntu-latest
    environment: production
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4

    - name: Set up JDK 23
      uses: actions/setup-java@v4
      with:
        java-version: '23'
        distribution: 'temurin'

    - name: Cache Gradle packages
      uses: actions/cache@v4
      with:
        path: |
          ~/.gradle/caches
          ~/.gradle/wrapper
        key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
        restore-keys: |
          ${{ runner.os }}-gradle-

    - name: Build with Gradle
      run: ./gradlew build -x test

    - name: Build Docker image
      run: |
        docker build -f .github/Dockerfile -t geohod-backend:latest .

    - name: Save Docker image
      run: |
        docker save geohod-backend:latest | gzip > geohod-backend-latest.tar.gz

    - name: Copy files to server
      uses: appleboy/scp-action@v0.1.7
      with:
        host: ${{ vars.VPS_HOST }}
        username: ${{ secrets.VPS_USER }}
        key: ${{ secrets.VPS_SSH_KEY }}
        source: "geohod-backend-latest.tar.gz,deployments/prod/"
        target: "~/geohod-backend/"

    - name: Deploy to production
      uses: appleboy/ssh-action@v0.1.10
      with:
        host: ${{ vars.VPS_HOST }}
        username: ${{ secrets.VPS_USER }}
        key: ${{ secrets.VPS_SSH_KEY }}
        script: |
          cd ~/geohod-backend
          
          # Load Docker image
          docker load < geohod-backend-latest.tar.gz
          
          # Create .env.prod file
          cat > deployments/prod/.env.prod << EOF
          POSTGRES_DB=${{ secrets.POSTGRES_DB }}
          POSTGRES_USER=${{ secrets.POSTGRES_USER }}
          POSTGRES_PASSWORD=${{ secrets.POSTGRES_PASSWORD }}
          TELEGRAM_BOT_TOKEN=${{ secrets.TELEGRAM_BOT_TOKEN }}
          TELEGRAM_BOT_USERNAME=${{ secrets.TELEGRAM_BOT_USERNAME }}
          CORS_ALLOWED_ORIGINS=${{ vars.CORS_ALLOWED_ORIGINS }}
          GEOHOD_CREATED_EVENT_LINK_TEMPLATE=${{ vars.GEOHOD_CREATED_EVENT_LINK_TEMPLATE }}
          GEOHOD_REVIEW_LINK_TEMPLATE=${{ vars.GEOHOD_REVIEW_LINK_TEMPLATE }}
          EOF
          
          # Make scripts executable
          chmod +x deployments/prod/deploy-prod.sh
          
          # Run deployment
          ./deployments/prod/deploy-prod.sh
          
          # Cleanup
          rm geohod-backend-latest.tar.gz
```

### 5.2 .github/workflows/deploy-dev.yml

```yaml
name: Deploy to Development

on:
  push:
    branches: [ develop ]
  workflow_dispatch:

env:
  REGISTRY: ghcr.io
  IMAGE_NAME: ${{ github.repository }}

jobs:
  deploy-development:
    runs-on: ubuntu-latest
    environment: development
    
    steps:
    - name: Checkout code
      uses: actions/checkout@v4

    - name: Copy files to server
      uses: appleboy/scp-action@v0.1.7
      with:
        host: ${{ vars.VPS_HOST }}
        username: ${{ secrets.VPS_USER }}
        key: ${{ secrets.VPS_SSH_KEY }}
        source: "deployments/dev/,src/,build.gradle,settings.gradle,Dockerfile.dev,gradle/,gradlew,gradlew.bat"
        target: "~/geohod-backend-dev/"

    - name: Deploy to development
      uses: appleboy/ssh-action@v0.1.10
      with:
        host: ${{ vars.VPS_HOST }}
        username: ${{ secrets.VPS_USER }}
        key: ${{ secrets.VPS_SSH_KEY }}
        script: |
          cd ~/geohod-backend-dev
          
          # Create .env.dev file
          cat > deployments/dev/.env.dev << EOF
          POSTGRES_DB=${{ secrets.DEV_POSTGRES_DB }}
          POSTGRES_USER=${{ secrets.DEV_POSTGRES_USER }}
          POSTGRES_PASSWORD=${{ secrets.DEV_POSTGRES_PASSWORD }}
          TELEGRAM_BOT_TOKEN=${{ secrets.DEV_TELEGRAM_BOT_TOKEN }}
          TELEGRAM_BOT_USERNAME=${{ secrets.DEV_TELEGRAM_BOT_USERNAME }}
          CORS_ALLOWED_ORIGINS=${{ vars.DEV_CORS_ALLOWED_ORIGINS }}
          GEOHOD_CREATED_EVENT_LINK_TEMPLATE=${{ vars.DEV_GEOHOD_CREATED_EVENT_LINK_TEMPLATE }}
          GEOHOD_REVIEW_LINK_TEMPLATE=${{ vars.DEV_GEOHOD_REVIEW_LINK_TEMPLATE }}
          EOF
          
          # Make scripts executable
          chmod +x deployments/dev/deploy-dev.sh
          
          # Run deployment
          ./deployments/dev/deploy-dev.sh
```

## 6. Environment Management Commands

### Quick Reference Commands

```bash
# Production Environment
cd deployments/prod
./deploy-prod.sh                    # Deploy production
docker compose -p geohod-prod logs  # View production logs
docker compose -p geohod-prod ps    # Check production status
docker compose -p geohod-prod down  # Stop production

# Development Environment  
cd deployments/dev
./deploy-dev.sh                     # Deploy development
docker compose -p geohod-dev logs   # View development logs
docker compose -p geohod-dev ps     # Check development status
docker compose -p geohod-dev down   # Stop development

# Access Points
# Production: http://localhost:8080
# Development: http://localhost:8081 (Debug: 5005, DB: 5433)
```

## 7. Migration Steps from Current Setup

### Phase 1: Backup Current Data
1. Backup existing production database
2. Note current environment variables
3. Document current deployment process

### Phase 2: Create New Structure
1. Create the deployments directory structure
2. Create production configuration files
3. Create development configuration files
4. Create deployment scripts

### Phase 3: Test Development Environment
1. Deploy development environment on new ports
2. Verify all functionality works
3. Test database migrations
4. Validate Telegram integration

### Phase 4: Migrate Production
1. Schedule maintenance window
2. Stop current production services
3. Migrate data to new volume structure
4. Deploy using new production configuration
5. Verify all services are working

### Phase 5: Update CI/CD
1. Create GitHub environments
2. Update secrets and variables
3. Test deployment pipelines
4. Document new processes

## 8. Security Considerations

### Production Security
- Never commit `.env.prod` to version control
- Use strong, unique passwords for production database
- Restrict production Telegram bot to production domain
- Limit actuator endpoints exposure
- Use resource limits to prevent DoS

### Development Security
- Use separate development Telegram bot
- Development passwords can be simpler but still secure
- Allow broader CORS origins for development
- Enable all actuator endpoints for debugging
- Use separate development database

## 9. Monitoring and Maintenance

### Health Monitoring
- Production: Monitor http://localhost:8080/actuator/health
- Development: Monitor http://localhost:8081/actuator/health
- Set up automated alerts for production failures

### Log Management
- Production logs: `docker compose -p geohod-prod logs`
- Development logs: `docker compose -p geohod-dev logs`
- Consider log aggregation for production

### Backup Strategy
- Production: Daily automated backups
- Development: Weekly backups or on-demand
- Test restore procedures regularly

## 10. Troubleshooting Guide

### Common Issues

**Port Conflicts**
- Ensure production uses 8080/5432 and development uses 8081/5433
- Check no other services are using these ports

**Database Connection Issues**
- Verify environment variables are correctly set
- Check database container health status
- Ensure proper network connectivity

**Resource Issues**
- Monitor memory and CPU usage
- Adjust resource limits if needed
- Check for memory leaks in development

**Volume Issues**
- Ensure volumes are created before deployment
- Check volume permissions
- Verify backup/restore procedures

This implementation provides complete separation while maintaining operational simplicity and following industry best practices for multi-environment deployment.