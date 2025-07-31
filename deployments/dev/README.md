# Development Deployment Guide (CI/CD Based)

This directory contains all configuration files and scripts needed to deploy the Geohod backend application to the development environment using a CI/CD approach that builds on GitHub Actions and deploys pre-built images to avoid memory-intensive builds on the VPS.

## Directory Structure

- `docker-compose.dev.yml` - Development Docker Compose configuration (uses pre-built image)
- `.env.dev` - Development environment variables (safe to commit)
- `deploy-dev.sh` - Development deployment script (pulls pre-built image)
- `application-dev-optimized.yml` - Memory-optimized Spring configuration

## Deployment Process

The deployment now works in two phases:
1. **GitHub Actions builds and packages** the application into a Docker image
2. **Deployment script pulls and runs** the pre-built image on the VPS

### GitHub Actions Workflow
When code is pushed to the `develop` branch:
1. GitHub Actions builds the application using Gradle
2. Creates a Docker image with the built JAR
3. Packages the image and copies it to the VPS
4. Triggers the deployment script

### Manual Deployment
```bash
# Make the deployment script executable
chmod +x deploy-dev.sh

# Run the deployment (pulls pre-built image)
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

The development environment has been optimized to reduce memory consumption on the VPS:

- **No Local Build**: Eliminates memory-intensive Gradle build process on VPS
- **Pre-built Image**: Application is built on GitHub Actions with ample resources
- **Alpine JRE**: Reduced base image size using eclipse-temurin:23-jre-alpine
- **Container-Aware JVM**: Uses -XX:+UseContainerSupport and -XX:MaxRAMPercentage=75.0 for optimal memory management
- **PostgreSQL Optimization**: Reduced memory usage to ~128MB
- **Resource Constraints**: Docker limits prevent memory bloat

## Features

- **No Hot Reload**: Source code changes require rebuilding the image (eliminates local Gradle)
- **Debugging**: Remote debugging available on port 5005
- **Optimized Logging**: INFO level logging by default (DEBUG for application)
- **Limited Actuator Endpoints**: Only essential endpoints exposed

## Monitoring

- **Health Check**: `http://localhost:8081/actuator/health`
- **Logs**: `docker compose -p geohod-dev logs`
- **Status**: `docker compose -p geohod-dev ps`
- **Memory Usage**: `docker stats geohod-backend-dev geohod-postgres-dev`

## Development Workflow

1. **Push to Develop Branch**
   - GitHub Actions automatically builds and deploys

2. **Manual Deployment**
   ```bash
   ./deploy-dev.sh
   ```

3. **View Logs**
   ```bash
   docker compose -p geohod-dev logs -f
   ```

4. **Stop Development Environment**
   ```bash
   docker compose -p geohod-dev down
   ```

## Memory Optimization Benefits

### Before (Local Build)
- Gradle build: ~1-2GB memory during build process
- Application runtime: ~768MB
- Total memory requirement: ~2-3GB

### After (CI/CD Build)
- No local build: 0MB memory for building
- Application runtime: ~512MB
- Total memory requirement: ~512MB + small overhead

### Memory Savings
- **~1.5-2.5GB reduction** in peak memory usage
- **Eliminates build-time memory spikes** that were causing deployment failures
- **Consistent memory usage** without build overhead

## Troubleshooting

**Port Conflicts**
- Ensure development uses 8081/5433
- Check no other services are using these ports

**Database Connection Issues**
- Verify environment variables are correctly set
- Check database container health status
- Ensure proper network connectivity

**Deployment Issues**
- Check GitHub Actions logs for build failures
- Verify image was properly copied to VPS
- Ensure deployment script has proper permissions

**Memory Issues**
- Check container memory usage: `docker stats`
- Verify health: `curl http://localhost:8081/actuator/health`

**Debugging**
- Connect IDE debugger to localhost:5005
- Check debug logs: `docker compose -p geohod-dev logs geohod-app-dev`