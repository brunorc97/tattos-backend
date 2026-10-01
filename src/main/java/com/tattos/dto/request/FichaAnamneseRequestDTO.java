package com.tattos.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FichaAnamneseRequestDTO {

    @NotNull(message = "O ID do cliente é obrigatório")
    private Long clienteId;

    private Long agendamentoId;

    private String historicoSaude;

    private String alergias;

    @Builder.Default
    private Boolean termoAssinado = false;

    private String assinaturaDigital;

    @Builder.Default
    private Boolean diabetes = false;

    @Builder.Default
    private Boolean hemofilia = false;

    @Builder.Default
    private Boolean hipertensao = false;

    @Builder.Default
    private Boolean gestanteLactante = false;

    private String medicamentosUso;

    private String observacoes;
}
