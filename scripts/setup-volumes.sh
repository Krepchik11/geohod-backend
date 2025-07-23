#!/bin/bash

# Script to setup Docker volumes for Geohod application
# Run this script on the VPS before first deployment

set -e

echo "Setting up Docker volumes for Geohod application..."

# Create external volumes for data persistence
docker volume create --name=geohod_postgres_data

echo "✅ Volumes created successfully:"
docker volume ls | grep geohod_

# Display volume information
echo ""
echo "Volume details:"
docker volume inspect geohod_postgres_data

echo ""
echo "🎉 Volumes setup complete!"
echo "You can now deploy the application using: docker compose up -d"
