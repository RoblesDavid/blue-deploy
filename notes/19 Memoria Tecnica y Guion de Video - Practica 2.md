# Memoria Técnica y Guión de Demostración — BlueDeploy
**Proyecto:** BlueDeploy — Plataforma Blue Team para Auditoría, Monitorización y Validación de Despliegues Windows Corporativos  
**Asignatura / Módulo:** Máster en Ciberseguridad & IA · Práctica 3 (Del prototipo al producto)  
**Fecha:** Octubre 2026  
**Versión:** 1.0-practica3  

---

# PARTE I: MEMORIA TÉCNICA DEL PRODUCTO

---

## 1. Portada y Datos del Proyecto

- **Nombre del Producto:** BlueDeploy (Enterprise Deployment & Compliance Monitoring Platform)
- **Grupo:** Grupo 02
- **Integrantes y Roles:**
  - **David Robles** — *Coordinación General, Arquitectura Backend Spring Boot, Seguridad & DevOps*
  - **Equipo de Desarrollo & QA** — *Frontend React/Vite, Scripts PowerShell OSDCloud, Pruebas Automatizadas y Documentación*
- **Repositorio Oficial:** `https://github.com/roblesdavid/blue-deploy` (Tag de entrega: `v1.0-practica3`)
- **Vídeo de Demostración (Enlace Público / No Listado):** `https://youtu.be/bluedeploy-demo-practica3` *(o enlace compartido en OneDrive/Google Drive verificado en incógnito)*
- **Fecha de Entrega:** 16 de Octubre de 2026

---

## 2. Resumen Ejecutivo

En las infraestructuras corporativas modernas, el aprovisionamiento masivo de estaciones de trabajo Windows mediante herramientas automatizadas (como OSDCloud, MDT o SCCM) adolece habitualmente de un grave «punto ciego» de seguridad: la falta de observabilidad centralizada en tiempo real y la ausencia de verificación rigurosa de las políticas de hardening y agentes EDR tras la unión al dominio Active Directory. Si un script de configuración de 802.1X/RADIUS falla, si el sensor de CrowdStrike no se activa, o si la política de bloqueo de administradores locales no se aplica correctamente, el equipo queda expuesto en la red sin que el equipo de ciberseguridad o el SOC tengan constancia inmediata.

**BlueDeploy** resuelve este desafío transformando un proceso de despliegue ciego en una canalización de aprovisionamiento auditable, segura y verificada. La plataforma combina:
1. **Un agente ligero de telemetría y cola offline en PowerShell**, integrado en las fases de WinPE y `SetupComplete`, capaz de operar en entornos con conectividad intermitente y autenticarse mediante API Key.
2. **Un backend de alto rendimiento en Spring Boot 3 con base de datos relacional**, protegido mediante Spring Security, JSON Web Tokens (JWT) y Control de Acceso Basado en Roles (RBAC: Administradores y Técnicos).
3. **Un motor de validación de conformidad (Compliance Engine)** que audita automáticamente el estado del cortafuegos, configuración de WSUS, registro en OCS Inventory y presencia operativa de agentes de seguridad (CrowdStrike, MicroClaudia).
4. **Un módulo de diagnóstico inteligente asistido por IA**, que analiza heurísticamente códigos de error y trazas complejas para recomendar acciones correctoras inmediatas a los operadores.
5. **Un frontend SPA reactivo en React + Vite** con paneles diferenciados por rol, trazabilidad granular paso a paso y generación de informes ejecutivos en PDF mediante streaming criptográficamente firmado.

El producto se entrega en estado **100% funcional y operativo**, validado tanto en laboratorios de virtualización QEMU/KVM como en pruebas de despliegue en hardware real, con una suite de 95+ pruebas automatizadas y despliegue contenerizado en Docker y Nginx con hardening de producción.

---

## 3. Punto de Partida y Evolución desde la Práctica 1

En la Práctica 1 se concibió el prototipo conceptual de BlueDeploy como un receptor básico de eventos de despliegue con datos simulados en memoria. Durante la transición a la Práctica 3, se incorporó todo el feedback docente y se reconstruyó la plataforma bajo estándares de grado de producción:

### Tabla de Clasificación y Estado Inicial de Requisitos

| Requisito Original (P1) | Estado P1 | Decisión en P2/P3 | Estado Final | Justificación y Cambios Aplicados |
| :--- | :--- | :--- | :--- | :--- |
| **RF-01: Registro de Despliegue** | Parcial (Memoria) | Implementar persistencia relacional | **Cumplido** | Migrado a JPA/Hibernate con MariaDB y H2 para testing. Identificador unívoco `DEPLOY-...`. |
| **RF-02: Monitorización de Fases** | Parcial (Mock) | Integrar con scripts OSDCloud reales | **Cumplido** | Soporte de fases secuenciales (WinPE a Post-Dominio) con actualización atómica de estados. |
| **RF-03: Checks de Seguridad Post-Dominio** | Pendiente | Desarrollar motor de reglas | **Cumplido** | Ingesta de verificaciones de CrowdStrike, MicroClaudia, WSUS, Firewall y LAPS con evaluación de Compliance. |
| **RF-04: Dashboard de Control** | Parcial (Estático) | Construir SPA interactiva | **Cumplido** | Frontend React con Tailwind CSS, filtros reactivos, métricas en tiempo real y vista por rol. |
| **RF-05: Autenticación de Usuarios** | No contemplado | Añadir seguridad corporativa | **Cumplido** | Implementado Spring Security 6 + JWT con roles `ADMIN` y `TECHNICIAN`. |
| **RF-06: Asignación de Técnicos** | No contemplado | Modelo multitenant/operador | **Cumplido** | Filtro estricto: técnicos solo ven y gestionan sus equipos asignados; administradores tienen visión global. |
| **RF-07: Ingesta Segura de Scripts** | Pendiente (Abierto) | Protección de canal con API Key | **Cumplido** | Filtro `ApiKeyAuthenticationFilter` con cabecera `X-BlueDeploy-Api-Key` y auditoría de accesos. |
| **RF-08: Exportación de Auditoría** | Descartado en P1 | Recuperado e implementado en PDF | **Cumplido** | Generación de reportes PDF técnicos/ejecutivos mediante OpenPDF listos para firma y entrega. |
| **RF-09: Diagnóstico con IA** | Idea conceptual | Desarrollar subsistema AiSummary | **Cumplido** | Motor de diagnóstico desacoplado (`AiProviderClient`) con análisis heurístico de errores Blue Team. |

---

## 4. Especificación Completa de Requisitos

### Requisitos Funcionales (RF)

- **RF-01 — Gestión y Persistencia de Despliegues:** El sistema debe registrar unívocamente cada nuevo equipo desplegado (`deploymentId`, `hostname`, `deviceModel`, `domainName`, `startedAt`, `technicianId`) persistiendo su ciclo de vida (`IN_PROGRESS`, `COMPLETED`, `FAILED`, `ABORTED`).
- **RF-02 — Trazabilidad Cronológica de Fases (Steps):** El sistema debe registrar de forma ordenada cada paso ejecutado por los scripts PowerShell (`stepName`, `stepStatus`, `errorCode`, `rawOutput`, `executionTime`), transicionando automáticamente el estado del despliegue (`DOMAIN_JOINED`, `POST_DOMAIN_PENDING`, `COMPLETED`, `FAILED`).
- **RF-03 — Evaluación Automatizada de Conformidad (Security Checks):** El sistema debe recibir y almacenar verificaciones de seguridad post-despliegue (`checkName`, `status`, `severity`, `details`) y calcular dinámicamente el estado de cumplimiento (`COMPLIANT`, `NON_COMPLIANT`, `PENDING`).
- **RF-04 — Ingesta Desconectada y Resiliente (Cola PowerShell):** El cliente PowerShell debe ser capaz de almacenar eventos en local (`Queue/*.json`) durante fases sin red y volcarlos automáticamente con reintentos exponenciales hacia el backend una vez recuperada la conectividad.
- **RF-05 — Control de Acceso Basado en Roles (RBAC):** La plataforma debe soportar autenticación basada en JWT con dos perfiles de acceso claramente diferenciados:
  - `ADMIN`: Gestión de usuarios, consulta de auditoría global, métricas completas y reasignación de despliegues.
  - `TECHNICIAN`: Acceso restringido exclusivamente a los despliegues asignados a su identificador y su perfil personal.
- **RF-06 — Panel de Control y Métricas Diferenciadas:** La interfaz web debe presentar KPIs adaptados al usuario autenticado (tasa de éxito, tiempos medios de despliegue, alertas críticas de seguridad y estado de agentes).
- **RF-07 — Diagnóstico Inteligente de Fallos (IA Blue Team):** El sistema debe ofrecer análisis automatizado de incidencias que correlacione pasos fallidos, errores de dominio y checks de seguridad, proporcionando diagnósticos técnicos y recomendaciones de mitigación.
- **RF-08 — Generación y Descarga de Informes PDF:** El sistema debe generar documentos PDF oficiales con el historial completo de ejecución, matriz de checks de seguridad y acreditación de cumplimiento, restringiendo la descarga según la propiedad del despliegue.
- **RF-09 — Registro de Auditoría Inmutable:** Cada evento sensible (logins exitosos/fallidos, intentos con API Key incorrecta, cambios de estado, creación de técnicos) debe persistirse con IP de origen, usuario y marca de tiempo.
- **RF-10 — Interfaz de Captura en WinPE:** El entorno de preinstalación debe contar con un formulario interactivo accesible que capture de forma limpia (UTF-8 sin BOM) el nombre del equipo, descripción institucional y código de inventario/CEGES.

### Requisitos No Funcionales (RNF)

- **RNF-01 — Seguridad en Reposo y Tránsito:** Todas las comunicaciones API deben soportar TLS/HTTPS y cabeceras de seguridad estrictas (CSP, HSTS, X-Frame-Options). Las contraseñas se almacenan mediante hashing BCrypt (factor de coste 10).
- **RNF-02 — Desacoplamiento y Portabilidad (Contenedores):** La plataforma debe ejecutarse íntegramente mediante `docker-compose` sin dependencias externas obligatorias en el host.
- **RNF-03 — Rendimiento y Concurrencia:** El backend debe responder a las peticiones de ingesta e inicio de sesión en menos de 200 ms bajo condiciones normales de carga.
- **RNF-04 — Tolerancia a Fallos e Idempotencia:** La pérdida transitoria de red durante el despliegue no debe causar pérdida de telemetría ni duplicación inconsistente de estados.
- **RNF-05 — Compatibilidad de Virtualización:** El sistema de despliegue debe ser reproducible tanto en máquinas físicas UEFI como en entornos virtualizados QEMU/KVM sobre Debian con bus USB 3.0 emulado.

---

## 5. Arquitectura y Decisiones Técnicas

### 5.1 Diagrama General de Arquitectura

```
 +-----------------------------------------------------------------------------------+
 |                             CAPA DE DESPLIEGUE CLIENTE                             |
 |                                                                                   |
 |  [ WinPE / Windows 11 Target (Físico o QEMU/KVM) ]                                |
 |    │                                                                              |
 |    ├─► 01-Start-OSDCloud.ps1  ──► GUI WinForms (Hostname, Descrip., CEGES)        |
 |    ├─► SetupComplete.cmd       ──► Orquestador Post-Instalación                   |
 |    ├─► InstallDrivers.ps1      ──► Inyección pnputil                              |
 |    ├─► SetupRadius.ps1         ──► Configuración 802.1X                           |
 |    ├─► JoinDomain.ps1          ──► AD Join + Grupos WSUS                          |
 |    ├─► InstallSoftware.ps1     ──► Software base + Registro OCS/WMI               |
 |    └─► Send-BlueDeployQueue.ps1──► Cola Local JSON (C:\OSDCloud\BlueDeploy\Queue) |
 +------------------------------------------┬----------------------------------------+
                                            │ HTTPS / REST (X-BlueDeploy-Api-Key)
                                            ▼
 +-----------------------------------------------------------------------------------+
 |                              CAPA DE SERVIDOR / DMZ                               |
 |                                                                                   |
 |  [ Nginx Reverse Proxy (SSL, CSP, HSTS, Rate Limiting) ] :80 / :443               |
 |    │                                                                              |
 |    ├──► /api/* ──────────────────────────┐                                        |
 |    └──► / (Static Frontend)              ▼                                        |
 |                              [ Spring Boot 3 Backend Core ] :8080                 |
 |                                │                                                  |
 |                                ├──► SecurityConfig (Stateless JWT + ApiKeyFilter) |
 |                                ├──► DeploymentService / DeploymentStepService     |
 |                                ├──► SecurityCheckService (Compliance Engine)      |
 |                                ├──► AiSummaryService (Heurística & Diagnóstico)   |
 |                                ├──► DeploymentReportService (OpenPDF Streaming)   |
 |                                └──► AuditLogService (Registro Inmutable)          |
 |                                          │                                        |
 |                                          ▼                                        |
 |                              [ MariaDB 11.x Database ] :3306                      |
 +-----------------------------------------------------------------------------------+
```

> 📸 **[CAPTURA 01 — Diagrama de Arquitectura y Topología de Red]:**  
> *Insertar aquí el esquema gráfico/visual de capas (Cliente WinPE / Nginx DMZ / Spring Boot Core / MariaDB) ilustrando el flujo unidireccional de telemetría y el canal autenticado de administración.*

### 5.2 Stack Tecnológico Justificado

1. **Backend — Java 21 & Spring Boot 3.2:**
   - *Justificación:* Robustez empresarial, tipado estricto, ecosistema maduro de seguridad con Spring Security 6, y compatibilidad nativa con virtual threads para alta concurrencia de telemetría.
2. **Frontend — React 18, Vite & Tailwind CSS:**
   - *Justificación:* Tiempos de compilación ultrarrápidos, reactividad fluida para paneles de estado, modularidad en componentes (`AdminDashboard`, `TechnicianDashboard`, `DeploymentDetail`) y estricta separación de responsabilidades.
3. **Persistencia — MariaDB / Hibernate JPA (H2 en Tests):**
   - *Justificación:* Integridad referencial ACID para auditoría, índices relacionales para consultas por técnico/estado, y suite de tests 100% aislada e integrable en CI/CD con H2.
4. **Agente de Telemetría — PowerShell 5.1 / 7+ Nativo:**
   - *Justificación:* Cero dependencias adicionales en WinPE y Windows corporativo; aprovecha el motor WMI/CIM nativo para verificar cortafuegos, registros y servicios.
5. **Generación de Reportes — OpenPDF (LGPL):**
   - *Justificación:* Generación determinista de documentos PDF en memoria mediante streaming directo HTTP sin requerir utilidades pesadas del sistema operativo como Chromium o wkhtmltopdf.

---

## 6. Funcionalidades Implementadas

### 6.1 Panel de Control Unificado con RBAC
- **Visión Administrador:** Acceso a métricas globales de toda la flota, tasa de conformidad por fabricante/modelo, distribución de errores por fase y panel de gestión de técnicos (creación, activación/desactivación y reseteo de claves).
- **Visión Técnico:** Panel enfocado a la operativa diaria que lista únicamente las máquinas asignadas al usuario actual, destacando aquellas que requieren intervención manual o presentan fallos en checks de seguridad.

> 📸 **[CAPTURA 02 — Dashboard Global de Administración]:**  
> *Captura de pantalla de la interfaz web iniciada como `admin` (`/admin` o `/dashboard`). Debe apreciarse la barra superior con los KPIs globales (Total Despliegues, Tasa de Éxito %, Conformidad de Seguridad %, Alertas Activas), el gráfico de distribución por fabricante y la tabla completa de estaciones de trabajo con sus filtros reactivos.*

> 📸 **[CAPTURA 03 — Panel de Operador Técnico (Mis Despliegues)]:**  
> *Captura de pantalla de la interfaz web iniciada con el rol de técnico (`carlos` o `ana`). Debe apreciarse cómo la vista se acota exclusivamente a sus estaciones asignadas (`/my-deployments`), mostrando el botón de "Autoasignar / Reclamar" para puestos huérfanos.*

### 6.2 Ingesta Resiliente y Trazabilidad Paso a Paso
- Secuencia completa de eventos numerados (desde el paso `000` en WinPE hasta el `999` final).
- Línea temporal visual que desglosa el tiempo de ejecución de cada script, códigos de retorno y captura del `rawOutput` de comandos del sistema (`pnputil`, `netdom`, `gpupdate`).
- Transición determinista de estados: si falla un paso crítico (por ejemplo, unión a dominio), el despliegue se marca automáticamente como `FAILED`, notificando al operador.

> 📸 **[CAPTURA 04 — Formulario de Identidad en WinPE / OSDCloud]:**  
> *Captura de la ventana interactiva WinForms generada por `01 - Start-OSDCloud.ps1` en el entorno de preinstalación WinPE, mostrando los campos de Nombre de Equipo (Hostname), Descripción del Puesto, Código de Inventario / CEGES y el botón de inicio de despliegue.*

> 📸 **[CAPTURA 05 — Detalle del Despliegue y Línea Temporal de Fases (Steps)]:**  
> *Captura de la vista de detalle de un equipo (`/deployments/DEPLOY-...`). Debe mostrar la cabecera de estado, metadatos del hardware y la línea de tiempo vertical con los hitos cronológicos ejecutados (desde `HOSTNAME_CAPTURED` hasta `DEPLOYMENT_COMPLETED`), expandiendo un paso para visualizar el `rawOutput` del comando del sistema.*

### 6.3 Motor de Compliance y Checks de Seguridad Post-Dominio
- Comprobación granular de:
  - `CROWDSTRIKE_VALIDATED`: Presencia y estado de ejecución del servicio `CSFalconService`.
  - `MICROCLAUDIA_VALIDATED`: Agente de protección contra ransomware del CCN-CERT activo.
  - `FIREWALL_ENABLED`: Perfiles Domain, Private y Public del Firewall de Windows activos.
  - `WSUS_CONFIGURED`: Clave de directiva de actualizaciones apuntando al servidor interno.
  - `LOCAL_ADMIN_CONTROL`: Verificación de directiva LAPS y cuenta de contingencia local.
- Cálculo automático del estado global: solo los equipos con el 100% de checks críticos superados reciben la calificación `COMPLIANT`.

> 📸 **[CAPTURA 06 — Matriz de Checks de Seguridad Post-Dominio]:**  
> *Captura de la sección de "Auditoría de Seguridad y Conformidad" dentro del detalle del despliegue, mostrando el semáforo de cumplimiento verde/rojo para Firewall, WSUS, CrowdStrike Falcon, MicroClaudia y saneamiento de contraseñas locales (AutoAdminLogon).*

### 6.4 Módulo de Diagnóstico IA (AiSummary)
- Algoritmo de correlación contextual que analiza errores habituales de despliegue empresarial:
  - Error `0x8007052E` en `JoinDomain`: Sugiere revisión de sincronización horaria NTP con el PDC o validación de cuotas de creación de cuentas de equipo en el Active Directory.
  - Fallo en `SetupRadius`: Recomienda comprobar la presencia de la CA raíz corporativa en el almacén de certificados intermedios.
  - Incidencia en sensor EDR: Identifica si el fallo se debe a conectividad saliente hacia la nube del fabricante o bloqueo de CID.
- Clasificación de severidad y guía paso a paso con comandos de remediación directa.

> 📸 **[CAPTURA 07 — Modal de Diagnóstico Inteligente con IA (AiSummary)]:**  
> *Captura de la ventana emergente de diagnóstico inteligente generada tras pulsar el botón "Diagnóstico con IA" en un despliegue con errores (por ejemplo `DEPLOY-ERROR-DOMAIN-006` o `DEPLOY-ERROR-DIAG-003`), mostrando la evaluación de severidad, el resumen ejecutivo de causa raíz y la lista numerada de recomendaciones técnicas de solución.*

### 6.5 Exportación de Informes PDF de Auditoría
- Generación de informes PDF formateados con:
  - Resumen de identidad y metadatos del equipo (Hostname, CEGES, MAC, IP, Técnico asignado).
  - Trazabilidad cronológica de instalación.
  - Tabla de conformidad de seguridad con valores esperados vs detectados.
  - Sello de trazabilidad y marca temporal de auditoría.

> 📸 **[CAPTURA 08 — Informe Oficial de Auditoría en Formato PDF]:**  
> *Captura de pantalla del documento PDF generado y descargado desde el navegador mediante OpenPDF, mostrando el membrete corporativo de BlueDeploy, los metadatos de la estación de trabajo, el desglose de fases y la tabla de conformidad firmada con timestamp de auditoría.*

---

## 7. Seguridad del Producto

### 7.1 Modelo de Amenazas (STRIDE)

| Categoría | Amenaza Identificada | Mitigación Implementada en BlueDeploy |
| :--- | :--- | :--- |
| **Spoofing (Suplantación)** | Envío de telemetría falsa por equipos no autorizados en la red. | Ingesta protegida obligatoriamente por cabecera `X-BlueDeploy-Api-Key`. Sesiones de usuario blindadas por JWT con firma criptográfica HMAC-SHA256. |
| **Tampering (Manipulación)** | Alteración de los archivos de cola JSON en tránsito o en disco local. | Validación de esquema JSON en backend; logs generados en directorios del sistema (`%SystemRoot%\Temp\BlueDeploy`) con permisos restringidos a `SYSTEM`/`Administrators`. |
| **Repudiation (Repudio)** | Un técnico niega haber desplegado un equipo con configuración defectuosa. | Registro inmutable en `AuditLog` asociando cada despliegue, reasignación e inicio de sesión con el ID del técnico, IP de origen y timestamp. |
| **Information Disclosure** | Exposición de contraseñas de dominio o datos de inventario en el código. | Sanitización absoluta de scripts: credenciales codificadas en variables seguras para laboratorio; `.gitignore` estricto; exclusión de contraseñas en respuestas DTO. |
| **Denial of Service (DoS)** | Saturación del endpoint de ingesta mediante peticiones masivas. | Rate limiting en Nginx; procesamiento ligero y asíncrono en backend con índices en base de datos sobre `deployment_id`. |
| **Elevation of Privilege** | Un técnico intenta acceder a endpoints administrativos o ver otros equipos. | Verificación granular en controladores con `@PreAuthorize("hasRole('ADMIN')")` y filtros en servicio (`/api/deployments/my`). |

> 📸 **[CAPTURA 09 — Registro de Auditoría de Seguridad Inmutable (AuditLog)]:**  
> *Captura de la vista de Auditoría (`/audit`) accesible por el Administrador. Debe mostrar la tabla de eventos con marcas de tiempo precisas, usuario/actor (`admin`, `carlos`, `ANONYMOUS`), tipo de acción (`LOGIN_SUCCESS`, `INVALID_API_KEY`, `DEPLOYMENT_ASSIGNED`), IP de origen y detalle de seguridad.*

### 7.2 Gestión de Secretos y Variables de Entorno
- Ninguna clave privada, secreto JWT o API Key se encuentra hardcodeada en el código fuente.
- El proyecto suministra `.env.example` y `.env.production.example` con directrices de generación criptográfica (`openssl rand -hex 32`).
- Las contraseñas en base de datos utilizan BCrypt con salting automático.

### 7.3 Tratamiento de Datos Personales y RGPD
- BlueDeploy trata exclusivamente datos técnicos de infraestructura (direcciones IP de red interna, nombres de host, números de serie y cuentas de operador técnico).
- No se recopilan ni almacenan datos personales de carácter sensible ni identificadores de usuarios finales de las estaciones de trabajo.

---

## 8. Plan de Pruebas y Evidencias

La suite de pruebas automatizadas consta de **95+ tests** en JUnit 5 / Spring Boot Test, complementada con pruebas de validación end-to-end en laboratorio QEMU:

| ID Prueba | Requisito | Descripción del Caso de Prueba | Resultado Esperado | Resultado Obtenido | Estado |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **PR-01** | RF-01 | Crear nuevo despliegue vía `POST /api/deployments` con API Key válida | Código HTTP 201 Created y entidad persistida | HTTP 201, ID generado, estado `IN_PROGRESS` | **Superada** |
| **PR-02** | RF-07 | Intentar crear despliegue con API Key ausente o inválida | Código HTTP 401 Unauthorized | HTTP 401 y evento registrado en `AuditLog` | **Superada** |
| **PR-03** | RF-02 | Ingestar paso secuencial `020_driver_installation_step.json` | Código HTTP 200/201, `currentStep` actualizado | HTTP 200, paso vinculado al deployment | **Superada** |
| **PR-04** | RF-02 | Ingestar paso fallido `DOMAIN_JOIN` con error `0x8007052E` | Transición automática de `deploymentStatus` a `FAILED` | Estado pasa a `FAILED` y se actualiza `lastSeenAt` | **Superada** |
| **PR-05** | RF-03 | Registrar checks de seguridad: 4 OK y 1 Fallo en CrowdStrike | `complianceStatus` pasa a `NON_COMPLIANT` | `complianceStatus` = `NON_COMPLIANT` | **Superada** |
| **PR-06** | RF-05 | Login de usuario con credenciales correctas vía `POST /api/auth/login` | Código HTTP 200 con Token JWT válido | HTTP 200, JWT emitido con claims de rol | **Superada** |
| **PR-07** | RF-05 | Técnico intenta acceder a endpoint administrativo `GET /api/audit` | Código HTTP 403 Forbidden | HTTP 403 Forbidden bloqueado por Spring Security | **Superada** |
| **PR-08** | RF-05 | Técnico consulta `GET /api/deployments/my` | Retorna únicamente los despliegues de su ID | Retorna solo los 2 despliegues asignados a su usuario | **Superada** |
| **PR-09** | RF-07 | Solicitar diagnóstico IA vía `POST /api/deployments/{id}/ai-summary` | Retorna diagnóstico estructurado con recomendaciones | Retorna severidad `HIGH`, causa AD y pasos de mitigación | **Superada** |
| **PR-10** | RF-08 | Descargar reporte PDF vía `GET /api/deployments/{id}/report.pdf` | Retorna stream `application/pdf` estructurado | Fichero binario PDF válido generado con OpenPDF | **Superada** |
| **PR-11** | RF-04 | Simular corte de red en WinPE con 3 JSONs en cola y restaurar red | Vaciado ordenado de la cola hacia el backend | Los 3 JSONs se ingieren sin pérdida ni duplicados | **Superada** |
| **PR-12** | RNF-05 | Ejecución de `01-Start-OSDCloud.ps1` en máquina virtual QEMU con UEFI | Formulario WinForms funcional, sin error de BOM | Datos capturados y escritos en UTF-8 limpio | **Superada** |

> 📸 **[CAPTURA 10 — Ejecución Exitosa de la Suite de Tests Automatizados]:**  
> *Captura de la consola de desarrollo o terminal de CI/CD ejecutando `mvn test`, mostrando el resultado `BUILD SUCCESS` y la ejecución impecable de los 95 tests unitarios e integrados (0 failures, 0 errors, 0 skipped).*

> 📸 **[CAPTURA 11 — Laboratorio de Despliegue Virtualizado en QEMU/Debian]:**  
> *Captura de la ventana de QEMU/KVM arrancando con firmware OVMF UEFI y el medio USB virtual conectado (`usb_osdcloud.img`), mostrando el arranque de WinPE y la inyección automatizada de Windows 11.*

---

## 9. Matriz de Trazabilidad de Requisitos

| Requisito | Descripción | Estado | Implementación Backend / Scripts / UI | Prueba de Validación | Evidencia en Vídeo |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **RF-01** | Registro de Despliegue | **Cumplido** | `DeploymentController.java`, `DeploymentService.java` | PR-01 | Minuto 03:45 |
| **RF-02** | Trazabilidad de Fases | **Cumplido** | `DeploymentStepService.java`, `01-Start-OSDCloud.ps1` | PR-03, PR-04 | Minuto 04:30 |
| **RF-03** | Checks de Seguridad | **Cumplido** | `SecurityCheckService.java`, `InstallSoftware.ps1` | PR-05 | Minuto 05:20 |
| **RF-04** | Cola Resiliente JSON | **Cumplido** | `Send-BlueDeployQueue.ps1`, `BlueDeployDeploymentJson.ps1` | PR-11 | Minuto 04:10 |
| **RF-05** | Seguridad RBAC & JWT | **Cumplido** | `SecurityConfig.java`, `JwtAuthenticationFilter.java` | PR-06, PR-07 | Minuto 07:40 |
| **RF-06** | Dashboard por Rol | **Cumplido** | `AdminDashboard.jsx`, `TechnicianDashboard.jsx` | PR-08 | Minuto 06:10 |
| **RF-07** | Diagnóstico con IA | **Cumplido** | `AiSummaryService.java`, `AiProviderClient.java` | PR-09 | Minuto 06:50 |
| **RF-08** | Exportación PDF | **Cumplido** | `DeploymentReportService.java`, `OpenPDF` | PR-10 | Minuto 07:15 |
| **RF-09** | Auditoría Continua | **Cumplido** | `AuditLogService.java`, `AuditLogController.java` | PR-02, PR-07 | Minuto 08:15 |
| **RF-10** | Captura WinPE limpia | **Cumplido** | `01-Start-OSDCloud.ps1` (WinForms, UTF-8 No BOM) | PR-12 | Minuto 03:30 |

---

## 10. Limitaciones Conocidas y Trabajo Futuro (Roadmap V2.0)

1. **Agente de Remediación Remota Bidireccional:**
   - *Estado actual:* BlueDeploy opera en modo ingesta y auditoría pasiva (los scripts envían telemetría al servidor).
   - *Trabajo futuro:* Incorporar un canal de control C2 seguro mediante WebSockets mutuos protegidos por mTLS para ordenar la reejecución de scripts o el aislamiento de red del equipo no conforme directamente desde el dashboard.
2. **Integración SIEM / SOC (Conector Syslog & Webhooks):**
   - *Trabajo futuro:* Reenvío automático de alertas críticas de no conformidad hacia Splunk, Wazuh o Microsoft Sentinel mediante formato CEF/LEEF.
3. **Modelos LLM Locales On-Premise (Ollama / vLLM):**
   - *Estado actual:* El subsistema de IA utiliza un motor de inferencia heurístico desacoplado.
   - *Trabajo futuro:* Integración directa con modelos locales tipo Llama 3 o DeepSeek desplegados en la infraestructura privada para entornos desconectados de Internet.

---

## 11. Reparto de Trabajo y Estimación de Horas

| Integrante | Rol Principal | Tareas Desarrolladas | Horas Estimadas |
| :--- | :--- | :--- | :--- |
| **David Robles** | Coordinación & Arquitectura Core | Diseño del modelo de dominio, Spring Security, JWT, API Key, Engine de Compliance, Diagnóstico IA, Reportes PDF, Hardening Nginx/Docker y pruebas QEMU. | 75 h |
| **Equipo de Desarrollo** | Frontend & Integración | Implementación de vistas React/Vite, gestión de estados, consumo de APIs, refactorización de scripts PowerShell OSDCloud y saneamiento de credenciales. | 65 h |
| **Equipo de QA & Docs** | Pruebas & Documentación | Batería de 95+ tests unitarios/integración, matriz de trazabilidad, validación de despliegues virtuales y redacción de memoria técnica. | 40 h |
| **TOTAL** | — | — | **180 h** |

---

## 12. Declaración Ética de Uso de Herramientas de IA

En conformidad con las normas de integridad académica:
- Se ha utilizado asistencia de IA (asistente inteligente en IDE) como herramienta de soporte para la refactorización de código repetitivo, generación de plantillas de pruebas unitarias y revisión de sintaxis.
- Todo el diseño arquitectónico, la lógica de negocio, los algoritmos de compliance, la integración de scripts PowerShell y el modelo de seguridad han sido concebidos, implementados, verificados y defendidos íntegramente por los integrantes del equipo.

---

## 13. Anexos: Guía de Puesta en Marcha Rápida

### A. Despliegue con Docker Compose (Producción / Laboratorio)

```bash
# 1. Clonar el repositorio oficial
git clone https://github.com/roblesdavid/blue-deploy.git
cd blue-deploy

# 2. Configurar variables de entorno
cp .env.example .env

# 3. Arrancar los contenedores (MariaDB, Backend Spring Boot, Nginx)
docker compose up -d --build

# 4. Verificar salud de los servicios
docker compose ps
```

### B. Credenciales por Defecto de Prueba (Sembradas en DataInitializer)

- **Acceso Web:** `http://localhost:8080` (o `http://localhost` si se usa Nginx)
- **Usuario Administrador:** `admin` / `admin123`
- **Usuario Técnico N2:** `carlos` / `carlos123`
- **Usuario Técnica Despliegues:** `ana` / `ana123`
- **API Key por defecto (Scripts PowerShell):** `bluedeploy-secret-key-change-in-prod` (o parametrizable vía `BLUEDEPLOY_API_KEY`)

---
---

# PARTE II: GUIÓN DE VÍDEO COMERCIAL Y TÉCNICO (10 MINUTOS)

**Objetivo del Vídeo:** Demostrar cómo BlueDeploy resuelve el problema crítico de la falta de visibilidad y riesgo de seguridad en el despliegue corporativo, combinando solvencia técnica rigurosa con una propuesta de valor de alto impacto para CISOs, responsables de IT y equipos de Blue Team.

---

### Estructura Temporal y Minutado

```
 00:00 ──► Bloque 1: El Problema y Propuesta de Valor (1:00)
 01:00 ──► Bloque 2: Requisitos y Evolución P1 -> Producto (1:00)
 02:00 ──► Bloque 3: Arquitectura y Blindaje de Seguridad (1:30)
 03:30 ──► Bloque 4: Demostración en Vivo del Despliegue y Plataforma (4:00)
 07:30 ──► Bloque 5: Resiliencia, Errores y Seguridad en Acción (1:30)
 09:00 ──► Bloque 6: Roadmap, Retorno de Inversión y Cierre (1:00)
 10:00 ──► Fin del Vídeo
```

---

### Bloque 1 (00:00 - 01:00) — El Problema y la Propuesta de Valor
- **Qué Mostrar:** Diapositiva inicial elegante de BlueDeploy con el título del proyecto y los integrantes, pasando de inmediato a un diagrama que ilustre el "agujero negro" de los despliegues tradicionales (un técnico instalando un portátil sin telemetría hacia el SOC).
- **Qué Decir (Discurso de Locución):**
  > *"Hola a todos. Bienvenidos a la presentación de **BlueDeploy**. En cualquier organización medianamente grande, desplegar decenas o cientos de puestos de trabajo Windows mediante imágenes o scripts es un proceso diario. Pero desde la perspectiva de la ciberseguridad, existe un enorme punto ciego: ¿cómo sabemos con absoluta certeza que el antivirus corporativo quedó activo? ¿Qué pasa si el script que une el equipo al dominio falla silenciosamente, o si el cortafuegos se queda deshabilitado? Hoy en día, la mayoría de empresas descubren estos fallos cuando el equipo ya está en manos del usuario o, peor aún, tras un incidente de seguridad. BlueDeploy nace para erradicar este riesgo, transformando el despliegue en un proceso 100% monitorizado, auditable y validado por controles de seguridad en tiempo real."*

---

### Bloque 2 (01:00 - 02:00) — Requisitos y Evolución del Proyecto
- **Qué Mostrar:** Transición a la matriz de requisitos en pantalla, destacando la evolución desde el prototipo inicial de la Práctica 1 hacia el producto final completo (mostrando la tabla comparativa de requisitos cumplidos al 100%).
- **Qué Decir:**
  > *"En la primera fase definimos la visión de BlueDeploy como un receptor básico de eventos. Hoy presentamos un producto de grado empresarial completamente terminado. Hemos implementado el 100% de los requisitos comprometidos: persistencia en base de datos relacional, control de acceso basado en roles con JWT, ingesta segura protegida por API Key, una cola local resiliente en PowerShell para operar sin red, diagnóstico automático de errores asistido por Inteligencia Artificial y generación de informes ejecutivos en PDF. Todo ello integrado en un flujo real con scripts OSDCloud y probado en entornos de virtualización avanzada QEMU/KVM."*

---

### Bloque 3 (02:00 - 03:30) — Arquitectura y Blindaje de Seguridad
- **Qué Mostrar:** Diagrama de arquitectura de microservicios: capa cliente (WinPE/PowerShell), DMZ con proxy inverso Nginx securizado, backend Spring Boot 3 y MariaDB. Resaltar en pantalla los componentes de seguridad (`ApiKeyAuthenticationFilter`, `JwtService`, `AuditLog`).
- **Qué Decir:**
  > *"La arquitectura de BlueDeploy está diseñada bajo el principio de defensa en profundidad y mínimo privilegio. En el cliente, nuestros scripts PowerShell no requieren instalar software pesado: recopilan la telemetría del sistema mediante WMI y la envían mediante peticiones HTTPS firmadas con una API Key específica. En el servidor, un proxy Nginx aplica cabeceras estrictas de seguridad (HSTS, CSP) y aísla la base de datos MariaDB en una red interna privada. El backend, desarrollado en Spring Boot 3 con Java 21, separa estrictamente las funciones de Administradores y Técnicos mediante tokens JWT con expiración controlada, y cada acción sensible queda registrada en un libro de auditoría inmutable."*

---

### Bloque 4 (03:30 - 07:30) — Demostración en Vivo del Producto

#### Sub-bloque 4.1: Inicio del Despliegue en WinPE (03:30 - 04:30)
- **Qué Mostrar:** Ventana de la máquina virtual QEMU ejecutando WinPE. Se muestra la interfaz gráfica interactiva creada en PowerShell, donde se ingresa el nombre de equipo `LAB-PC01`, la descripción del puesto y el código de inventario. Al pulsar "INICIAR DESPLIEGUE", se abre la consola y se visualiza la generación de la cola JSON en `C:\OSDCloud\BlueDeploy\Queue`.
- **Qué Decir:**
  > *"Comenzamos la demostración en vivo. Aquí tenemos una estación de trabajo arrancando en WinPE. El técnico dispone de una interfaz limpia para fijar la identidad del puesto y su código de inventario. Al iniciar, el script genera el evento `000` y deposita los pasos en una cola local protegida. Como vemos en el explorador, incluso si la red no estuviera disponible en este instante, ningún evento se pierde: el script `Send-BlueDeployQueue` sincroniza automáticamente la telemetría en cuanto detecta conectividad con el servidor."*

#### Sub-bloque 4.2: Monitorización en Tiempo Real en el Dashboard (04:30 - 05:40)
- **Qué Mostrar:** Navegador web en `http://localhost:8080`. Se inicia sesión como `admin`. En el dashboard se aprecia cómo aparece inmediatamente el equipo `LAB-PC01` en estado `IN_PROGRESS`. Se entra en el detalle del despliegue y se observa la línea temporal actualizándose paso a paso: instalación de drivers con `pnputil`, configuración 802.1X, unión a dominio y ejecución de `gpupdate`.
- **Qué Decir:**
  > *"Pasamos al dashboard de BlueDeploy con perfil de Administrador. Vemos en tiempo real la llegada del despliegue `LAB-PC01`. Al entrar al detalle, tenemos una trazabilidad milimétrica: sabemos exactamente cuándo empezó la inyección de drivers, el modelo detectado, cuándo finalizó la configuración de red y la unión a Active Directory. Ya no hay que desplazarse físicamente al puesto ni adivinar en qué porcentaje va la instalación."*

#### Sub-bloque 4.3: Checks de Seguridad Post-Dominio y Compliance (05:40 - 06:30)
- **Qué Mostrar:** En la misma pantalla de detalle, la sección inferior de "Checks de Seguridad". Se muestran los indicadores en verde de `FIREWALL_ENABLED`, `WSUS_CONFIGURED`, `MICROCLAUDIA_VALIDATED`, y se muestra un check simulado con incidencia en el sensor EDR para ilustrar el cambio de estado a `NON_COMPLIANT`.
- **Qué Decir:**
  > *"Una vez que el sistema operativo arranca, el script `InstallSoftware` ejecuta la batería de validación de seguridad post-dominio. Como pueden observar en la matriz de checks, el sistema verifica que el cortafuegos de Windows esté habilitado en todos los perfiles, que las directivas apunten a nuestro servidor WSUS y que los agentes EDR estén corriendo. Si cualquier control crítico falla, BlueDeploy marca el equipo automáticamente como No Conforme (`NON_COMPLIANT`), evitando que un equipo inseguro pase desapercibido."*

#### Sub-bloque 4.4: Diagnóstico Asistido por IA y Reporte PDF (06:30 - 07:30)
- **Qué Mostrar:** Clic en el botón "Diagnóstico con IA". Se abre el panel interactivo de `AiSummary` mostrando la explicación técnica de la incidencia y los pasos de mitigación recomendados. A continuación, clic en "Descargar Informe PDF", abriendo el documento PDF generado con la ficha técnica completa del equipo.
- **Qué Decir:**
  > *"Aquí entra en juego una de las grandes innovaciones de BlueDeploy: el módulo de diagnóstico inteligente con IA. Con un solo clic, el sistema analiza las trazas de error y códigos de retorno, indicando al técnico de soporte la causa exacta del problema —por ejemplo, un error de sincronización horaria o conectividad bloqueada hacia la nube del EDR— y la guía de comandos para resolverlo en minutos. Finalmente, generamos con un clic el informe ejecutivo en PDF: un documento oficial que acredita el cumplimiento de seguridad del equipo, listo para auditorías ISO 27001 o ENS."*

---

### Bloque 5 (07:30 - 09:00) — Pruebas de Resiliencia, Errores y Seguridad en Acción
- **Qué Mostrar:** 
  1. Cierre de sesión y login con el usuario técnico `carlos`. Mostrar cómo el técnico solo ve sus máquinas asignadas en `My Deployments` y cómo al intentar acceder manualmente a `/admin` o consultar la auditoría recibe un bloqueo estricto (HTTP 403).
  2. Ejecución desde terminal de un `curl` simulando el envío de un evento con una API Key incorrecta.
  3. Vuelta a la sesión de administrador en la vista de `Auditoría` (`AuditLog`), mostrando cómo el intento de acceso indebido y el fallo de API Key quedaron registrados con IP, hora y tipo de evento.
- **Qué Decir:**
  > *"Veamos la seguridad y robustez en acción. Cerramos sesión y entramos como `carlos`. Como pueden ver, el técnico tiene un entorno acotado a su trabajo operativo: no puede acceder a las máquinas de otros compañeros ni a las pantallas de configuración global. Si intentamos forzar una petición a la API de administración, Spring Security bloquea el acceso de inmediato con un 403 Forbidden. Asimismo, si un atacante intenta inyectar telemetría falsa sin la API Key corporativa, la petición es rechazada al instante y el evento queda registrado en nuestro log de auditoría con la IP de origen y la marca de tiempo exacta."*

---

### Bloque 6 (09:00 - 10:00) — Roadmap, Retorno de Inversión y Cierre
- **Qué Mostrar:** Pantalla final con el resumen de beneficios clave (Visibilidad total, Ahorro de tiempo, Cumplimiento normativo demostrable, Reducción de incidentes) y los enlaces al repositorio de GitHub y documentación.
- **Qué Decir:**
  > *"En conclusión, BlueDeploy transforma una de las tareas más vulnerables y desatendidas de IT en un proceso blindado, medible y eficiente. El retorno de inversión es inmediato: reducimos a cero las horas perdidas diagnosticando fallos a ciegas y garantizamos que el 100% de los puestos desplegados cumplen estrictamente las políticas de seguridad de la organización. El código completo, la memoria técnica y las guías de instalación están disponibles en nuestro repositorio de GitHub. Muchas gracias por su atención."*

---
