package com.tattos.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AjusteEstoqueDTO {

    @NotNull(message = "{validation.quantidade.required}")
    @Min(value = 1, message = "{validation.quantidade.min}")
    private Integer quantidade;

    private String motivo;
}
