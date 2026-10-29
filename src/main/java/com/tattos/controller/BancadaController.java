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
import org.springframework.web.bind.annotation.GetMapping;
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

    @Operation(summary = "Disponibilidade da bancada",
            description = "Grade de horários do dia (09h–19h, slots de 60min) marcando livre/ocupado. Agendamentos cancelados não bloqueiam.")
    @ApiResponse(responseCode = "200", description = "Grade retornada")
    @GetMapping("/disponibilidade")
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR')")
    public ResponseEntity<Map<String, Object>> disponibilidade(
            @RequestParam String cabine,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(bancadaService.disponibilidade(cabine, data));
    }
}
