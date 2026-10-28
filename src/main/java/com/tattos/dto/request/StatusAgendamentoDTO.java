package com.tattos.dto.request;

import com.tattos.model.enums.StatusAgendamento;
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
public class StatusAgendamentoDTO {

    @NotNull(message = "{validation.status.required}")
        @Schema(example = "CONFIRMADO")private StatusAgendamento status;

    
    @Schema(example = "Cliente remarcou")private String motivoCancelamento;
}
