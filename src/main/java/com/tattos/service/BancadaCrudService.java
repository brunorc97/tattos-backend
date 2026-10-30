package com.tattos.service;

import com.tattos.dto.request.BancadaRequestDTO;
import com.tattos.dto.response.BancadaResponseDTO;
import com.tattos.exception.BusinessException;
import com.tattos.exception.ResourceNotFoundException;
import com.tattos.model.Bancada;
import com.tattos.model.enums.StatusAgendamento;
import com.tattos.repository.AgendamentoRepository;
import com.tattos.repository.BancadaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BancadaCrudService {

    private final BancadaRepository bancadaRepository;
    private final AgendamentoRepository agendamentoRepository;

    @Transactional(readOnly = true)
    public List<BancadaResponseDTO> listar() {
        return bancadaRepository.findAll().stream()
                .map(BancadaResponseDTO::fromEntity)
                .toList();
    }

    @Transactional
    public BancadaResponseDTO criar(BancadaRequestDTO dto) {
        String nome = dto.getNome().trim();
        if (bancadaRepository.existsByNome(nome)) {
            throw new BusinessException("Já existe uma bancada com o nome '" + nome + "'.");
        }
        Bancada b = Bancada.builder()
                .nome(nome)
                .descricao(dto.getDescricao())
                .ativa(dto.getAtiva() == null || dto.getAtiva())
                .build();
        return BancadaResponseDTO.fromEntity(bancadaRepository.save(b));
    }

    @Transactional
    public BancadaResponseDTO atualizar(Long id, BancadaRequestDTO dto) {
        Bancada b = bancadaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bancada não encontrada com ID: " + id));
        String nome = dto.getNome().trim();
        bancadaRepository.findByNome(nome)
                .filter(existente -> !existente.getId().equals(id))
                .ifPresent(existente -> {
                    throw new BusinessException("Já existe uma bancada com o nome '" + nome + "'.");
                });
        b.setNome(nome);
        b.setDescricao(dto.getDescricao());
        if (dto.getAtiva() != null) {
            b.setAtiva(dto.getAtiva());
        }
        return BancadaResponseDTO.fromEntity(bancadaRepository.save(b));
    }

    @Transactional
    public void deletar(Long id) {
        Bancada b = bancadaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bancada não encontrada com ID: " + id));
        boolean ocupadaNoFuturo = agendamentoRepository.findComFiltros(null, null, null,
                LocalDateTime.now(), null).stream()
                .filter(a -> a.getStatus() != StatusAgendamento.CANCELADO)
                .anyMatch(a -> b.getNome().equalsIgnoreCase(a.getCabine())
                        || (a.getBancada() != null && a.getBancada().getId().equals(id)));
        if (ocupadaNoFuturo) {
            throw new BusinessException("A bancada '" + b.getNome() + "' possui agendamentos futuros e não pode ser removida.");
        }
        bancadaRepository.delete(b);
    }
}
