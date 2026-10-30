package com.tattos.controller;

import com.tattos.dto.request.StudioInfoRequestDTO;
import com.tattos.dto.response.StudioInfoResponseDTO;
import com.tattos.service.StudioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Studio", description = "Vitrine pública do estúdio: informações, tatuadores, trabalhos, ideias e avaliações")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/studio")
@RequiredArgsConstructor
public class StudioController {

    private final StudioService studioService;
    private final com.tattos.service.TatuadorVitrineService tatuadorVitrineService;

    @Operation(summary = "Tatuadores do estúdio para a vitrine",
            description = "Perfil público com média de avaliações (media de (notaTatuagem+notaTatuador)/2, 1 casa decimal; null sem avaliações). Somente tatuadores ativos.")
    @ApiResponse(responseCode = "200", description = "Lista retornada")
    @GetMapping("/tatuadores")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<java.util.List<com.tattos.dto.response.TatuadorVitrineResponseDTO>> tatuadores() {
        return ResponseEntity.ok(tatuadorVitrineService.listar());
    }

    @Operation(summary = "Informações do estúdio")
    @ApiResponse(responseCode = "200", description = "Informações retornadas")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StudioInfoResponseDTO> obter() {
        return ResponseEntity.ok(studioService.obter());
    }

    @Operation(summary = "Atualizar informações do estúdio (somente GESTOR)")
    @ApiResponse(responseCode = "200", description = "Informações atualizadas")
    @PatchMapping
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<StudioInfoResponseDTO> atualizar(@Valid @RequestBody StudioInfoRequestDTO dto) {
        return ResponseEntity.ok(studioService.atualizar(dto));
    }
}
