## Flujo actual real

1. Inicio de instalación desde USB con imagen Windows 11 corporativa.
2. El técnico introduce el nombre del equipo.
3. El hostname se guarda en un archivo de texto plano.
4. Instalación del sistema operativo.
5. Ejecución de script de eliminación de bloatware.
6. Detección del modelo exacto del equipo.
7. Instalación automática de drivers.
8. Reinicio.
9. Configuración de red 802.1X / RADIUS.
10. Reinicio.
11. Unión del equipo al dominio corporativo.
12. Reinicio.
13. Ejecución de gpupdate.
14. Configuración o unión a WSUS.
15. Reinicio.
16. Selección e instalación de aplicaciones.
17. Limpieza de carpeta OSDCloud.
18. Validaciones post-dominio.
19. Validación de CrowdStrike.
20. Validación de MicroClaudia.
21. Validación final Blue Team.

## Fases a registrar en BlueDeploy

- HOSTNAME_CAPTURED
- WINDOWS_INSTALLATION
- BLOATWARE_REMOVAL
- DRIVER_MODEL_DETECTED
- DRIVER_INSTALLATION
- RADIUS_8021X_CONFIGURATION
- DOMAIN_JOIN
- GPUPDATE
- WSUS_CONFIGURATION
- POST_DOMAIN_REBOOT
- APPLICATION_SELECTION
- OSDCLOUD_CLEANUP
- CROWDSTRIKE_VALIDATION
- MICROCLAUDIA_VALIDATION
- LOCAL_ADMIN_VALIDATION
- FINAL_SECURITY_VALIDATION

## Consideraciones

La conectividad existe durante el despliegue, aunque no es necesaria hasta la unión al dominio.

BlueDeploy podrá recibir eventos durante el despliegue, pero los scripts deberán guardar copia local por si falla la comunicación con la API.

CrowdStrike y MicroClaudia no deben considerarse requisitos previos a la unión al dominio, sino controles post-dominio.