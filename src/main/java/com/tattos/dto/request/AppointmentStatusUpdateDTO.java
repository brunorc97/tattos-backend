package com.tattos.dto.request;

import com.tattos.model.enums.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

public record AppointmentStatusUpdateDTO(
    @NotNull(message = "O novo status da sessão é obrigatório")
    AppointmentStatus status
) {}
