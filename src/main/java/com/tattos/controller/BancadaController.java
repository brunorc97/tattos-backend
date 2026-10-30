package com.tattos.controller;

import com.tattos.service.BancadaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

@Tag(name = "Bancadas", description = "Disponibilidade de bancadas por dia e horário")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/bancadas")
@RequiredArgsConstructor
public class BancadaController {

    private final BancadaService bancadaService;
    private final com.tattos.service.BancadaCrudService bancadaCrudService;

    @Operation(summary = "Listar bancadas")
    @ApiResponse(responseCode = "200", description = "Lista de bancadas")
    @GetMapping
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR')")
    public ResponseEntity<java.util.List<com.tattos.dto.response.BancadaResponseDTO>> listar() {
        return ResponseEntity.ok(bancadaCrudService.listar());
    }

    @Operation(summary = "Cadastrar bancada")
    @ApiResponse(responseCode = "201", description = "Bancada criada")
    @PostMapping
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<com.tattos.dto.response.BancadaResponseDTO> criar(
            @jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody com.tattos.dto.request.BancadaRequestDTO dto) {
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(bancadaCrudService.criar(dto));
    }

    @Operation(summary = "Atualizar bancada")
    @ApiResponse(responseCode = "200", description = "Bancada atualizada")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<com.tattos.dto.response.BancadaResponseDTO> atualizar(
            @org.springframework.web.bind.annotation.PathVariable Long id,
            @jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody com.tattos.dto.request.BancadaRequestDTO dto) {
        return ResponseEntity.ok(bancadaCrudService.atualizar(id, dto));
    }

    @Operation(summary = "Remover bancada",
            description = "Recusa a remoção quando há agendamentos futuros na bancada.")
    @ApiResponse(responseCode = "204", description = "Bancada removida")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<Void> deletar(@org.springframework.web.bind.annotation.PathVariable Long id) {
        bancadaCrudService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Disponibilidade da bancada",
            description = "Grade de horários do dia (09h–19h, slots de 60min) marcando livre/ocupado. Preferir bancadaId; cabine (texto) está deprecated.")
    @ApiResponse(responseCode = "200", description = "Grade retornada")
    @GetMapping("/disponibilidade")
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR')")
    public ResponseEntity<Map<String, Object>> disponibilidade(
            @RequestParam(required = false) Long bancadaId,
            @RequestParam(required = false) String cabine,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        if (bancadaId != null) {
            return ResponseEntity.ok(bancadaService.disponibilidade(bancadaId, data));
        }
        return ResponseEntity.ok(bancadaService.disponibilidade(cabine, data));
    }
}
