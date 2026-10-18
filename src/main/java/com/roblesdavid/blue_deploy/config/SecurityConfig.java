package com.roblesdavid.blue_deploy.config;

import com.roblesdavid.blue_deploy.security.ApiKeyAuthenticationFilter;
import com.roblesdavid.blue_deploy.security.CustomUserDetailsService;
import com.roblesdavid.blue_deploy.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuración de seguridad Spring Security con autenticación JWT sin estado (stateless),
 * autenticación por API Key para endpoints de ingesta (X-BlueDeploy-Api-Key)
 * y autorización basada en roles (ADMIN, TECHNICIAN, API_CLIENT).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;
    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          ApiKeyAuthenticationFilter apiKeyAuthenticationFilter,
                          CustomUserDetailsService userDetailsService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.apiKeyAuthenticationFilter = apiKeyAuthenticationFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/checks/**").permitAll()
                        .requestMatchers("/api/audit/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/deployments/*/assign-technician/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/deployments/*/claim").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/deployments/*/status").hasAnyRole("ADMIN", "TECHNICIAN", "API_CLIENT")
                        .requestMatchers("/api/deployments/my").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/deployments").hasAnyRole("API_CLIENT", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/deployments/*/steps").hasAnyRole("API_CLIENT", "ADMIN", "TECHNICIAN")
                        .requestMatchers(HttpMethod.POST, "/api/deployments/*/checks").hasAnyRole("API_CLIENT", "ADMIN", "TECHNICIAN")
                        .requestMatchers(HttpMethod.PUT, "/api/deployments/*/checks/**").hasAnyRole("API_CLIENT", "ADMIN", "TECHNICIAN")
                        .requestMatchers("/api/deployments/**").permitAll()
                        .requestMatchers("/api/dashboard/**").permitAll()
                        .requestMatchers("/api/users/technicians/**").hasRole("ADMIN")
                        .requestMatchers("/api/users/me").authenticated()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setCharacterEncoding("UTF-8");
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("{\"error\":\"No autorizado\",\"message\":\"" + authException.getMessage() + "\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setCharacterEncoding("UTF-8");
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.getWriter().write("{\"error\":\"Acceso denegado\",\"message\":\"" + accessDeniedException.getMessage() + "\"}");
                        })
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(apiKeyAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${cors.allowed-origins:*}") String allowedOrigins
    ) {
        CorsConfiguration configuration = new CorsConfiguration();
        if ("*".equals(allowedOrigins.trim())) {
            configuration.addAllowedOriginPattern("*");
        } else {
            for (String origin : allowedOrigins.split(",")) {
                if (!origin.isBlank()) {
                    configuration.addAllowedOrigin(origin.trim());
                }
            }
        }
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
