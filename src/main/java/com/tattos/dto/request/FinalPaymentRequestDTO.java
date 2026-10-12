package com.tattos.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * DTO para finalizar o pagamento do agendamento, permitindo incluir um valor de cobrança adicional.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinalPaymentRequestDTO {

    @NotNull(message = "O valor da cobrança adicional é obrigatório")
    @DecimalMin(value = "0.0", inclusive = true, message = "O valor não pode ser negativo")
    private BigDecimal valorCobrancaAdicional;
}
