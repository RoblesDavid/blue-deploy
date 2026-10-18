#!/usr/bin/env bash
# =============================================================================
# setup-hetzner.sh  —  Prepara un VPS Hetzner limpio para BlueDeploy (Hardening P2)
#
# Uso:   bash setup-hetzner.sh
# SO:    Ubuntu 22.04 / 24.04 (Hetzner Cloud)
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

# ── 2. Instalar Docker y herramientas de seguridad ───────────────────────────
if command -v docker &>/dev/null; then
    info "Docker ya instalado: $(docker --version)"
else
    info "Instalando Docker..."
    apt-get install -y -qq ca-certificates curl gnupg ufw fail2ban logrotate
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
    ufw allow 443/tcp   comment "HTTPS (BlueDeploy SSL)"
    ufw --force enable
    ufw status verbose
else
    warn "UFW no disponible — configura el firewall manualmente"
fi

# ── 5. Clonar / actualizar repositorio ───────────────────────────────────────
if [[ -d "$APP_DIR/.git" ]]; then
    info "Repositorio ya clonado, haciendo pull..."
    git -C "$APP_DIR" pull origin main || true
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
    chmod 600 "$ENV_FILE"
    chown "$APP_USER":"$APP_USER" "$ENV_FILE"
    
    # Generar secretos criptográficos aleatorios automáticamente si es no interactivo
    JWT_GEN=$(openssl rand -base64 48 | tr -d '\n')
    API_KEY_GEN=$(openssl rand -hex 32)
    DB_PASS_GEN=$(openssl rand -base64 24 | tr -d '/+=')
    DB_ROOT_GEN=$(openssl rand -base64 24 | tr -d '/+=')

    sed -i "s|CAMBIA_POR_UN_SECRETO_SEGURO_DE_AL_MENOS_64_CARACTERES_ALEATORIOS|${JWT_GEN}|g" "$ENV_FILE"
    sed -i "s|CAMBIA_POR_API_KEY_ALEATORIA_ALFANUMERICA_SEGURA|${API_KEY_GEN}|g" "$ENV_FILE"
    sed -i "s|CAMBIA_ESTA_CONTRASENA_SEGURA_MIN24_CARACTERES|${DB_PASS_GEN}|g" "$ENV_FILE"
    sed -i "s|CAMBIA_ESTA_CONTRASENA_ROOT_SEGURA_MIN24_CARACTERES|${DB_ROOT_GEN}|g" "$ENV_FILE"

    info "Secretos iniciales autogenerados en $ENV_FILE"
fi

# ── 7. Script de Backups automáticos de MariaDB ───────────────────────────────
BACKUP_SCRIPT="$APP_DIR/backup-db.sh"
cat << 'EOF' > "$BACKUP_SCRIPT"
#!/usr/bin/env bash
set -euo pipefail
BACKUP_DIR="/opt/bluedeploy/backups"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
mkdir -p "$BACKUP_DIR"
docker exec blue-db mariadb-dump -u bluedeploy_user -p"$DB_PASSWORD" bluedeploy_prod | gzip > "$BACKUP_DIR/bluedeploy_$TIMESTAMP.sql.gz"
find "$BACKUP_DIR" -type f -name "*.sql.gz" -mtime +14 -delete
EOF
chmod +x "$BACKUP_SCRIPT"
chown "$APP_USER":"$APP_USER" "$BACKUP_SCRIPT"

# ── 8. Lanzar la aplicación ──────────────────────────────────────────────────
info "Construyendo imágenes Docker..."
cd "$APP_DIR"
docker compose -f docker-compose.prod.yml build

info "Iniciando servicios..."
docker compose -f docker-compose.prod.yml up -d

info "Esperando que los servicios arranquen..."
sleep 10
docker compose -f docker-compose.prod.yml ps

echo ""
echo -e "${GREEN}==================================================${NC}"
echo -e "${GREEN}  BlueDeploy Práctica 2 desplegado correctamente  ${NC}"
echo -e "${GREEN}  Accede en: http://$(curl -s ifconfig.me 2>/dev/null || echo 'IP-SERVIDOR')${NC}"
echo -e "${GREEN}==================================================${NC}"
