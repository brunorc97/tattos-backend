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

    @NotBlank(message = "O nome do projeto é obrigatório")
    @Size(min = 2, max = 255, message = "O nome do projeto deve ter entre 2 e 255 caracteres")
    private String nome;

    private String descricao;

    private Boolean aprovado;
}
