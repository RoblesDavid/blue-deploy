
## Entidades principales

### Deployment

Representa el despliegue completo de un equipo.

Campos propuestos:

- id
- deploymentId
- hostname
- deviceModel
- domainName
- startedAt
- finishedAt
- currentStep
- deploymentStatus
- complianceStatus
- lastSeenAt

### DeploymentStep

Representa una fase ejecutada durante el despliegue.

Campos propuestos:

- id
- deployment
- stepName
- stepStatus
- startedAt
- finishedAt
- message
- errorCode
- rawOutput

### SecurityCheck

Representa una comprobación de seguridad.

Campos propuestos:

- id
- deployment
- checkName
- checkStatus
- severity
- expectedValue
- actualValue
- message
- checkedAt

### Alert

Representa una alerta derivada de errores o incumplimientos.

Campos propuestos:

- id
- hostname
- type
- severity
- message
- createdAt

## Estados de Deployment

- IN_PROGRESS
- DOMAIN_JOINED
- POST_DOMAIN_PENDING
- COMPLETED
- COMPLETED_WITH_WARNINGS
- FAILED
- NON_COMPLIANT

## Estados de Step

- PENDING
- RUNNING
- SUCCESS
- WARNING
- FAILED
- SKIPPED

## Estados de Compliance

- PENDING
- COMPLIANT
- NON_COMPLIANT
- UNKNOWN

## Checks iniciales

- FIREWALL_ENABLED
- WSUS_CONFIGURED
- CROWDSTRIKE_VALIDATED
- MICROCLAUDIA_VALIDATED
- LOCAL_ADMIN_VALIDATED
- DOMAIN_JOIN_VALIDATED
- RADIUS_8021X_VALIDATED