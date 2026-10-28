package com.tattos.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FichaAnamneseRequestDTO {

    @NotNull(message = "{validation.cliente.id.required}")
        @Schema(example = "1")private Long clienteId;

    
    @Schema(example = "10")private Long agendamentoId;

    
    @Schema(example = "Sem cirurgias recentes")private String historicoSaude;

    
    @Schema(example = "Níquel")private String alergias;

    @Builder.Default
        @Schema(example = "true")private Boolean termoAssinado = false;

    
    @Schema(example = "base64-da-assinatura")private String assinaturaDigital;

    @Builder.Default
        @Schema(example = "false")private Boolean diabetes = false;

    @Builder.Default
        @Schema(example = "false")private Boolean hemofilia = false;

    @Builder.Default
        @Schema(example = "false")private Boolean hipertensao = false;

    @Builder.Default
        @Schema(example = "false")private Boolean gestanteLactante = false;

    
    @Schema(example = "Nenhum")private String medicamentosUso;

    
    @Schema(example = "Pele sensível")private String observacoes;
}
