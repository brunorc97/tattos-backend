package com.tattos.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TatuadorVitrineResponseDTO {

    private Long id;
    private String nome;
    private String especialidade;
    private String bio;
    private String fotoUrl;
    private BigDecimal mediaAvaliacao;
    private long totalAvaliacoes;
}
