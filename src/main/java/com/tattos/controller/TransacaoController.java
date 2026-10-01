package com.tattos.controller;

import com.tattos.dto.request.TransacaoRequestDTO;
import com.tattos.dto.response.BalancoFinanceiroDTO;
import com.tattos.dto.response.TransacaoResponseDTO;
import com.tattos.model.enums.StatusTransacao;
import com.tattos.model.enums.TipoTransacao;
import com.tattos.service.TransacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/financeiro", "/api/financial"})
@RequiredArgsConstructor
public class TransacaoController {

    private final TransacaoService transacaoService;

    @GetMapping({"/transacoes", "/transactions"})
    public ResponseEntity<List<TransacaoResponseDTO>> listar(
            @RequestParam(required = false) TipoTransacao tipo,
            @RequestParam(required = false) Long tatuadorId,
            @RequestParam(required = false) StatusTransacao status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(transacaoService.listar(tipo, tatuadorId, status, inicio, fim));
    }

    @GetMapping({"/transacoes/{id}", "/transactions/{id}"})
    public ResponseEntity<TransacaoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(transacaoService.buscarPorId(id));
    }

    @PostMapping({"/transacoes", "/transactions"})
    public ResponseEntity<TransacaoResponseDTO> criar(@Valid @RequestBody TransacaoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transacaoService.criar(dto));
    }

    /**
     * Dashboard Gerencial "Smile Now, Cry Later"
     * Apresenta o balanço mensal confrontando faturamento bruto com custos e repasses.
     */
    @GetMapping({"/balanco", "/summary"})
    public ResponseEntity<BalancoFinanceiroDTO> obterBalancoMensal(
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer ano) {
        int mesResolvido = (mes != null) ? mes : LocalDate.now().getMonthValue();
        int anoResolvido = (ano != null) ? ano : LocalDate.now().getYear();

        return ResponseEntity.ok(transacaoService.obterBalancoMensal(mesResolvido, anoResolvido));
    }

    /**
     * Total de repasses de comissão devidos a um determinado tatuador no período.
     */
    @GetMapping({"/repasses/{tatuadorId}", "/splits/{tatuadorId}"})
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

    @DeleteMapping({"/transacoes/{id}", "/transactions/{id}"})
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        transacaoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
