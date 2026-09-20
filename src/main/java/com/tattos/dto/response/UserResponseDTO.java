package com.tattos.dto.response;

import com.tattos.model.User;
import com.tattos.model.enums.UserRole;

public record UserResponseDTO(
    Long id,
    String username,
    String name,
    String email,
    UserRole role
) {
    public static UserResponseDTO fromEntity(User user) {
        return new UserResponseDTO(
            user.getId(),
            user.getUsername(),
            user.getName(),
            user.getEmail(),
            user.getRole()
        );
    }
}
