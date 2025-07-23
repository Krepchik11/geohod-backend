# Geohod Backend Docker Compose Deployment Guide

This guide provides step-by-step instructions for migrating from direct Docker deployment to Docker Compose orchestration.

## Phase 1: Infrastructure Setup (One-time setup)

### Prerequisites
- Docker and Docker Compose installed on VPS
- SSH access to VPS
- GitHub repository with updated workflow

### 1.1 Volume Setup
Run the volume setup script on your VPS:

```bash
# Copy setup script to VPS
scp scripts/setup-volumes.sh user@your-vps-ip:/tmp/
ssh user@your-vps-ip "chmod +x /tmp/setup-volumes.sh && /tmp/setup-volumes.sh"
```

This creates the required Docker volumes:
- `geohod_postgres_data` - PostgreSQL data persistence

### 1.2 Environment Configuration
Ensure the following GitHub secrets and variables are configured:

**GitHub Secrets:**
- `POSTGRES_DB` - Database name
- `POSTGRES_USER` - Database username
- `POSTGRES_PASSWORD` - Database password
- `TELEGRAM_BOT_TOKEN` - Telegram bot token
- `TELEGRAM_BOT_USERNAME` - Telegram bot username
- `VPS_USER` - SSH username for VPS
- `VPS_SSH_KEY` - SSH private key for VPS access

**GitHub Variables:**
- `VPS_HOST` - VPS IP address or hostname
- `CORS_ALLOWED_ORIGINS` - Allowed CORS origins
- `GEOHOD_CREATED_EVENT_LINK_TEMPLATE` - Event link template
- `GEOHOD_REVIEW_LINK_TEMPLATE` - Review link template

## Phase 2: GitHub Actions Integration

### 2.1 Workflow Overview
The updated GitHub Actions workflow:
1. Builds the application JAR
2. Creates Docker image
3. Transfers image to VPS
4. Deploys using Docker Compose
5. Performs health checks and cleanup

### 2.2 Manual Deployment (Optional)
For manual deployment, use these commands on the VPS:

```bash
# Navigate to application directory
cd /opt/geohod-backend

# Create .env file (if not using GitHub Actions)
cp .env.template .env
# Edit .env with your actual values

# Deploy
docker compose up -d

# Check status
docker compose ps

# View logs
docker compose logs -f
```

## Phase 3: Operations and Maintenance

### 3.1 Backup Operations
Create backups of persistent data:

```bash
# Create backup
./scripts/backup-volumes.sh

# Backup will be created in ./backups/[timestamp]/
```

### 3.2 Restore Operations
Restore from backup:

```bash
# List available backups
ls -la backups/

# Restore from specific backup
./scripts/restore-volumes.sh backups/20240723_143000
```

### 3.3 Health Monitoring
Check service health:

```bash
# Check all services
docker compose ps

# Check specific service
docker compose ps app

# View logs
docker compose logs app
docker compose logs postgres
```

### 3.4 Updates and Rollbacks
Update to new version:

```bash
# Pull latest changes (if using GitHub Actions, this happens automatically)
docker compose pull
docker compose up -d
```

Rollback to previous version:

```bash
# Stop current services
docker compose down

# Load previous image (if available)
docker load < /path/to/previous-image.tar

# Start with previous image
docker compose up -d
```

## Troubleshooting

### Common Issues

**1. Volumes not found**
```bash
# Create missing volumes
docker volume create geohod_postgres_data
```

**2. Database connection issues**
```bash
# Check PostgreSQL logs
docker compose logs postgres

# Test database connection
docker compose exec postgres pg_isready -U $POSTGRES_USER -d $POSTGRES_DB
```

**3. Application not starting**
```bash
# Check application logs
docker compose logs app

# Check if environment variables are set
docker compose exec app env | grep -E "(POSTGRES|TELEGRAM|JWT)"
```

### Migration Checklist

- [ ] Docker and Docker Compose installed on VPS
- [ ] GitHub secrets and variables configured
- [ ] Volumes created using setup script
- [ ] Test deployment on staging environment
- [ ] Update DNS/proxy configuration if needed
- [ ] Monitor first deployment for issues
- [ ] Document any custom configurations

## Support
For issues or questions, refer to:
- Application logs: `docker compose logs -f`
- System logs: `journalctl -u docker`
- GitHub Actions logs in repository Actions tab
