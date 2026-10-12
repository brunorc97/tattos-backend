package com.tattos.controller;

import com.tattos.dto.request.AgendamentoRequestDTO;
import com.tattos.dto.request.StatusAgendamentoDTO;
import com.tattos.dto.response.AgendamentoResponseDTO;
import com.tattos.model.enums.StatusAgendamento;
import com.tattos.service.AgendamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Agendamentos", description = "Gestão de sessões de tatuagem e agendamentos")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping({"/api/agendamentos", "/api/appointments", "/api/sessions"})
@RequiredArgsConstructor
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    @Operation(summary = "Listar agendamentos", description = "Retorna todos os agendamentos com filtros opcionais.")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR','CLIENTE')")
    public ResponseEntity<List<AgendamentoResponseDTO>> listar(
            @Parameter(description = "Filtrar por ID do tatuador") @RequestParam(required = false) Long tatuadorId,
            @Parameter(description = "Filtrar por ID do cliente")  @RequestParam(required = false) Long clienteId,
            @Parameter(description = "Filtrar por status")         @RequestParam(required = false) StatusAgendamento status,
            @Parameter(description = "Filtrar por data (ISO)")     @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(agendamentoService.listar(tatuadorId, clienteId, status, data));
    }

    @Operation(summary = "Buscar agendamento por ID")
    @ApiResponse(responseCode = "200", description = "Agendamento encontrado")
    @ApiResponse(responseCode = "404", description = "Agendamento não encontrado")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR','CLIENTE')")
    public ResponseEntity<AgendamentoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(agendamentoService.buscarPorId(id));
    }

    @Operation(summary = "Criar agendamento")
    @ApiResponse(responseCode = "201", description = "Agendamento criado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "409", description = "Conflito de horário")
    @PostMapping
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR','CLIENTE')")
    public ResponseEntity<AgendamentoResponseDTO> criar(@Valid @RequestBody AgendamentoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(agendamentoService.criar(dto));
    }

    @Operation(summary = "Atualizar agendamento")
    @ApiResponse(responseCode = "200", description = "Agendamento atualizado")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR')")
    public ResponseEntity<AgendamentoResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody AgendamentoRequestDTO dto) {
        return ResponseEntity.ok(agendamentoService.atualizar(id, dto));
    }

    @Operation(summary = "Atualizar status do agendamento")
    @ApiResponse(responseCode = "200", description = "Status atualizado")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR')")
    public ResponseEntity<AgendamentoResponseDTO> atualizarStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusAgendamentoDTO dto) {
        return ResponseEntity.ok(agendamentoService.atualizarStatus(id, dto.getStatus(), dto.getMotivoCancelamento()));
    }

    @Operation(summary = "Obter código PIX do sinal", description = "Retorna o código PIX Copia e Cola para pagamento do sinal do agendamento")
    @ApiResponse(responseCode = "200", description = "Código PIX retornado")
    @GetMapping("/{id}/pix")
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR','CLIENTE')")
    public ResponseEntity<String> obterCodigoPix(@PathVariable Long id) {
        String pix = agendamentoService.obterCodigoPixSinal(id);
        return ResponseEntity.ok(pix);
    }

    @Operation(summary = "Deletar agendamento")
    @ApiResponse(responseCode = "204", description = "Deletado com sucesso")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        agendamentoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
