package com.tattos.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjetoRequestDTO {

    @NotBlank(message = "{validation.projeto.nome.required}")
    @Size(min = 2, max = 255, message = "{validation.projeto.nome.size}")
        @Schema(example = "Capa de costas oriental")private String nome;

    
    @Schema(example = "Dragão japonês, 6 sessões")private String descricao;

    
    @Schema(example = "false")private Boolean aprovado;

    @jakarta.validation.constraints.NotNull(message = "{validation.cliente.id.required}")
    @Schema(example = "1")private Long clienteId;

    @Schema(example = "2")private Long tatuadorId;

    @Schema(example = "2027-01-15")private java.time.LocalDate prazo;
}
