package com.tattos.dto.response;

import com.tattos.model.FichaAnamnese;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FichaAnamneseResponseDTO {

    @Schema(example = "1")private Long id;
    private Long clienteId;
    private String clienteNome;
    private String clienteCpf;
    private String clienteEmail;
    private Long agendamentoId;
    private String historicoSaude;
    private String alergias;
    private Boolean termoAssinado;
    private String assinaturaDigital;
    private Boolean alertaClinico;
    private Boolean riscoDermatologico;
    private Boolean diabetes;
    private Boolean hemofilia;
    private Boolean hipertensao;
    private Boolean gestanteLactante;
    private String medicamentosUso;
    private String observacoes;
    private LocalDateTime dataCriacao;

    public static FichaAnamneseResponseDTO fromEntity(FichaAnamnese f) {
        if (f == null) return null;
        return FichaAnamneseResponseDTO.builder()
                .id(f.getId())
                .clienteId(f.getCliente() != null ? f.getCliente().getId() : null)
                .clienteNome(f.getCliente() != null ? f.getCliente().getNome() : null)
                .clienteEmail(f.getCliente() != null ? f.getCliente().getEmail() : null)
                .agendamentoId(f.getAgendamento() != null ? f.getAgendamento().getId() : null)
                .historicoSaude(f.getHistoricoSaude())
                .alergias(f.getAlergias())
                .termoAssinado(f.getTermoAssinado())
                .assinaturaDigital(f.getAssinaturaDigital())
                .alertaClinico(f.getAlertaClinico())
                .riscoDermatologico(f.getRiscoDermatologico())
                .diabetes(f.getDiabetes())
                .hemofilia(f.getHemofilia())
                .hipertensao(f.getHipertensao())
                .gestanteLactante(f.getGestanteLactante())
                .medicamentosUso(f.getMedicamentosUso())
                .observacoes(f.getObservacoes())
                .dataCriacao(f.getDataCriacao())
                .build();
    }
}
