# Despliegue en Hetzner Cloud y Hardening de Producción

## Objetivo

Desplegar BlueDeploy en un VPS de Hetzner Cloud con arquitectura multicapa contenerizada: MariaDB 11, Spring Boot 3 / Java 21 y Nginx (frontend SPA React + reverse proxy con hardening y preparación SSL).

---

## Arquitectura en Producción (Práctica 2)

```text
Internet ──▶ Nginx :80 / :443 (Reverse Proxy + SPA)
                 │
                 ├──▶ /              → React SPA (Estático con Cache-Control)
                 ├──▶ /.well-known/  → Certbot Challenge (Let's Encrypt)
                 └──▶ /api/*         → Spring Boot :8080 (Red interna blue-net)
                                              │
                                         MariaDB :3306 (Red interna blue-net)
```

Solo Nginx expone puertos al exterior (80 y 443). MariaDB y Spring Boot residen de forma estricta en la red bridge interna `blue-net`, inaccesibles directamente desde Internet.

---

## Evolución Práctica 2 — Seguridad y Hardening

### Variables de entorno añadidas (`.env`):
```text
JWT_SECRET=
BLUEDEPLOY_API_KEY=
CORS_ALLOWED_ORIGINS=
DB_NAME=bluedeploy_prod
DB_USER=bluedeploy_user
DB_PASSWORD=
DB_ROOT_PASSWORD=
SPRING_PROFILES_ACTIVE=prod
```

### Mejoras de seguridad aplicadas:
- **Cabeceras HTTP de seguridad en Nginx (`nginx/default.conf`)**:
  - `X-Frame-Options: DENY` (anti-clickjacking).
  - `X-Content-Type-Options: nosniff` (anti-MIME sniffing).
  - `Referrer-Policy: strict-origin-when-cross-origin`.
  - `Content-Security-Policy`: restricción de orígenes de ejecución y conectividad.
  - `server_tokens off`: ocultación de banner de versión de Nginx.
- **Aislamiento de servicios**: MariaDB y backend sin mapeo de puertos al host (`ports: []` omitido).
- **Políticas de firewall con UFW**: solo puertos 22 (SSH), 80 (HTTP) y 443 (HTTPS) abiertos.
- **Backups automáticos de MariaDB**: script `backup-db.sh` configurado con compresión gzip y retención de 14 días.
- **Preparación HTTPS / Certbot**: volumen `certbot_www` montado en `/.well-known/acme-challenge/`.
- **Sanitización de logs**: ausencia total de tokens o credenciales en texto plano en la salida estándar y archivos de log.

---

## Guía de Despliegue Paso a Paso

### 1. Servidor de Producción Hetzner
- **IP Pública:** `178.105.231.10`
- **Ruta de la Aplicación:** `/opt/bluedeploy`
- **Página de Inicio / Landing Page:** `http://178.105.231.10/` (con introducción, acceso a login y contacto de David Robles Gaona)
- **Endpoint de Telemetría OSDCloud:** `http://178.105.231.10/api/deployments` (autenticado con `X-BlueDeploy-Api-Key`)

### 2. Sincronización Directa de Archivos mediante Rsync
Para subir el código directamente al host manteniendo los archivos de entorno de producción:
```bash
rsync -avz \
  --exclude='.git/' \
  --exclude='.env*' \
  --exclude='node_modules/' \
  --exclude='target/' \
  ./ \
  root@178.105.231.10:/opt/bluedeploy/
```

### 3. Configuración de Secretos en el Servidor
```bash
ssh root@178.105.231.10
# Asegurar la existencia y permisos de .env
cp /opt/bluedeploy/.env.production /opt/bluedeploy/.env 2>/dev/null || true
chmod 600 /opt/bluedeploy/.env
```

### 4. Arranque y Verificación de Salud
```bash
cd /opt/bluedeploy
docker compose -f docker-compose.prod.yml up -d --build
docker compose -f docker-compose.prod.yml ps
curl -i http://localhost/api/checks/health
```
