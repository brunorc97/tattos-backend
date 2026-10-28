package com.tattos.controller;

import com.tattos.dto.request.KanbanCardRequestDTO;
import com.tattos.dto.response.KanbanCardResponseDTO;
import com.tattos.service.KanbanCardService;
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

@Tag(name = "Kanban", description = "Kanban board for budgets and projects")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping({"/api/v1/kanban", "/api/v1/kanban-cards", "/api/kanban", "/api/kanban-cards"})
@RequiredArgsConstructor
public class KanbanCardController {

    private final KanbanCardService kanbanCardService;

    @Operation(summary = "Listar cartões Kanban")
    @GetMapping
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR','CLIENTE')")
    public ResponseEntity<List<KanbanCardResponseDTO>> listar() {
        return ResponseEntity.ok(kanbanCardService.listarKanbanCards());
    }

    @Operation(summary = "Criar cartão Kanban")
    @PostMapping
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR')")
    public ResponseEntity<KanbanCardResponseDTO> criar(@Valid @RequestBody KanbanCardRequestDTO dto) {
        KanbanCardResponseDTO created = kanbanCardService.criarKanbanCard(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
