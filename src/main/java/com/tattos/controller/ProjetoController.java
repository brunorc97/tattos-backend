package com.tattos.controller;

import com.tattos.dto.request.ProjetoRequestDTO;
import com.tattos.dto.response.ProjetoResponseDTO;
import com.tattos.service.ProjetoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Projetos", description = "Gestão de projetos e aprovação")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping({"/api/v1/projetos", "/api/v1/projects", "/api/projetos", "/api/projects"})
@RequiredArgsConstructor
public class ProjetoController {

    private final ProjetoService projetoService;

    @Operation(summary = "Listar projetos", description = "Tatuador vê os seus; cliente os dele; gestor filtra por tatuador, status de aprovação e cliente.")
    @GetMapping
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR','CLIENTE')")
    public ResponseEntity<List<ProjetoResponseDTO>> listar(
            @RequestParam(required = false) Long tatuadorId,
            @RequestParam(required = false) String statusAprovacao,
            @RequestParam(required = false) Long clienteId,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.tattos.model.Usuario usuario) {
        return ResponseEntity.ok(projetoService.listarComEscopo(tatuadorId, statusAprovacao, clienteId, usuario));
    }

    @Operation(summary = "Fila de projetos",
            description = "Projetos ordenados com pendentes primeiro, depois os mais antigos. Tatuador vê apenas os seus; gestor pode filtrar por tatuadorId.")
    @GetMapping("/fila")
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR')")
    public ResponseEntity<List<ProjetoResponseDTO>> fila(
            @RequestParam(required = false) Long tatuadorId,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.tattos.model.Usuario usuario) {
        return ResponseEntity.ok(projetoService.fila(tatuadorId, usuario));
    }

    @Operation(summary = "Criar projeto")
    @PostMapping
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<ProjetoResponseDTO> criar(@Valid @RequestBody ProjetoRequestDTO dto) {
        ProjetoResponseDTO created = projetoService.criarProjeto(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    public record ComprovanteRequest(@jakarta.validation.constraints.NotNull Long arquivoId) {}
    public record ConfirmarSinalRequest(@jakarta.validation.constraints.NotBlank String formaPagamento) {}
    public record ConfigurarSinalRequest(boolean necessitaSinal, java.math.BigDecimal valorSinal) {}

    @Operation(summary = "Configurar sinal do projeto", description = "Define se o projeto necessita de sinal e o valor (teto: maior orçamento vinculado).")
    @PostMapping("/{id}/sinal")
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR')")
    public ResponseEntity<ProjetoResponseDTO> configurarSinal(
            @PathVariable Long id,
            @RequestBody ConfigurarSinalRequest body,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.tattos.model.Usuario usuario) {
        return ResponseEntity.ok(projetoService.configurarSinal(id, body.necessitaSinal(), body.valorSinal()));
    }

    @Operation(summary = "Anexar comprovante do sinal", description = "Cliente dono do projeto anexa o PDF do comprovante de pagamento do sinal.")
    @PostMapping("/{id}/sinal/comprovante")
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR','CLIENTE')")
    public ResponseEntity<ProjetoResponseDTO> anexarComprovante(
            @PathVariable Long id,
            @Valid @RequestBody ComprovanteRequest body,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.tattos.model.Usuario usuario) {
        return ResponseEntity.ok(projetoService.anexarComprovanteSinal(id, body.arquivoId(), usuario));
    }

    @Operation(summary = "Confirmar sinal pago", description = "Gestor/tatuador dá entrada do sinal após conferir o comprovante.")
    @PostMapping("/{id}/sinal/confirmar")
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR')")
    public ResponseEntity<ProjetoResponseDTO> confirmarSinal(
            @PathVariable Long id,
            @Valid @RequestBody ConfirmarSinalRequest body,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.tattos.model.Usuario usuario) {
        return ResponseEntity.ok(projetoService.confirmarSinal(id, body.formaPagamento(), usuario));
    }

    @Operation(summary = "Decidir aprovação do projeto", description = "Aprova ou recusa com motivo obrigatório na recusa.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Decisão registrada")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Recusa sem motivo")
    @PostMapping("/{id}/decisao")
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR')")
    public ResponseEntity<ProjetoResponseDTO> decidir(
            @PathVariable Long id,
            @Valid @RequestBody com.tattos.dto.request.AprovacaoProjetoRequestDTO dto,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.tattos.model.Usuario usuario) {
        return ResponseEntity.ok(projetoService.decidir(id, dto, usuario));
    }

    @Operation(summary = "Aprovar projeto (legado)", description = "Deprecated: use POST /{id}/decisao.")
    @Deprecated
    @PostMapping("/{id}/aprovar")
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<ProjetoResponseDTO> aprovar(@PathVariable Long id) {
        ProjetoResponseDTO approved = projetoService.aprovarProjeto(id);
        return ResponseEntity.ok(approved);
    }
}
