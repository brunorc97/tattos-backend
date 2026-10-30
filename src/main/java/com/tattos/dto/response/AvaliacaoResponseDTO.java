package com.tattos.dto.response;

import com.tattos.model.Avaliacao;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvaliacaoResponseDTO {

    private Long id;
    private Long trabalhoId;
    private Long tatuadorId;
    private String tatuadorNome;
    private String clienteNome;
    private Integer notaTatuagem;
    private Integer notaTatuador;
    private String comentario;
    private LocalDate data;

    public static AvaliacaoResponseDTO fromEntity(Avaliacao a) {
        if (a == null) return null;
        return AvaliacaoResponseDTO.builder()
                .id(a.getId())
                .trabalhoId(a.getTrabalho() != null ? a.getTrabalho().getId() : null)
                .tatuadorId(a.getTatuador() != null ? a.getTatuador().getId() : null)
                .tatuadorNome(a.getTatuador() != null ? a.getTatuador().getNome() : null)
                .clienteNome(a.getCliente() != null ? a.getCliente().getNome() : null)
                .notaTatuagem(a.getNotaTatuagem())
                .notaTatuador(a.getNotaTatuador())
                .comentario(a.getComentario())
                .data(a.getData())
                .build();
    }
}
