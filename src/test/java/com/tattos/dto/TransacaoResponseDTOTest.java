package com.tattos.dto;

import com.tattos.dto.response.TransacaoResponseDTO;
import com.tattos.model.TransacaoFinanceira;
import com.tattos.model.enums.StatusRepasse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransacaoResponseDTOTest {

    @Test
    void fromEntityExpoeStatusRepasse() {
        TransacaoFinanceira t = TransacaoFinanceira.builder()
                .descricao("x").valor(BigDecimal.TEN).build();
        TransacaoResponseDTO dto = TransacaoResponseDTO.fromEntity(t);
        assertEquals(StatusRepasse.PENDENTE, dto.getStatusRepasse());
    }
}
