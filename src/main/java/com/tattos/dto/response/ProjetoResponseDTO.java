package com.tattos.dto.response;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
public class ProjetoResponseDTO {
    @Schema(example = "1")private Long id;
    private String nome;
    private String descricao;
    private boolean aprovado;
    private Long clienteId;
    private Long tatuadorId;
    private java.time.LocalDate prazo;
    private String statusAprovacao;
    private String motivoRecusa;
    private String motivoRecusaDetalhe;
    private boolean necessitaSinal;
    private java.math.BigDecimal valorSinal;
    private boolean sinalPago;
    private String formaPagamentoSinal;
    private java.time.LocalDateTime dataPagamentoSinal;
    private Long comprovanteSinalId;
    private java.time.LocalDateTime dataCriacao;
}
