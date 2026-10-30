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
public class TrabalhoPendenteResponseDTO {

    private Long id;
    private Long tatuadorId;
    private String tatuadorNome;
    private String titulo;
    private String estilo;
    private LocalDate dataConclusao;

    public static TrabalhoPendenteResponseDTO fromEntity(Trabalho t) {
        if (t == null) return null;
        return TrabalhoPendenteResponseDTO.builder()
                .id(t.getId())
                .tatuadorId(t.getTatuador() != null ? t.getTatuador().getId() : null)
                .tatuadorNome(t.getTatuador() != null ? t.getTatuador().getNome() : null)
                .titulo(t.getTitulo())
                .estilo(t.getEstilo())
                .dataConclusao(t.getData())
                .build();
    }
}
