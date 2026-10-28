package com.tattos.controller;

import com.tattos.dto.request.ProjetoRequestDTO;
import com.tattos.dto.response.ProjetoResponseDTO;
import com.tattos.service.ProjetoService;
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

@Tag(name = "Projetos", description = "Gestão de projetos e aprovação")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping({"/api/projetos", "/api/projects"})
@RequiredArgsConstructor
public class ProjetoController {

    private final ProjetoService projetoService;

    @Operation(summary = "Listar projetos")
    @GetMapping
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR','CLIENTE')")
    public ResponseEntity<List<ProjetoResponseDTO>> listar() {
        return ResponseEntity.ok(projetoService.listarProjetos());
    }

    @Operation(summary = "Criar projeto")
    @PostMapping
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<ProjetoResponseDTO> criar(@Valid @RequestBody ProjetoRequestDTO dto) {
        ProjetoResponseDTO created = projetoService.criarProjeto(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Aprovar projeto")
    @PostMapping("/{id}/aprovar")
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<ProjetoResponseDTO> aprovar(@PathVariable Long id) {
        ProjetoResponseDTO approved = projetoService.aprovarProjeto(id);
        return ResponseEntity.ok(approved);
    }
}
