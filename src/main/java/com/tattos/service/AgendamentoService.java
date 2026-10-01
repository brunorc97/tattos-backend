package com.tattos.service;

import com.tattos.dto.request.AgendamentoRequestDTO;
import com.tattos.dto.response.AgendamentoResponseDTO;
import com.tattos.exception.BusinessException;
import com.tattos.exception.ConflictException;
import com.tattos.exception.ResourceNotFoundException;
import com.tattos.model.Agendamento;
import com.tattos.model.TransacaoFinanceira;
import com.tattos.model.Usuario;
import com.tattos.model.enums.StatusAgendamento;
import com.tattos.model.enums.TipoTransacao;
import com.tattos.repository.AgendamentoRepository;
import com.tattos.repository.TransacaoFinanceiraRepository;
import com.tattos.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final TransacaoFinanceiraRepository transacaoRepository;

    @Transactional
    public AgendamentoResponseDTO criar(AgendamentoRequestDTO dto) {
        Usuario cliente = usuarioRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com ID: " + dto.getClienteId()));

        Usuario tatuador = usuarioRepository.findById(dto.getTatuadorId())
                .orElseThrow(() -> new ResourceNotFoundException("Tatuador não encontrado com ID: " + dto.getTatuadorId()));

        if (!tatuador.isTatuador()) {
            throw new BusinessException("O usuário informado como tatuador não possui o perfil de TATUADOR.");
        }

        LocalDateTime inicio = dto.getDataHora();
        int duracao = (dto.getDuracaoMinutos() != null && dto.getDuracaoMinutos() > 0) ? dto.getDuracaoMinutos() : 60;
        LocalDateTime fim = inicio.plusMinutes(duracao);

        validarConflitos(tatuador.getId(), dto.getCabine(), inicio, fim, null);

        Agendamento agendamento = Agendamento.builder()
                .cliente(cliente)
                .tatuador(tatuador)
                .dataHora(inicio)
                .duracaoMinutos(duracao)
                .dataHoraFim(fim)
                .cabine(dto.getCabine().trim())
                .status(StatusAgendamento.AGENDADO)
                .valor(dto.getValor())
                .valorSinal(dto.getValorSinal() != null ? dto.getValorSinal() : java.math.BigDecimal.ZERO)
                .observacoes(dto.getObservacoes())
                .build();

        Agendamento salvo = agendamentoRepository.save(agendamento);
        return AgendamentoResponseDTO.fromEntity(salvo);
    }

    @Transactional
    public AgendamentoResponseDTO atualizar(Long id, AgendamentoRequestDTO dto) {
        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado com ID: " + id));

        Usuario cliente = usuarioRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com ID: " + dto.getClienteId()));

        Usuario tatuador = usuarioRepository.findById(dto.getTatuadorId())
                .orElseThrow(() -> new ResourceNotFoundException("Tatuador não encontrado com ID: " + dto.getTatuadorId()));

        if (!tatuador.isTatuador()) {
            throw new BusinessException("O usuário informado como tatuador não possui o perfil de TATUADOR.");
        }

        LocalDateTime inicio = dto.getDataHora();
        int duracao = (dto.getDuracaoMinutos() != null && dto.getDuracaoMinutos() > 0) ? dto.getDuracaoMinutos() : 60;
        LocalDateTime fim = inicio.plusMinutes(duracao);

        validarConflitos(tatuador.getId(), dto.getCabine(), inicio, fim, id);

        agendamento.setCliente(cliente);
        agendamento.setTatuador(tatuador);
        agendamento.setDataHora(inicio);
        agendamento.setDuracaoMinutos(duracao);
        agendamento.setDataHoraFim(fim);
        agendamento.setCabine(dto.getCabine().trim());
        agendamento.setValor(dto.getValor());
        agendamento.setValorSinal(dto.getValorSinal());
        agendamento.setObservacoes(dto.getObservacoes());

        Agendamento atualizado = agendamentoRepository.save(agendamento);
        return AgendamentoResponseDTO.fromEntity(atualizado);
    }

    @Transactional
    public AgendamentoResponseDTO atualizarStatus(Long id, StatusAgendamento novoStatus, String motivo) {
        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado com ID: " + id));

        agendamento.setStatus(novoStatus);

        if (novoStatus == StatusAgendamento.CANCELADO && motivo != null && !motivo.trim().isEmpty()) {
            String obs = agendamento.getObservacoes() != null ? agendamento.getObservacoes() + "\n" : "";
            agendamento.setObservacoes(obs + "[Cancelado] Motivo: " + motivo);
        }

        // Se o agendamento foi realizado com sucesso, gera a receita financeira com repasse
        if (novoStatus == StatusAgendamento.REALIZADO && agendamento.getValor() != null) {
            TransacaoFinanceira transacao = TransacaoFinanceira.builder()
                    .descricao("Procedimento Concluído - Agendamento #" + agendamento.getId() + " (" + agendamento.getCliente().getNome() + ")")
                    .valor(agendamento.getValor())
                    .tipo(TipoTransacao.RECEITA)
                    .data(LocalDate.now())
                    .tatuador(agendamento.getTatuador())
                    .agendamento(agendamento)
                    .categoria("Procedimento Tattoo")
                    .formaPagamento("A Definir")
                    .build();
            transacao.calcularRepasse();
            transacaoRepository.save(transacao);
        }

        Agendamento atualizado = agendamentoRepository.save(agendamento);
        return AgendamentoResponseDTO.fromEntity(atualizado);
    }

    @Transactional
    public void cancelar(Long id, String motivo) {
        atualizarStatus(id, StatusAgendamento.CANCELADO, motivo);
    }

    @Transactional(readOnly = true)
    public AgendamentoResponseDTO buscarPorId(Long id) {
        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado com ID: " + id));
        return AgendamentoResponseDTO.fromEntity(agendamento);
    }

    @Transactional(readOnly = true)
    public List<AgendamentoResponseDTO> listar(Long tatuadorId, Long clienteId, StatusAgendamento status, LocalDate data) {
        LocalDateTime dataInicio = data != null ? data.atStartOfDay() : null;
        LocalDateTime dataFim = data != null ? data.atTime(LocalTime.MAX) : null;

        List<Agendamento> lista = agendamentoRepository.findComFiltros(tatuadorId, clienteId, status, dataInicio, dataFim);
        return lista.stream().map(AgendamentoResponseDTO::fromEntity).toList();
    }

    @Transactional
    public void deletar(Long id) {
        if (!agendamentoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Agendamento não encontrado com ID: " + id);
        }
        agendamentoRepository.deleteById(id);
    }

    private void validarConflitos(Long tatuadorId, String cabine, LocalDateTime inicio, LocalDateTime fim, Long id) {
        if (agendamentoRepository.existeConflitoTatuador(tatuadorId, inicio, fim, id)) {
            throw new ConflictException("Conflito de agenda: o tatuador já possui agendamento confirmado neste intervalo.");
        }

        if (agendamentoRepository.existeConflitoCabine(cabine.trim(), inicio, fim, id)) {
            throw new ConflictException("Conflito de espaço: a cabine '" + cabine.trim() + "' já está ocupada neste intervalo.");
        }
    }
}
