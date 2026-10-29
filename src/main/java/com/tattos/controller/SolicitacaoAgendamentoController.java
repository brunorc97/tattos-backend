package com.tattos.controller;

import com.tattos.dto.request.SolicitacaoAgendamentoRequestDTO;
import com.tattos.dto.response.SolicitacaoAgendamentoResponseDTO;
import com.tattos.model.Usuario;
import com.tattos.service.SolicitacaoAgendamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Tag(name = "Solicitações de Agendamento", description = "Fluxo de solicitação iniciado pelo cliente, aceito/recusado pelo estúdio")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/solicitacoes-agendamento")
@RequiredArgsConstructor
public class SolicitacaoAgendamentoController {

    private final SolicitacaoAgendamentoService service;

    public record AceiteRequest(
            @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataHora,
            @NotBlank String cabine,
            @NotNull BigDecimal valor,
            Integer duracaoMinutos,
            Long tatuadorId) {}

    public record RecusaRequest(@NotBlank String motivo) {}

    @Operation(summary = "Criar solicitação", description = "Cliente descreve a ideia, anexa referência e indica preferências.")
    @ApiResponse(responseCode = "201", description = "Solicitação criada")
    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<SolicitacaoAgendamentoResponseDTO> criar(
            @Valid @RequestBody SolicitacaoAgendamentoRequestDTO dto,
            @AuthenticationPrincipal Usuario cliente) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(dto, cliente));
    }

    @Operation(summary = "Listar solicitações", description = "Cliente vê as próprias; tatuador vê as suas e as sem tatuador; gestor vê todas.")
    @GetMapping
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR','CLIENTE')")
    public ResponseEntity<Page<SolicitacaoAgendamentoResponseDTO>> listar(
            @AuthenticationPrincipal Usuario usuario,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(service.listar(usuario, pageable));
    }

    @Operation(summary = "Aceitar solicitação", description = "Cria o agendamento vinculado e marca a solicitação como ACEITA.")
    @ApiResponse(responseCode = "201", description = "Solicitação aceita e agendamento criado")
    @ApiResponse(responseCode = "409", description = "Solicitação já decidida")
    @PostMapping("/{id}/aceitar")
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR')")
    public ResponseEntity<SolicitacaoAgendamentoResponseDTO> aceitar(
            @PathVariable Long id,
            @Valid @RequestBody AceiteRequest body,
            @AuthenticationPrincipal Usuario staff) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.aceitar(id, body.dataHora(), body.cabine(), body.valor(), body.duracaoMinutos(), staff, body.tatuadorId()));
    }

    @Operation(summary = "Recusar solicitação", description = "Marca como RECUSADA com motivo para contato posterior.")
    @PostMapping("/{id}/recusar")
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR')")
    public ResponseEntity<SolicitacaoAgendamentoResponseDTO> recusar(
            @PathVariable Long id,
            @Valid @RequestBody RecusaRequest body,
            @AuthenticationPrincipal Usuario staff) {
        return ResponseEntity.ok(service.recusar(id, body.motivo(), staff));
    }
}
