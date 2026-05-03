package com.roblesdavid.blue_deploy.dto;

/**
 *
 * DTO Salida de datos del sistema
 */
public class CheckResponseDTO {
    public Long id;
    public String hostname;
    public String user;
    public String date;

    public Boolean firewallEnabled;
    public Boolean antivirusEnabled;
    public Boolean diskEncrypted;
    public Boolean updatesEnabled;
}
