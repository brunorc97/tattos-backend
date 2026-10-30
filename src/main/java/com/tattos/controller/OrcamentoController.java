package com.tattos.controller;

import com.tattos.dto.request.OrcamentoRequestDTO;
import com.tattos.dto.response.OrcamentoResponseDTO;
import com.tattos.service.OrcamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Orcamentos", description = "Gestão de orçamentos (Budgets)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping({"/api/v1/orcamentos", "/api/v1/budgets", "/api/orcamentos", "/api/budgets"})
@RequiredArgsConstructor
public class OrcamentoController {

    private final OrcamentoService orcamentoService;

    @Operation(summary = "Listar orçamentos")
    @GetMapping
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR')")
    public ResponseEntity<List<OrcamentoResponseDTO>> listar() {
        return ResponseEntity.ok(orcamentoService.listarOrcamentos());
    }

    @Operation(summary = "Criar um orçamento")
    @PostMapping
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR')")
    public ResponseEntity<OrcamentoResponseDTO> criar(@Valid @RequestBody OrcamentoRequestDTO dto) {
        OrcamentoResponseDTO created = orcamentoService.criarOrcamento(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    public record StatusRequest(@jakarta.validation.constraints.NotNull com.tattos.model.enums.StatusOrcamento status) {}

    @Operation(summary = "Alterar status do orçamento",
            description = "RASCUNHO→ENVIADO (gestor/tatuador do projeto); ENVIADO→ACEITO/RECUSADO (cliente do projeto/gestor). ACEITO aprova o projeto.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Status atualizado")
    @org.springframework.web.bind.annotation.PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR','CLIENTE')")
    public ResponseEntity<OrcamentoResponseDTO> alterarStatus(
            @org.springframework.web.bind.annotation.PathVariable Long id,
            @Valid @RequestBody StatusRequest body,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.tattos.model.Usuario logado) {
        return ResponseEntity.ok(orcamentoService.alterarStatus(id, body.status(), logado));
    }
}
