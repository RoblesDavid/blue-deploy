package com.roblesdavid.blue_deploy.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity
@Table(name="system_check")

public class Check {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String hostname;

    @Column(name = "username")
    private String user;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name ="firewall_enabled")
    private Boolean firewallEnabled;

    @Column(name = "raw_json", columnDefinition = "TEXT")
    private String rawJson;

    // Getters y setters
    public Long getId() { return id; }

    public String getHostname() { return hostname; }
    public void setHostname(String hostname) { this.hostname = hostname; }

    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Boolean getFirewallEnabled() {
        return firewallEnabled;
    }

    public void setFirewallEnabled(Boolean firewallEnabled) {
        this.firewallEnabled = firewallEnabled;
    }

    public String getRawJson() { return rawJson; }
    public void setRawJson(String rawJson) { this.rawJson = rawJson; }
}
