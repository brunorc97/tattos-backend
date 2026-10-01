package com.tattos.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDTO {

    private String token;

    @Builder.Default
    private String tipo = "Bearer";

    private Long tempoExpiracaoMs;

    private UsuarioResponseDTO usuario;
}
