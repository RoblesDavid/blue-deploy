## Objetivo

Desplegar BlueDeploy en un VPS de Hetzner Cloud con los tres servicios contenerizados: MariaDB, Spring Boot y Nginx (frontend + proxy inverso).

---

## Arquitectura en producción

```
Internet ──▶ Nginx :80 ──▶ /          → React SPA (estático)
                       ──▶ /api/*     → Spring Boot :8080 (red interna)
                                              │
                                         MariaDB :3306 (red interna)
```

Solo Nginx expone un puerto al exterior (80). Los otros dos servicios viven en la red Docker interna `blue-net`, inaccesibles desde fuera del servidor.

---

## Archivos añadidos y por qué

### `frontend/Dockerfile`

Build multi-etapa:
- **Etapa 1 (Node 20 Alpine):** instala dependencias y ejecuta `npm run build` → genera `/app/dist`
- **Etapa 2 (Nginx Alpine):** copia el bundle estático y el `nginx/default.conf`

Sin este Dockerfile el frontend solo puede ejecutarse en modo desarrollo (`npm run dev`). En producción necesita un servidor de ficheros estáticos.

### `nginx/default.conf`

Configura Nginx para:
1. `try_files $uri $uri/ /index.html` — redirige cualquier ruta desconocida a `index.html` para que React Router funcione (SPA HTML5 history)
2. `location /api/` — reenvía las peticiones al backend `http://app:8080` mediante proxy inverso
3. Caché agresiva (1 año) en assets JS/CSS/imágenes con nombre hasheado (Vite los hashea automáticamente)
4. `server_tokens off` — oculta la versión de Nginx por seguridad

### `docker-compose.prod.yml`

Diferencias clave respecto a `docker-compose.yml` (desarrollo):

| Aspecto | Desarrollo | Producción |
|---------|-----------|------------|
| MariaDB puerto externo | 3306:3306 | (no expuesto) |
| Spring Boot puerto externo | 8080:8080 | (no expuesto) |
| Frontend | no existe | nginx :80 |
| Restart policy | always | unless-stopped |
| Healthchecks | no | sí (orquesta el arranque) |
| Red explícita | no | blue-net |

### `.github/workflows/ci.yml`

Ejecuta en cada push y PR:
- Tests de integración del backend con H2
- Build de producción del frontend (`npm run build`)
- Lint del frontend

Evita que llegue código roto a `main`.

### `.github/workflows/deploy.yml`

Se dispara únicamente al hacer push/merge a `main`. Pasos:
1. Corre los tests (falla el deploy si hay errores)
2. SSH al servidor Hetzner
3. `git pull` + `docker compose build` + `docker compose up -d`
4. Limpia imágenes antiguas

Requiere configurar estos **Secrets** en GitHub → Settings → Secrets:
- `HETZNER_HOST` — IP pública del VPS
- `HETZNER_USER` — usuario SSH 
- `HETZNER_SSH_KEY` — clave privada SSH 
- `HETZNER_PORT` — (opcional) si el SSH no está en el 22

### `scripts/deploy/setup-hetzner.sh`

Script idempotente para preparar un VPS limpio:
1. Actualiza el sistema
2. Instala Docker + Docker Compose Plugin
3. Crea usuario sin privilegios root
4. Configura UFW: permite SSH (22), HTTP (80), HTTPS (443); bloquea el resto
5. Clona el repositorio en `/opt/bluedeploy`
6. Solicita editar el `.env` antes de continuar
7. Lanza `docker compose -f docker-compose.prod.yml up -d`

### `.env.production.example`

Plantilla con todas las variables de entorno necesarias en producción, incluyendo marcadores para las de práctica 2 (JWT, API Key). El archivo `.env` real nunca debe commitearse.

---

## Guía de despliegue paso a paso

### 1. Contratar el VPS en Hetzner

- Ir a console.hetzner.com → Cloud → New Server
- Imagen: **Ubuntu 22.04**
- Tipo: **CX22** (2 vCPU, 4 GB RAM) — suficiente para la práctica
- Región: Falkenstein (EU) o Helsinki
- Añadir tu clave SSH pública en el proceso de creación

### 2. Configurar el repositorio en GitHub

Ir a tu repositorio → **Settings → Secrets and variables → Actions** y añadir:

```
HETZNER_HOST     → 1.2.3.4  (IP del servidor)
HETZNER_USER     → root      (o el usuario deploy)
HETZNER_SSH_KEY  → (pegar el contenido de tu clave privada ~/.ssh/id_ed25519)
```

Generar par de claves si no tienes:
```bash
ssh-keygen -t ed25519 -C "bluedeploy-deploy" -f ~/.ssh/bluedeploy_deploy
# Añadir la pública al servidor: ssh-copy-id -i ~/.ssh/bluedeploy_deploy.pub root@IP
```

### 3. Primer despliegue manual (una sola vez)

```bash
ssh root@IP_DEL_SERVIDOR

# Descargar y ejecutar el script de setup
curl -fsSL https://raw.githubusercontent.com/TU_USUARIO/blue-deploy/main/scripts/deploy/setup-hetzner.sh -o setup.sh
bash setup.sh
```

El script pedirá que edites el `.env` antes de continuar:
```bash
nano /opt/bluedeploy/.env
```
Rellenar con contraseñas reales (usar `openssl rand -base64 32` para generarlas).

### 4. Verificar que está funcionando

```bash
# Desde el servidor:
docker compose -f /opt/bluedeploy/docker-compose.prod.yml ps
docker compose -f /opt/bluedeploy/docker-compose.prod.yml logs -f

# Desde tu navegador:
http://IP_DEL_SERVIDOR       → Login del dashboard React
http://IP_DEL_SERVIDOR/api/checks/health  → {"status":"UP"}
```

### 5. Despliegues posteriores (automáticos)

Cada vez que hagas push a `main`, GitHub Actions:
1. Corre los tests
2. SSH al servidor
3. Reconstruye y reinicia los contenedores

El tiempo de downtime es de ~30–60 segundos durante el rebuild.

---

## Troubleshooting

### El frontend devuelve 404 al recargar una ruta

Verificar que `nginx/default.conf` tiene `try_files $uri $uri/ /index.html;`. Sin esto, Nginx busca el fichero físico y devuelve 404 para rutas SPA como `/admin/deployments`.

### El frontend no puede llamar a `/api/`

```bash
docker compose -f docker-compose.prod.yml exec frontend nginx -t
# Verificar que proxy_pass apunta a http://app:8080
```

### Spring Boot no arranca (error de conexión a BD)

Los healthchecks garantizan el orden, pero MariaDB puede tardar en inicializar:
```bash
docker compose -f docker-compose.prod.yml logs db
# Esperar a ver: "ready for connections" antes de que arranque app
```

### Los cambios en el código no se reflejan

```bash
docker compose -f docker-compose.prod.yml build --no-cache
docker compose -f docker-compose.prod.yml up -d
```

---

## Pendiente (Práctica 2)

- Añadir HTTPS con Let's Encrypt (opciones: Certbot + Nginx, o Caddy como proxy inverso)
- Configurar dominio propio en Hetzner DNS
- Añadir `.env` con `JWT_SECRET` y `API_KEY` para autenticación real
- Monitorización con Grafana + Prometheus o Uptime Kuma
- Backups automáticos del volumen `db_data` a Hetzner Object Storage
