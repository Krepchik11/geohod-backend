# Development Deployment Guide

This directory contains all configuration files and scripts needed to deploy the Geohod backend application to the development environment.

## Directory Structure

- `docker-compose.dev.yml` - Development Docker Compose configuration
- `.env.dev` - Development environment variables (safe to commit)
- `deploy-dev.sh` - Development deployment script

## Deployment Process

1. **Deploy**
   ```bash
   # Make the deployment script executable
   chmod +x deploy-dev.sh
   
   # Run the deployment (builds from source)
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

## Features

- **Hot Reload**: Source code changes are automatically reflected (via volume mounting)
- **Debugging**: Remote debugging available on port 5005
- **Verbose Logging**: DEBUG and TRACE level logging enabled
- **All Actuator Endpoints**: Full access to Spring Boot actuator endpoints

## Monitoring

- **Health Check**: `http://localhost:8081/actuator/health`
- **Logs**: `docker compose -p geohod-dev logs`
- **Status**: `docker compose -p geohod-dev ps`

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

**Debugging**
- Connect IDE debugger to localhost:5005
- Check debug logs: `docker compose -p geohod-dev logs geohod-app-dev`