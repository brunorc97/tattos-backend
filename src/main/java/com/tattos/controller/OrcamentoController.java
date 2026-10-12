package com.tattos.controller;

import com.tattos.dto.request.OrcamentoRequestDTO;
import com.tattos.dto.response.OrcamentoResponseDTO;
import com.tattos.service.OrcamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Orcamentos", description = "Gestão de orçamentos (Budgets)")
@RestController
@RequestMapping({"/api/orcamentos", "/api/budgets"})
@RequiredArgsConstructor
public class OrcamentoController {

    private final OrcamentoService orcamentoService;

    @Operation(summary = "Listar orçamentos")
    @GetMapping
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR','CLIENTE')")
    public ResponseEntity<List<OrcamentoResponseDTO>> listar() {
        return ResponseEntity.ok(orcamentoService.listarOrcamentos());
    }

    @Operation(summary = "Criar um orçamento")
    @PostMapping
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR')")
    public ResponseEntity<OrcamentoResponseDTO> criar(@RequestBody OrcamentoRequestDTO dto) {
        OrcamentoResponseDTO created = orcamentoService.criarOrcamento(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
