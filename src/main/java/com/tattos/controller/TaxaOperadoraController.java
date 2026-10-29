package com.tattos.controller;

import com.tattos.model.TaxaOperadora;
import com.tattos.model.enums.ModalidadePagamento;
import com.tattos.repository.TaxaOperadoraRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "Taxas de Operadoras", description = "Taxas das maquininhas de cartão por modalidade (débito, crédito à vista, parcelado)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/financeiro/taxas-operadoras")
@RequiredArgsConstructor
public class TaxaOperadoraController {

    private final TaxaOperadoraRepository taxaRepository;

    public record TaxaRequest(
            @NotBlank @jakarta.validation.constraints.Size(max = 60) String operadora,
            @NotNull ModalidadePagamento modalidade,
            @NotNull @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal taxaPercentual,
            Boolean ativo) {}

    @Operation(summary = "Cadastrar taxa", description = "Única por operadora+modalidade. Edição não recalcula transações passadas.")
    @PostMapping
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<TaxaOperadora> criar(@Valid @RequestBody TaxaRequest body) {
        taxaRepository.findByOperadoraAndModalidade(body.operadora().trim(), body.modalidade())
                .ifPresent(t -> { throw new com.tattos.exception.ConflictException(
                        "Já existe taxa cadastrada para " + body.operadora().trim() + " / " + body.modalidade()); });
        TaxaOperadora taxa = taxaRepository.save(TaxaOperadora.builder()
                .operadora(body.operadora().trim())
                .modalidade(body.modalidade())
                .taxaPercentual(body.taxaPercentual())
                .ativo(body.ativo() == null || body.ativo())
                .build());
        return ResponseEntity.status(HttpStatus.CREATED).body(taxa);
    }

    @Operation(summary = "Listar taxas ativas")
    @GetMapping
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<List<TaxaOperadora>> listar() {
        return ResponseEntity.ok(taxaRepository.findByAtivoTrue());
    }

    @Operation(summary = "Atualizar taxa", description = "Afeta apenas transações futuras (valores de transações passadas são persistidos).")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<TaxaOperadora> atualizar(@PathVariable Long id, @Valid @RequestBody TaxaRequest body) {
        TaxaOperadora taxa = taxaRepository.findById(id)
                .orElseThrow(() -> new com.tattos.exception.ResourceNotFoundException("Taxa não encontrada com ID: " + id));
        taxa.setTaxaPercentual(body.taxaPercentual());
        if (body.ativo() != null) {
            taxa.setAtivo(body.ativo());
        }
        return ResponseEntity.ok(taxaRepository.save(taxa));
    }
}
