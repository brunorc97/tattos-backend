package com.tattos.controller;

import com.tattos.dto.request.AjusteEstoqueDTO;
import com.tattos.dto.request.InsumoRequestDTO;
import com.tattos.dto.response.InsumoResponseDTO;
import com.tattos.service.InsumoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Estoque / Insumos", description = "Gestão de insumos e controle de estoque do estúdio")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping({"/api/v1/estoque", "/api/v1/stock", "/api/v1/insumos", "/api/estoque", "/api/stock", "/api/insumos"})
@RequiredArgsConstructor
public class InsumoController {

    private final InsumoService insumoService;

    @Operation(summary = "Listar insumos", description = "Retorna insumos paginados (Page) com filtros opcionais por categoria ou nome. Suporta page, size e sort.")
    @ApiResponse(responseCode = "200", description = "Página retornada com sucesso")
    @GetMapping({"", "/paginado", "/paged"})
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR')")
    public ResponseEntity<Page<InsumoResponseDTO>> listarPaginado(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String query,
            @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(insumoService.listarPaginado(categoria, query, pageable));
    }

    @Operation(summary = "Alertas de estoque crítico", description = "Retorna insumos abaixo do nível crítico definido.")
    @ApiResponse(responseCode = "200", description = "Alertas retornados")
    @GetMapping({"/alertas", "/alerts"})
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR')")
    public ResponseEntity<List<InsumoResponseDTO>> verificarAlertas() {
        return ResponseEntity.ok(insumoService.verificarEstoque());
    }

    @Operation(summary = "Buscar insumo por ID")
    @ApiResponse(responseCode = "200", description = "Insumo encontrado")
    @ApiResponse(responseCode = "404", description = "Insumo não encontrado")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR')")
    public ResponseEntity<InsumoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(insumoService.buscarPorId(id));
    }

    @Operation(summary = "Cadastrar insumo")
    @ApiResponse(responseCode = "201", description = "Insumo criado")
    @PostMapping
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR')")
    public ResponseEntity<InsumoResponseDTO> criar(@Valid @RequestBody InsumoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(insumoService.criar(dto));
    }

    @Operation(summary = "Atualizar insumo")
    @ApiResponse(responseCode = "200", description = "Insumo atualizado")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR')")
    public ResponseEntity<InsumoResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody InsumoRequestDTO dto) {
        return ResponseEntity.ok(insumoService.atualizar(id, dto));
    }

    @Operation(summary = "Registrar entrada de estoque (deprecated)",
            description = "Use POST /{id}/entrada com custo do material. Este endpoint não grava histórico.",
            deprecated = true)
    @ApiResponse(responseCode = "200", description = "Estoque atualizado")
    @Deprecated
    @PatchMapping("/{id}/entrada")
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR')")
    public ResponseEntity<InsumoResponseDTO> registrarEntrada(
            @PathVariable Long id,
            @Valid @RequestBody AjusteEstoqueDTO dto) {
        return ResponseEntity.ok(insumoService.registrarEntrada(id, dto.getQuantidade()));
    }

    @Operation(summary = "Registrar saída de estoque (deprecated)",
            description = "Use POST /{id}/saida informando quem retirou. Este endpoint não grava histórico.",
            deprecated = true)
    @ApiResponse(responseCode = "200", description = "Estoque atualizado")
    @Deprecated
    @PatchMapping("/{id}/saida")
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR')")
    public ResponseEntity<InsumoResponseDTO> registrarSaida(
            @PathVariable Long id,
            @Valid @RequestBody AjusteEstoqueDTO dto) {
        return ResponseEntity.ok(insumoService.registrarSaida(id, dto.getQuantidade()));
    }

    @Operation(summary = "Registrar entrada de estoque com custo",
            description = "Grava movimentação de ENTRADA; quando custoUnitario é informado, recalcula o valor unitário como média simples dos custos de entrada.")
    @ApiResponse(responseCode = "200", description = "Estoque atualizado")
    @PostMapping("/{id}/entrada")
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR')")
    public ResponseEntity<InsumoResponseDTO> entrada(
            @PathVariable Long id,
            @Valid @RequestBody com.tattos.dto.request.EntradaEstoqueDTO dto) {
        return ResponseEntity.ok(insumoService.registrarEntrada(id, dto));
    }

    @Operation(summary = "Registrar saída de estoque com responsável",
            description = "Grava movimentação de SAIDA com o tatuador/gestor que retirou o material.")
    @ApiResponse(responseCode = "200", description = "Estoque atualizado")
    @PostMapping("/{id}/saida")
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR')")
    public ResponseEntity<InsumoResponseDTO> saida(
            @PathVariable Long id,
            @Valid @RequestBody com.tattos.dto.request.SaidaEstoqueDTO dto) {
        return ResponseEntity.ok(insumoService.registrarSaida(id, dto));
    }

    @Operation(summary = "Histórico de movimentações do insumo",
            description = "Lista entradas e saídas, mais recentes primeiro.")
    @ApiResponse(responseCode = "200", description = "Histórico retornado")
    @GetMapping("/{id}/movimentacoes")
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR')")
    public ResponseEntity<java.util.List<com.tattos.dto.response.MovimentacaoEstoqueResponseDTO>> movimentacoes(
            @PathVariable Long id) {
        return ResponseEntity.ok(insumoService.listarMovimentacoes(id));
    }

    @Operation(summary = "Deletar insumo")
    @ApiResponse(responseCode = "204", description = "Deletado com sucesso")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        insumoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
