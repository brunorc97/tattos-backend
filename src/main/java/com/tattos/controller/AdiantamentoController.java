package com.tattos.controller;

import com.tattos.dto.response.TransacaoResponseDTO;
import com.tattos.model.TransacaoFinanceira;
import com.tattos.service.AdiantamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "Adiantamentos", description = "Adiantamentos a tatuadores com abatimento automático nas comissões")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/financeiro/adiantamentos")
@RequiredArgsConstructor
public class AdiantamentoController {

    private final AdiantamentoService adiantamentoService;

    public record AdiantamentoRequest(
            @NotNull Long tatuadorId,
            @NotNull @DecimalMin("0.01") BigDecimal valor,
            Long reciboId,
            @Size(max = 255) String observacao) {}

    @Operation(summary = "Registrar adiantamento", description = "Gestor adianta valor ao tatuador, com recibo opcional (PDF/imagem).")
    @PostMapping
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<TransacaoResponseDTO> criar(@Valid @RequestBody AdiantamentoRequest body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adiantamentoService.criar(body.tatuadorId(), body.valor(), body.reciboId(), body.observacao()));
    }

    @Operation(summary = "Listar adiantamentos", description = "Gestor: todos; tatuador: apenas os próprios. emAberto=true filtra os com saldo.")
    @GetMapping
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR')")
    public ResponseEntity<Map<String, Object>> listar(
            @RequestParam(required = false) Long tatuadorId,
            @RequestParam(required = false, defaultValue = "false") boolean emAberto,
            @AuthenticationPrincipal com.tattos.model.Usuario logado,
            @PageableDefault(size = 20, sort = "data") Pageable pageable) {
        // tatuador vê apenas os próprios
        Long tatuadorEfetivo = (logado != null && logado.isTatuador()) ? logado.getId() : tatuadorId;
        Page<TransacaoFinanceira> page = adiantamentoService.listar(tatuadorEfetivo, emAberto, pageable);

        Map<String, Object> body = new HashMap<>();
        body.put("content", page.getContent().stream().map(TransacaoResponseDTO::fromEntity).toList());
        body.put("totalElements", page.getTotalElements());
        body.put("totalPages", page.getTotalPages());
        if (tatuadorEfetivo != null) {
            body.put("saldoEmAberto", adiantamentoService.saldoEmAberto(tatuadorEfetivo));
        }
        return ResponseEntity.ok(body);
    }
}
