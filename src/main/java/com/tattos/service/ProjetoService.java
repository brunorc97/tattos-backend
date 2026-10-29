package com.tattos.service;

import com.tattos.dto.request.ProjetoRequestDTO;
import com.tattos.dto.response.ProjetoResponseDTO;
import com.tattos.model.Projeto;
import com.tattos.repository.ProjetoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjetoService {

    private final ProjetoRepository projetoRepository;
    private final com.tattos.repository.UsuarioRepository usuarioRepository;
    private final com.tattos.repository.ArquivoRepository arquivoRepository;

    @Transactional
    public ProjetoResponseDTO criarProjeto(ProjetoRequestDTO dto) {
        com.tattos.model.Usuario cliente = usuarioRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new com.tattos.exception.ResourceNotFoundException(
                        "Cliente não encontrado com ID: " + dto.getClienteId()));

        Projeto.ProjetoBuilder builder = Projeto.builder()
                .nome(dto.getNome())
                .descricao(dto.getDescricao())
                .cliente(cliente)
                .prazo(dto.getPrazo())
                .statusAprovacao(Boolean.TRUE.equals(dto.getAprovado())
                        ? com.tattos.model.enums.StatusAprovacaoProjeto.APROVADO
                        : com.tattos.model.enums.StatusAprovacaoProjeto.PENDENTE);

        if (dto.getTatuadorId() != null) {
            com.tattos.model.Usuario tatuador = usuarioRepository.findById(dto.getTatuadorId())
                    .orElseThrow(() -> new com.tattos.exception.ResourceNotFoundException(
                            "Tatuador não encontrado com ID: " + dto.getTatuadorId()));
            builder.tatuador(tatuador);
        }

        Projeto saved = projetoRepository.save(builder.build());
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ProjetoResponseDTO> listarProjetos() {
        return listarComEscopo(null, null, null, null);
    }

    /**
     * Lista com escopo por role: TATUADOR vê apenas os seus; CLIENTE apenas os
     * dele; GESTOR filtra livremente por tatuador/status/cliente.
     */
    @Transactional(readOnly = true)
    public List<ProjetoResponseDTO> listarComEscopo(Long tatuadorId, String statusAprovacao,
                                                    Long clienteId, com.tattos.model.Usuario logado) {
        if (logado != null && logado.isTatuador()) {
            tatuadorId = logado.getId();
        } else if (logado != null && logado.isCliente()) {
            clienteId = logado.getId();
        }
        com.tattos.model.enums.StatusAprovacaoProjeto status = null;
        if (statusAprovacao != null && !statusAprovacao.isBlank()) {
            status = com.tattos.model.enums.StatusAprovacaoProjeto.valueOf(statusAprovacao.trim().toUpperCase());
        }
        return projetoRepository.buscarComFiltros(tatuadorId, status, clienteId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProjetoResponseDTO decidir(Long id, com.tattos.dto.request.AprovacaoProjetoRequestDTO dto) {
        Projeto projeto = projetoRepository.findById(id)
                .orElseThrow(() -> new com.tattos.exception.ResourceNotFoundException("Projeto não encontrado com ID: " + id));

        if (Boolean.TRUE.equals(dto.getAprovado())) {
            projeto.setStatusAprovacao(com.tattos.model.enums.StatusAprovacaoProjeto.APROVADO);
            projeto.setMotivoRecusa(null);
            projeto.setMotivoRecusaDetalhe(null);
        } else {
            if (dto.getMotivoRecusa() == null) {
                throw new com.tattos.exception.BusinessException("O motivo da recusa é obrigatório ao não aprovar o projeto.");
            }
            projeto.setStatusAprovacao(com.tattos.model.enums.StatusAprovacaoProjeto.NAO_APROVADO);
            projeto.setMotivoRecusa(dto.getMotivoRecusa());
            projeto.setMotivoRecusaDetalhe(dto.getDetalhe());
        }
        return mapToResponse(projetoRepository.save(projeto));
    }

    @Transactional
    public ProjetoResponseDTO aprovarProjeto(Long id) {
        Projeto projeto = projetoRepository.findById(id)
                .orElseThrow(() -> new com.tattos.exception.ResourceNotFoundException("Projeto não encontrado com ID: " + id));
        projeto.setStatusAprovacao(com.tattos.model.enums.StatusAprovacaoProjeto.APROVADO);
        Projeto saved = projetoRepository.save(projeto);
        return mapToResponse(saved);
    }

    @Transactional
    public ProjetoResponseDTO anexarComprovanteSinal(Long id, Long arquivoId, com.tattos.model.Usuario logado) {
        Projeto projeto = projetoRepository.findById(id)
                .orElseThrow(() -> new com.tattos.exception.ResourceNotFoundException("Projeto não encontrado com ID: " + id));
        if (logado != null && logado.isCliente()
                && (projeto.getCliente() == null || !projeto.getCliente().getId().equals(logado.getId()))) {
            throw new org.springframework.security.access.AccessDeniedException("Este projeto pertence a outro cliente.");
        }
        com.tattos.model.Arquivo arquivo = arquivoRepository.findById(arquivoId)
                .orElseThrow(() -> new com.tattos.exception.ResourceNotFoundException("Arquivo não encontrado com ID: " + arquivoId));
        if (!"application/pdf".equals(arquivo.getContentType())) {
            throw new com.tattos.exception.BusinessException("O comprovante deve ser um PDF.");
        }
        projeto.setComprovanteSinal(arquivo);
        return mapToResponse(projetoRepository.save(projeto));
    }

    @Transactional
    public ProjetoResponseDTO confirmarSinal(Long id, String formaPagamento) {
        Projeto projeto = projetoRepository.findById(id)
                .orElseThrow(() -> new com.tattos.exception.ResourceNotFoundException("Projeto não encontrado com ID: " + id));
        if (!projeto.isNecessitaSinal() || projeto.getValorSinal() == null) {
            throw new com.tattos.exception.BusinessException("O projeto não possui sinal configurado.");
        }
        projeto.setSinalPago(true);
        projeto.setFormaPagamentoSinal(formaPagamento);
        projeto.setDataPagamentoSinal(java.time.LocalDateTime.now());
        return mapToResponse(projetoRepository.save(projeto));
    }

    private ProjetoResponseDTO mapToResponse(Projeto projeto) {
        ProjetoResponseDTO dto = new ProjetoResponseDTO();
        dto.setId(projeto.getId());
        dto.setNome(projeto.getNome());
        dto.setDescricao(projeto.getDescricao());
        dto.setAprovado(projeto.isAprovado());
        dto.setClienteId(projeto.getCliente() != null ? projeto.getCliente().getId() : null);
        dto.setTatuadorId(projeto.getTatuador() != null ? projeto.getTatuador().getId() : null);
        dto.setPrazo(projeto.getPrazo());
        dto.setStatusAprovacao(projeto.getStatusAprovacao() != null ? projeto.getStatusAprovacao().name() : null);
        dto.setMotivoRecusa(projeto.getMotivoRecusa() != null ? projeto.getMotivoRecusa().name() : null);
        dto.setMotivoRecusaDetalhe(projeto.getMotivoRecusaDetalhe());
        dto.setNecessitaSinal(projeto.isNecessitaSinal());
        dto.setValorSinal(projeto.getValorSinal());
        dto.setSinalPago(projeto.isSinalPago());
        dto.setFormaPagamentoSinal(projeto.getFormaPagamentoSinal());
        dto.setDataPagamentoSinal(projeto.getDataPagamentoSinal());
        dto.setComprovanteSinalId(projeto.getComprovanteSinal() != null ? projeto.getComprovanteSinal().getId() : null);
        dto.setDataCriacao(projeto.getDataCriacao());
        return dto;
    }
}
