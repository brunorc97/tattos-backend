package com.tattos.dto.request;

import com.tattos.model.enums.AppointmentStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record AppointmentRequestDTO(
    @NotBlank(message = "O nome do cliente é obrigatório")
    String clientName,

    String clientPhone,

    @NotNull(message = "O ID do tatuador é obrigatório")
    Long artistId,

    @NotBlank(message = "A identificação da bancada é obrigatória")
    String station,

    @NotNull(message = "A data do agendamento é obrigatória")
    LocalDate date,

    @NotNull(message = "O horário de início é obrigatório")
    LocalTime startTime,

    @NotNull(message = "A duração da sessão (em horas) é obrigatória")
    @Positive(message = "A duração deve ser maior que zero")
    Double durationHours,

    String style,

    @NotNull(message = "O valor total da sessão é obrigatório")
    @DecimalMin(value = "0.00", message = "O valor não pode ser negativo")
    BigDecimal price,

    @DecimalMin(value = "0.00", message = "O sinal não pode ser negativo")
    BigDecimal deposit,

    AppointmentStatus status,

    String notes
) {
    public BigDecimal getEffectiveDeposit() {
        return deposit != null ? deposit : BigDecimal.ZERO;
    }

    public AppointmentStatus getEffectiveStatus() {
        return status != null ? status : AppointmentStatus.CONFIRMADO;
    }
}
