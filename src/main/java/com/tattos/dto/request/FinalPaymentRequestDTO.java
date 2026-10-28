package com.tattos.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO para finalizar o pagamento do agendamento, permitindo incluir um valor de cobrança adicional.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinalPaymentRequestDTO {

    @NotNull(message = "{validation.pagamento.valor.required}")
    @DecimalMin(value = "0.0", inclusive = true, message = "{validation.valor.nonnegative}")
        @Schema(example = "50.00")private BigDecimal valorCobrancaAdicional;
}
