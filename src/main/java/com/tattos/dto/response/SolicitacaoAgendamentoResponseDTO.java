package com.tattos.dto.response;

import com.tattos.model.SolicitacaoAgendamento;
import com.tattos.model.enums.StatusSolicitacao;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitacaoAgendamentoResponseDTO {

    @Schema(example = "1")
    private Long id;
    private Long clienteId;
    private String clienteNome;
    private Long tatuadorId;
    private String tatuadorNome;
    private Long projetoId;
    private Long imagemReferenciaId;
    private String ideia;
    private String estilo;
    private LocalDate diaPreferencia;
    private String semanaPreferencia;
    private StatusSolicitacao status;
    private String motivoRecusa;
    private Long agendamentoId;
    private LocalDateTime createdAt;

    public static SolicitacaoAgendamentoResponseDTO fromEntity(SolicitacaoAgendamento s) {
        return SolicitacaoAgendamentoResponseDTO.builder()
                .id(s.getId())
                .clienteId(s.getCliente() != null ? s.getCliente().getId() : null)
                .clienteNome(s.getCliente() != null ? s.getCliente().getNome() : null)
                .tatuadorId(s.getTatuador() != null ? s.getTatuador().getId() : null)
                .tatuadorNome(s.getTatuador() != null ? s.getTatuador().getNome() : null)
                .projetoId(s.getProjeto() != null ? s.getProjeto().getId() : null)
                .imagemReferenciaId(s.getImagemReferencia() != null ? s.getImagemReferencia().getId() : null)
                .ideia(s.getIdeia())
                .estilo(s.getEstilo())
                .diaPreferencia(s.getDiaPreferencia())
                .semanaPreferencia(s.getSemanaPreferencia())
                .status(s.getStatus())
                .motivoRecusa(s.getMotivoRecusa())
                .agendamentoId(s.getAgendamento() != null ? s.getAgendamento().getId() : null)
                .createdAt(s.getCreatedAt())
                .build();
    }
}
