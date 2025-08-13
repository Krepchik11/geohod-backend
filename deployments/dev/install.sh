#!/bin/bash
set -euo pipefail

STAGING_DIR="${HOME}/geohod-backend-dev"
SERVICE_NAME="geohod-backend-dev"
IMAGE_TARBALL="${STAGING_DIR}/geohod-backend-image.tar.gz"
SERVICE_DIR="${HOME}/.config/systemd/user"

echo "--- Starting Geohod Backend Deployment ---"

echo "--> Setting up systemd user service..."
mkdir -p "${SERVICE_DIR}"
cp "${STAGING_DIR}/${SERVICE_NAME}.service" "${SERVICE_DIR}/"
systemctl --user enable "${SERVICE_NAME}.service"

echo "--> Loading image from ${IMAGE_TARBALL}..."
podman load -i "${IMAGE_TARBALL}"

echo "--> Reloading systemd user daemon and restarting service..."
systemctl --user daemon-reload
systemctl --user restart "${SERVICE_NAME}"

echo "--> Cleaning up old images..."
podman image prune -f --filter "label=stage=dev" --filter "until=24h"

echo "--- Geohod Backend deployment finished ---"