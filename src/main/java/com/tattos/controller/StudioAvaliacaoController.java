package com.tattos.controller;

import com.tattos.dto.request.AvaliacaoRequestDTO;
import com.tattos.dto.response.AvaliacaoResponseDTO;
import com.tattos.dto.response.TrabalhoPendenteResponseDTO;
import com.tattos.model.Usuario;
import com.tattos.service.AvaliacaoService;
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

@Tag(name = "Studio — Avaliações", description = "Avaliações de trabalhos pelos clientes")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/studio/avaliacoes")
@RequiredArgsConstructor
public class StudioAvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    @Operation(summary = "Listar avaliações (mais recentes primeiro)")
    @ApiResponse(responseCode = "200", description = "Lista retornada")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AvaliacaoResponseDTO>> listar() {
        return ResponseEntity.ok(avaliacaoService.listar());
    }

    @Operation(summary = "Trabalhos concluídos do cliente ainda não avaliados")
    @ApiResponse(responseCode = "200", description = "Lista retornada")
    @GetMapping("/pendentes")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<List<TrabalhoPendenteResponseDTO>> pendentes(
            @AuthenticationPrincipal Usuario logado) {
        return ResponseEntity.ok(avaliacaoService.pendentes(logado));
    }

    @Operation(summary = "Avaliações enviadas pelo cliente logado")
    @ApiResponse(responseCode = "200", description = "Lista retornada")
    @GetMapping("/minhas")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<List<AvaliacaoResponseDTO>> minhas(
            @AuthenticationPrincipal Usuario logado) {
        return ResponseEntity.ok(avaliacaoService.minhas(logado));
    }

    @Operation(summary = "Avaliar trabalho",
            description = "Somente o cliente dono do trabalho; uma avaliação por trabalho. Notas inteiras de 1 a 5.")
    @ApiResponse(responseCode = "201", description = "Avaliação criada")
    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<AvaliacaoResponseDTO> avaliar(@Valid @RequestBody AvaliacaoRequestDTO dto,
                                                        @AuthenticationPrincipal Usuario logado) {
        return ResponseEntity.status(HttpStatus.CREATED).body(avaliacaoService.avaliar(dto, logado));
    }
}
