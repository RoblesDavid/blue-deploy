# ── Etapa 1: compilar el bundle de React ─────────────────────────────────────
# Usamos node:22 (npm 10+) para compatibilidad con lockfileVersion 3
FROM node:22-alpine AS build
WORKDIR /app

# Copiamos manifiestos primero para aprovechar el caché de capas
COPY frontend/package*.json ./
RUN npm install --prefer-offline

COPY frontend/ .
RUN npm run build
# El resultado queda en /app/dist

# ── Etapa 2: servir el bundle con Nginx ───────────────────────────────────────
FROM nginx:1.27-alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx/default.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
