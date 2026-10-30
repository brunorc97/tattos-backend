package com.tattos.controller;

import com.tattos.service.ComissaoExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * Controller responsável por exportar o relatório de comissões de um tatuador.
 * O formato atualmente suportado é CSV (texto delimitado por ponto‑e‑vírgula).
 */
@Tag(name = "Comissões", description = "Exportação de relatórios de comissão para tatuadores")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping({"/api/v1/comissoes", "/api/v1/commissions", "/api/comissoes", "/api/commissions"})
@RequiredArgsConstructor
public class ComissaoController {

    private final ComissaoExportService comissaoExportService;
    private final com.tattos.service.ComissaoService comissaoService;

    @Operation(summary = "Fechamento de comissões em JSON",
            description = "Retorna o fechamento do tatuador no período com o status de repasse por item (PENDENTE/AGENDADO/PAGO). Tatuador só acessa o próprio fechamento.")
    @ApiResponse(responseCode = "200", description = "Fechamento com itens e totais")
    @GetMapping("/tatuador/{tatuadorId}/fechamento")
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR')")
    public ResponseEntity<com.tattos.dto.response.FechamentoComissaoDTO> fechamento(
            @PathVariable @NotNull Long tatuadorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.tattos.model.Usuario logado) {
        return ResponseEntity.ok(comissaoService.fechamento(tatuadorId, inicio, fim, logado));
    }

    @Operation(summary = "Exportar comissões em CSV",
            description = "Retorna um arquivo CSV contendo todas as transações financeiras de um tatuador entre as datas informadas.")
    @ApiResponse(responseCode = "200", description = "CSV com as comissões")
    @GetMapping("/tatuador/{tatuadorId}/fechamento/export")
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR')")
    public ResponseEntity<String> exportarComissoesCsv(
            @PathVariable @NotNull Long tatuadorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        String csv = comissaoExportService.exportarCsv(tatuadorId, inicio, fim);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=commissions.csv")
                .contentType(MediaType.TEXT_PLAIN)
                .body(csv);
    }
}
