package com.tattos.controller;

import com.tattos.dto.request.FichaAnamneseRequestDTO;
import com.tattos.dto.response.FichaAnamneseResponseDTO;
import com.tattos.dto.request.AssinaturaRequestDTO;
import com.tattos.model.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.tattos.service.FichaAnamneseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Tag(name = "Fichas de Anamnese", description = "Biossegurança: registro e consulta de fichas clínicas de clientes")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping({"/api/v1/fichas-anamnese", "/api/v1/anamnese", "/api/v1/anamneses", "/api/fichas-anamnese", "/api/anamnese", "/api/anamneses"})
@RequiredArgsConstructor
public class FichaAnamneseController {

    private final FichaAnamneseService fichaService;

    @Operation(summary = "Criar ficha de anamnese", description = "Gestor/tatuador criam para qualquer cliente; cliente cria apenas para si (clienteId do token).")
    @ApiResponse(responseCode = "201", description = "Ficha criada")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    @PostMapping
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR','CLIENTE')")
    public ResponseEntity<FichaAnamneseResponseDTO> criar(
            @Valid @RequestBody FichaAnamneseRequestDTO dto,
            @AuthenticationPrincipal Usuario usuario) {
        if (usuario != null && usuario.isCliente()) {
            dto.setClienteId(usuario.getId());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(fichaService.criar(dto));
    }

    @Operation(summary = "Listar fichas", description = "Gestor filtra por cliente/tatuador/mês/ano; tatuador vê apenas fichas dos seus clientes.")
    @ApiResponse(responseCode = "200", description = "Fichas retornadas")
    @GetMapping
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR')")
    public ResponseEntity<List<FichaAnamneseResponseDTO>> listarTodas(
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) Long tatuadorId,
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer ano,
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(fichaService.listarComFiltros(clienteId, tatuadorId, mes, ano, usuario));
    }

    @Operation(summary = "Alertas clínicos", description = "Retorna fichas com restrições ou condições clínicas impeditivas.")
    @ApiResponse(responseCode = "200", description = "Alertas retornados")
    @GetMapping("/alertas")
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR')")
    public ResponseEntity<List<FichaAnamneseResponseDTO>> listarAlertas() {
        return ResponseEntity.ok(fichaService.listarAlertasClinicos());
    }

    @Operation(summary = "Buscar ficha por ID")
    @ApiResponse(responseCode = "200", description = "Ficha encontrada")
    @ApiResponse(responseCode = "404", description = "Ficha não encontrada")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR','CLIENTE')")
    public ResponseEntity<FichaAnamneseResponseDTO> buscarPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {
        FichaAnamneseResponseDTO ficha = fichaService.buscarPorId(id);
        if (isOtherClientRecord(usuario, ficha.getClienteId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(ficha);
    }

    @Operation(summary = "Buscar fichas por cliente")
    @ApiResponse(responseCode = "200", description = "Fichas do cliente retornadas")
    @GetMapping("/cliente/{clienteId}")
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR','CLIENTE')")
    public ResponseEntity<List<FichaAnamneseResponseDTO>> buscarPorCliente(
            @PathVariable Long clienteId,
            @AuthenticationPrincipal Usuario usuario) {
        if (isOtherClientRecord(usuario, clienteId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(fichaService.buscarPorCliente(clienteId));
    }

    @Operation(summary = "Buscar ficha por agendamento")
    @ApiResponse(responseCode = "200", description = "Ficha do agendamento retornada")
    @GetMapping("/agendamento/{agendamentoId}")
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR')")
    public ResponseEntity<FichaAnamneseResponseDTO> buscarPorAgendamento(@PathVariable Long agendamentoId) {
        return ResponseEntity.ok(fichaService.buscarPorAgendamento(agendamentoId));
    }

    @Operation(
        summary = "Verificar aptidão do cliente",
        description = "Indica se o cliente está apto para realizar procedimento com base em suas fichas clínicas."
    )
    @ApiResponse(responseCode = "200", description = "Aptidão verificada")
    @GetMapping("/cliente/{clienteId}/aptidao")
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR')")
    public ResponseEntity<Map<String, Object>> verificarAptidao(@PathVariable Long clienteId) {
        boolean apto = fichaService.validarAptidaoCliente(clienteId);
        return ResponseEntity.ok(Map.of(
                "clienteId", clienteId,
                "aptoParaProcedimento", apto,
                "mensagem", apto
                        ? "Cliente sem restrições clínicas impeditivas"
                        : "Cliente possui alertas clínicos ou não possui ficha preenchida"
        ));
    }

    @Operation(summary = "Assinar termo de consentimento", description = "O cliente confirma a assinatura digital (flag) e registra o e‑mail para auditoria.")
    @ApiResponse(responseCode = "204", description = "Assinatura registrada com sucesso")
    @PostMapping("/{id}/assinatura")
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR','CLIENTE')")
    public ResponseEntity<Void> assinarTermo(
            @PathVariable Long id,
            @Valid @RequestBody AssinaturaRequestDTO dto,
            @AuthenticationPrincipal Usuario usuario) {
        if (usuario != null && usuario.isCliente()) {
            FichaAnamneseResponseDTO ficha = fichaService.buscarPorId(id);
            if (isOtherClientRecord(usuario, ficha.getClienteId())
                    || !usuario.getEmail().equalsIgnoreCase(dto.getEmail())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            dto.setUsuarioId(usuario.getId());
        }
        fichaService.assinarTermo(id, dto);
        return ResponseEntity.noContent().build();
    }

    private boolean isOtherClientRecord(Usuario usuario, Long clienteId) {
        return usuario != null
                && usuario.isCliente()
                && !Objects.equals(usuario.getId(), clienteId);
    }
}
