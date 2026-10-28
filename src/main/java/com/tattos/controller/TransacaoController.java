package com.tattos.controller;

import com.tattos.dto.request.TransacaoRequestDTO;
import com.tattos.dto.response.BalancoFinanceiroDTO;
import com.tattos.dto.response.FechamentoComissaoDTO;
import com.tattos.dto.response.TransacaoResponseDTO;
import com.tattos.model.enums.StatusTransacao;
import com.tattos.model.enums.TipoTransacao;
import com.tattos.service.TransacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Tag(name = "Financeiro", description = "Transações financeiras, balanço mensal e repasses de comissão")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping({"/api/v1/financeiro", "/api/v1/financial", "/api/financeiro", "/api/financial"})
@RequiredArgsConstructor
public class TransacaoController {

    private final TransacaoService transacaoService;

    @Operation(summary = "Listar transações", description = "Filtra transações paginadas (Page) por tipo, tatuador, status e período. Suporta page, size e sort.")
    @ApiResponse(responseCode = "200", description = "Página retornada com sucesso")
    @GetMapping({"/transacoes", "/transactions", "/transacoes/paginado", "/transactions/paged"})
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR')")
    public ResponseEntity<Page<TransacaoResponseDTO>> listarPaginado(
            @RequestParam(required = false) TipoTransacao tipo,
            @RequestParam(required = false) Long tatuadorId,
            @RequestParam(required = false) StatusTransacao status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @PageableDefault(size = 20, sort = "data") Pageable pageable) {
        return ResponseEntity.ok(transacaoService.listarPaginado(tipo, tatuadorId, status, inicio, fim, pageable));
    }

    @Operation(summary = "Buscar transação por ID")
    @ApiResponse(responseCode = "200", description = "Transação encontrada")
    @ApiResponse(responseCode = "404", description = "Transação não encontrada")
    @GetMapping({"/transacoes/{id}", "/transactions/{id}"})
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR')")
    public ResponseEntity<TransacaoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(transacaoService.buscarPorId(id));
    }

    @Operation(summary = "Criar transação financeira")
    @ApiResponse(responseCode = "201", description = "Transação criada")
    @PostMapping({"/transacoes", "/transactions"})
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR')")
    public ResponseEntity<TransacaoResponseDTO> criar(@Valid @RequestBody TransacaoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transacaoService.criar(dto));
    }

    @Operation(
        summary = "Balanço mensal",
        description = "Dashboard financeiro: faturamento bruto vs custos e repasses (\"Smile Now, Cry Later\")."
    )
    @ApiResponse(responseCode = "200", description = "Balanço calculado com sucesso")
    @GetMapping({"/balanco", "/summary"})
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<BalancoFinanceiroDTO> obterBalancoMensal(
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer ano) {
        int mesResolvido = (mes != null) ? mes : LocalDate.now().getMonthValue();
        int anoResolvido = (ano != null) ? ano : LocalDate.now().getYear();
        return ResponseEntity.ok(transacaoService.obterBalancoMensal(mesResolvido, anoResolvido));
    }

    @Operation(summary = "Calcular repasse de tatuador", description = "Retorna o total de comissão devida a um tatuador no período.")
    @ApiResponse(responseCode = "200", description = "Repasse calculado")
    @GetMapping({"/repasses/{tatuadorId}", "/splits/{tatuadorId}"})
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<Map<String, Object>> calcularRepasse(
            @PathVariable Long tatuadorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        BigDecimal totalRepasse = transacaoService.calcularRepasseTatuador(tatuadorId, inicio, fim);
        return ResponseEntity.ok(Map.of(
                "tatuadorId", tatuadorId,
                "periodoInicio", inicio != null ? inicio.toString() : "Início do mês atual",
                "periodoFim", fim != null ? fim.toString() : "Fim do mês atual",
                "totalRepasse", totalRepasse
        ));
    }

    @Operation(summary = "Relatório de fechamento de comissões do tatuador", description = "Gera o demonstrativo detalhado de comissões acumuladas por período para repasse.")
    @ApiResponse(responseCode = "200", description = "Relatório gerado com sucesso")
    @GetMapping({"/comissoes/{tatuadorId}/fechamento", "/commissions/{tatuadorId}/statement"})
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR')")
    public ResponseEntity<FechamentoComissaoDTO> gerarFechamentoComissoes(
            @PathVariable Long tatuadorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(transacaoService.gerarFechamentoComissoes(tatuadorId, inicio, fim));
    }

    @Operation(summary = "Deletar transação")
    @ApiResponse(responseCode = "204", description = "Deletada com sucesso")
    @DeleteMapping({"/transacoes/{id}", "/transactions/{id}"})
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        transacaoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
