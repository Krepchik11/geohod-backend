#!/bin/bash
set -euo pipefail

# This script must be run as root via sudo.
if [ "$(id -u)" -ne 0 ]; then
  echo "This script requires superuser privileges. Please run with sudo." >&2
  exit 1
fi

# Determine the home directory of the user who invoked sudo
SUDO_USER_HOME=$(getent passwd "${SUDO_USER}" | cut -d: -f6)
STAGING_DIR="${SUDO_USER_HOME}/geohod-backend-dev"
CONFIG_DIR="/etc/geohod"
SERVICE_NAME="geohod-backend-dev"
ENV_FILE="geohod-dev.env"

echo "--- Starting Geohod Backend Dev Deployment ---"

# 1. Validate required files exist in staging area
echo "--> Validating artifacts in ${STAGING_DIR}..."
if [ ! -f "${STAGING_DIR}/${ENV_FILE}" ] || \
   [ ! -f "${STAGING_DIR}/${SERVICE_NAME}.service" ] || \
   ! ls "${STAGING_DIR}"/geohod-backend-dev-*.tar.gz 1> /dev/null 2>&1; then
    echo "Error: Missing required deployment files in ${STAGING_DIR}." >&2
    exit 1
fi
echo "Artifacts validated."

# 2. Setup configuration
echo "--> Updating environment configuration..."
mkdir -p "${CONFIG_DIR}"
cp "${STAGING_DIR}/${ENV_FILE}" "${CONFIG_DIR}/${ENV_FILE}"
chown root:root "${CONFIG_DIR}/${ENV_FILE}"
chmod 640 "${CONFIG_DIR}/${ENV_FILE}"
echo "Environment file placed at ${CONFIG_DIR}/${ENV_FILE}"

# 3. Load the new image from tarball
source "${CONFIG_DIR}/${ENV_FILE}"
IMAGE_TARBALL="${STAGING_DIR}/geohod-backend-${GEOHOD_IMAGE_TAG}.tar.gz"

echo "--> Loading image from ${IMAGE_TARBALL}..."
if ! podman load -i "${IMAGE_TARBALL}"; then
    echo "Error: Failed to load image from ${IMAGE_TARBALL}" >&2
    exit 1
fi
echo "Image loaded successfully."

# 4. Update and enable systemd service
echo "--> Updating systemd service..."
cp "${STAGING_DIR}/${SERVICE_NAME}.service" "/etc/systemd/system/${SERVICE_NAME}.service"
chown root:root "/etc/systemd/system/${SERVICE_NAME}.service"
chmod 644 "/etc/systemd/system/${SERVICE_NAME}.service"

echo "--> Reloading systemd daemon and restarting service..."
systemctl daemon-reload
systemctl enable "${SERVICE_NAME}"
systemctl restart "${SERVICE_NAME}"

# 5. Clean up old images
echo "--> Cleaning up old images..."
podman image prune -f --filter "label=stage=dev" --filter "until=24h"

echo "--- Geohod Backend dev deployment finished successfully ---"
exit 0