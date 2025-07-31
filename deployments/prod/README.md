# Production Deployment Guide

This directory contains all configuration files and scripts needed to deploy the Geohod backend application to the production environment.

## Directory Structure

- `docker-compose.prod.yml` - Production Docker Compose configuration
- `.env.prod.template` - Template for production environment variables (copy to `.env.prod` and fill in values)
- `deploy-prod.sh` - Production deployment script

## Deployment Process

1. **Setup Environment Variables**
   ```bash
   # Copy the template and fill in actual values
   cp .env.prod.template .env.prod
   # Edit .env.prod with your production values
   ```

2. **Setup Volumes**
   ```bash
   # Run the volume setup script from the project root
   ../scripts/setup-volumes-prod.sh
   ```

3. **Deploy**
   ```bash
   # Make the deployment script executable
   chmod +x deploy-prod.sh
   
   # Run the deployment
   ./deploy-prod.sh
   ```

## Production Environment Details

- **Application Port**: 8080
- **Database Port**: 5432
- **Container Names**: 
  - Application: `geohod-backend-prod`
  - Database: `geohod-postgres-prod`
- **Docker Network**: `geohod-prod-network`
- **Docker Volume**: `geohod_postgres_data_prod`

## Monitoring

- **Health Check**: `http://localhost:8080/actuator/health`
- **Logs**: `docker compose -p geohod-prod logs`
- **Status**: `docker compose -p geohod-prod ps`

## Security Considerations

- Never commit `.env.prod` to version control
- Use strong, unique passwords for production database
- Restrict production Telegram bot to production domain
- Limit actuator endpoints exposure
- Use resource limits to prevent DoS

## Troubleshooting

**Port Conflicts**
- Ensure production uses 8080/5432
- Check no other services are using these ports

**Database Connection Issues**
- Verify environment variables are correctly set
- Check database container health status
- Ensure proper network connectivity

**Resource Issues**
- Monitor memory and CPU usage
- Adjust resource limits if needed