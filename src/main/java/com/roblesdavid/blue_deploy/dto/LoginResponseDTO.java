package com.roblesdavid.blue_deploy.dto;

import com.roblesdavid.blue_deploy.model.UserRole;

/**
 * DTO de salida para la respuesta tras un login exitoso.
 */
public class LoginResponseDTO {

    private String token;
    private String tokenType = "Bearer";
    private long expiresIn;
    private Long id;
    private String username;
    private String name;
    private String email;
    private UserRole role;
    private String initials;
    private String department;

    public LoginResponseDTO() {
    }

    public LoginResponseDTO(String token, String tokenType, long expiresIn, Long id,
                            String username, String name, String email, UserRole role,
                            String initials, String department) {
        this.token = token;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.id = id;
        this.username = username;
        this.name = name;
        this.email = email;
        this.role = role;
        this.initials = initials;
        this.department = department;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getInitials() {
        return initials;
    }

    public void setInitials(String initials) {
        this.initials = initials;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
