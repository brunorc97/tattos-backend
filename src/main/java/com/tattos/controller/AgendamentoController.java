package com.tattos.controller;

import com.tattos.dto.request.AgendamentoRequestDTO;
import com.tattos.dto.request.StatusAgendamentoDTO;
import com.tattos.dto.response.AgendamentoResponseDTO;
import com.tattos.dto.request.PixSinalRequestDTO;
import com.tattos.dto.response.PixSinalResponseDTO;
import com.tattos.dto.request.FinalPaymentRequestDTO;
import com.tattos.dto.response.AgendamentoResponseDTO;
import com.tattos.model.Usuario;
import com.tattos.model.enums.StatusAgendamento;
import com.tattos.service.AgendamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Tag(name = "Agendamentos", description = "Gestão de sessões de tatuagem e agendamentos")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping({"/api/v1/agendamentos", "/api/v1/appointments", "/api/v1/sessions", "/api/agendamentos", "/api/appointments", "/api/sessions"})
@RequiredArgsConstructor
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    @Operation(summary = "Listar agendamentos", description = "Retorna agendamentos paginados (Page) com filtros opcionais. Suporta page, size e sort.")
    @ApiResponse(responseCode = "200", description = "Página retornada com sucesso")
    @GetMapping({"", "/paginado", "/paged"})
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR','CLIENTE')")
    public ResponseEntity<Page<AgendamentoResponseDTO>> listarPaginado(
            @Parameter(description = "Filtrar por ID do tatuador") @RequestParam(required = false) Long tatuadorId,
            @Parameter(description = "Filtrar por ID do cliente")  @RequestParam(required = false) Long clienteId,
            @Parameter(description = "Filtrar por status")         @RequestParam(required = false) StatusAgendamento status,
            @Parameter(description = "Filtrar por data (ISO)")     @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @Parameter(description = "Filtrar por cabine/bancada") @RequestParam(required = false) String cabine,
            @Parameter(description = "Somente agendamentos futuros") @RequestParam(required = false, defaultValue = "false") boolean futuras,
            @PageableDefault(size = 20, sort = "dataHora") Pageable pageable,
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(agendamentoService.listarComEscopo(tatuadorId, clienteId, status, data, cabine, futuras, usuario, pageable));
    }

    @Operation(summary = "Agenda consolidada por período",
            description = "Lista agendamentos entre inicio e fim (obrigatórios), ordenados por dataHora. Tatuador vê a própria agenda; cliente, as próprias sessões.")
    @ApiResponse(responseCode = "200", description = "Agenda do período")
    @GetMapping("/agenda")
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR','CLIENTE')")
    public ResponseEntity<java.util.List<AgendamentoResponseDTO>> agenda(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(agendamentoService.agendaPorPeriodo(inicio, fim, usuario));
    }

    @Operation(summary = "Buscar agendamento por ID")
    @ApiResponse(responseCode = "200", description = "Agendamento encontrado")
    @ApiResponse(responseCode = "404", description = "Agendamento não encontrado")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR','CLIENTE')")
    public ResponseEntity<AgendamentoResponseDTO> buscarPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {
        AgendamentoResponseDTO agendamento = agendamentoService.buscarPorId(id);
        if (isOtherClientAppointment(usuario, agendamento.getClienteId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(agendamento);
    }

    @Operation(summary = "Criar agendamento")
    @ApiResponse(responseCode = "201", description = "Agendamento criado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "409", description = "Conflito de horário")
    @PostMapping
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR','CLIENTE')")
    public ResponseEntity<AgendamentoResponseDTO> criar(
            @Valid @RequestBody AgendamentoRequestDTO dto,
            @AuthenticationPrincipal Usuario usuario) {
        if (usuario != null && usuario.isCliente()) {
            dto.setClienteId(usuario.getId());
        }
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

    @Operation(summary = "Gerar PIX do sinal com percentual")
    @ApiResponse(responseCode = "200", description = "PIX gerado com sucesso")
    @PostMapping("/{id}/pix")
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR','CLIENTE')")
    public ResponseEntity<PixSinalResponseDTO> gerarPix(
            @PathVariable Long id,
            @Valid @RequestBody PixSinalRequestDTO request,
            @AuthenticationPrincipal Usuario usuario) {
        if (usuario != null && usuario.isCliente()) {
            AgendamentoResponseDTO agendamento = agendamentoService.buscarPorId(id);
            if (isOtherClientAppointment(usuario, agendamento.getClienteId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        PixSinalResponseDTO response = agendamentoService.gerarPixSinal(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Marcar sinal como pago")
    @ApiResponse(responseCode = "200", description = "Sinal marcado como pago")
    @PatchMapping("/{id}/sinal-pago")
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR')")
    public ResponseEntity<Void> marcarSinalPago(@PathVariable Long id) {
        agendamentoService.marcarSinalPago(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Finalizar pagamento com cobrança adicional")
    @ApiResponse(responseCode = "200", description = "Pagamento finalizado")
    @PatchMapping("/{id}/finalizar-pagamento")
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR')")
    public ResponseEntity<Void> finalizarPagamento(@PathVariable Long id, @Valid @RequestBody FinalPaymentRequestDTO request) {
        agendamentoService.finalizarPagamento(id, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Deletar agendamento")
    @ApiResponse(responseCode = "204", description = "Deletado com sucesso")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        agendamentoService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    private boolean isOtherClientAppointment(Usuario usuario, Long clienteId) {
        return usuario != null
                && usuario.isCliente()
                && !Objects.equals(usuario.getId(), clienteId);
    }
}
