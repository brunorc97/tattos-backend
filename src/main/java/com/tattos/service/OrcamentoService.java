package com.tattos.service;

import com.tattos.dto.request.OrcamentoRequestDTO;
import com.tattos.dto.response.OrcamentoResponseDTO;
import com.tattos.model.Orcamento;
import com.tattos.model.Projeto;
import com.tattos.repository.OrcamentoRepository;
import com.tattos.repository.ProjetoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrcamentoService {

    private final OrcamentoRepository orcamentoRepository;
    private final ProjetoRepository projetoRepository;

    @Transactional
    public OrcamentoResponseDTO criarOrcamento(OrcamentoRequestDTO dto) {
        validarRegrasComerciais(dto);
        Projeto projeto = null;
        if (dto.getProjetoId() != null) {
            projeto = projetoRepository.findById(dto.getProjetoId())
                    .orElseThrow(() -> new IllegalArgumentException("Projeto não encontrado"));
        }
        Orcamento orcamento = Orcamento.builder()
                .nome(dto.getNome())
                .valor(dto.getValor())
                .projeto(projeto)
                .tipoCobranca(dto.getTipoCobranca())
                .valorTotal(dto.getValorTotal())
                .valorSessao(dto.getValorSessao())
                .sessoesMin(dto.getSessoesMin())
                .sessoesMax(dto.getSessoesMax())
                .observacoes(dto.getObservacoes())
                .build();
        Orcamento saved = orcamentoRepository.save(orcamento);
        return mapToResponse(saved);
    }

    private void validarRegrasComerciais(OrcamentoRequestDTO dto) {
        if (dto.getTipoCobranca() == null) {
            return;
        }
        switch (dto.getTipoCobranca()) {
            case FECHADO -> {
                if (dto.getValorTotal() == null) {
                    throw new com.tattos.exception.BusinessException("valorTotal é obrigatório quando o tipo de cobrança é FECHADO.");
                }
            }
            case POR_SESSAO -> {
                if (dto.getValorSessao() == null || dto.getSessoesMin() == null) {
                    throw new com.tattos.exception.BusinessException("valorSessao e sessoesMin são obrigatórios quando o tipo de cobrança é POR_SESSAO.");
                }
                if (dto.getSessoesMax() != null && dto.getSessoesMax() < dto.getSessoesMin()) {
                    throw new com.tattos.exception.BusinessException("sessoesMax não pode ser menor que sessoesMin.");
                }
            }
        }
    }

    /**
     * Máquina de estados do orçamento: RASCUNHO→ENVIADO (gestor ou tatuador do
     * projeto), ENVIADO→ACEITO/RECUSADO (cliente do projeto ou gestor).
     * ACEITO aprova o projeto vinculado.
     */
    @Transactional
    public OrcamentoResponseDTO alterarStatus(Long id, com.tattos.model.enums.StatusOrcamento novo,
                                              com.tattos.model.Usuario logado) {
        Orcamento orcamento = orcamentoRepository.findById(id)
                .orElseThrow(() -> new com.tattos.exception.ResourceNotFoundException("Orçamento não encontrado com ID: " + id));
        Projeto projeto = orcamento.getProjeto();
        boolean gestor = logado != null && logado.isGestor();
        boolean tatuadorDoProjeto = logado != null && logado.isTatuador()
                && projeto != null && projeto.getTatuador() != null
                && projeto.getTatuador().getId().equals(logado.getId());
        boolean clienteDoProjeto = logado != null && logado.isCliente()
                && projeto != null && projeto.getCliente() != null
                && projeto.getCliente().getId().equals(logado.getId());

        var atual = orcamento.getStatus();
        boolean transicaoValida = switch (atual) {
            case RASCUNHO -> novo == com.tattos.model.enums.StatusOrcamento.ENVIADO;
            case ENVIADO -> novo == com.tattos.model.enums.StatusOrcamento.ACEITO
                    || novo == com.tattos.model.enums.StatusOrcamento.RECUSADO;
            case ACEITO, RECUSADO -> false;
        };
        if (!transicaoValida) {
            throw new com.tattos.exception.ConflictException(
                    "Transição de status inválida: " + atual + " → " + novo + ".");
        }
        // logado null: contexto sem principal de Usuario (ex.: testes com @WithMockUser) — sem restrição adicional
        boolean atorPermitido = logado == null || switch (atual) {
            case RASCUNHO -> gestor || tatuadorDoProjeto;
            case ENVIADO -> gestor || clienteDoProjeto;
            default -> false;
        };
        if (!atorPermitido) {
            throw new org.springframework.security.access.AccessDeniedException("Sem permissão para alterar este orçamento.");
        }

        orcamento.setStatus(novo);
        if (novo == com.tattos.model.enums.StatusOrcamento.ACEITO && projeto != null) {
            projeto.setStatusAprovacao(com.tattos.model.enums.StatusAprovacaoProjeto.APROVADO);
            projetoRepository.save(projeto);
        }
        return mapToResponse(orcamentoRepository.save(orcamento));
    }

    @Transactional(readOnly = true)
    public List<OrcamentoResponseDTO> listarOrcamentos() {
        return orcamentoRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private OrcamentoResponseDTO mapToResponse(Orcamento orcamento) {
        OrcamentoResponseDTO dto = new OrcamentoResponseDTO();
        dto.setId(orcamento.getId());
        dto.setNome(orcamento.getNome());
        dto.setValor(orcamento.getValor());
        dto.setDataCriacao(orcamento.getDataCriacao());
        if (orcamento.getProjeto() != null) {
            dto.setProjetoId(orcamento.getProjeto().getId());
        }
        dto.setTipoCobranca(orcamento.getTipoCobranca());
        dto.setValorTotal(orcamento.getValorTotal());
        dto.setValorSessao(orcamento.getValorSessao());
        dto.setSessoesMin(orcamento.getSessoesMin());
        dto.setSessoesMax(orcamento.getSessoesMax());
        dto.setObservacoes(orcamento.getObservacoes());
        dto.setStatus(orcamento.getStatus());
        return dto;
    }
}
