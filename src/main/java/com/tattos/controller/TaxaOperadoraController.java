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

    public record TaxaOperadoraDTO(Long id, String operadora, String modalidade,
                                   BigDecimal taxaPercentual, boolean ativo,
                                   java.util.List<ParcelaTaxaDTO> parcelasTaxas) {
        static TaxaOperadoraDTO from(TaxaOperadora t) {
            return new TaxaOperadoraDTO(t.getId(), t.getOperadora(), t.getModalidade().name(),
                    t.getTaxaPercentual(), t.isAtivo(),
                    t.getParcelasTaxas() == null ? java.util.List.of() : t.getParcelasTaxas().stream()
                            .map(p -> new ParcelaTaxaDTO(p.getParcelas(), p.getTaxaPercentual()))
                            .toList());
        }
    }

    public record ParcelaTaxaDTO(Integer parcelas, BigDecimal taxaPercentual) {}

    public record ParcelaTaxaRequest(
            @NotNull @jakarta.validation.constraints.Min(1) @jakarta.validation.constraints.Max(10) Integer parcelas,
            @NotNull @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal taxaPercentual) {}

    public record TaxaRequest(
            @NotBlank @jakarta.validation.constraints.Size(max = 60) String operadora,
            @NotNull ModalidadePagamento modalidade,
            @NotNull @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal taxaPercentual,
            Boolean ativo,
            @Valid java.util.List<ParcelaTaxaRequest> parcelas) {}

    @Operation(summary = "Cadastrar taxa",
            description = "Única por operadora+modalidade. CREDITO_PARCELADO exige a grade completa 1x–10x. Edição não recalcula transações passadas.")
    @PostMapping
    @PreAuthorize("hasRole('GESTOR')")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<TaxaOperadoraDTO> criar(@Valid @RequestBody TaxaRequest body) {
        String operadora = body.operadora().trim();
        taxaRepository.findByOperadoraAndModalidade(operadora, body.modalidade())
                .ifPresent(t -> { throw new com.tattos.exception.ConflictException(
                        "Já existe taxa cadastrada para " + operadora + " / " + body.modalidade()); });

        TaxaOperadora taxa = TaxaOperadora.builder()
                .operadora(operadora)
                .modalidade(body.modalidade())
                .taxaPercentual(body.taxaPercentual())
                .ativo(body.ativo() == null || body.ativo())
                .build();

        if (body.modalidade() == ModalidadePagamento.CREDITO_PARCELADO) {
            validarGradeCompleta(body.parcelas());
            for (ParcelaTaxaRequest p : body.parcelas()) {
                taxa.getParcelasTaxas().add(com.tattos.model.TaxaOperadoraParcela.builder()
                        .taxaOperadora(taxa)
                        .parcelas(p.parcelas())
                        .taxaPercentual(p.taxaPercentual())
                        .build());
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(TaxaOperadoraDTO.from(taxaRepository.save(taxa)));
    }

    private void validarGradeCompleta(java.util.List<ParcelaTaxaRequest> parcelas) {
        if (parcelas == null || parcelas.size() != 10) {
            throw new com.tattos.exception.BusinessException(
                    "Crédito parcelado exige a grade completa de taxas (1x a 10x).");
        }
        java.util.Set<Integer> numeros = new java.util.HashSet<>();
        for (ParcelaTaxaRequest p : parcelas) {
            numeros.add(p.parcelas());
        }
        if (numeros.size() != 10 || !numeros.containsAll(java.util.Set.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))) {
            throw new com.tattos.exception.BusinessException(
                    "A grade deve conter exatamente as parcelas de 1x a 10x, sem repetição.");
        }
    }

    @Operation(summary = "Listar taxas ativas")
    @GetMapping
    @PreAuthorize("hasRole('GESTOR')")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<List<TaxaOperadoraDTO>> listar() {
        return ResponseEntity.ok(taxaRepository.findByAtivoTrue().stream()
                .map(TaxaOperadoraDTO::from).toList());
    }

    @Operation(summary = "Atualizar taxa", description = "Afeta apenas transações futuras (valores de transações passadas são persistidos).")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('GESTOR')")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<TaxaOperadoraDTO> atualizar(@PathVariable Long id, @Valid @RequestBody TaxaRequest body) {
        TaxaOperadora taxa = taxaRepository.findById(id)
                .orElseThrow(() -> new com.tattos.exception.ResourceNotFoundException("Taxa não encontrada com ID: " + id));
        taxa.setTaxaPercentual(body.taxaPercentual());
        if (body.ativo() != null) {
            taxa.setAtivo(body.ativo());
        }
        return ResponseEntity.ok(TaxaOperadoraDTO.from(taxaRepository.save(taxa)));
    }
}
