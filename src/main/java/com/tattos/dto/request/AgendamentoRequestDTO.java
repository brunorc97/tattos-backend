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

    @NotNull(message = "O ID do cliente é obrigatório")
    private Long clienteId;

    @NotNull(message = "O ID do tatuador é obrigatório")
    private Long tatuadorId;

    @NotNull(message = "A data e horário do agendamento são obrigatórios")
    @FutureOrPresent(message = "A data e hora do agendamento não podem ser no passado")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm[:ss]")
    private LocalDateTime dataHora;

    @Builder.Default
    private Integer duracaoMinutos = 60;

    @NotBlank(message = "A cabine é obrigatória")
    private String cabine;

    @NotNull(message = "O valor do procedimento é obrigatório")
    private BigDecimal valor;

    @Builder.Default
    private BigDecimal valorSinal = BigDecimal.ZERO;

    private String observacoes;
}
