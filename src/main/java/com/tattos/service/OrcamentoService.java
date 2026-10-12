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
        Projeto projeto = null;
        if (dto.getProjetoId() != null) {
            projeto = projetoRepository.findById(dto.getProjetoId())
                    .orElseThrow(() -> new IllegalArgumentException("Projeto não encontrado"));
        }
        Orcamento orcamento = Orcamento.builder()
                .nome(dto.getNome())
                .valor(dto.getValor())
                .projeto(projeto)
                .build();
        Orcamento saved = orcamentoRepository.save(orcamento);
        return mapToResponse(saved);
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
        return dto;
    }
}
