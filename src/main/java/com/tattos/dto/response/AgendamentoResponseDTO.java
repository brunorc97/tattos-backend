package com.tattos.dto.response;

import com.tattos.model.Agendamento;
import com.tattos.model.enums.StatusAgendamento;
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
public class AgendamentoResponseDTO {

    @Schema(example = "1")private Long id;
    private LocalDateTime dataHora;
    private LocalDateTime dataHoraFim;
    private Integer duracaoMinutos;
    private String cabine;
    private Long bancadaId;
    private String bancadaNome;
    private StatusAgendamento status;
    private BigDecimal valor;
    private BigDecimal valorSinal;
    private boolean sinalPago;
    private String codigoPixSinal;
    private BigDecimal descontoAplicado;
    private BigDecimal valorFinal;
    private String observacoes;
    private BigDecimal valorCobrancaAdicional;
    private boolean pagamentoCompleto;
    private LocalDateTime dataPagamento;

    private Long clienteId;
    private String clienteNome;
    private String clienteTelefone;
    private String clienteEmail;

    private Long tatuadorId;
    private String tatuadorNome;
    private String tatuadorEspecialidade;

    private Long fichaAnamneseId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AgendamentoResponseDTO fromEntity(Agendamento a) {
        if (a == null) return null;
        BigDecimal valorBase = a.getValor() != null ? a.getValor() : BigDecimal.ZERO;
        BigDecimal desconto = a.getDescontoAplicado() != null ? a.getDescontoAplicado() : BigDecimal.ZERO;
        BigDecimal valorFinal = valorBase.subtract(desconto);

        return AgendamentoResponseDTO.builder()
                .id(a.getId())
                .dataHora(a.getDataHora())
                .dataHoraFim(a.getDataHoraFim())
                .duracaoMinutos(a.getDuracaoMinutos())
                .cabine(a.getCabine())
                .bancadaId(a.getBancada() != null ? a.getBancada().getId() : null)
                .bancadaNome(a.getBancada() != null ? a.getBancada().getNome() : null)
                .status(a.getStatus())
                .valor(valorBase)
                .valorSinal(a.getValorSinal())
                .sinalPago(a.isSinalPago())
                .codigoPixSinal(a.getCodigoPixSinal())
                .descontoAplicado(desconto)
                .valorFinal(valorFinal)
                .observacoes(a.getObservacoes())
                .valorCobrancaAdicional(a.getValorCobrancaAdicional())
                .pagamentoCompleto(a.isPagamentoCompleto())
                .dataPagamento(a.getDataPagamento())
                .clienteId(a.getCliente() != null ? a.getCliente().getId() : null)
                .clienteNome(a.getCliente() != null ? a.getCliente().getNome() : null)
                .clienteTelefone(a.getCliente() != null ? a.getCliente().getTelefone() : null)
                .clienteEmail(a.getCliente() != null ? a.getCliente().getEmail() : null)
                .tatuadorId(a.getTatuador() != null ? a.getTatuador().getId() : null)
                .tatuadorNome(a.getTatuador() != null ? a.getTatuador().getNome() : null)
                .tatuadorEspecialidade(a.getTatuador() != null ? a.getTatuador().getEspecialidade() : null)
                .fichaAnamneseId(a.getFichaAnamnese() != null ? a.getFichaAnamnese().getId() : null)
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
