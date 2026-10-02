package com.tattos.controller;

import com.tattos.dto.request.FichaAnamneseRequestDTO;
import com.tattos.dto.response.FichaAnamneseResponseDTO;
import com.tattos.service.FichaAnamneseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Fichas de Anamnese", description = "Biossegurança: registro e consulta de fichas clínicas de clientes")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping({"/api/fichas-anamnese", "/api/anamnese", "/api/anamneses"})
@RequiredArgsConstructor
public class FichaAnamneseController {

    private final FichaAnamneseService fichaService;

    @Operation(summary = "Listar todas as fichas")
    @ApiResponse(responseCode = "200", description = "Fichas retornadas")
    @GetMapping
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR')")
    public ResponseEntity<List<FichaAnamneseResponseDTO>> listarTodas() {
        return ResponseEntity.ok(fichaService.listarTodas());
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
    public ResponseEntity<FichaAnamneseResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(fichaService.buscarPorId(id));
    }

    @Operation(summary = "Buscar fichas por cliente")
    @ApiResponse(responseCode = "200", description = "Fichas do cliente retornadas")
    @GetMapping("/cliente/{clienteId}")
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR','CLIENTE')")
    public ResponseEntity<List<FichaAnamneseResponseDTO>> buscarPorCliente(@PathVariable Long clienteId) {
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

    @Operation(summary = "Registrar ficha de anamnese")
    @ApiResponse(responseCode = "201", description = "Ficha registrada com sucesso")
    @PostMapping
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR','CLIENTE')")
    public ResponseEntity<FichaAnamneseResponseDTO> registrar(@Valid @RequestBody FichaAnamneseRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fichaService.registrarFicha(dto));
    }
}
