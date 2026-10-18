package com.roblesdavid.blue_deploy.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roblesdavid.blue_deploy.model.AppUser;
import com.roblesdavid.blue_deploy.model.UserRole;
import com.roblesdavid.blue_deploy.repository.AiSummaryRepository;
import com.roblesdavid.blue_deploy.repository.AlertRepository;
import com.roblesdavid.blue_deploy.repository.AppUserRepository;
import com.roblesdavid.blue_deploy.repository.AuditLogRepository;
import com.roblesdavid.blue_deploy.repository.CheckRepository;
import com.roblesdavid.blue_deploy.repository.DeploymentRepository;
import com.roblesdavid.blue_deploy.repository.DeploymentStepRepository;
import com.roblesdavid.blue_deploy.repository.SecurityCheckRepository;
import com.roblesdavid.blue_deploy.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class BaseApiIntegrationTest {

    protected static final String TEST_API_KEY = "test-api-key-12345";
    protected static final String API_KEY_HEADER = "X-BlueDeploy-Api-Key";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected AuditLogRepository auditLogRepository;

    @Autowired
    protected AiSummaryRepository aiSummaryRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private CheckRepository checkRepository;

    @Autowired
    private SecurityCheckRepository securityCheckRepository;

    @Autowired
    private DeploymentStepRepository deploymentStepRepository;

    @Autowired
    private DeploymentRepository deploymentRepository;

    @Autowired
    protected AppUserRepository appUserRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected JwtService jwtService;

    @BeforeEach
    void cleanupDatabase() {
        auditLogRepository.deleteAll();
        aiSummaryRepository.deleteAll();
        alertRepository.deleteAll();
        checkRepository.deleteAll();
        securityCheckRepository.deleteAll();
        deploymentStepRepository.deleteAll();
        deploymentRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    protected String obtainAdminToken() {
        AppUser admin = appUserRepository.findByUsername("admin_test").orElseGet(() -> {
            AppUser u = new AppUser();
            u.setUsername("admin_test");
            u.setPassword(passwordEncoder.encode("AdminPass123!"));
            u.setName("Admin Test");
            u.setEmail("admin_test@bluedeploy.local");
            u.setRole(UserRole.ADMIN);
            u.setDepartment("SecOps");
            u.setInitials("AT");
            u.setActive(true);
            return appUserRepository.save(u);
        });
        java.util.Map<String, Object> claims = new java.util.HashMap<>();
        claims.put("role", admin.getRole().name());
        claims.put("name", admin.getName());
        claims.put("email", admin.getEmail());
        return jwtService.generateToken(claims, admin.getUsername());
    }

    protected String obtainTechToken(String username) {
        AppUser tech = appUserRepository.findByUsername(username).orElseGet(() -> {
            AppUser u = new AppUser();
            u.setUsername(username);
            u.setPassword(passwordEncoder.encode("TechPass123!"));
            u.setName(username + " Test");
            u.setEmail(username + "@bluedeploy.local");
            u.setRole(UserRole.TECHNICIAN);
            u.setDepartment("Workplace");
            u.setInitials("TT");
            u.setActive(true);
            return appUserRepository.save(u);
        });
        java.util.Map<String, Object> claims = new java.util.HashMap<>();
        claims.put("role", tech.getRole().name());
        claims.put("name", tech.getName());
        claims.put("email", tech.getEmail());
        return jwtService.generateToken(claims, tech.getUsername());
    }

    protected ResultActions createDeployment(String deploymentId) throws Exception {
        String payload = """
                {
                  "deploymentId": "%s",
                  "hostname": "PC-TEST",
                  "deviceModel": "Dell Latitude 5440",
                  "domainName": "corp.local"
                }
                """.formatted(deploymentId);

        return mockMvc.perform(post("/api/deployments")
                .header(API_KEY_HEADER, TEST_API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload));
    }
}
