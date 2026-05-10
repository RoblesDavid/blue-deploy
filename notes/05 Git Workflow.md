
## Ramas principales

### main

Contiene versiones estables y demostrables.

Solo se fusiona código probado.

### develop

Contiene la integración de funcionalidades en desarrollo.

Es la rama principal de trabajo.

## Ramas de funcionalidad

Formato:


text feature/nombre-funcionalidad``` 

Ejemplos:


text feature/project-notes feature/deployment-model feature/deployment-steps feature/security-checks feature/dashboard-summary feature/powershell-agent feature/hetzner-deploy

## Flujo recomendado

1. Crear rama desde develop.
2. Implementar funcionalidad concreta.
3. Probar localmente.
4. Hacer commit claro.
5. Fusionar en develop.
6. Cuando develop esté estable, fusionar en main.

## Cuándo pasar a main

Solo pasar a main cuando:

- El backend arranca correctamente.
- Docker Compose funciona.
- No hay errores críticos.
- El frontend carga.
- La funcionalidad principal está probada.
- El README está actualizado.
- Hay evidencia en notes/08-evidencias.md.

## Formato de commits

Ejemplos:

text docs: add BlueDeploy architecture notes feat: add deployment domain model feat: add deployment creation endpoint feat: register deployment steps feat: add security check model fix: handle null deployment dates refactor: separate health endpoint from check controller docs: update roadmap for practice 2