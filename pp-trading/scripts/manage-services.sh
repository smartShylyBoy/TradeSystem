#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
RUNTIME_DIR="$ROOT_DIR/scripts/.runtime"
LOG_DIR="$RUNTIME_DIR/logs"
PID_DIR="$RUNTIME_DIR/pids"
ENV_FILE="$ROOT_DIR/.env"
DB_CONTAINER_NAME="${DB_CONTAINER_NAME:-trading-timescaledb}"
DB_IMAGE="${DB_IMAGE:-timescale/timescaledb:latest-pg16}"
BACKEND_MODULES=("web-service" "market-data-service")
FRONTEND_MODULE="web-app"

log() {
  printf '[pp-trading] %s\n' "$1"
}

ensure_runtime_dirs() {
  mkdir -p "$LOG_DIR" "$PID_DIR"
}

load_env() {
  if [[ -f "$ENV_FILE" ]]; then
    set -a
    # shellcheck disable=SC1090
    source "$ENV_FILE"
    set +a
  fi

  DB_HOST="${DB_HOST:-localhost}"
  DB_PORT="${DB_PORT:-5432}"
  DB_NAME="${DB_NAME:-trading_platform}"
  DB_USER="${DB_USER:-trading}"
  DB_PASSWORD="${DB_PASSWORD:-trading123}"
}

require_command() {
  if ! command -v "$1" >/dev/null 2>&1; then
    log "Missing required command: $1"
    exit 1
  fi
}

resolve_maven() {
  if [[ -x "$ROOT_DIR/mvnw" ]]; then
    printf '%s' "$ROOT_DIR/mvnw"
    return 0
  fi

  if command -v mvn >/dev/null 2>&1; then
    printf '%s' "mvn"
    return 0
  fi

  return 1
}

resolve_frontend_runner() {
  local module_dir="$ROOT_DIR/$FRONTEND_MODULE"

  if [[ -f "$module_dir/pnpm-lock.yaml" ]] && command -v pnpm >/dev/null 2>&1; then
    printf '%s' "pnpm"
    return 0
  fi

  if [[ -f "$module_dir/yarn.lock" ]] && command -v yarn >/dev/null 2>&1; then
    printf '%s' "yarn"
    return 0
  fi

  if [[ -f "$module_dir/package.json" ]] && command -v npm >/dev/null 2>&1; then
    printf '%s' "npm"
    return 0
  fi

  return 1
}

pid_is_running() {
  local pid="$1"
  kill -0 "$pid" >/dev/null 2>&1
}

start_process() {
  local name="$1"
  local module_dir="$2"
  local log_file="$LOG_DIR/$name.log"
  local pid_file="$PID_DIR/$name.pid"
  shift 2

  if [[ -f "$pid_file" ]]; then
    local existing_pid
    existing_pid="$(cat "$pid_file")"
    if [[ -n "$existing_pid" ]] && pid_is_running "$existing_pid"; then
      log "$name is already running (pid $existing_pid)"
      return 0
    fi
    rm -f "$pid_file"
  fi

  (
    cd "$module_dir"
    nohup "$@" > "$log_file" 2>&1 &
    echo $! > "$pid_file"
  )

  log "Started $name, log: $log_file"
}

stop_process() {
  local name="$1"
  local pid_file="$PID_DIR/$name.pid"

  if [[ ! -f "$pid_file" ]]; then
    log "$name is not running"
    return 0
  fi

  local pid
  pid="$(cat "$pid_file")"

  if [[ -z "$pid" ]] || ! pid_is_running "$pid"; then
    rm -f "$pid_file"
    log "$name is not running"
    return 0
  fi

  kill "$pid" >/dev/null 2>&1 || true

  for _ in {1..10}; do
    if ! pid_is_running "$pid"; then
      rm -f "$pid_file"
      log "Stopped $name"
      return 0
    fi
    sleep 1
  done

  kill -9 "$pid" >/dev/null 2>&1 || true
  rm -f "$pid_file"
  log "Force stopped $name"
}

wait_for_database() {
  local attempts=30

  for ((i=1; i<=attempts; i++)); do
    if docker exec "$DB_CONTAINER_NAME" pg_isready -U "$DB_USER" -d "$DB_NAME" >/dev/null 2>&1; then
      return 0
    fi
    sleep 2
  done

  log "Database did not become ready in time"
  exit 1
}

initialize_database_if_needed() {
  local init_sql="$ROOT_DIR/db/init/01_init_kline.sql"

  if [[ ! -f "$init_sql" ]]; then
    log "Skip database initialization: $init_sql not found"
    return 0
  fi

  local table_count
  table_count="$(docker exec -i "$DB_CONTAINER_NAME" psql -U "$DB_USER" -d "$DB_NAME" -tAc "SELECT count(*) FROM pg_tables WHERE schemaname = 'public' AND tablename IN ('kline_daily','kline_weekly','kline_monthly');" | tr -d '[:space:]')"

  if [[ "$table_count" == "3" ]]; then
    log "Database schema already initialized"
    return 0
  fi

  if [[ "$table_count" == "0" ]]; then
    log "Initializing database schema"
    docker exec -i "$DB_CONTAINER_NAME" psql -U "$DB_USER" -d "$DB_NAME" < "$init_sql"
    return 0
  fi

  log "Database schema is partially initialized (found $table_count/3 tables); skipping auto-init"
}

start_database() {
  require_command docker
  load_env

  if docker container inspect "$DB_CONTAINER_NAME" >/dev/null 2>&1; then
    if [[ "$(docker inspect -f '{{.State.Running}}' "$DB_CONTAINER_NAME")" == "true" ]]; then
      log "Database container is already running"
    else
      docker start "$DB_CONTAINER_NAME" >/dev/null
      log "Started database container $DB_CONTAINER_NAME"
    fi
  else
    docker run -d \
      --name "$DB_CONTAINER_NAME" \
      --restart unless-stopped \
      -p "$DB_PORT:5432" \
      -e POSTGRES_DB="$DB_NAME" \
      -e POSTGRES_USER="$DB_USER" \
      -e POSTGRES_PASSWORD="$DB_PASSWORD" \
      -v "$DB_CONTAINER_NAME-data:/var/lib/postgresql/data" \
      "$DB_IMAGE" >/dev/null
    log "Created and started database container $DB_CONTAINER_NAME"
  fi

  wait_for_database
  initialize_database_if_needed
}

stop_database() {
  require_command docker

  if ! docker container inspect "$DB_CONTAINER_NAME" >/dev/null 2>&1; then
    log "Database container $DB_CONTAINER_NAME does not exist"
    return 0
  fi

  if [[ "$(docker inspect -f '{{.State.Running}}' "$DB_CONTAINER_NAME")" == "true" ]]; then
    docker stop "$DB_CONTAINER_NAME" >/dev/null
    log "Stopped database container $DB_CONTAINER_NAME"
  else
    log "Database container $DB_CONTAINER_NAME is already stopped"
  fi
}

start_backends() {
  local mvn_cmd
  if ! mvn_cmd="$(resolve_maven)"; then
    log "Skipping Java services: Maven is not available"
    return 0
  fi

  for module in "${BACKEND_MODULES[@]}"; do
    local module_dir="$ROOT_DIR/$module"
    if [[ -f "$module_dir/pom.xml" ]]; then
      start_process "$module" "$module_dir" "$mvn_cmd" spring-boot:run
    else
      log "Skipping $module: pom.xml not found"
    fi
  done
}

start_frontend() {
  local module_dir="$ROOT_DIR/$FRONTEND_MODULE"
  if [[ ! -d "$module_dir" ]]; then
    log "Skipping $FRONTEND_MODULE: directory not found"
    return 0
  fi

  local runner
  if ! runner="$(resolve_frontend_runner)"; then
    log "Skipping $FRONTEND_MODULE: package.json or package manager not found"
    return 0
  fi

  case "$runner" in
    pnpm)
      start_process "$FRONTEND_MODULE" "$module_dir" pnpm dev
      ;;
    yarn)
      start_process "$FRONTEND_MODULE" "$module_dir" yarn dev
      ;;
    npm)
      start_process "$FRONTEND_MODULE" "$module_dir" npm run dev
      ;;
  esac
}

stop_app_services() {
  stop_process "$FRONTEND_MODULE"

  for module in "${BACKEND_MODULES[@]}"; do
    stop_process "$module"
  done
}

show_status() {
  if command -v docker >/dev/null 2>&1 && docker container inspect "$DB_CONTAINER_NAME" >/dev/null 2>&1; then
    log "database: $(docker inspect -f '{{.State.Status}}' "$DB_CONTAINER_NAME")"
  else
    log "database: not created"
  fi

  for name in "$FRONTEND_MODULE" "${BACKEND_MODULES[@]}"; do
    local pid_file="$PID_DIR/$name.pid"
    if [[ -f "$pid_file" ]]; then
      local pid
      pid="$(cat "$pid_file")"
      if [[ -n "$pid" ]] && pid_is_running "$pid"; then
        log "$name: running (pid $pid)"
      else
        log "$name: stopped"
      fi
    else
      log "$name: stopped"
    fi
  done
}

start_all() {
  ensure_runtime_dirs
  start_database
  start_backends
  start_frontend
  log "Startup sequence finished"
}

stop_all() {
  ensure_runtime_dirs
  stop_app_services
  stop_database
  log "Shutdown sequence finished"
}

usage() {
  cat <<'EOF'
Usage: bash scripts/manage-services.sh <start|stop|restart|status>
EOF
}

main() {
  local action="${1:-}"

  case "$action" in
    start)
      start_all
      ;;
    stop)
      stop_all
      ;;
    restart)
      stop_all
      start_all
      ;;
    status)
      ensure_runtime_dirs
      show_status
      ;;
    *)
      usage
      exit 1
      ;;
  esac
}

main "$@"
