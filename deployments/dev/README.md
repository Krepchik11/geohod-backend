# Development Deployment Guide

This directory contains all configuration files and scripts needed to deploy the Geohod backend application to the development environment.

## Directory Structure

- `docker-compose.dev.yml` - Development Docker Compose configuration (memory optimized)
- `.env.dev` - Development environment variables (safe to commit)
- `deploy-dev.sh` - Development deployment script
- `application-dev-optimized.yml` - Memory-optimized Spring configuration

## Deployment Process

1. **Deploy**
   ```bash
   # Make the deployment script executable
   chmod +x deploy-dev.sh
   
   # Run the deployment (builds from source with memory optimizations)
   ./deploy-dev.sh
   ```

## Development Environment Details

- **Application Port**: 8081
- **Debug Port**: 5005
- **Database Port**: 5433
- **Container Names**:
  - Application: `geohod-backend-dev`
  - Database: `geohod-postgres-dev`
- **Docker Network**: `geohod-dev-network`
- **Docker Volume**: `geohod_postgres_data_dev`

## Memory Optimizations

The development environment has been optimized to reduce memory consumption by approximately 60%:

- **JRE instead of JDK**: Reduced base image size
- **JVM Heap Limits**: Constrained to 384MB maximum
- **Gradle Memory Limits**: Constrained to 512MB maximum
- **PostgreSQL Optimization**: Reduced memory usage to ~128MB
- **Reduced Logging**: Less memory overhead from logging
- **Resource Constraints**: Docker limits prevent memory bloat

## Features

- **Hot Reload**: Source code changes are automatically reflected (via volume mounting)
- **Debugging**: Remote debugging available on port 5005
- **Optimized Logging**: INFO level logging by default (DEBUG for application)
- **Limited Actuator Endpoints**: Only essential endpoints exposed

## Monitoring

- **Health Check**: `http://localhost:8081/actuator/health`
- **Logs**: `docker compose -p geohod-dev logs`
- **Status**: `docker compose -p geohod-dev ps`
- **Memory Usage**: `docker stats geohod-backend-dev geohod-postgres-dev`

## Development Workflow

1. **Start Development Environment**
   ```bash
   ./deploy-dev.sh
   ```

2. **View Logs**
   ```bash
   docker compose -p geohod-dev logs -f
   ```

3. **Stop Development Environment**
   ```bash
   docker compose -p geohod-dev down
   ```

4. **Rebuild and Restart**
   ```bash
   docker compose -p geohod-dev up -d --build
   ```

## Memory Optimization Options

### Standard Optimization (Default)
- Application memory limit: 512MB
- PostgreSQL memory limit: 256MB
- Total estimated usage: ~768MB

### Ultra-Minimal Configuration
To further reduce memory usage, you can:

1. **Disable Debug Agent**: Comment out the JAVA_TOOL_OPTIONS line in docker-compose.dev.yml
2. **Reduce Heap Size**: Set JAVA_OPTS to `-Xmx256m -Xms128m`
3. **Minimal Logging**: Set LOGGING_LEVEL_ME_GEOHOD to WARN

Example for ultra-minimal:
```bash
# In .env.dev, add:
SPRING_PROFILES_ACTIVE=dev-optimized,minimal
```

## Troubleshooting

**Port Conflicts**
- Ensure development uses 8081/5433
- Check no other services are using these ports

**Database Connection Issues**
- Verify environment variables are correctly set
- Check database container health status
- Ensure proper network connectivity

**Build Issues**
- Clean Gradle cache: `docker compose -p geohod-dev down -v`
- Rebuild: `./deploy-dev.sh`

**Memory Issues**
- Check container memory usage: `docker stats`
- Verify health: `curl http://localhost:8081/actuator/health`
- Consider using ultra-minimal configuration (see above)

**Debugging**
- Connect IDE debugger to localhost:5005
- Check debug logs: `docker compose -p geohod-dev logs geohod-app-dev`