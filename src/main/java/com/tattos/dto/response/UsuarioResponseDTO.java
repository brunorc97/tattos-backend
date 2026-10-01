package com.tattos.dto.response;

import com.tattos.model.Usuario;
import com.tattos.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioResponseDTO {

    private Long id;
    private String nome;
    private String email;
    private UserRole role;
    private String telefone;
    private String endereco;
    private String especialidade;
    private String bio;
    private String cargo;
    private BigDecimal comissaoPercentual;
    private boolean ativo;
    private LocalDateTime createdAt;

    public static UsuarioResponseDTO fromEntity(Usuario u) {
        if (u == null) return null;
        return UsuarioResponseDTO.builder()
                .id(u.getId())
                .nome(u.getNome())
                .email(u.getEmail())
                .role(u.getRole())
                .telefone(u.getTelefone())
                .endereco(u.getEndereco())
                .especialidade(u.getEspecialidade())
                .bio(u.getBio())
                .cargo(u.getCargo())
                .comissaoPercentual(u.getComissaoPercentual())
                .ativo(u.isAtivo())
                .createdAt(u.getCreatedAt())
                .build();
    }
}
