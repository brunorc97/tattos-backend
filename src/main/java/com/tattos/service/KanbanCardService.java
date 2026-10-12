package com.tattos.service;

import com.tattos.dto.request.KanbanCardRequestDTO;
import com.tattos.dto.response.KanbanCardResponseDTO;
import com.tattos.model.KanbanCard;
import com.tattos.model.Orcamento;
import com.tattos.model.Projeto;
import com.tattos.repository.KanbanCardRepository;
import com.tattos.repository.OrcamentoRepository;
import com.tattos.repository.ProjetoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KanbanCardService {

    private final KanbanCardRepository kanbanCardRepository;
    private final OrcamentoRepository orcamentoRepository;
    private final ProjetoRepository projetoRepository;

    @Transactional
    public KanbanCardResponseDTO criarKanbanCard(KanbanCardRequestDTO dto) {
        Orcamento orcamento = null;
        if (dto.getOrcamentoId() != null) {
            orcamento = orcamentoRepository.findById(dto.getOrcamentoId())
                    .orElseThrow(() -> new IllegalArgumentException("Orcamento não encontrado"));
        }
        Projeto projeto = null;
        if (dto.getProjetoId() != null) {
            projeto = projetoRepository.findById(dto.getProjetoId())
                    .orElseThrow(() -> new IllegalArgumentException("Projeto não encontrado"));
        }
        KanbanCard card = KanbanCard.builder()
                .titulo(dto.getTitulo())
                .descricao(dto.getDescricao())
                .status(dto.getStatus())
                .ordem(dto.getOrdem())
                .orcamento(orcamento)
                .projeto(projeto)
                .build();
        KanbanCard saved = kanbanCardRepository.save(card);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<KanbanCardResponseDTO> listarKanbanCards() {
        return kanbanCardRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private KanbanCardResponseDTO mapToResponse(KanbanCard card) {
        KanbanCardResponseDTO dto = new KanbanCardResponseDTO();
        dto.setId(card.getId());
        dto.setTitulo(card.getTitulo());
        dto.setDescricao(card.getDescricao());
        dto.setStatus(card.getStatus());
        dto.setOrdem(card.getOrdem());
        if (card.getOrcamento() != null) {
            dto.setOrcamentoId(card.getOrcamento().getId());
        }
        if (card.getProjeto() != null) {
            dto.setProjetoId(card.getProjeto().getId());
        }
        return dto;
    }
}
