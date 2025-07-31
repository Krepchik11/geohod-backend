# Geohod Backend Deployment Separation Guide

This guide provides comprehensive instructions for setting up and managing separate development and production environments for the Geohod backend application.

## Overview

The deployment separation implementation provides:

- **Complete Isolation**: Separate Docker networks, volumes, and containers
- **Port Separation**: Different ports for each environment (8080/5432 for prod, 8081/5433 for dev)
- **Environment-Specific Configuration**: Separate .env files and Docker Compose configurations
- **Resource Management**: Resource limits to prevent one environment from affecting the other
- **CI/CD Integration**: Automated deployment workflows for both environments

## Directory Structure

```
geohod-backend/
├── deployments/
│   ├── prod/
│   │   ├── docker-compose.prod.yml
│   │   ├── .env.prod.template
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
│   └── migrate-from-current.sh
├── .github/
│   └── workflows/
│       ├── deploy-prod.yml
│       └── deploy-dev.yml
└── docs/
    └── DEPLOYMENT_SEPARATION_GUIDE.md
```

## Environment Details

### Production Environment

- **Application Port**: 8080
- **Database Port**: 5432
- **Container Names**: 
  - Application: `geohod-backend-prod`
  - Database: `geohod-postgres-prod`
- **Docker Network**: `geohod-prod-network`
- **Docker Volume**: `geohod_postgres_data_prod`
- **Resource Limits**: 2GB RAM, 1.5 CPUs

### Development Environment

- **Application Port**: 8081
- **Debug Port**: 5005
- **Database Port**: 5433
- **Container Names**: 
  - Application: `geohod-backend-dev`
  - Database: `geohod-postgres-dev`
- **Docker Network**: `geohod-dev-network`
- **Docker Volume**: `geohod_postgres_data_dev`
- **Resource Limits**: 1GB RAM, 1.0 CPUs

## Initial Setup

### 1. Create Required Volumes

```bash
# Create production volumes
./scripts/setup-volumes-prod.sh

# Create development volumes
./scripts/setup-volumes-dev.sh
```

### 2. Configure Environment Variables

**Production:**
```bash
# Copy template and fill in values
cp deployments/prod/.env.prod.template deployments/prod/.env.prod
# Edit deployments/prod/.env.prod with production values
```

**Development:**
```bash
# Development .env.dev is already configured with safe defaults
# Edit if needed: deployments/dev/.env.dev
```

## Deployment Process

### Production Deployment

```bash
cd deployments/prod
chmod +x deploy-prod.sh
./deploy-prod.sh
```

### Development Deployment

```bash
cd deployments/dev
chmod +x deploy-dev.sh
./deploy-dev.sh
```

## Migration from Current Setup

If you're migrating from the previous single-environment setup:

```bash
# Run the migration script
./scripts/migrate-from-current.sh
```

This script will:
1. Backup your current setup
2. Create new volumes for dev/prod separation
3. Migrate existing production data
4. Create environment files

## CI/CD Configuration

### GitHub Environments

Configure the following secrets and variables in your GitHub repository under **Settings > Environments** for both the `production` and `development` environments.

Use the same names for secrets and variables in both environments, but assign different values to ensure proper separation.

### Required Secrets (Both Environments)

- `POSTGRES_DB` - Database name (different values for each environment)
- `POSTGRES_USER` - Database username (different values for each environment)
- `POSTGRES_PASSWORD` - Database password (different values for each environment)
- `TELEGRAM_BOT_TOKEN` - Telegram bot token (different bots for each environment)
- `TELEGRAM_BOT_USERNAME` - Telegram bot username (different bots for each environment)
- `VPS_USER` - SSH username for VPS (typically same for both)
- `VPS_SSH_KEY` - SSH private key for VPS access (typically same for both)

### Required Variables (Both Environments)

- `VPS_HOST` - VPS IP address or hostname (typically same for both)
- `CORS_ALLOWED_ORIGINS` - CORS origins (different values for each environment)
- `GEOHOD_CREATED_EVENT_LINK_TEMPLATE` - Event link template (different values for each environment)
- `GEOHOD_REVIEW_LINK_TEMPLATE` - Review link template (different values for each environment)

**Important**: Ensure that you configure the secrets and variables in the GitHub Environments settings, not directly in the workflow file. This ensures proper security and separation of configuration.

## Operations and Maintenance

### Backup Operations

**Production:**
```bash
./scripts/backup-prod.sh
```

**Development:**
```bash
./scripts/backup-dev.sh
```

### Health Monitoring

**Production:**
```bash
# Check service status
docker compose -p geohod-prod ps

# View logs
docker compose -p geohod-prod logs -f

# Health check
curl http://localhost:8080/actuator/health
```

**Development:**
```bash
# Check service status
docker compose -p geohod-dev ps

# View logs
docker compose -p geohod-dev logs -f

# Health check
curl http://localhost:8081/actuator/health
```

### Updates and Rollbacks

**Update to New Version:**

**Production:**
```bash
cd deployments/prod
./deploy-prod.sh
```

**Development:**
```bash
cd deployments/dev
./deploy-dev.sh
```

## Security Considerations

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

## Troubleshooting

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

## Best Practices

1. **Always backup before major changes**
2. **Test in development before deploying to production**
3. **Use separate Telegram bots for dev and prod**
4. **Monitor resource usage to prevent contention**
5. **Regularly update dependencies and base images**
6. **Document any custom configurations**
7. **Use version control for all configuration files**