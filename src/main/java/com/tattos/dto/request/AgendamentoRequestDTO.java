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
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgendamentoRequestDTO {

    @NotNull(message = "{validation.cliente.id.required}")
        @Schema(example = "1")private Long clienteId;

    @NotNull(message = "{validation.tatuador.id.required}")
        @Schema(example = "2")private Long tatuadorId;

    @NotNull(message = "{validation.agendamento.data.required}")
    @FutureOrPresent(message = "{validation.future}")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm[:ss]")
        @Schema(example = "2026-11-15T14:00:00")private LocalDateTime dataHora;

    @Builder.Default
        @Schema(example = "120")private Integer duracaoMinutos = 60;

    @NotBlank(message = "{validation.cabine.required}")
        @Schema(example = "A1")private String cabine;

    @NotNull(message = "{validation.procedimento.valor.required}")
        @Schema(example = "1500.00")private BigDecimal valor;

    @Builder.Default
        @Schema(example = "300.00")private BigDecimal valorSinal = BigDecimal.ZERO;

    
    @Schema(example = "ABC123")private String codigoIndicacao;

    @Schema(example = "BEMVINDO10")private String codigoCupom;

    
    @Schema(example = "Cliente prefere música ambiente")private String observacoes;
}
