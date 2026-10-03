package com.roblesdavid.blue_deploy.mapper;

import com.roblesdavid.blue_deploy.dto.UserResponseDTO;
import com.roblesdavid.blue_deploy.model.AppUser;

/**
 * Mapper para convertir la entidad {@link AppUser} en su DTO de salida {@link UserResponseDTO}.
 */
public class UserMapper {

    /**
     * Mapea un AppUser a UserResponseDTO.
     *
     * @param user entidad usuario
     * @return DTO con datos públicos del usuario
     */
    public static UserResponseDTO toDTO(AppUser user) {
        if (user == null) {
            return null;
        }

        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setName(user.getName());
        dto.setInitials(user.getInitials());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole() != null ? user.getRole().name() : null);
        dto.setDepartment(user.getDepartment());
        dto.setActive(user.isActive());
        dto.setCreatedAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
        dto.setUpdatedAt(user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : null);

        return dto;
    }
}
