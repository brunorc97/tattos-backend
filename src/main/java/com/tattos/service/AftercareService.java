package com.tattos.service;

import com.tattos.dto.response.AftercareResponseDTO;
import com.tattos.model.Aftercare;
import com.tattos.model.Agendamento;
import com.tattos.model.Cliente;
import com.tattos.repository.AftercareRepository;
import com.tattos.repository.AgendamentoRepository;
import com.tattos.repository.ClienteRepository;
import com.tattos.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AftercareService {

    private final AftercareRepository aftercareRepository;
    private final ClienteRepository clienteRepository;
    private final AgendamentoRepository agendamentoRepository;

    /**
     * Cria um registro de after‑care para o cliente e agendamento informados.
     */
    @Transactional
    public AftercareResponseDTO criar(Long clienteId, Long agendamentoId, String notes) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com ID: " + clienteId));
        Agendamento agendamento = agendamentoRepository.findById(agendamentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado com ID: " + agendamentoId));
        // Garantir que o agendamento pertence ao cliente
        if (!agendamento.getCliente().getId().equals(cliente.getId())) {
            throw new IllegalArgumentException("O agendamento informado não pertence ao cliente.");
        }
        Aftercare aftercare = Aftercare.builder()
                .cliente(cliente)
                .agendamento(agendamento)
                .notes(notes)
                .build();
        Aftercare salvo = aftercareRepository.save(aftercare);
        return AftercareResponseDTO.fromEntity(salvo);
    }

    @Transactional(readOnly = true)
    public AftercareResponseDTO buscarPorId(Long id) {
        Aftercare aftercare = aftercareRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aftercare não encontrado com ID: " + id));
        return AftercareResponseDTO.fromEntity(aftercare);
    }

    @Transactional(readOnly = true)
    public List<AftercareResponseDTO> listarPorCliente(Long clienteId) {
        List<Aftercare> lista = aftercareRepository.findByClienteId(clienteId);
        return lista.stream().map(AftercareResponseDTO::fromEntity).collect(Collectors.toList());
    }

    @Transactional
    public AftercareResponseDTO atualizar(Long id, String notes) {
        Aftercare aftercare = aftercareRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aftercare não encontrado com ID: " + id));
        aftercare.setNotes(notes);
        aftercare.setUpdatedAt(LocalDateTime.now());
        Aftercare salvo = aftercareRepository.save(aftercare);
        return AftercareResponseDTO.fromEntity(salvo);
    }

    @Transactional
    public void deletar(Long id) {
        if (!aftercareRepository.existsById(id)) {
            throw new ResourceNotFoundException("Aftercare não encontrado com ID: " + id);
        }
        aftercareRepository.deleteById(id);
    }

    /**
     * Atualiza o progresso de cura baseado nos dias decorridos desde o procedimento.
     * Regra simples: 30 dias para 100% de cura.
     */
    @Transactional
    public void atualizarProgressos() {
        List<Aftercare> todos = aftercareRepository.findAll();
        for (Aftercare a : todos) {
            long dias = a.getDaysSinceProcedure();
            int progresso = (int) Math.min(100, (dias * 100) / 30);
            a.setHealingProgress(progresso);
        }
        aftercareRepository.saveAll(todos);
    }
}
