package com.roblesdavid.blue_deploy.service;

import com.roblesdavid.blue_deploy.dto.CreateTechnicianRequestDTO;
import com.roblesdavid.blue_deploy.dto.UpdateProfileRequestDTO;
import com.roblesdavid.blue_deploy.dto.UserResponseDTO;
import com.roblesdavid.blue_deploy.mapper.UserMapper;
import com.roblesdavid.blue_deploy.model.AppUser;
import com.roblesdavid.blue_deploy.model.UserRole;
import com.roblesdavid.blue_deploy.repository.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de negocio para la administración de usuarios y técnicos.
 */
@Service
public class UserService {

    private final AppUserRepository userRepository;

    public UserService(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Obtiene los datos del perfil de un usuario por su identificador.
     *
     * @param userId identificador del usuario
     * @return DTO del usuario
     */
    @Transactional(readOnly = true)
    public UserResponseDTO getUserProfileById(Long userId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con id: " + userId));
        return UserMapper.toDTO(user);
    }

    /**
     * Obtiene los datos del perfil de un usuario por su nombre de usuario.
     *
     * @param username nombre de usuario
     * @return DTO del usuario
     */
    @Transactional(readOnly = true)
    public UserResponseDTO getUserProfileByUsername(String username) {
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con username: " + username));
        return UserMapper.toDTO(user);
    }

    /**
     * Actualiza el perfil de un usuario dado su identificador.
     *
     * @param userId identificador del usuario
     * @param dto datos a actualizar
     * @return DTO actualizado
     */
    @Transactional
    public UserResponseDTO updateUserProfile(Long userId, UpdateProfileRequestDTO dto) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con id: " + userId));

        return applyProfileUpdate(user, dto);
    }

    /**
     * Actualiza el perfil de un usuario dado su nombre de usuario.
     *
     * @param username nombre de usuario
     * @param dto datos a actualizar
     * @return DTO actualizado
     */
    @Transactional
    public UserResponseDTO updateUserProfileByUsername(String username, UpdateProfileRequestDTO dto) {
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con username: " + username));

        return applyProfileUpdate(user, dto);
    }

    /**
     * Lista todos los técnicos registrados en la plataforma.
     *
     * @return listado de técnicos
     */
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllTechnicians() {
        return userRepository.findByRole(UserRole.TECHNICIAN)
                .stream()
                .map(UserMapper::toDTO)
                .toList();
    }

    /**
     * Da de alta un nuevo técnico en el sistema.
     *
     * @param dto datos del técnico a crear
     * @return técnico creado
     */
    @Transactional
    public UserResponseDTO createTechnician(CreateTechnicianRequestDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("El nombre de usuario ya está en uso: " + dto.getUsername());
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("El correo electrónico ya está en uso: " + dto.getEmail());
        }

        AppUser user = new AppUser();
        user.setUsername(dto.getUsername().trim());
        user.setPassword(dto.getPassword());
        user.setName(dto.getName().trim());
        user.setEmail(dto.getEmail().trim());
        user.setRole(UserRole.TECHNICIAN);
        user.setDepartment(dto.getDepartment());
        user.setActive(true);

        if (dto.getInitials() != null && !dto.getInitials().isBlank()) {
            user.setInitials(dto.getInitials().trim().toUpperCase());
        } else {
            user.setInitials(generateInitials(user.getName()));
        }

        AppUser saved = userRepository.save(user);
        return UserMapper.toDTO(saved);
    }

    /**
     * Modifica los datos de un técnico específico.
     *
     * @param id identificador del técnico
     * @param dto nuevos datos
     * @return técnico actualizado
     */
    @Transactional
    public UserResponseDTO updateTechnician(Long id, UpdateProfileRequestDTO dto) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con id: " + id));

        if (user.getRole() != UserRole.TECHNICIAN) {
            throw new IllegalArgumentException("El usuario especificado no es un técnico");
        }

        return applyProfileUpdate(user, dto);
    }

    /**
     * Desactiva (borrado lógico) a un técnico.
     *
     * @param id identificador del técnico
     * @return técnico desactivado
     */
    @Transactional
    public UserResponseDTO disableTechnician(Long id) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con id: " + id));

        user.setActive(false);
        AppUser saved = userRepository.save(user);
        return UserMapper.toDTO(saved);
    }

    private UserResponseDTO applyProfileUpdate(AppUser user, UpdateProfileRequestDTO dto) {
        if (!user.getEmail().equalsIgnoreCase(dto.getEmail())) {
            userRepository.findByEmail(dto.getEmail()).ifPresent(existing -> {
                if (!existing.getId().equals(user.getId())) {
                    throw new IllegalArgumentException("El correo electrónico ya está en uso: " + dto.getEmail());
                }
            });
            user.setEmail(dto.getEmail().trim());
        }

        user.setName(dto.getName().trim());
        user.setDepartment(dto.getDepartment());

        if (dto.getInitials() != null && !dto.getInitials().isBlank()) {
            user.setInitials(dto.getInitials().trim().toUpperCase());
        } else {
            user.setInitials(generateInitials(user.getName()));
        }

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(dto.getPassword());
        }

        AppUser saved = userRepository.save(user);
        return UserMapper.toDTO(saved);
    }

    private String generateInitials(String name) {
        if (name == null || name.isBlank()) {
            return "";
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }
}
