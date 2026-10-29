package com.tattos.controller;

import com.tattos.dto.response.AftercareResponseDTO;
import com.tattos.service.AftercareService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('GESTOR','TATUADOR')")
@RequestMapping({"/api/v1/aftercare", "/api/aftercare"})
@RequiredArgsConstructor
public class AftercareController {

    private final AftercareService aftercareService;

    @PostMapping
    public ResponseEntity<AftercareResponseDTO> criar(@RequestParam Long clienteId,
                                                     @RequestParam Long agendamentoId,
                                                     @RequestParam(required = false) String notes) {
        AftercareResponseDTO dto = aftercareService.criar(clienteId, agendamentoId, notes);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AftercareResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(aftercareService.buscarPorId(id));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<AftercareResponseDTO>> listarPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(aftercareService.listarPorCliente(clienteId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AftercareResponseDTO> atualizar(@PathVariable Long id,
                                                          @RequestParam(required = false) String notes) {
        return ResponseEntity.ok(aftercareService.atualizar(id, notes));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        aftercareService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
