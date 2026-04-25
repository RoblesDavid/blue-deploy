# BlueDeploy 🚀

Proyecto de automatización de despliegue de Windows con validación de seguridad (Blue Team).

## 🎯 Objetivo

Automatizar el despliegue de equipos Windows mediante OSDCloud e integrar un sistema de auditoría de seguridad basado en checks tipo CIS, centralizando los resultados en un panel web.

## 🧱 Estado actual (MVP)

* API REST en Spring Boot funcionando
* Contenerización con Docker
* Endpoint de prueba `/api/checks`
* Base para integración con scripts PowerShell

## 🛠️ Tecnologías

* Java 21
* Spring Boot
* Docker
* Maven

## 🚀 Cómo ejecutar

```bash
docker compose build
docker compose up
```

Acceder a:

```
http://localhost:8080/api/checks
```

## 📌 Roadmap (resumen)

* [ ] Recepción de JSON desde PowerShell
* [ ] Persistencia de datos
* [ ] Dashboard web
* [ ] Checks CIS automatizados
* [ ] Integración con IA

---

Proyecto desarrollado como parte de formación en Ciberseguridad e IA.
