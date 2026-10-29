package com.tattos.dto.response;

import com.tattos.model.Usuario;
import com.tattos.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioResponseDTO {

    @Schema(example = "1")private Long id;
    private String nome;
    private String email;
    private UserRole role;
    private String telefone;
    private String endereco;
    private String especialidade;
    private String bio;
    private String cargo;
    private BigDecimal comissaoPercentual;
    private String codigoIndicacao;
    private java.time.LocalDate dataNascimento;
    private String pixChave;
    private String banco;
    private String agencia;
    private String conta;
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
                .codigoIndicacao(u.getCodigoIndicacao())
                .dataNascimento(u.getDataNascimento())
                .pixChave(u.getPixChave())
                .banco(u.getBanco())
                .agencia(u.getAgencia())
                .conta(u.getConta())
                .ativo(u.isAtivo())
                .createdAt(u.getCreatedAt())
                .build();
    }
}
