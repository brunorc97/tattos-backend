package com.tattos.controller;

import com.tattos.model.ContatoCliente;
import com.tattos.model.Usuario;
import com.tattos.repository.AgendamentoRepository;
import com.tattos.repository.ContatoClienteRepository;
import com.tattos.repository.UsuarioRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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

import java.time.LocalDateTime;
import java.util.Map;

@Tag(name = "CRM de Clientes", description = "Histórico de contato e perfil do cliente")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
public class ClienteCrmController {

    private final ContatoClienteRepository contatoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final com.tattos.service.ClientePerfilService perfilService;
    private final com.tattos.repository.TrabalhoRealizadoRepository trabalhoRepository;
    private final com.tattos.repository.ArquivoRepository arquivoRepository;

    public record ContatoRequest(
            @NotNull ContatoCliente.Tipo tipo,
            @NotBlank @Size(max = 1000) String resumo,
            LocalDateTime dataContato) {}

    @Operation(summary = "Registrar contato", description = "Gestor registra para qualquer cliente; tatuador apenas para clientes com agendamento com ele.")
    @PostMapping("/{clienteId}/contatos")
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR')")
    public ResponseEntity<Map<String, Object>> registrar(
            @PathVariable Long clienteId,
            @Valid @RequestBody ContatoRequest body,
            @AuthenticationPrincipal Usuario autor) {
        Usuario cliente = buscarCliente(clienteId);
        validarVinculoTatuador(clienteId, autor);

        ContatoCliente contato = contatoRepository.save(ContatoCliente.builder()
                .cliente(cliente)
                .autor(autor)
                .tipo(body.tipo())
                .resumo(body.resumo())
                .dataContato(body.dataContato() != null ? body.dataContato() : LocalDateTime.now())
                .build());

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", contato.getId(),
                "tipo", contato.getTipo().name(),
                "resumo", contato.getResumo(),
                "dataContato", contato.getDataContato().toString()
        ));
    }

    @Operation(summary = "Listar contatos do cliente", description = "Mesma regra de vínculo do registro.")
    @GetMapping("/{clienteId}/contatos")
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR')")
    public ResponseEntity<Page<com.tattos.dto.response.ClientePerfilDTO.ContatoResumo>> listar(
            @PathVariable Long clienteId,
            @AuthenticationPrincipal Usuario autor,
            @PageableDefault(size = 20) Pageable pageable) {
        buscarCliente(clienteId);
        validarVinculoTatuador(clienteId, autor);
        return ResponseEntity.ok(contatoRepository.findByClienteIdOrderByDataContatoDesc(clienteId, pageable)
                .map(ct -> com.tattos.dto.response.ClientePerfilDTO.ContatoResumo.builder()
                        .id(ct.getId())
                        .tipo(ct.getTipo().name())
                        .resumo(ct.getResumo())
                        .dataContato(ct.getDataContato())
                        .build()));
    }

    public record TrabalhoRequest(
            @Size(max = 500) String descricao,
            @NotNull Long tatuadorId,
            @NotNull java.time.LocalDate dataTrabalho,
            Long imagemId,
            Long agendamentoId) {}

    @Operation(summary = "Registrar trabalho realizado", description = "Registra trabalho no cliente com foto opcional, tatuador responsável e data.")
    @PostMapping("/{clienteId}/trabalhos")
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR')")
    public ResponseEntity<Map<String, Object>> registrarTrabalho(
            @PathVariable Long clienteId,
            @Valid @RequestBody TrabalhoRequest body,
            @AuthenticationPrincipal Usuario autor) {
        Usuario cliente = buscarCliente(clienteId);
        validarVinculoTatuador(clienteId, autor);

        Usuario tatuador = usuarioRepository.findById(body.tatuadorId())
                .filter(Usuario::isTatuador)
                .orElseThrow(() -> new com.tattos.exception.ResourceNotFoundException(
                        "Tatuador não encontrado com ID: " + body.tatuadorId()));

        com.tattos.model.TrabalhoRealizado trabalho = com.tattos.model.TrabalhoRealizado.builder()
                .cliente(cliente)
                .tatuador(tatuador)
                .descricao(body.descricao())
                .dataTrabalho(body.dataTrabalho())
                .build();

        // tatuador só registra trabalho em seu próprio nome
        if (autor != null && autor.isTatuador() && !autor.getId().equals(body.tatuadorId())) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Você só pode registrar trabalhos em seu próprio nome.");
        }

        if (body.imagemId() != null) {
            var imagem = arquivoRepository.findById(body.imagemId())
                    .orElseThrow(() -> new com.tattos.exception.ResourceNotFoundException(
                            "Imagem não encontrada com ID: " + body.imagemId()));
            if (imagem.getContentType() == null || !imagem.getContentType().startsWith("image/")) {
                throw new com.tattos.exception.BusinessException("O arquivo anexado não é uma imagem.");
            }
            trabalho.setImagem(imagem);
        }
        if (body.agendamentoId() != null) {
            var agendamento = agendamentoRepository.findById(body.agendamentoId())
                    .orElseThrow(() -> new com.tattos.exception.ResourceNotFoundException(
                            "Agendamento não encontrado com ID: " + body.agendamentoId()));
            if (!agendamento.getCliente().getId().equals(clienteId)) {
                throw new com.tattos.exception.BusinessException(
                        "O agendamento informado não pertence a este cliente.");
            }
            trabalho.setAgendamento(agendamento);
        }

        var salvo = trabalhoRepository.save(trabalho);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", salvo.getId(),
                "descricao", salvo.getDescricao() != null ? salvo.getDescricao() : "",
                "tatuadorId", tatuador.getId(),
                "dataTrabalho", salvo.getDataTrabalho().toString()
        ));
    }

    @Operation(summary = "Listar trabalhos do cliente")
    @GetMapping("/{clienteId}/trabalhos")
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR')")
    public ResponseEntity<java.util.List<Map<String, Object>>> listarTrabalhos(
            @PathVariable Long clienteId,
            @AuthenticationPrincipal Usuario autor) {
        buscarCliente(clienteId);
        validarVinculoTatuador(clienteId, autor);
        return ResponseEntity.ok(trabalhoRepository.findByClienteIdOrderByDataTrabalhoDesc(clienteId).stream()
                .map(t -> {
                    Map<String, Object> m = new java.util.HashMap<String, Object>();
                    m.put("id", t.getId());
                    m.put("descricao", t.getDescricao());
                    m.put("tatuadorId", t.getTatuador().getId());
                    m.put("tatuadorNome", t.getTatuador().getNome());
                    m.put("dataTrabalho", t.getDataTrabalho().toString());
                    m.put("imagemId", t.getImagem() != null ? t.getImagem().getId() : null);
                    m.put("imagemUrl", t.getImagem() != null ? "/api/v1/files/" + t.getImagem().getId() : null);
                    return m;
                })
                .toList());
    }

    @Operation(summary = "Perfil 360° do cliente", description = "Agregação: dados, trabalhos, projetos recusados, fichas, alergias, contatos e próximo aniversário.")
    @GetMapping("/{clienteId}/perfil")
    @PreAuthorize("hasAnyRole('GESTOR','TATUADOR')")
    public ResponseEntity<com.tattos.dto.response.ClientePerfilDTO> perfil(
            @PathVariable Long clienteId,
            @AuthenticationPrincipal Usuario autor) {
        buscarCliente(clienteId);
        validarVinculoTatuador(clienteId, autor);
        return ResponseEntity.ok(perfilService.montar(clienteId));
    }

    private Usuario buscarCliente(Long clienteId) {
        return usuarioRepository.findById(clienteId)
                .filter(Usuario::isCliente)
                .orElseThrow(() -> new com.tattos.exception.ResourceNotFoundException(
                        "Cliente não encontrado com ID: " + clienteId));
    }

    private void validarVinculoTatuador(Long clienteId, Usuario autor) {
        if (autor != null && autor.isTatuador()) {
            boolean vinculado = agendamentoRepository.existsByTatuadorIdAndClienteId(autor.getId(), clienteId);
            if (!vinculado) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "Este cliente não possui agendamentos com você.");
            }
        }
    }
}
