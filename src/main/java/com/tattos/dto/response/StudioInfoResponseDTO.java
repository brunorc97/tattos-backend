package com.tattos.dto.response;

import com.tattos.model.StudioInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudioInfoResponseDTO {

    private String nome;
    private String descricao;
    private String endereco;
    private String horario;
    private String instagram;

    public static StudioInfoResponseDTO fromEntity(StudioInfo s) {
        if (s == null) return null;
        return StudioInfoResponseDTO.builder()
                .nome(s.getNome())
                .descricao(s.getDescricao())
                .endereco(s.getEndereco())
                .horario(s.getHorario())
                .instagram(s.getInstagram())
                .build();
    }
}
