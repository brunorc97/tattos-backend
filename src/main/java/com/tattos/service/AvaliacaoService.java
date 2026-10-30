package com.tattos.service;

import com.tattos.dto.request.AvaliacaoRequestDTO;
import com.tattos.dto.response.AvaliacaoResponseDTO;
import com.tattos.dto.response.TrabalhoPendenteResponseDTO;
import com.tattos.exception.BusinessException;
import com.tattos.exception.ConflictException;
import com.tattos.exception.ResourceNotFoundException;
import com.tattos.model.Avaliacao;
import com.tattos.model.Trabalho;
import com.tattos.model.Usuario;
import com.tattos.repository.AvaliacaoRepository;
import com.tattos.repository.TrabalhoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final TrabalhoRepository trabalhoRepository;

    @Transactional(readOnly = true)
    public List<AvaliacaoResponseDTO> listar() {
        return avaliacaoRepository.findAllByOrderByDataDesc().stream()
                .map(AvaliacaoResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TrabalhoPendenteResponseDTO> pendentes(Usuario cliente) {
        return trabalhoRepository.findPendentesAvaliacaoDoCliente(cliente.getId()).stream()
                .map(TrabalhoPendenteResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AvaliacaoResponseDTO> minhas(Usuario cliente) {
        return avaliacaoRepository.findByClienteIdOrderByDataDesc(cliente.getId()).stream()
                .map(AvaliacaoResponseDTO::fromEntity)
                .toList();
    }

    @Transactional
    public AvaliacaoResponseDTO avaliar(AvaliacaoRequestDTO dto, Usuario cliente) {
        Trabalho trabalho = trabalhoRepository.findById(dto.getTrabalhoId())
                .orElseThrow(() -> new ResourceNotFoundException("Trabalho não encontrado com ID: " + dto.getTrabalhoId()));
        if (trabalho.getCliente() == null) {
            throw new BusinessException("Este trabalho é uma peça de portfólio e não pode ser avaliado.");
        }
        if (!trabalho.getCliente().getId().equals(cliente.getId())) {
            throw new AccessDeniedException("Somente o cliente dono do trabalho pode avaliá-lo.");
        }
        if (avaliacaoRepository.existsByTrabalhoId(trabalho.getId())) {
            throw new ConflictException("Este trabalho já foi avaliado.");
        }

        Avaliacao avaliacao = Avaliacao.builder()
                .trabalho(trabalho)
                .cliente(cliente)
                .tatuador(trabalho.getTatuador())
                .notaTatuagem(dto.getNotaTatuagem())
                .notaTatuador(dto.getNotaTatuador())
                .comentario(dto.getComentario())
                .data(LocalDate.now())
                .build();
        return AvaliacaoResponseDTO.fromEntity(avaliacaoRepository.save(avaliacao));
    }
}
