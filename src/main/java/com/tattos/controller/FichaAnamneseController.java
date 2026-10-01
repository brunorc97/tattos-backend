package com.tattos.controller;

import com.tattos.dto.request.FichaAnamneseRequestDTO;
import com.tattos.dto.response.FichaAnamneseResponseDTO;
import com.tattos.service.FichaAnamneseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/fichas-anamnese", "/api/anamnese", "/api/anamneses"})
@RequiredArgsConstructor
public class FichaAnamneseController {

    private final FichaAnamneseService fichaService;

    @GetMapping
    public ResponseEntity<List<FichaAnamneseResponseDTO>> listarTodas() {
        return ResponseEntity.ok(fichaService.listarTodas());
    }

    @GetMapping("/alertas")
    public ResponseEntity<List<FichaAnamneseResponseDTO>> listarAlertas() {
        return ResponseEntity.ok(fichaService.listarAlertasClinicos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FichaAnamneseResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(fichaService.buscarPorId(id));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<FichaAnamneseResponseDTO>> buscarPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(fichaService.buscarPorCliente(clienteId));
    }

    @GetMapping("/agendamento/{agendamentoId}")
    public ResponseEntity<FichaAnamneseResponseDTO> buscarPorAgendamento(@PathVariable Long agendamentoId) {
        return ResponseEntity.ok(fichaService.buscarPorAgendamento(agendamentoId));
    }

    @GetMapping("/cliente/{clienteId}/aptidao")
    public ResponseEntity<Map<String, Object>> verificarAptidao(@PathVariable Long clienteId) {
        boolean apto = fichaService.validarAptidaoCliente(clienteId);
        return ResponseEntity.ok(Map.of(
                "clienteId", clienteId,
                "aptoParaProcedimento", apto,
                "mensagem", apto ? "Cliente sem restrições clínicas impeditivas" : "Cliente possui alertas clínicos ou não possui ficha preenchida"
        ));
    }

    @PostMapping
    public ResponseEntity<FichaAnamneseResponseDTO> registrar(@Valid @RequestBody FichaAnamneseRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fichaService.registrarFicha(dto));
    }
}
