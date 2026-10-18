package com.roblesdavid.blue_deploy.config;

import com.roblesdavid.blue_deploy.model.*;
import com.roblesdavid.blue_deploy.repository.AppUserRepository;
import com.roblesdavid.blue_deploy.repository.AuditLogRepository;
import com.roblesdavid.blue_deploy.repository.DeploymentRepository;
import com.roblesdavid.blue_deploy.repository.DeploymentStepRepository;
import com.roblesdavid.blue_deploy.repository.SecurityCheckRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Inicializador de datos para entornos de desarrollo y pruebas locales.
 * Crea usuarios y datos iniciales si la base de datos está vacía, incluyendo
 * el despliegue real documentado en scripts/powershell/bluedeploy/examples/.
 */
@Component
@org.springframework.context.annotation.Profile("!test")
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final AppUserRepository userRepository;
    private final DeploymentRepository deploymentRepository;
    private final DeploymentStepRepository stepRepository;
    private final SecurityCheckRepository checkRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            AppUserRepository userRepository,
            DeploymentRepository deploymentRepository,
            DeploymentStepRepository stepRepository,
            SecurityCheckRepository checkRepository,
            AuditLogRepository auditLogRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.deploymentRepository = deploymentRepository;
        this.stepRepository = stepRepository;
        this.checkRepository = checkRepository;
        this.auditLogRepository = auditLogRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        initUsers();
        initDeployments();
        initAuditLogs();
    }

    private void initUsers() {
        if (!userRepository.existsByUsername("admin")) {
            AppUser admin = new AppUser();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setName("Administrador BlueDeploy");
            admin.setEmail("admin@bluedeploy.local");
            admin.setRole(UserRole.ADMIN);
            admin.setInitials("AD");
            admin.setDepartment("SecOps & Infrastructure");
            admin.setActive(true);
            userRepository.save(admin);
            log.info("Usuario inicial 'admin' creado.");
        }

        if (!userRepository.existsByUsername("carlos")) {
            AppUser carlos = new AppUser();
            carlos.setUsername("carlos");
            carlos.setPassword(passwordEncoder.encode("carlos123"));
            carlos.setName("Carlos Rodríguez");
            carlos.setEmail("carlos.rodriguez@bluedeploy.local");
            carlos.setRole(UserRole.TECHNICIAN);
            carlos.setInitials("CR");
            carlos.setDepartment("Soporte Workplace N2");
            carlos.setActive(true);
            userRepository.save(carlos);
            log.info("Usuario inicial 'carlos' creado.");
        }

        if (!userRepository.existsByUsername("ana")) {
            AppUser ana = new AppUser();
            ana.setUsername("ana");
            ana.setPassword(passwordEncoder.encode("ana123"));
            ana.setName("Ana García");
            ana.setEmail("ana.garcia@bluedeploy.local");
            ana.setRole(UserRole.TECHNICIAN);
            ana.setInitials("AG");
            ana.setDepartment("Despliegues Especiales");
            ana.setActive(true);
            userRepository.save(ana);
            log.info("Usuario inicial 'ana' creado.");
        }
    }

    private void initDeployments() {
        AppUser carlos = userRepository.findByUsername("carlos").orElse(null);
        AppUser ana = userRepository.findByUsername("ana").orElse(null);

        if (!deploymentRepository.existsByDeploymentId("DEPLOY-AGSNEG-TESTICBLUE-20261002")) {
            log.info("Sembrando despliegue real AGSNEG 'DEPLOY-AGSNEG-TESTICBLUE-20261002'...");
            seedRealDeployment(carlos);
        }

        if (!deploymentRepository.existsByDeploymentId("DEPLOY-OSDCLOUD-AUTO-001")) {
            log.info("Sembrando despliegues de prueba adicionales...");
            seedSecondaryDeployments(carlos, ana);
            seedExtraDeployments(carlos, ana);
        }
    }

    private void seedRealDeployment(AppUser technician) {
        String depId = "DEPLOY-AGSNEG-TESTICBLUE-20261002";

        Deployment deployment = new Deployment();
        deployment.setDeploymentId(depId);
        deployment.setHostname("TESTICBLUE");
        deployment.setDeviceModel("HP ProDesk 400 G5 SFF");
        deployment.setDomainName("dominio.local");
        deployment.setCurrentStep("DEPLOYMENT_COMPLETED");
        deployment.setDeploymentStatus(DeploymentStatus.COMPLETED);
        deployment.setComplianceStatus(ComplianceStatus.COMPLIANT);
        deployment.setStartedAt(LocalDateTime.parse("2026-10-02T10:50:20"));
        deployment.setFinishedAt(LocalDateTime.parse("2026-10-02T11:27:00"));
        deployment.setLastSeenAt(LocalDateTime.parse("2026-10-02T11:27:00"));
        deployment.setTechnician(technician);
        Deployment saved = deploymentRepository.save(deployment);

        // Pasos reales tomados de examples
        addStep(saved, "HOSTNAME_CAPTURED", StepStatus.SUCCESS, "Hostname capturado desde entrada de tecnico y guardado en archivo de texto", null, "Hostname=TESTICBLUE", "2026-10-02T10:50:20");
        addStep(saved, "WINDOWS_INSTALLATION", StepStatus.SUCCESS, "Imagen de Windows 11 aplicada exitosamente usando motor OSDCloud", null, "OSImageIndex=1; TargetDrive=C:", "2026-10-02T10:50:20");
        addStep(saved, "SETUPCOMPLETE_COPIED", StepStatus.SUCCESS, "SetupComplete.cmd copiado correctamente a Scripts", null, "Source=C:\\OSDCloud\\Scripts\\SetupComplete\\SetupComplete.cmd; Destination=C:\\Windows\\Setup\\Scripts", "2026-10-02T10:50:23");
        addStep(saved, "SETUPCOMPLETE_EXECUTED", StepStatus.SUCCESS, "SetupComplete executed successfully. Autologon and InstallDrivers task were configured.", null, "SetupComplete.cmd executed; InstallDrivers task registered; AutoLogon configured", "2026-10-02T10:54:40");
        addStep(saved, "DRIVER_MODEL_DETECTED", StepStatus.SUCCESS, "Device model detected successfully", null, "Model=HP ProDesk 400 G5 SFF", "2026-10-02T11:22:11");
        addStep(saved, "DRIVER_INSTALLATION", StepStatus.WARNING, "Drivers installation process completed", "DRIVER_INSTALL_ERRORS", "Model=HP ProDesk 400 G5 SFF; Total=30; Installed=16; Skipped=0; Errors=14", "2026-10-02T11:22:45");
        addStep(saved, "BLOATWARE_REMOVAL", StepStatus.SUCCESS, "Bloatware removal process completed", null, "Removed=2; NotFound=2", "2026-10-02T11:22:46");
        addStep(saved, "PRINTER_CLEANUP", StepStatus.SUCCESS, "Default printers cleanup completed", null, "Removed=0; Preserved=1", "2026-10-02T11:22:47");
        addStep(saved, "SETUPRADIUS_TASK_REGISTERED", StepStatus.SUCCESS, "Next deployment orchestration task successfully registered in system schedule", null, "TaskName=SetupRadius; Path=C:\\OSDCloud\\Scripts\\SetupComplete\\SetupRadius.ps1; RunAs=System", "2026-10-02T11:22:50");
        addStep(saved, "RADIUS_SERVICES_CONFIG", StepStatus.SUCCESS, "802.1X and EAP Host services started successfully", null, "Dot3svc=Running; EapHost=Running", "2026-10-02T11:23:56");
        addStep(saved, "RADIUS_PROFILE_APPLICATION", StepStatus.SUCCESS, "802.1X enterprise LAN network profile applied successfully via netsh", null, "ExitCode=0; Output= El perfil del equipo se agrego correctamente.", "2026-10-02T11:23:57");
        addStep(saved, "JOIN_DOMAIN_TASK_REGISTERED", StepStatus.SUCCESS, "JoinDomain orchestration task was scheduled successfully", null, "TaskName=JoinDomain; Path=C:\\OSDCloud\\Scripts\\SetupComplete\\JoinDomain.ps1; RunAs=System", "2026-10-02T11:23:57");
        addStep(saved, "JOIN_DOMAIN_EXECUTION", StepStatus.SUCCESS, "The computer successfully joined the active directory domain structure", null, "Domain [dominio.local] - OUPath [OU:Equipos | OU:AGS-NordesteGranada | OU:Especializada | OU:Granada | DC:dominio | DC:local]", "2026-10-02T11:25:02");
        addStep(saved, "JOIN_DOMAIN_AUTOLOGON_CONFIG", StepStatus.SUCCESS, "Transition AutoLogon credentials written to registry keys successfully", null, "AutoLogonUser=test1; Count=2", "2026-10-02T11:25:02");
        addStep(saved, "AD_WSUS_TASK_REGISTERED", StepStatus.SUCCESS, "Next deployment orchestration task successfully registered in system schedule", null, "TaskName=AddComputerToADGroup_WSUS; Path=C:\\OSDCloud\\Scripts\\SetupComplete\\AddComputerToADGroup_WSUS.ps1; RunAs=System", "2026-10-02T11:25:02");
        addStep(saved, "GROUP_POLICY_UPDATE", StepStatus.SUCCESS, "Group policies updated successfully on the device", null, "ExitCode=0", "2026-10-02T11:26:50");
        addStep(saved, "AD_GROUP_MEMBERSHIP_MANAGEMENT", StepStatus.SUCCESS, "The computer object was successfully added to the designated WSUS AD Target Group", null, "Group=GR40_GG_WSUS_W10_PRO; ComputerSAM=TESTICBLUE$; ComputerDN=CN=TESTICBLUE,OU=Equipos,OU=AGS-NordesteGranada,OU=Especializada,OU=Granada,DC=dominio,DC=local", "2026-10-02T11:26:51");
        addStep(saved, "INSTALL_SOFTWARE_TASK_REGISTERED", StepStatus.SUCCESS, "The next orchestration step task for software deployment was registered successfully", null, "TaskName=InstallSoftware; RunAs=TESTICBLUE\\test1; LogonType=Interactive; RunLevel=Highest", "2026-10-02T11:26:57");
        addStep(saved, "DEPLOYMENT_COMPLETED", StepStatus.SUCCESS, "Despliegue AGSNEG completado exitosamente", null, "Hostname=TESTICBLUE; OSVersion=Windows11; TargetDrive=C:", "2026-10-02T11:27:00");

        // Checks de seguridad reales
        addCheck(saved, "FIREWALL_ENABLED", CheckStatus.PASSED, Severity.HIGH, "Enabled", "Enabled", "Windows Defender Firewall activo", "2026-10-02T11:26:55");
        addCheck(saved, "WSUS_CONFIGURED", CheckStatus.PASSED, Severity.MEDIUM, "WSUS server configurado", "WSUS server configurado", "WSUS corporativo OK", "2026-10-02T11:26:56");
        addCheck(saved, "CROWDSTRIKE_VALIDATED", CheckStatus.PASSED, Severity.CRITICAL, "Running", "Running", "CrowdStrike Falcon activo y reportando", "2026-10-02T11:26:57");
        addCheck(saved, "MICROCLAUDIA_VALIDATED", CheckStatus.PASSED, Severity.CRITICAL, "Installed", "Installed", "MicroClaudia instalada correctamente", "2026-10-02T11:26:58");
        addCheck(saved, "LOCAL_ADMIN_VALIDATED", CheckStatus.PASSED, Severity.HIGH, "AutoAdminLogon=0", "AutoAdminLogon=0", "Autologon eliminado", "2026-10-02T11:26:59");
    }

    private void seedSecondaryDeployments(AppUser carlos, AppUser ana) {
        // Caso 2: Despliegue sin asignar (Huérfano OSDCloud) -> Para probar "Autoasignación y Reclamación"
        Deployment d2 = new Deployment();
        d2.setDeploymentId("DEPLOY-OSDCLOUD-AUTO-001");
        d2.setHostname("PC-RECLAMAR-BLUE");
        d2.setDeviceModel("Dell OptiPlex 7090");
        d2.setDomainName("dominio.local");
        d2.setCurrentStep("RADIUS_SERVICES_CONFIG");
        d2.setDeploymentStatus(DeploymentStatus.IN_PROGRESS);
        d2.setComplianceStatus(ComplianceStatus.PENDING);
        d2.setStartedAt(LocalDateTime.parse("2026-10-15T14:10:00"));
        d2.setLastSeenAt(LocalDateTime.parse("2026-10-15T14:25:00"));
        d2.setTechnician(null); // Sin técnico asignado
        Deployment saved2 = deploymentRepository.save(d2);

        addStep(saved2, "HOSTNAME_CAPTURED", StepStatus.SUCCESS, "Hostname capturado en OSDCloud", null, "Hostname=PC-RECLAMAR-BLUE", "2026-10-15T14:10:00");
        addStep(saved2, "WINDOWS_INSTALLATION", StepStatus.SUCCESS, "Instalacion SO Windows 11 finalizada", null, "OSImageIndex=1", "2026-10-15T14:18:00");
        addStep(saved2, "RADIUS_SERVICES_CONFIG", StepStatus.RUNNING, "Configurando autenticacion de red 802.1X", null, "Dot3svc=Starting", "2026-10-15T14:25:00");

        addCheck(saved2, "FIREWALL_ENABLED", CheckStatus.PASSED, Severity.HIGH, "Enabled", "Enabled", "Firewall activo", "2026-10-15T14:24:00");
        addCheck(saved2, "RADIUS_8021X_VALIDATED", CheckStatus.PENDING, Severity.HIGH, "Validado", "En proceso", "Autenticacion 802.1X en configuracion", "2026-10-15T14:25:00");

        // Caso 3: Despliegue con Comprobación Pendiente -> Para probar "Flujo de Remediación de Comprobaciones"
        Deployment d3 = new Deployment();
        d3.setDeploymentId("DEPLOY-REMEDIATION-002");
        d3.setHostname("PC-REMEDIAR-BLUE");
        d3.setDeviceModel("Lenovo ThinkPad T14 Gen 2");
        d3.setDomainName("dominio.local");
        d3.setCurrentStep("AD_WSUS_TASK_REGISTERED");
        d3.setDeploymentStatus(DeploymentStatus.COMPLETED_WITH_WARNINGS);
        d3.setComplianceStatus(ComplianceStatus.NON_COMPLIANT);
        d3.setStartedAt(LocalDateTime.parse("2026-10-15T11:00:00"));
        d3.setFinishedAt(LocalDateTime.parse("2026-10-15T11:35:00"));
        d3.setLastSeenAt(LocalDateTime.parse("2026-10-15T11:35:00"));
        d3.setTechnician(carlos);
        Deployment saved3 = deploymentRepository.save(d3);

        addStep(saved3, "HOSTNAME_CAPTURED", StepStatus.SUCCESS, "Hostname capturado", null, "Hostname=PC-REMEDIAR-BLUE", "2026-10-15T11:00:00");
        addStep(saved3, "JOIN_DOMAIN_EXECUTION", StepStatus.SUCCESS, "Equipo unido a dominio corporativo", null, "Domain=dominio.local", "2026-10-15T11:20:00");
        addStep(saved3, "DEPLOYMENT_COMPLETED", StepStatus.SUCCESS, "Despliegue finalizado con comprobaciones pendientes", null, "Status=Finalizado", "2026-10-15T11:35:00");

        addCheck(saved3, "FIREWALL_ENABLED", CheckStatus.PASSED, Severity.HIGH, "Enabled", "Enabled", "Firewall activo", "2026-10-15T11:30:00");
        addCheck(saved3, "CROWDSTRIKE_VALIDATED", CheckStatus.FAILED, Severity.CRITICAL, "Running", "Stopped", "CrowdStrike Falcon no sincronizado (requiere remediacion)", "2026-10-15T11:34:00");
        addCheck(saved3, "MICROCLAUDIA_VALIDATED", CheckStatus.WARNING, Severity.MEDIUM, "Installed", "Pending", "MicroClaudia pendiente de validacion de firma", "2026-10-15T11:35:00");

        // Caso 4: Despliegue Fallido -> Para probar "Diagnóstico con IA" y "Exportar PDF"
        Deployment d4 = new Deployment();
        d4.setDeploymentId("DEPLOY-ERROR-DIAG-003");
        d4.setHostname("PC-HOSPITAL-BLUE");
        d4.setDeviceModel("HP EliteDesk 800 G6");
        d4.setDomainName("dominio.local");
        d4.setCurrentStep("INSTALL_SOFTWARE");
        d4.setDeploymentStatus(DeploymentStatus.FAILED);
        d4.setComplianceStatus(ComplianceStatus.NON_COMPLIANT);
        d4.setStartedAt(LocalDateTime.parse("2026-10-15T08:00:00"));
        d4.setFinishedAt(LocalDateTime.parse("2026-10-15T08:45:00"));
        d4.setLastSeenAt(LocalDateTime.parse("2026-10-15T08:45:00"));
        d4.setTechnician(carlos);
        Deployment saved4 = deploymentRepository.save(d4);

        addStep(saved4, "HOSTNAME_CAPTURED", StepStatus.SUCCESS, "Hostname capturado", null, "Hostname=PC-HOSPITAL-BLUE", "2026-10-15T08:00:00");
        addStep(saved4, "DRIVER_INSTALLATION", StepStatus.SUCCESS, "Drivers HP instalados", null, "Model=HP EliteDesk 800 G6", "2026-10-15T08:20:00");
        addStep(saved4, "INSTALL_SOFTWARE", StepStatus.FAILED, "Fallo critico al instalar suite clinica", "ERR_INSTALL_1603", "MSI Error 1603: Fatal error during installation. Package: ClinicalSuite_v4.msi", "2026-10-15T08:45:00");

        addCheck(saved4, "FIREWALL_ENABLED", CheckStatus.PASSED, Severity.HIGH, "Enabled", "Enabled", "Firewall activo", "2026-10-15T08:40:00");
        addCheck(saved4, "CROWDSTRIKE_VALIDATED", CheckStatus.PASSED, Severity.CRITICAL, "Running", "Running", "CrowdStrike Falcon en ejecucion", "2026-10-15T08:42:00");

        // Caso 5: Despliegue de Ana -> Para verificar aislamiento de rol entre técnicos
        Deployment d5 = new Deployment();
        d5.setDeploymentId("DEPLOY-ANA-WORK-004");
        d5.setHostname("PC-ANA-BLUE");
        d5.setDeviceModel("Dell Latitude 5540");
        d5.setDomainName("dominio.local");
        d5.setCurrentStep("JOIN_DOMAIN_EXECUTION");
        d5.setDeploymentStatus(DeploymentStatus.IN_PROGRESS);
        d5.setComplianceStatus(ComplianceStatus.PENDING);
        d5.setStartedAt(LocalDateTime.parse("2026-10-15T15:00:00"));
        d5.setLastSeenAt(LocalDateTime.parse("2026-10-15T15:20:00"));
        d5.setTechnician(ana);
        Deployment saved5 = deploymentRepository.save(d5);

        addStep(saved5, "HOSTNAME_CAPTURED", StepStatus.SUCCESS, "Hostname capturado", null, "Hostname=PC-ANA-BLUE", "2026-10-15T15:00:00");
        addStep(saved5, "JOIN_DOMAIN_EXECUTION", StepStatus.SUCCESS, "Unido a dominio correctamente", null, "Domain=dominio.local", "2026-10-15T15:20:00");

        addCheck(saved5, "FIREWALL_ENABLED", CheckStatus.PASSED, Severity.HIGH, "Enabled", "Enabled", "Firewall activo", "2026-10-15T15:15:00");
        addCheck(saved5, "DOMAIN_JOIN_VALIDATED", CheckStatus.PASSED, Severity.CRITICAL, "dominio.local", "dominio.local", "Equipo unido a dominio", "2026-10-15T15:20:00");
        addCheck(saved5, "CROWDSTRIKE_VALIDATED", CheckStatus.PENDING, Severity.CRITICAL, "Running", "Pendiente", "Sensor CrowdStrike pendiente de sincronizacion GPO", "2026-10-15T15:20:00");
    }

    private void seedExtraDeployments(AppUser carlos, AppUser ana) {
        // Caso 6: Éxito total Portátil Dirección (Ana García)
        Deployment d6 = new Deployment();
        d6.setDeploymentId("DEPLOY-SUCCESS-PORTATIL-005");
        d6.setHostname("PORTATIL-DIREC-01");
        d6.setDeviceModel("Lenovo ThinkPad X1 Carbon Gen 10");
        d6.setDomainName("dominio.local");
        d6.setCurrentStep("DEPLOYMENT_COMPLETED");
        d6.setDeploymentStatus(DeploymentStatus.COMPLETED);
        d6.setComplianceStatus(ComplianceStatus.COMPLIANT);
        d6.setStartedAt(LocalDateTime.parse("2026-10-16T09:15:00"));
        d6.setFinishedAt(LocalDateTime.parse("2026-10-16T09:48:30"));
        d6.setLastSeenAt(LocalDateTime.parse("2026-10-16T09:48:30"));
        d6.setTechnician(ana);
        Deployment saved6 = deploymentRepository.save(d6);

        addStep(saved6, "HOSTNAME_CAPTURED", StepStatus.SUCCESS, "Hostname capturado y validado con nomenclatura corporativa", null, "Hostname=PORTATIL-DIREC-01", "2026-10-16T09:15:00");
        addStep(saved6, "WINDOWS_INSTALLATION", StepStatus.SUCCESS, "Imagen Windows 11 Enterprise 23H2 aplicada correctamente", null, "OS=Windows11; Edition=Enterprise", "2026-10-16T09:22:10");
        addStep(saved6, "DRIVER_INSTALLATION", StepStatus.SUCCESS, "Paquete de drivers Lenovo SCCM instalado al 100%", null, "Drivers=34; Installed=34; Errors=0", "2026-10-16T09:28:40");
        addStep(saved6, "SETUPRADIUS_TASK_REGISTERED", StepStatus.SUCCESS, "Perfil 802.1X Wi-Fi corporativo y certificados aplicados", null, "SSID=CORP-SECURE-WIFI", "2026-10-16T09:33:15");
        addStep(saved6, "JOIN_DOMAIN_EXECUTION", StepStatus.SUCCESS, "Equipo unido a OU=Portatiles,DC=dominio,DC=local", null, "Domain=dominio.local", "2026-10-16T09:37:00");
        addStep(saved6, "GROUP_POLICY_UPDATE", StepStatus.SUCCESS, "Directivas BitLocker y LAPS aplicadas por GPO", null, "GPOStatus=Success", "2026-10-16T09:40:20");
        addStep(saved6, "INSTALL_SOFTWARE", StepStatus.SUCCESS, "Instalacion de Office 365, Edge corporativo y VPN", null, "Packages=5; ExitCode=0", "2026-10-16T09:47:00");
        addStep(saved6, "DEPLOYMENT_COMPLETED", StepStatus.SUCCESS, "Despliegue finalizado exitosamente con verificacion de hardware", null, "Status=COMPLETED", "2026-10-16T09:48:30");

        addCheck(saved6, "FIREWALL_ENABLED", CheckStatus.PASSED, Severity.HIGH, "Enabled", "Enabled", "Perfiles de Firewall (Domain, Private, Public) activos", "2026-10-16T09:48:00");
        addCheck(saved6, "WSUS_CONFIGURED", CheckStatus.PASSED, Severity.MEDIUM, "WSUS server configurado", "WSUS server configurado", "Servidor WSUS asignado correctamente", "2026-10-16T09:48:05");
        addCheck(saved6, "CROWDSTRIKE_VALIDATED", CheckStatus.PASSED, Severity.CRITICAL, "Running", "Running", "Sensor CrowdStrike Falcon v7.14 operativo", "2026-10-16T09:48:10");
        addCheck(saved6, "MICROCLAUDIA_VALIDATED", CheckStatus.PASSED, Severity.CRITICAL, "Installed", "Installed", "Proteccion antiransomware MicroClaudia activa", "2026-10-16T09:48:15");
        addCheck(saved6, "LOCAL_ADMIN_VALIDATED", CheckStatus.PASSED, Severity.HIGH, "AutoAdminLogon=0", "AutoAdminLogon=0", "Credenciales temporales eliminadas del registro", "2026-10-16T09:48:20");

        // Caso 7: Despliegue Fallido en Unión a Dominio (Carlos Rodríguez) -> Ideal para probar Diagnóstico IA de AD
        Deployment d7 = new Deployment();
        d7.setDeploymentId("DEPLOY-ERROR-DOMAIN-006");
        d7.setHostname("PC-LAB-CONSULTA-02");
        d7.setDeviceModel("Dell OptiPlex 3080 SFF");
        d7.setDomainName("dominio.local");
        d7.setCurrentStep("JOIN_DOMAIN_EXECUTION");
        d7.setDeploymentStatus(DeploymentStatus.FAILED);
        d7.setComplianceStatus(ComplianceStatus.NON_COMPLIANT);
        d7.setStartedAt(LocalDateTime.parse("2026-10-16T11:00:00"));
        d7.setFinishedAt(LocalDateTime.parse("2026-10-16T11:24:12"));
        d7.setLastSeenAt(LocalDateTime.parse("2026-10-16T11:24:12"));
        d7.setTechnician(carlos);
        Deployment saved7 = deploymentRepository.save(d7);

        addStep(saved7, "HOSTNAME_CAPTURED", StepStatus.SUCCESS, "Hostname capturado", null, "Hostname=PC-LAB-CONSULTA-02", "2026-10-16T11:00:00");
        addStep(saved7, "WINDOWS_INSTALLATION", StepStatus.SUCCESS, "Imagen aplicada en particion NVMe", null, "TargetDrive=C:", "2026-10-16T11:08:00");
        addStep(saved7, "DRIVER_INSTALLATION", StepStatus.SUCCESS, "Drivers Dell OptiPlex inyectados correctamente", null, "Model=Dell OptiPlex 3080", "2026-10-16T11:15:30");
        addStep(saved7, "RADIUS_SERVICES_CONFIG", StepStatus.SUCCESS, "Servicio Dot3svc iniciado", null, "Status=Running", "2026-10-16T11:20:00");
        addStep(saved7, "JOIN_DOMAIN_EXECUTION", StepStatus.FAILED, "Fallo critico al unir al dominio Active Directory", "0x8007052E", "System error 1326 has occurred. Logon failure: unknown user name or bad password. PDC replication delay detected.", "2026-10-16T11:24:12");

        addCheck(saved7, "FIREWALL_ENABLED", CheckStatus.PASSED, Severity.HIGH, "Enabled", "Enabled", "Firewall activo", "2026-10-16T11:22:00");
        addCheck(saved7, "DOMAIN_JOIN_VALIDATED", CheckStatus.FAILED, Severity.CRITICAL, "dominio.local", "WORKGROUP", "El equipo no pudo completar la autenticacion en el Directorio Activo", "2026-10-16T11:24:00");

        // Caso 8: Despliegue Fallido por Fallo de Hardware SMART
        Deployment d8 = new Deployment();
        d8.setDeploymentId("DEPLOY-ABORTED-DISK-007");
        d8.setHostname("PC-URGENCIAS-09");
        d8.setDeviceModel("HP ProDesk 600 G4");
        d8.setDomainName("dominio.local");
        d8.setCurrentStep("DISK_PARTITIONING");
        d8.setDeploymentStatus(DeploymentStatus.FAILED);
        d8.setComplianceStatus(ComplianceStatus.PENDING);
        d8.setStartedAt(LocalDateTime.parse("2026-10-16T16:30:00"));
        d8.setFinishedAt(LocalDateTime.parse("2026-10-16T16:35:40"));
        d8.setLastSeenAt(LocalDateTime.parse("2026-10-16T16:35:40"));
        d8.setTechnician(carlos);
        Deployment saved8 = deploymentRepository.save(d8);

        addStep(saved8, "HOSTNAME_CAPTURED", StepStatus.SUCCESS, "Hostname capturado", null, "Hostname=PC-URGENCIAS-09", "2026-10-16T16:30:00");
        addStep(saved8, "DISK_PARTITIONING", StepStatus.FAILED, "Error de hardware E/S durante particionado de disco", "DISK_IO_ERROR_0X5", "Diskpart failed: The request could not be performed because of an I/O device error. SMART status: BAD.", "2026-10-16T16:35:40");

        // Caso 9: Despliegue Exitoso Estación de Quirófano (Carlos Rodríguez)
        Deployment d9 = new Deployment();
        d9.setDeploymentId("DEPLOY-SUCCESS-QUIROFANO-008");
        d9.setHostname("PC-QUIROFANO-01");
        d9.setDeviceModel("HP EliteOne 800 G6 All-in-One");
        d9.setDomainName("dominio.local");
        d9.setCurrentStep("DEPLOYMENT_COMPLETED");
        d9.setDeploymentStatus(DeploymentStatus.COMPLETED);
        d9.setComplianceStatus(ComplianceStatus.COMPLIANT);
        d9.setStartedAt(LocalDateTime.parse("2026-10-17T08:00:00"));
        d9.setFinishedAt(LocalDateTime.parse("2026-10-17T08:38:15"));
        d9.setLastSeenAt(LocalDateTime.parse("2026-10-17T08:38:15"));
        d9.setTechnician(carlos);
        Deployment saved9 = deploymentRepository.save(d9);

        addStep(saved9, "HOSTNAME_CAPTURED", StepStatus.SUCCESS, "Hostname capturado en WinPE", null, "Hostname=PC-QUIROFANO-01", "2026-10-17T08:00:00");
        addStep(saved9, "WINDOWS_INSTALLATION", StepStatus.SUCCESS, "Windows 11 Pro instalado con perfil quirurgico", null, "TargetDrive=C:", "2026-10-17T08:07:30");
        addStep(saved9, "SETUPCOMPLETE_EXECUTED", StepStatus.SUCCESS, "SetupComplete ejecutado con exito", null, "TasksScheduled=3", "2026-10-17T08:12:00");
        addStep(saved9, "DRIVER_INSTALLATION", StepStatus.SUCCESS, "Drivers pantalla tactil y audio HD instalados", null, "Model=HP EliteOne 800 G6", "2026-10-17T08:18:00");
        addStep(saved9, "SETUPRADIUS_TASK_REGISTERED", StepStatus.SUCCESS, "Autenticacion 802.1X VLAN asistencial aplicada", null, "VLAN=140", "2026-10-17T08:22:00");
        addStep(saved9, "JOIN_DOMAIN_EXECUTION", StepStatus.SUCCESS, "Unido a dominio en OU=Quirofanos", null, "Domain=dominio.local", "2026-10-17T08:26:00");
        addStep(saved9, "INSTALL_SOFTWARE", StepStatus.SUCCESS, "Visor DICOM y cliente de historia clinica instalados", null, "SoftwareSuite=PACS_Client", "2026-10-17T08:35:00");
        addStep(saved9, "DEPLOYMENT_COMPLETED", StepStatus.SUCCESS, "Puesto quirurgico listo para operacion clinica", null, "Status=COMPLETED", "2026-10-17T08:38:15");

        addCheck(saved9, "FIREWALL_ENABLED", CheckStatus.PASSED, Severity.HIGH, "Enabled", "Enabled", "Firewall activo con reglas DICOM", "2026-10-17T08:37:00");
        addCheck(saved9, "WSUS_CONFIGURED", CheckStatus.PASSED, Severity.MEDIUM, "WSUS server configurado", "WSUS server configurado", "Politica WSUS sanitaria aplicada", "2026-10-17T08:37:20");
        addCheck(saved9, "CROWDSTRIKE_VALIDATED", CheckStatus.PASSED, Severity.CRITICAL, "Running", "Running", "CrowdStrike Falcon activo en modo Prevention", "2026-10-17T08:37:40");
        addCheck(saved9, "MICROCLAUDIA_VALIDATED", CheckStatus.PASSED, Severity.CRITICAL, "Installed", "Installed", "MicroClaudia activa", "2026-10-17T08:37:50");
        addCheck(saved9, "LOCAL_ADMIN_VALIDATED", CheckStatus.PASSED, Severity.HIGH, "AutoAdminLogon=0", "AutoAdminLogon=0", "Autologon deshabilitado", "2026-10-17T08:38:00");
    }

    private void addStep(Deployment deployment, String name, StepStatus status, String msg, String err, String raw, String reportedAt) {
        DeploymentStep step = new DeploymentStep();
        step.setDeployment(deployment);
        step.setStepName(name);
        step.setStepStatus(status);
        step.setMessage(msg);
        step.setErrorCode(err);
        step.setRawOutput(raw);
        step.setReportedAt(LocalDateTime.parse(reportedAt));
        stepRepository.save(step);
    }

    private void addCheck(Deployment deployment, String name, CheckStatus status, Severity severity, String exp, String act, String msg, String reportedAt) {
        SecurityCheck check = new SecurityCheck();
        check.setDeployment(deployment);
        check.setCheckName(name);
        check.setCheckStatus(status);
        check.setSeverity(severity);
        check.setExpectedValue(exp);
        check.setActualValue(act);
        check.setMessage(msg);
        check.setCheckedAt(LocalDateTime.parse(reportedAt));
        check.setReportedAt(LocalDateTime.parse(reportedAt));
        checkRepository.save(check);
    }

    private void initAuditLogs() {
        if (auditLogRepository.count() == 0) {
            log.info("Sembrando registros de auditoria iniciales...");
            auditLogRepository.save(new AuditLog(AuditAction.LOGIN_SUCCESS, "admin", "admin", "USER", "Inicio de sesion exitoso desde consola web administrativa", "192.168.1.50"));
            auditLogRepository.save(new AuditLog(AuditAction.LOGIN_SUCCESS, "carlos", "carlos", "USER", "Inicio de sesion exitoso de tecnico N2", "192.168.1.110"));
            auditLogRepository.save(new AuditLog(AuditAction.TECHNICIAN_CREATED, "admin", "ana", "USER", "Alta de nuevo perfil tecnico para Ana Garcia", "192.168.1.50"));
            auditLogRepository.save(new AuditLog(AuditAction.INVALID_API_KEY, "ANONYMOUS", "/api/deployments/DEPLOY-TEST/steps", "SECURITY", "Rechazado intento de telemetria con cabecera X-BlueDeploy-Api-Key invalida", "192.168.1.205"));
            auditLogRepository.save(new AuditLog(AuditAction.DEPLOYMENT_ASSIGNED, "carlos", "DEPLOY-AGSNEG-TESTICBLUE-20261002", "DEPLOYMENT", "Despliegue asignado al tecnico Carlos Rodriguez", "192.168.1.110"));
            auditLogRepository.save(new AuditLog(AuditAction.SCRIPT_EVENT_RECEIVED, "SYSTEM_AGENT", "DEPLOY-AGSNEG-TESTICBLUE-20261002", "STEP", "Paso 999_deployment_completed_step recibido e indexado", "192.168.1.145"));
            auditLogRepository.save(new AuditLog(AuditAction.COMPLIANCE_STATUS_CHANGED, "SYSTEM_ENGINE", "DEPLOY-SUCCESS-PORTATIL-005", "COMPLIANCE", "Estado de cumplimiento evaluado: COMPLIANT (5/5 checks superados)", "127.0.0.1"));
            auditLogRepository.save(new AuditLog(AuditAction.DEPLOYMENT_STATUS_CHANGED, "SYSTEM_ENGINE", "DEPLOY-ERROR-DOMAIN-006", "DEPLOYMENT", "Transicion de estado a FAILED por error 0x8007052E en JOIN_DOMAIN_EXECUTION", "192.168.1.146"));
        }
    }
}
