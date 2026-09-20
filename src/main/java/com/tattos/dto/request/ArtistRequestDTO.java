package com.tattos.dto.request;

import com.tattos.model.enums.ArtistStatus;
import com.tattos.model.enums.ArtistTag;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ArtistRequestDTO(
    @NotBlank(message = "O nome do tatuador é obrigatório")
    String name,

    @NotNull(message = "A tag do tatuador (RESIDENTE/VISITANTE) é obrigatória")
    ArtistTag tag,

    @NotBlank(message = "A especialidade é obrigatória")
    String specialty,

    @NotNull(message = "O percentual de comissão é obrigatório")
    @DecimalMin(value = "0.0", message = "Comissão mínima é 0%")
    @DecimalMax(value = "100.0", message = "Comissão máxima é 100%")
    BigDecimal splitRate,

    String station,
    String phone,
    String instagram,

    @Email(message = "Formato de e-mail inválido")
    String email,

    ArtistStatus status,
    String bio,
    String originStudio,
    LocalDate guestPeriodStart,
    LocalDate guestPeriodEnd
) {}
