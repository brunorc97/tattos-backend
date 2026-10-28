package com.tattos.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgendamentoRequestDTO {

    @NotNull(message = "{validation.cliente.id.required}")
    private Long clienteId;

    @NotNull(message = "{validation.tatuador.id.required}")
    private Long tatuadorId;

    @NotNull(message = "{validation.agendamento.data.required}")
    @FutureOrPresent(message = "{validation.future}")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm[:ss]")
    private LocalDateTime dataHora;

    @Builder.Default
    private Integer duracaoMinutos = 60;

    @NotBlank(message = "{validation.cabine.required}")
    private String cabine;

    @NotNull(message = "{validation.procedimento.valor.required}")
    private BigDecimal valor;

    @Builder.Default
    private BigDecimal valorSinal = BigDecimal.ZERO;

    private String codigoIndicacao;

    private String observacoes;
}
