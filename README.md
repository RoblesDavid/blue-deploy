# 🛡️ BlueDeploy

Proyecto de automatización de despliegue de sistemas Windows con monitorización de seguridad tipo **Blue Team / SOC**.

---

## 🎯 Objetivo

Automatizar el despliegue de equipos Windows mediante OSDCloud y validar su estado de seguridad en tiempo real mediante checks tipo CIS, centralizando los resultados en un dashboard web.

---

## 🧱 Estado actual (v0.2 - MVP funcional)

### Backend
- API REST en Spring Boot
- Arquitectura limpia: DTO → Service → Entity
- Persistencia en MariaDB
- Recepción y validación de JSON
- Procesamiento de checks (firewall)

### Frontend
- Dashboard SOC en React + Vite
- Auto-refresh de datos
- Visualización de estados (ON / OFF / UNKNOWN)
- Filtros por estado
- Timeline de eventos
- Métricas en tiempo real

### Seguridad
- Sistema básico de autenticación
- Validación de datos de entrada (DTO + Jakarta Validation)

---

## 🛠️ Tecnologías

### Backend
- Java 21
- Spring Boot
- Maven
- MariaDB
- Docker

### Frontend
- React
- Vite
- React Router

---

## 🚀 Ejecución

🔄 Flujo del sistema
PowerShell → API REST → Service → DB → Dashboard

📊 Ejemplo de check
{
  "date": "2026-05-01T16:22:03.367Z",
  "user": "soc-analyst",
  "hostname": "frontend-node",
  "checks": {
    "firewall_enabled": true
  }
}

### Backend + DB

```bash
docker compose build
docker compose up

## 📌 Roadmap (resumen)

* [ ] Multi-check real (antivirus, disk encryption, updates)
* [ ] Sistema de alertas (HIGH / MEDIUM / LOW)
* [ ] Dashboard avanzado (gráficas, SIEM-like)
* [ ] Autenticación real (JWT)
* [ ] Checks CIS automatizados
* [ ] Hardening Docker + DB
* [ ] Integración con scripts PowerShell reales
* [ ] Integración con IA
 
---

Proyecto desarrollado como parte de formación en Ciberseguridad e IA.
