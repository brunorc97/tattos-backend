package com.tattos.dto.response;

import com.tattos.model.Trabalho;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrabalhoResponseDTO {

    private Long id;
    private Long tatuadorId;
    private String tatuadorNome;
    private String titulo;
    private String estilo;
    private String descricao;
    private String fotoUrl;
    private LocalDate data;

    public static TrabalhoResponseDTO fromEntity(Trabalho t) {
        if (t == null) return null;
        return TrabalhoResponseDTO.builder()
                .id(t.getId())
                .tatuadorId(t.getTatuador() != null ? t.getTatuador().getId() : null)
                .tatuadorNome(t.getTatuador() != null ? t.getTatuador().getNome() : null)
                .titulo(t.getTitulo())
                .estilo(t.getEstilo())
                .descricao(t.getDescricao())
                .fotoUrl(t.getFotoUrl())
                .data(t.getData())
                .build();
    }
}
