package com.tattos.dto.response;

import com.tattos.model.IdeiaTatuagem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdeiaResponseDTO {

    private Long id;
    private String titulo;
    private String estilo;
    private String descricao;
    private String imagemUrl;

    public static IdeiaResponseDTO fromEntity(IdeiaTatuagem i) {
        if (i == null) return null;
        return IdeiaResponseDTO.builder()
                .id(i.getId())
                .titulo(i.getTitulo())
                .estilo(i.getEstilo())
                .descricao(i.getDescricao())
                .imagemUrl(i.getImagemUrl())
                .build();
    }
}
