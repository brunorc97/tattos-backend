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

    @Transactional
    public ProjetoResponseDTO criarProjeto(ProjetoRequestDTO dto) {
        Projeto projeto = Projeto.builder()
                .nome(dto.getNome())
                .descricao(dto.getDescricao())
                .aprovado(dto.getAprovado() != null ? dto.getAprovado() : false)
                .build();
        Projeto saved = projetoRepository.save(projeto);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ProjetoResponseDTO> listarProjetos() {
        return projetoRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProjetoResponseDTO aprovarProjeto(Long id) {
        Projeto projeto = projetoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Projeto não encontrado"));
        projeto.setAprovado(true);
        Projeto saved = projetoRepository.save(projeto);
        return mapToResponse(saved);
    }

    private ProjetoResponseDTO mapToResponse(Projeto projeto) {
        ProjetoResponseDTO dto = new ProjetoResponseDTO();
        dto.setId(projeto.getId());
        dto.setNome(projeto.getNome());
        dto.setDescricao(projeto.getDescricao());
        dto.setAprovado(projeto.isAprovado());
        dto.setDataCriacao(projeto.getDataCriacao());
        return dto;
    }
}
