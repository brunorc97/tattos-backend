package com.tattos.dto.response;

import com.tattos.model.Bancada;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BancadaResponseDTO {

    private Long id;
    private String nome;
    private String descricao;
    private boolean ativa;

    public static BancadaResponseDTO fromEntity(Bancada b) {
        if (b == null) return null;
        return BancadaResponseDTO.builder()
                .id(b.getId())
                .nome(b.getNome())
                .descricao(b.getDescricao())
                .ativa(b.isAtiva())
                .build();
    }
}
