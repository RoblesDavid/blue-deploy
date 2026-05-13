
## Decisión 1: BlueDeploy no bloqueará inicialmente la unión a dominio

### Contexto

CrowdStrike y MicroClaudia se instalan mediante políticas después de que el equipo se una al dominio corporativo.

### Decisión

BlueDeploy no se plantea inicialmente como un sistema de bloqueo previo a la unión al dominio.

### Justificación

Bloquear la unión al dominio impediría que se apliquen las políticas necesarias para instalar determinados componentes de seguridad.

### Consecuencia

El sistema se orienta a monitorización, trazabilidad, validación post-dominio y generación de alertas.

---

## Decisión 2: El despliegue se modelará por fases

### Contexto

El flujo OSDCloud ejecuta varios scripts y reinicios.

### Decisión

Cada fase relevante se registrará como un DeploymentStep.

### Justificación

Esto permite detectar el punto exacto de fallo y mostrar el progreso del despliegue en el dashboard.

---

## Decisión 3: PowerShell ejecuta, BlueDeploy evalúa

### Contexto

Los scripts PowerShell ya realizan acciones reales sobre el equipo.

### Decisión

PowerShell será responsable de ejecutar acciones y enviar resultados. BlueDeploy será responsable de persistir, evaluar y visualizar.

### Justificación

Evita duplicar lógica y mantiene una arquitectura limpia.

---

## Decisión 4: Check será reemplazado progresivamente por SecurityCheck

### Contexto

El modelo actual Check fue útil para el MVP, pero no representa todo el ciclo de despliegue.

### Decisión

Se introducirá un nuevo modelo SecurityCheck vinculado a Deployment.

### Justificación

Permite registrar controles concretos de seguridad, su severidad, valor esperado, valor real y estado.