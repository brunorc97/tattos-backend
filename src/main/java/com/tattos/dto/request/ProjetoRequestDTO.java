package com.tattos.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjetoRequestDTO {

    @NotBlank(message = "{validation.projeto.nome.required}")
    @Size(min = 2, max = 255, message = "{validation.projeto.nome.size}")
    private String nome;

    private String descricao;

    private Boolean aprovado;
}
