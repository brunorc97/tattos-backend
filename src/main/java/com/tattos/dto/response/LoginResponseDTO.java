package com.tattos.dto.response;

public record LoginResponseDTO(
    String token,
    String type,
    UserResponseDTO user
) {
    public static LoginResponseDTO of(String token, UserResponseDTO user) {
        return new LoginResponseDTO(token, "Bearer", user);
    }
}
