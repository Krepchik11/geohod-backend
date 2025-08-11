#!/usr/bin/env bash
set -euo pipefail
# Minimal privileged installer wrapper for Geohod dev systemd deployment
# Path (repository): deployments/dev/geohod-deploy-systemd-dev.sh
#
# Responsibilities (minimal):
# - Validate staging directory and required artifacts
# - Copy env/secret files into /etc/geohod with strict permissions
# - Load application image (podman load)
# - Ensure Postgres image is available (podman pull)
# - Install systemd unit files to /etc/systemd/system
# - Run systemctl daemon-reload, enable and start units in order
# - Subcommands: install|update|remove|status|dry-run
#
# Security:
# - This script is intended to be installed as root-owned (root:root) and mode 750
# - Keep logic small and auditable. No secrets are printed.
#
# Exit codes:
# 0  Success
# 10 Missing args / usage
# 20 Staging validation failure
# 30 Podman/systemctl missing
# 40 Unit install failure
# 50 Permission failure

readonly SCRIPT_NAME="$(basename "$0")"
readonly ETC_DIR="/etc/geohod"
readonly SYSTEMD_DIR="/etc/systemd/system"
readonly REQUIRED_ARTIFACT_IMAGE_GLOB="geohod-backend-dev-*.tar.gz"
readonly REQUIRED_ENV_NAME="geohod-dev.env"
readonly PG_IMG="docker.io/library/postgres:17-alpine"

# Host data paths (used for bind mounts in systemd unit files).
# These defaults can be overridden by exporting PG_UID/PG_GID/BACKEND_UID/BACKEND_GID
# in the environment before invoking this script (useful for CI).
readonly POSTGRES_DATA_DIR="/var/lib/geohod/postgres"
readonly BACKEND_DATA_DIR="/var/lib/geohod/backend-data"
readonly DEFAULT_PG_UID="${PG_UID:-999}"
readonly DEFAULT_PG_GID="${PG_GID:-$DEFAULT_PG_UID}"
readonly DEFAULT_BACKEND_UID="${BACKEND_UID:-1000}"
readonly DEFAULT_BACKEND_GID="${BACKEND_GID:-$DEFAULT_BACKEND_UID}"

log() { echo "[$SCRIPT_NAME] $*"; }
err() { echo "[$SCRIPT_NAME] ERROR: $*" >&2; }

usage() {
  cat <<EOF
Usage: $SCRIPT_NAME <command> <staging-dir>
Commands:
  install   - Install units, copy env/secrets, load image, enable+start units
  update    - Same as install (idempotent)
  remove    - Stop & disable units and remove unit files (does not remove images)
  status    - Show systemctl status for the dev units
  dry-run   - Validate staging dir and show planned actions
Notes:
  - staging-dir must be accessible and owned/managed by the deploy user (CI).
  - This script performs privileged operations and should be invoked via sudo by the deploy user:
      sudo /usr/local/bin/geohod-deploy-systemd-dev.sh install /tmp/staging
EOF
}

require_command() {
  local cmd="$1"
  if ! command -v "$cmd" >/dev/null 2>&1; then
    err "Required command not found: $cmd"
    return 1
  fi
  return 0
}

validate_prereqs() {
  local missing=0
  require_command podman || missing=1
  require_command systemctl || missing=1
  if [ "$missing" -ne 0 ]; then
    err "One or more required commands are missing (podman, systemctl)"
    return 1
  fi
  return 0
}

find_image_tarball() {
  # Find a tarball matching naming convention in staging
  local staging="$1"
  shopt -s nullglob
  local matches=( "$staging"/$REQUIRED_ARTIFACT_IMAGE_GLOB )
  shopt -u nullglob
  if [ "${#matches[@]}" -eq 0 ]; then
    return 1
  fi
  # Return the first match
  printf '%s' "${matches[0]}"
  return 0
}

copy_env_and_secrets() {
  local staging="$1"
  local env_src="$staging/$REQUIRED_ENV_NAME"

  if [ ! -f "$env_src" ]; then
    err "Env file not found in staging: $env_src"
    return 1
  fi

  mkdir -p "$ETC_DIR"
  chmod 750 "$ETC_DIR"

  # Copy env file, ensuring strict perms (owner root, group root, 640)
  cp -f -- "$env_src" "$ETC_DIR/geohod-dev.env"
  chown root:root "$ETC_DIR/geohod-dev.env"
  chmod 640 "$ETC_DIR/geohod-dev.env"

  # Optionally pick up a pg-password file if provided (named 'pg-password')
  if [ -f "$staging/pg-password" ]; then
    cp -f -- "$staging/pg-password" "$ETC_DIR/pg-password"
    chown root:root "$ETC_DIR/pg-password"
    chmod 600 "$ETC_DIR/pg-password"
  fi

  log "Environment and secrets copied to $ETC_DIR with strict permissions"
  return 0
}

# Ensure required host data directories exist and have safe ownership/permissions.
# Defaults can be overridden by exporting PG_UID/PG_GID/BACKEND_UID/BACKEND_GID before invoking this script.
ensure_data_dirs() {
  log "Ensuring host data directories exist and have correct permissions"
  local failed=0

  for d in "$POSTGRES_DATA_DIR" "$BACKEND_DATA_DIR"; do
    if [ ! -d "$d" ]; then
      log "Creating directory: $d"
      mkdir -p "$d" || { err "Failed to create $d"; failed=1; continue; }
    fi
  done

  # Attempt to set ownership using supplied/default UIDs/GIDs. If chown fails (e.g., non-root),
  # log a warning but continue — the systemd unit/podman run may still fail later if ownership is required.
  if chown "${DEFAULT_PG_UID}:${DEFAULT_PG_GID}" "$POSTGRES_DATA_DIR" 2>/dev/null; then
    log "Set ownership ${DEFAULT_PG_UID}:${DEFAULT_PG_GID} on $POSTGRES_DATA_DIR"
  else
    log "Warning: failed to chown $POSTGRES_DATA_DIR (check UID/GID or run as root)"
  fi

  if chown "${DEFAULT_BACKEND_UID}:${DEFAULT_BACKEND_GID}" "$BACKEND_DATA_DIR" 2>/dev/null; then
    log "Set ownership ${DEFAULT_BACKEND_UID}:${DEFAULT_BACKEND_GID} on $BACKEND_DATA_DIR"
  else
    log "Warning: failed to chown $BACKEND_DATA_DIR (check UID/GID or run as root)"
  fi

  # Apply conservative permissions
  chmod 700 "$POSTGRES_DATA_DIR" || true
  chmod 750 "$BACKEND_DATA_DIR" || true

  [ "$failed" -eq 0 ]
}

install_unit_files() {
  local staging="$1"
  local unit_dir="$staging/systemd"
  if [ ! -d "$unit_dir" ]; then
    err "Systemd unit directory not found in staging: $unit_dir"
    return 1
  fi

  # Copy each .service file into /etc/systemd/system/
  local failed=0
  local copied=0
  for f in "$unit_dir"/*.service; do
    [ -e "$f" ] || continue
    local dest="$SYSTEMD_DIR/$(basename "$f")"
    if cp -f -- "$f" "$dest"; then
      chown root:root "$dest"
      chmod 644 "$dest"
      log "Installed unit: $(basename "$f") -> $dest"
      copied=$((copied+1))
    else
      err "Failed to copy $f -> $dest"
      failed=1
      continue
    fi
  done

  if [ "$copied" -eq 0 ]; then
    err "No unit files were copied from staging ($unit_dir). Expected at least one .service file."
    return 1
  fi

  if [ "$failed" -ne 0 ]; then
    err "One or more unit files failed to copy"
    return 1
  fi

  log "Unit files installed to $SYSTEMD_DIR (count: $copied)"
  return 0
}

enable_and_start_units() {
  # Units in order per plan
  local units=(
    "container-geohod-postgres-dev.service"
    "geohod-dev-pod.service"
    "container-geohod-backend-dev.service"
  )

  systemctl daemon-reload

  for u in "${units[@]}"; do
    log "Checking unit file exists for: $u"
    if [ ! -f "$SYSTEMD_DIR/$u" ]; then
      err "Unit file missing: $SYSTEMD_DIR/$u"
      err "Aborting enable/start sequence"
      return 1
    fi

    log "Enabling unit: $u"
    if ! systemctl enable "$u"; then
      err "systemctl enable failed for $u"
      systemctl status "$u" --no-pager || true
      return 1
    fi

    log "Starting unit: $u"
    if ! systemctl restart "$u"; then
      err "Failed to start $u"
      systemctl status "$u" --no-pager || true
      return 1
    fi

    # Wait a moment for service state to settle
    sleep 2
  done

  log "Requested enable & start for dev units"
  return 0
}

stop_and_disable_units() {
  local units=(
    "container-geohod-backend-dev.service"
    "geohod-dev-pod.service"
    "container-geohod-postgres-dev.service"
  )

  for u in "${units[@]}"; do
    log "Stopping unit: $u"
    systemctl stop "$u" || true
    log "Disabling unit: $u"
    systemctl disable "$u" || true
  done

  systemctl daemon-reload
  log "Units stopped and disabled"
  return 0
}

remove_units_files() {
  local names=( "container-geohod-backend-dev.service" "geohod-dev-pod.service" "container-geohod-postgres-dev.service" )
  local failed=0
  for n in "${names[@]}"; do
    local p="$SYSTEMD_DIR/$n"
    if [ -f "$p" ]; then
      rm -f -- "$p" || { err "Failed to remove $p"; failed=1; }
      log "Removed unit file: $p"
    fi
  done
  systemctl daemon-reload
  [ "$failed" -eq 0 ]
}

show_status() {
  systemctl status container-geohod-postgres-dev.service --no-pager || true
  systemctl status geohod-dev-pod.service --no-pager || true
  systemctl status container-geohod-backend-dev.service --no-pager || true
  return 0
}

perform_podman_image_load() {
  local tarball="$1"
  if [ ! -f "$tarball" ]; then
    err "Image tarball missing: $tarball"
    return 1
  fi

  log "Loading application image from tarball (this may take a while)..."
  podman load -i "$tarball"
  log "Image load completed"
  return 0
}

ensure_postgres_image() {
  # Ensure postgres image is present (pull if not)
  if podman image exists "$PG_IMG" >/dev/null 2>&1; then
    log "Postgres image already present: $PG_IMG"
    return 0
  fi

  log "Pulling Postgres image: $PG_IMG"
  podman pull "$PG_IMG"
  log "Postgres image available"
  return 0
}

main_install() {
  local staging="$1"

  # validation
  if [ ! -d "$staging" ]; then
    err "Staging directory does not exist: $staging"
    return 20
  fi

  validate_prereqs || return 30

  # Required artifacts: image tarball, env file, systemd dir
  local tarball
  tarball="$(find_image_tarball "$staging")" || { err "Image tarball matching '$REQUIRED_ARTIFACT_IMAGE_GLOB' not found in staging"; return 20; }

  if [ ! -f "$staging/$REQUIRED_ENV_NAME" ]; then
    err "Required env file '$REQUIRED_ENV_NAME' missing in staging"
    return 20
  fi

  if [ ! -d "$staging/systemd" ]; then
    err "Required 'systemd' directory (with unit files) missing in staging"
    return 20
  fi

  # Copy secrets/env
  copy_env_and_secrets "$staging" || return 50

  # Load image
  perform_podman_image_load "$tarball" || return 40

  # Ensure postgres image
  ensure_postgres_image || return 40

  # Install units
  install_unit_files "$staging" || return 40

  # Ensure host data directories exist & have correct ownership so Podman bind mounts succeed.
  ensure_data_dirs || { err "Failed to prepare host data directories"; return 50; }

  # Enable and start
  enable_and_start_units || return 40

  log "Install completed successfully"
  return 0
}

main_remove() {
  validate_prereqs || return 30
  stop_and_disable_units || return 0
  remove_units_files || return 0
  log "Remove completed"
  return 0
}

main_status() {
  validate_prereqs || return 30
  show_status
  return 0
}

main_dry_run() {
  local staging="$1"
  log "Dry-run validation for staging: $staging"
  if [ ! -d "$staging" ]; then
    err "Staging directory does not exist: $staging"
    return 20
  fi

  local tarball
  if tarball="$(find_image_tarball "$staging")"; then
    log "Would load image: $tarball"
  else
    log "No image tarball found matching '$REQUIRED_ARTIFACT_IMAGE_GLOB' (would fail)"
  fi

  if [ -f "$staging/$REQUIRED_ENV_NAME" ]; then
    log "Env file present: $staging/$REQUIRED_ENV_NAME"
  else
    log "Env file missing: $staging/$REQUIRED_ENV_NAME"
  fi

  if [ -d "$staging/systemd" ]; then
    log "Unit files present under: $staging/systemd"
    ls -1 "$staging/systemd"/*.service 2>/dev/null || true
  else
    log "Unit files directory missing: $staging/systemd"
  fi

  log "Dry-run complete"
  return 0
}

# Entrypoint
if [ "${#}" -lt 2 ]; then
  usage
  exit 10
fi

cmd="$1"
staging_dir="$2"

case "$cmd" in
  install|update)
    main_install "$staging_dir"
    exit $?
    ;;
  remove)
    main_remove
    exit $?
    ;;
  status)
    main_status
    exit $?
    ;;
  dry-run)
    main_dry_run "$staging_dir"
    exit $?
    ;;
  *)
    usage
    exit 10
    ;;
esac