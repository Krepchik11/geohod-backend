#!/bin/bash
set -euo pipefail

# This script runs as the deployment user, not root.

STAGING_DIR="${HOME}/geohod-backend"
# Per XDG Base Directory Specification, user-specific config files are in ~/.config
CONFIG_DIR="${HOME}/.config/geohod"
# Per systemd spec, user services are in ~/.config/systemd/user
SERVICE_DIR="${HOME}/.config/systemd/user"
SERVICE_NAME="geohod-backend-prod"
ENV_FILE="geohod-prod.env"

echo "--- Starting Geohod Backend Prod User Deployment ---"

# 1. Validate required files exist in staging area
echo "--> Validating artifacts in ${STAGING_DIR}..."
if [ ! -f "${STAGING_DIR}/${ENV_FILE}" ] || \
   [ ! -f "${STAGING_DIR}/${SERVICE_NAME}.service" ] || \
   [ -z "$(find "${STAGING_DIR}" -name 'geohod-backend-prod-*.tar.gz' -print -quit)" ]; then
    echo "Error: Missing required deployment files in ${STAGING_DIR}." >&2
    exit 1
fi
echo "Artifacts validated."

# 2. Create shared network
# This command is idempotent. It will do nothing if the network already exists.
echo "--> Ensuring 'geohod-net' network exists..."
podman network create geohod-prod-net || true

# 3. Setup configuration
echo "--> Updating environment configuration..."
mkdir -p "${CONFIG_DIR}"
cp "${STAGING_DIR}/${ENV_FILE}" "${CONFIG_DIR}/${ENV_FILE}"
echo "Environment file placed at ${CONFIG_DIR}/${ENV_FILE}"

# 3. Load the new image from tarball
# The GEOHOD_IMAGE_TAG is needed to find the correct tarball.
source "${CONFIG_DIR}/${ENV_FILE}"
IMAGE_TARBALL="${STAGING_DIR}/geohod-backend-${GEOHOD_IMAGE_TAG}.tar.gz"

echo "--> Loading image from ${IMAGE_TARBALL}..."
if ! podman load -i "${IMAGE_TARBALL}"; then
    echo "Error: Failed to load image from ${IMAGE_TARBALL}" >&2
    exit 1
fi
echo "Image loaded successfully."

# 4. Update and enable systemd user service
echo "--> Updating systemd user service..."
mkdir -p "${SERVICE_DIR}"
cp "${STAGING_DIR}/${SERVICE_NAME}.service" "${SERVICE_DIR}/${SERVICE_NAME}.service"

echo "--> Reloading systemd user daemon and restarting service..."
systemctl --user daemon-reload
systemctl --user enable "${SERVICE_NAME}"
systemctl --user restart "${SERVICE_NAME}"

# 5. Clean up old images
echo "--> Cleaning up old images..."
podman image prune -f --filter "label=stage=prod" --filter "until=24h"

echo "--- Geohod Backend prod user deployment finished successfully ---"
exit 0