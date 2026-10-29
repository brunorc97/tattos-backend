package com.tattos.controller;

import com.tattos.model.Cupom;
import com.tattos.model.Usuario;
import com.tattos.service.CupomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Cupons", description = "Promoções e cupons de desconto (studio-wide ou por tatuador)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/cupons")
@RequiredArgsConstructor
public class CupomController {

    private final CupomService cupomService;

    public record CupomRequest(
            @NotBlank @Size(max = 40) String codigo,
            @NotNull Cupom.Tipo tipo,
            @NotNull @DecimalMin("0.01") BigDecimal valor,
            Long tatuadorId,
            LocalDate validade,
            Integer limiteUsos) {}

    @Operation(summary = "Criar cupom", description = "Gestor cria para o studio ou um tatuador; tatuador só para si.")
    @PostMapping
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR')")
    public ResponseEntity<Map<String, Object>> criar(
            @Valid @RequestBody CupomRequest body,
            @AuthenticationPrincipal Usuario autor) {
        Cupom c = cupomService.criar(body.codigo(), body.tipo(), body.valor(),
                body.tatuadorId(), body.validade(), body.limiteUsos(), autor);
        return ResponseEntity.status(HttpStatus.CREATED).body(toMap(c));
    }

    @Operation(summary = "Listar cupons ativos", description = "Gestor: todos; tatuador: próprios + studio-wide.")
    @GetMapping
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR')")
    public ResponseEntity<List<Map<String, Object>>> listar(@AuthenticationPrincipal Usuario logado) {
        return ResponseEntity.ok(cupomService.listar(logado).stream().map(this::toMap).toList());
    }

    @Operation(summary = "Desativar cupom")
    @PatchMapping("/{id}/desativar")
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR')")
    public ResponseEntity<Void> desativar(@PathVariable Long id, @AuthenticationPrincipal Usuario logado) {
        cupomService.desativar(id, logado);
        return ResponseEntity.noContent().build();
    }

    private Map<String, Object> toMap(Cupom c) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", c.getId());
        m.put("codigo", c.getCodigo());
        m.put("tipo", c.getTipo().name());
        m.put("valor", c.getValor());
        m.put("tatuadorId", c.getTatuador() != null ? c.getTatuador().getId() : null);
        m.put("validade", c.getValidade());
        m.put("limiteUsos", c.getLimiteUsos());
        m.put("usos", c.getUsos());
        m.put("ativo", c.isAtivo());
        return m;
    }
}
