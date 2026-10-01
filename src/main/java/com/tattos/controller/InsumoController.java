package com.tattos.controller;

import com.tattos.dto.request.AjusteEstoqueDTO;
import com.tattos.dto.request.InsumoRequestDTO;
import com.tattos.dto.response.InsumoResponseDTO;
import com.tattos.service.InsumoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/estoque", "/api/stock", "/api/insumos"})
@RequiredArgsConstructor
public class InsumoController {

    private final InsumoService insumoService;

    @GetMapping
    public ResponseEntity<List<InsumoResponseDTO>> listar(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String query) {
        return ResponseEntity.ok(insumoService.listar(categoria, query));
    }

    /**
     * Endpoint crítico para monitoramento em tempo real de suprimentos abaixo do nível crítico.
     */
    @GetMapping({"/alertas", "/alerts"})
    public ResponseEntity<List<InsumoResponseDTO>> verificarAlertas() {
        return ResponseEntity.ok(insumoService.verificarEstoque());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InsumoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(insumoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<InsumoResponseDTO> criar(@Valid @RequestBody InsumoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(insumoService.criar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InsumoResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody InsumoRequestDTO dto) {
        return ResponseEntity.ok(insumoService.atualizar(id, dto));
    }

    @PatchMapping("/{id}/entrada")
    public ResponseEntity<InsumoResponseDTO> registrarEntrada(
            @PathVariable Long id,
            @Valid @RequestBody AjusteEstoqueDTO dto) {
        return ResponseEntity.ok(insumoService.registrarEntrada(id, dto.getQuantidade()));
    }

    @PatchMapping("/{id}/saida")
    public ResponseEntity<InsumoResponseDTO> registrarSaida(
            @PathVariable Long id,
            @Valid @RequestBody AjusteEstoqueDTO dto) {
        return ResponseEntity.ok(insumoService.registrarSaida(id, dto.getQuantidade()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        insumoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
