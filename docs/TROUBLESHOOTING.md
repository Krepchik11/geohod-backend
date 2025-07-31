# Geohod Backend Deployment Troubleshooting Guide

This guide provides solutions for common issues that may occur during deployment and operation of the Geohod backend application in both development and production environments.

## Common Deployment Issues

### 1. Port Conflicts

**Symptoms:**
- Error messages like "port is already allocated"
- Services fail to start
- Deployment scripts fail

**Solutions:**

**Check which ports are in use:**
```bash
# Check production ports (8080, 5432)
sudo netstat -tlnp | grep :8080
sudo netstat -tlnp | grep :5432

# Check development ports (8081, 5433, 5005)
sudo netstat -tlnp | grep :8081
sudo netstat -tlnp | grep :5433
sudo netstat -tlnp | grep :5005
```

**Stop conflicting services:**
```bash
# Stop services using specific ports
sudo fuser -k 8080/tcp
sudo fuser -k 5432/tcp
```

**Verify port separation:**
- Production: 8080 (app), 5432 (db)
- Development: 8081 (app), 5433 (db), 5005 (debug)

### 2. Docker Volume Issues

**Symptoms:**
- "no such volume" errors
- Data persistence issues
- Database initialization failures

**Solutions:**

**Check existing volumes:**
```bash
docker volume ls | grep geohod
```

**Create missing volumes:**
```bash
# Production
./scripts/setup-volumes-prod.sh

# Development
./scripts/setup-volumes-dev.sh
```

**Remove and recreate volumes (data loss warning):**
```bash
# Stop services first
docker compose -p geohod-prod down
docker compose -p geohod-dev down

# Remove volumes
docker volume rm geohod_postgres_data_prod
docker volume rm geohod_postgres_data_dev

# Recreate volumes
./scripts/setup-volumes-prod.sh
./scripts/setup-volumes-dev.sh
```

### 3. Environment Variable Issues

**Symptoms:**
- Database connection failures
- Telegram bot not working
- Application fails to start

**Solutions:**

**Verify environment files exist and are properly configured:**
```bash
# Production
ls -la deployments/prod/.env.prod
cat deployments/prod/.env.prod

# Development
ls -la deployments/dev/.env.dev
cat deployments/dev/.env.dev
```

**Common missing variables:**
- `POSTGRES_DB`
- `POSTGRES_USER`
- `POSTGRES_PASSWORD`
- `TELEGRAM_BOT_TOKEN`
- `TELEGRAM_BOT_USERNAME`

### 4. Docker Compose Issues

**Symptoms:**
- "service not found" errors
- "network not found" errors
- Services not starting properly

**Solutions:**

**Validate Docker Compose files:**
```bash
# Production
cd deployments/prod
docker compose -f docker-compose.prod.yml config

# Development
cd deployments/dev
docker compose -f docker-compose.dev.yml config
```

**Recreate networks:**
```bash
# Remove existing networks
docker network rm geohod-prod-network geohod-dev-network 2>/dev/null || true

# Redeploy to recreate networks
cd deployments/prod && ./deploy-prod.sh
cd deployments/dev && ./deploy-dev.sh
```

## Database Issues

### 1. Database Connection Failures

**Symptoms:**
- "Connection refused" errors
- "Unable to acquire JDBC connection"
- Application health checks failing

**Solutions:**

**Check database container status:**
```bash
# Production
docker compose -p geohod-prod ps postgres-prod

# Development
docker compose -p geohod-dev ps postgres-dev
```

**Check database logs:**
```bash
# Production
docker compose -p geohod-prod logs postgres-prod

# Development
docker compose -p geohod-dev logs postgres-dev
```

**Test database connectivity:**
```bash
# Production
docker compose -p geohod-prod exec postgres-prod pg_isready -U $POSTGRES_USER -d $POSTGRES_DB

# Development
docker compose -p geohod-dev exec postgres-dev pg_isready -U $POSTGRES_USER -d $POSTGRES_DB
```

### 2. Database Initialization Issues

**Symptoms:**
- Database not initialized
- Missing tables
- Liquibase migration failures

**Solutions:**

**Check application logs for database errors:**
```bash
# Production
docker compose -p geohod-prod logs geohod-app

# Development
docker compose -p geohod-dev logs geohod-app-dev
```

**Force database recreation (data loss warning):**
```bash
# Stop services
docker compose -p geohod-prod down
docker compose -p geohod-dev down

# Remove volumes
docker volume rm geohod_postgres_data_prod geohod_postgres_data_dev

# Recreate volumes and redeploy
./scripts/setup-volumes-prod.sh
./scripts/setup-volumes-dev.sh
cd deployments/prod && ./deploy-prod.sh
cd deployments/dev && ./deploy-dev.sh
```

## Application Issues

### 1. Application Fails to Start

**Symptoms:**
- Container exits immediately
- Health checks failing
- Application not responding

**Solutions:**

**Check application logs:**
```bash
# Production
docker compose -p geohod-prod logs geohod-app

# Development
docker compose -p geohod-dev logs geohod-app-dev
```

**Check for port conflicts:**
```bash
sudo netstat -tlnp | grep :8080
sudo netstat -tlnp | grep :8081
```

**Verify environment variables:**
```bash
# Production
docker compose -p geohod-prod exec geohod-app env | grep -E "(POSTGRES|TELEGRAM)"

# Development
docker compose -p geohod-dev exec geohod-app-dev env | grep -E "(POSTGRES|TELEGRAM)"
```

### 2. Telegram Integration Issues

**Symptoms:**
- Authentication failures
- Notifications not sent
- Bot not responding

**Solutions:**

**Verify Telegram bot token:**
```bash
# Check if token is set
echo $TELEGRAM_BOT_TOKEN

# Test token validity (replace TOKEN with actual token)
curl -s "https://api.telegram.org/bot<TOKEN>/getMe"
```

**Check Telegram bot configuration:**
- Ensure bot token matches the one from @BotFather
- Verify bot username is correct
- Check if bot is properly configured for the domain

### 3. Health Check Failures

**Symptoms:**
- Deployment scripts report health check failures
- Services marked as unhealthy
- Monitoring alerts

**Solutions:**

**Manual health check:**
```bash
# Production
curl -f http://localhost:8080/actuator/health

# Development
curl -f http://localhost:8081/actuator/health
```

**Check specific health indicators:**
```bash
# Production
curl http://localhost:8080/actuator/health/db
curl http://localhost:8080/actuator/health/diskSpace

# Development
curl http://localhost:8081/actuator/health/db
curl http://localhost:8081/actuator/health/diskSpace
```

## CI/CD Issues

### 1. GitHub Actions Deployment Failures

**Symptoms:**
- Workflows failing
- Deployment not triggered
- Files not copied to server

**Solutions:**

**Check GitHub environment configuration:**
- Verify `production` and `development` environments exist
- Ensure required secrets and variables are set

**Check SSH connectivity:**
- Verify `VPS_HOST`, `VPS_USER`, and `VPS_SSH_KEY` are correct
- Test SSH connection manually

**Check file permissions:**
```bash
# On VPS, ensure deployment scripts are executable
chmod +x ~/geohod-backend/deployments/prod/deploy-prod.sh
chmod +x ~/geohod-backend-dev/deployments/dev/deploy-dev.sh
```

### 2. Docker Image Build Issues

**Symptoms:**
- Build failures in CI/CD
- Missing dependencies
- Application not starting from built image

**Solutions:**

**Test local Docker build:**
```bash
# Build production image
docker build -f .github/Dockerfile -t geohod-backend:latest .

# Test development build
cd deployments/dev
docker compose -f docker-compose.dev.yml build
```

## Resource Issues

### 1. Memory/CPU Exhaustion

**Symptoms:**
- System slowdown
- Containers being killed
- "Out of memory" errors

**Solutions:**

**Check resource usage:**
```bash
# Docker resource usage
docker stats

# System resource usage
htop
free -h
```

**Adjust resource limits:**
- Modify `deploy.resources` section in Docker Compose files
- Consider reducing limits for development environment
- Monitor production resource usage and adjust accordingly

### 2. Disk Space Issues

**Symptoms:**
- "No space left on device" errors
- Database write failures
- Docker operations failing

**Solutions:**

**Check disk usage:**
```bash
df -h
docker system df
```

**Clean up Docker resources:**
```bash
# Remove unused containers, networks, images
docker system prune -a

# Remove unused volumes
docker volume prune

# Clean up build cache
docker builder prune
```

## Backup and Restore Issues

### 1. Backup Failures

**Symptoms:**
- Backup scripts failing
- Incomplete backups
- Permission errors

**Solutions:**

**Check backup directory permissions:**
```bash
ls -la ./backups/
mkdir -p ./backups/prod ./backups/dev
```

**Verify container names:**
- Production: `geohod-postgres-prod`
- Development: `geohod-postgres-dev`

### 2. Restore Failures

**Symptoms:**
- Data not restored
- Database connection issues after restore
- Application errors with restored data

**Solutions:**

**Stop services before restore:**
```bash
docker compose -p geohod-prod down
docker compose -p geohod-dev down
```

**Verify backup integrity:**
```bash
# Check backup contents
ls -la ./backups/prod/TIMESTAMP/
tar -tzf ./backups/prod/TIMESTAMP/postgres_data.tar.gz
```

## Network Issues

### 1. Container Communication Issues

**Symptoms:**
- Application cannot connect to database
- Services cannot communicate
- Network timeouts

**Solutions:**

**Check network connectivity between containers:**
```bash
# Production
docker compose -p geohod-prod exec geohod-app ping postgres-prod

# Development
docker compose -p geohod-dev exec geohod-app-dev ping postgres-dev
```

**Verify network configuration:**
- Check that services are on the correct networks
- Ensure network names match between services

### 2. External Access Issues

**Symptoms:**
- Cannot access application from outside
- API calls failing
- Health checks from monitoring tools failing

**Solutions:**

**Verify port mappings:**
```bash
# Check if ports are exposed
docker compose -p geohod-prod ps
docker compose -p geohod-dev ps
```

**Check firewall settings:**
```bash
# Check if ports are blocked by firewall
sudo ufw status
sudo iptables -L
```

## Migration Issues

### 1. Data Migration Failures

**Symptoms:**
- Migration script errors
- Data not transferred
- Inconsistent data between environments

**Solutions:**

**Check source and target volumes:**
```bash
docker volume ls | grep geohod
```

**Verify migration script permissions:**
```bash
chmod +x scripts/migrate-from-current.sh
```

### 2. Configuration Migration Issues

**Symptoms:**
- Environment variables not migrated
- Missing configuration files
- Services not starting after migration

**Solutions:**

**Verify environment file creation:**
```bash
ls -la deployments/prod/.env.prod
ls -la deployments/dev/.env.dev
```

**Check file permissions:**
```bash
chmod 600 deployments/prod/.env.prod
chmod 600 deployments/dev/.env.dev
```

## Monitoring and Logging Issues

### 1. Log Access Issues

**Symptoms:**
- Cannot view logs
- Log files empty
- Log rotation issues

**Solutions:**

**Check log access:**
```bash
# Production
docker compose -p geohod-prod logs --tail=100

# Development
docker compose -p geohod-dev logs --tail=100
```

**Check log file locations:**
- Docker logs are managed by Docker
- Application logs are within containers

### 2. Monitoring Failures

**Symptoms:**
- Health checks failing
- Monitoring alerts not working
- Metrics not collected

**Solutions:**

**Verify actuator endpoints:**
```bash
# Production
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/metrics

# Development
curl http://localhost:8081/actuator/health
curl http://localhost:8081/actuator/metrics
```

## Performance Issues

### 1. Slow Application Response

**Symptoms:**
- High response times
- Database queries slow
- Application timeouts

**Solutions:**

**Check resource usage:**
```bash
docker stats
```

**Analyze database queries:**
```bash
# Enable query logging temporarily
docker compose -p geohod-prod exec postgres-prod psql -U $POSTGRES_USER -d $POSTGRES_DB -c "ALTER SYSTEM SET log_statement = 'all';"
```

### 2. High Memory Usage

**Symptoms:**
- JVM memory issues
- Container memory limits exceeded
- System slowdown

**Solutions:**

**Adjust JVM memory settings:**
- Modify Docker Compose files to set appropriate memory limits
- Adjust `XX:MaxRAMPercentage` in Dockerfile

**Monitor memory usage:**
```bash
docker stats --format "table {{.Container}}\t{{.Name}}\t{{.MemUsage}}\t{{.MemPerc}}"
```

## Security Issues

### 1. Unauthorized Access

**Symptoms:**
- Suspicious activity in logs
- Unauthorized database access
- Unexpected changes to data

**Solutions:**

**Check access logs:**
```bash
docker compose -p geohod-prod logs | grep -i "unauthorized\|denied\|forbidden"
```

**Verify security configuration:**
- Check that production secrets are not in version control
- Verify database passwords are strong
- Ensure proper network isolation

### 2. Vulnerability Issues

**Symptoms:**
- Security scan failures
- Outdated base images
- Known vulnerabilities in dependencies

**Solutions:**

**Update base images:**
```bash
# Pull latest base images
docker pull postgres:17-alpine
docker pull openjdk:23-jdk-slim
```

**Scan for vulnerabilities:**
```bash
# Use tools like Trivy or Clair
trivy image geohod-backend:latest
```

## Recovery Procedures

### 1. Complete System Recovery

**Steps:**
1. Stop all services
2. Backup current state if possible
3. Restore from latest backup
4. Verify data integrity
5. Restart services
6. Monitor for issues

### 2. Rollback to Previous Version

**Steps:**
1. Stop current services
2. Load previous Docker image
3. Restore previous database backup if needed
4. Deploy with previous configuration
5. Verify functionality

## Prevention Best Practices

1. **Regular Backups**: Schedule automated backups
2. **Monitoring**: Set up alerts for critical issues
3. **Testing**: Test deployments in staging first
4. **Documentation**: Keep documentation up to date
5. **Security**: Regular security audits and updates
6. **Resource Management**: Monitor and adjust resource allocation
7. **Log Management**: Implement proper log rotation
8. **Disaster Recovery**: Maintain recovery procedures and test them regularly