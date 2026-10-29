package com.tattos.controller;

import com.tattos.dto.response.PagamentoComissaoDTO;
import com.tattos.dto.response.TransacaoResponseDTO;
import com.tattos.model.PagamentoComissao;
import com.tattos.model.enums.StatusRepasse;
import com.tattos.model.enums.TipoTransacao;
import com.tattos.repository.TransacaoFinanceiraRepository;
import com.tattos.service.PagamentoComissaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Pagamentos de Comissão", description = "Seleção de trabalhos, abatimento de adiantamentos, confirmação e histórico")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/financeiro")
@RequiredArgsConstructor
public class PagamentoComissaoController {

    private final PagamentoComissaoService pagamentoService;
    private final TransacaoFinanceiraRepository transacaoRepository;
    private final com.tattos.service.ReciboPdfService reciboPdfService;
    private final com.tattos.service.AdiantamentoService adiantamentoService;

    public record PagamentoRequest(
            @NotNull Long tatuadorId,
            @NotEmpty List<Long> transacaoIds,
            @NotNull @Pattern(regexp = "PIX|DINHEIRO") String formaPagamento,
            Long comprovanteId) {}

    @Operation(summary = "Trabalhos do tatuador por status de repasse",
            description = "Lista receitas do tatuador com valor de repasse, filtradas por status (PENDENTE/AGENDADO/PAGO) e período.")
    @GetMapping("/comissoes/{tatuadorId}/trabalhos")
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<Page<TransacaoResponseDTO>> trabalhos(
            @PathVariable Long tatuadorId,
            @RequestParam(required = false) StatusRepasse statusRepasse,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @PageableDefault(size = 50, sort = "data") Pageable pageable) {
        return ResponseEntity.ok(transacaoRepository
                .findTrabalhosParaRepasse(tatuadorId, statusRepasse, inicio, fim, pageable)
                .map(TransacaoResponseDTO::fromEntity));
    }

    @Operation(summary = "Criar pagamento de comissão",
            description = "Marca as transações como AGENDADO, abate adiantamentos em aberto (FIFO) e agenda o pagamento. Comprovante/recibo opcional (arquivoId).")
    @PostMapping("/pagamentos-comissao")
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<PagamentoComissaoDTO> criar(@Valid @RequestBody PagamentoRequest body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pagamentoService.criar(body.tatuadorId(), body.transacaoIds(),
                        body.formaPagamento(), body.comprovanteId()));
    }

    @Operation(summary = "Confirmar pagamento", description = "Marca como PAGO e baixa as transações.")
    @PostMapping("/pagamentos-comissao/{id}/confirmar")
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<PagamentoComissaoDTO> confirmar(@PathVariable Long id) {
        return ResponseEntity.ok(pagamentoService.confirmar(id));
    }

    @Operation(summary = "Recibo do pagamento em PDF", description = "Recibo detalhado; gestor ou o próprio tatuador do pagamento.")
    @GetMapping("/pagamentos-comissao/{id}/recibo.pdf")
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR')")
    public ResponseEntity<byte[]> reciboPdf(
            @PathVariable Long id,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.tattos.model.Usuario logado) {
        byte[] pdf = pagamentoService.gerarReciboPdf(id, logado);
        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=recibo-pagamento-" + id + ".pdf")
                .body(pdf);
    }

    @Operation(summary = "Meus repasses (visão do tatuador)",
            description = "Trabalhos com status de recebimento, pagamentos e adiantamentos em aberto do próprio tatuador logado.")
    @GetMapping("/meus-repasses")
    @PreAuthorize("hasRole('TATUADOR')")
    public ResponseEntity<java.util.Map<String, Object>> meusRepasses(
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.tattos.model.Usuario tatuador,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @PageableDefault(size = 20, sort = "data") Pageable pageable) {
        var trabalhos = transacaoRepository
                .findTrabalhosParaRepasse(tatuador.getId(), null, inicio, fim, pageable)
                .map(TransacaoResponseDTO::fromEntity);
        var pagamentos = pagamentoService.historico(tatuador.getId(), null,
                org.springframework.data.domain.Pageable.unpaged()).getContent();
        var adiantamentos = adiantamentoService.saldoEmAberto(tatuador.getId());

        return ResponseEntity.ok(java.util.Map.of(
                "trabalhos", trabalhos,
                "pagamentos", pagamentos,
                "adiantamentosEmAberto", adiantamentos
        ));
    }

    @Operation(summary = "Histórico de pagamentos do tatuador")
    @GetMapping("/pagamentos-comissao")
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<Page<PagamentoComissaoDTO>> historico(
            @RequestParam Long tatuadorId,
            @RequestParam(required = false) PagamentoComissao.Status status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(pagamentoService.historico(tatuadorId, status, pageable));
    }
}
