package com.tattos.dto.request;

import com.tattos.model.enums.MotivoRecusaProjeto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AprovacaoProjetoRequestDTO {

    @NotNull(message = "{validation.required}")
    @Schema(example = "true")
    private Boolean aprovado;

    @Schema(example = "VALOR")
    private MotivoRecusaProjeto motivoRecusa;

    @Size(max = 500)
    @Schema(example = "Cliente achou o valor acima do orçado")
    private String detalhe;
}
