package com.tattos.dto.request;

import com.tattos.model.enums.StatusAgendamento;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusAgendamentoDTO {

    @NotNull(message = "{validation.status.required}")
    private StatusAgendamento status;

    private String motivoCancelamento;
}
