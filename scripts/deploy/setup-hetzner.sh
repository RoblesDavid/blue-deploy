#!/usr/bin/env bash
# =============================================================================
# setup-hetzner.sh  —  Prepara un VPS Hetzner limpio para BlueDeploy
#
# Uso:   bash setup-hetzner.sh
# SO:    Ubuntu 22.04 / 24.04 (recomendado en Hetzner Cloud)
# Nota:  Idempotente — puede ejecutarse varias veces sin efectos secundarios.
# =============================================================================

set -euo pipefail

APP_DIR="/opt/bluedeploy"
REPO_URL="https://github.com/RoblesDavid/blue-deploy.git"
APP_USER="deploy"

# ── Colores ──────────────────────────────────────────────────────────────────
GREEN='\033[0;32m'; YELLOW='\033[1;33m'; RED='\033[0;31m'; NC='\033[0m'
info()    { echo -e "${GREEN}[INFO]${NC}  $*"; }
warn()    { echo -e "${YELLOW}[WARN]${NC}  $*"; }
error()   { echo -e "${RED}[ERROR]${NC} $*"; exit 1; }

[[ $EUID -ne 0 ]] && error "Ejecutar como root: sudo bash setup-hetzner.sh"

# ── 1. Actualización del sistema ─────────────────────────────────────────────
info "Actualizando paquetes del sistema..."
apt-get update -qq && apt-get upgrade -y -qq

# ── 2. Instalar Docker ───────────────────────────────────────────────────────
if command -v docker &>/dev/null; then
    info "Docker ya instalado: $(docker --version)"
else
    info "Instalando Docker..."
    apt-get install -y -qq ca-certificates curl gnupg
    install -m 0755 -d /etc/apt/keyrings
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
    chmod a+r /etc/apt/keyrings/docker.gpg
    echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
        https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" \
        > /etc/apt/sources.list.d/docker.list
    apt-get update -qq
    apt-get install -y -qq docker-ce docker-ce-cli containerd.io docker-compose-plugin
    systemctl enable --now docker
    info "Docker instalado correctamente"
fi

# ── 3. Usuario de despliegue ─────────────────────────────────────────────────
if ! id "$APP_USER" &>/dev/null; then
    info "Creando usuario $APP_USER..."
    useradd -m -s /bin/bash "$APP_USER"
    usermod -aG docker "$APP_USER"
else
    info "Usuario $APP_USER ya existe"
    usermod -aG docker "$APP_USER"
fi

# ── 4. Firewall (UFW) ────────────────────────────────────────────────────────
if command -v ufw &>/dev/null; then
    info "Configurando UFW..."
    ufw --force reset
    ufw default deny incoming
    ufw default allow outgoing
    ufw allow 22/tcp    comment "SSH"
    ufw allow 80/tcp    comment "HTTP (BlueDeploy)"
    ufw allow 443/tcp   comment "HTTPS (futuro)"
    ufw --force enable
    ufw status verbose
else
    warn "UFW no disponible — configura el firewall manualmente"
fi

# ── 5. Clonar / actualizar repositorio ───────────────────────────────────────
if [[ -d "$APP_DIR/.git" ]]; then
    info "Repositorio ya clonado, haciendo pull..."
    git -C "$APP_DIR" pull origin main
else
    info "Clonando repositorio en $APP_DIR..."
    mkdir -p "$APP_DIR"
    git clone "$REPO_URL" "$APP_DIR"
fi
chown -R "$APP_USER":"$APP_USER" "$APP_DIR"

# ── 6. Crear .env de producción ──────────────────────────────────────────────
ENV_FILE="$APP_DIR/.env"
if [[ -f "$ENV_FILE" ]]; then
    warn ".env ya existe — no se sobreescribe"
else
    info "Creando .env de producción desde plantilla..."
    cp "$APP_DIR/.env.production.example" "$ENV_FILE"
    warn "IMPORTANTE: edita $ENV_FILE con tus contraseñas antes de continuar"
    warn "  nano $ENV_FILE"
    echo ""
    read -r -p "Pulsa ENTER cuando hayas editado el .env... "
fi

# ── 7. Lanzar la aplicación ──────────────────────────────────────────────────
info "Construyendo imágenes Docker (puede tardar varios minutos)..."
cd "$APP_DIR"
docker compose -f docker-compose.prod.yml build

info "Iniciando servicios..."
docker compose -f docker-compose.prod.yml up -d

info "Esperando que los servicios arranquen..."
sleep 10
docker compose -f docker-compose.prod.yml ps

echo ""
echo -e "${GREEN}==================================================${NC}"
echo -e "${GREEN}  BlueDeploy desplegado correctamente             ${NC}"
echo -e "${GREEN}  Accede en: http://$(curl -s ifconfig.me)        ${NC}"
echo -e "${GREEN}==================================================${NC}"
