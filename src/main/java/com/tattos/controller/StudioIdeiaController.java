package com.tattos.controller;

import com.tattos.dto.request.IdeiaRequestDTO;
import com.tattos.dto.response.IdeiaResponseDTO;
import com.tattos.model.IdeiaTatuagem;
import com.tattos.repository.IdeiaTatuagemRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Studio — Ideias", description = "Ideias de tatuagem para inspiração dos clientes")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/studio/ideias")
@RequiredArgsConstructor
public class StudioIdeiaController {

    private final IdeiaTatuagemRepository ideiaRepository;

    @Operation(summary = "Listar ideias de tatuagem")
    @ApiResponse(responseCode = "200", description = "Lista retornada")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<IdeiaResponseDTO>> listar() {
        return ResponseEntity.ok(ideiaRepository.findAll().stream()
                .map(IdeiaResponseDTO::fromEntity)
                .toList());
    }

    @Operation(summary = "Publicar ideia (GESTOR/TATUADOR)")
    @ApiResponse(responseCode = "201", description = "Ideia publicada")
    @PostMapping
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR')")
    public ResponseEntity<IdeiaResponseDTO> criar(@Valid @RequestBody IdeiaRequestDTO dto) {
        IdeiaTatuagem ideia = IdeiaTatuagem.builder()
                .titulo(dto.getTitulo().trim())
                .estilo(dto.getEstilo())
                .descricao(dto.getDescricao())
                .imagemUrl(dto.getImagemUrl())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(IdeiaResponseDTO.fromEntity(ideiaRepository.save(ideia)));
    }
}
