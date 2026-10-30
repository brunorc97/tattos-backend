package com.tattos.controller;

import com.tattos.dto.request.TrabalhoRequestDTO;
import com.tattos.dto.response.TrabalhoResponseDTO;
import com.tattos.model.Usuario;
import com.tattos.service.TrabalhoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Studio — Trabalhos", description = "Portfólio de trabalhos da vitrine do estúdio")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/studio/trabalhos")
@RequiredArgsConstructor
public class StudioTrabalhoController {

    private final TrabalhoService trabalhoService;

    @Operation(summary = "Listar trabalhos do portfólio (mais recentes primeiro)")
    @ApiResponse(responseCode = "200", description = "Lista retornada")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TrabalhoResponseDTO>> listar() {
        return ResponseEntity.ok(trabalhoService.listar());
    }

    @Operation(summary = "Publicar trabalho",
            description = "GESTOR publica para qualquer tatuador; TATUADOR apenas para si mesmo.")
    @ApiResponse(responseCode = "201", description = "Trabalho publicado")
    @PostMapping
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR')")
    public ResponseEntity<TrabalhoResponseDTO> criar(@Valid @RequestBody TrabalhoRequestDTO dto,
                                                     @AuthenticationPrincipal Usuario logado) {
        return ResponseEntity.status(HttpStatus.CREATED).body(trabalhoService.criar(dto, logado));
    }
}
