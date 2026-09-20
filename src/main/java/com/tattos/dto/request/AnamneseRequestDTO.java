package com.tattos.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AnamneseRequestDTO(
    @NotBlank(message = "O nome do cliente é obrigatório")
    String clientName,

    @NotBlank(message = "O CPF do cliente é obrigatório")
    String cpf,

    String phone,
    LocalDate birthDate,
    String procedureType,
    String bodyLocation,
    String artistName,

    @NotNull(message = "A data da ficha de anamnese é obrigatória")
    LocalDate date,

    String allergies,

    @NotBlank(message = "A assinatura digital é obrigatória")
    String signatureData,

    Boolean diabetes,
    Boolean hemofilia,
    Boolean keloids,
    Boolean hepatiteHiv,
    Boolean pregnantNursing,
    Boolean alcohol24h,
    Boolean fedWell,
    Boolean cardiacCondition,
    Boolean medicationsInUse,
    String notes
) {
    public boolean hasAnyConditionAlert() {
        return Boolean.TRUE.equals(diabetes) ||
               Boolean.TRUE.equals(hemofilia) ||
               Boolean.TRUE.equals(keloids) ||
               Boolean.TRUE.equals(hepatiteHiv) ||
               Boolean.TRUE.equals(pregnantNursing) ||
               Boolean.TRUE.equals(alcohol24h) ||
               Boolean.TRUE.equals(cardiacCondition) ||
               Boolean.TRUE.equals(medicationsInUse) ||
               Boolean.FALSE.equals(fedWell);
    }
}
