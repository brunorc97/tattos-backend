package com.tattos.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/** Perfil 360° do cliente: agregação read-only das fontes existentes. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientePerfilDTO {

    private UsuarioResponseDTO cliente;
    private long totalTrabalhos;
    private ProjetosResumo projetos;
    private List<FichaResumo> ultimasFichas;
    private String alergias;
    private List<ContatoResumo> ultimosContatos;
    private LocalDate proximoAniversario;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProjetosResumo {
        private long total;
        private long aprovados;
        private List<ProjetoRecusado> naoAprovados;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProjetoRecusado {
        private Long id;
        private String nome;
        private String motivoRecusa;
        private String motivoRecusaDetalhe;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FichaResumo {
        private Long id;
        private java.time.LocalDateTime data;
        private boolean alertaClinico;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContatoResumo {
        private Long id;
        private String tipo;
        private String resumo;
        private java.time.LocalDateTime dataContato;
    }
}
