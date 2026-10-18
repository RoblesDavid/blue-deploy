# Diagnóstico Inteligente de Incidencias con IA (AiSummary)

## Objetivo

Proveer asistencia automatizada para la resolución ágil de incidencias en despliegues fallidos o con no conformidades de seguridad, reduciendo el tiempo medio de resolución (MTTR) para los técnicos de soporte y operadores de Blue Team.

---

## Arquitectura del Módulo

- **Entidad `AiSummary`**: Almacena el diagnóstico, causa probable, acciones recomendadas en JSON, severidad y timestamp.
- **Cliente `AiProviderClient`**: Interfaz desacoplada que permite enchufar proveedores externos reales (OpenAI, Anthropic, Ollama local) manteniendo un proveedor heurístico y mock por defecto.
- **Servicio `AiSummaryService`**: Agrega los datos del despliegue (fases OSDCloud, códigos de error, rawOutput y checks de seguridad) y ejecuta el análisis.

---

## Heurísticas y Reglas de Análisis de Blue Team

El motor analiza patrones clave de error:
1. **Unión a Dominio (`DOMAIN_JOIN_FAILURE` / `0x8007052E`)**:
   - Diagnóstico: Problemas de resolución DNS, sincronización horaria NTP (< 5 min) o credenciales delegadas de cuenta de servicio.
   - Severidad: `HIGH`.
2. **Autenticación 802.1X / Radius**:
   - Diagnóstico: Falta de despliegue de certificado de CA raíz o perfil de supplicant wifi rechazado por NPS/Radius.
   - Severidad: `HIGH`.
3. **Agentes EDR (CrowdStrike / MicroClaudia)**:
   - Diagnóstico: Incompatibilidad de versión de sensor, timeout en comunicación con la nube o conectividad CID bloqueada.
   - Severidad: `CRITICAL`.
4. **Cifrado BitLocker / TPM**:
   - Diagnóstico: TPM no inicializado o clave de recuperación no respaldada.
   - Severidad: `HIGH`.

---

## Endpoints REST

- `POST /api/deployments/{deploymentId}/ai-summary`: Genera y persiste un nuevo diagnóstico.
- `GET /api/deployments/{deploymentId}/ai-summary`: Obtiene el último análisis generado.

---

## Interfaz de Usuario

El botón **Diagnóstico IA** en la vista de detalle de despliegue despliega un panel estilizado con:
- Badge de severidad (`CRITICAL`, `HIGH`, `MEDIUM`, `LOW`).
- Causa técnica explicada en lenguaje natural.
- Lista interactiva de recomendaciones operativas paso a paso.
