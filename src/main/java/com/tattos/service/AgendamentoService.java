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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

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

        BigDecimal desconto = calcularDescontoIndicacao(dto.getCodigoIndicacao(), cliente.getId(), dto.getValor());

        BigDecimal sinal = (dto.getValorSinal() != null && dto.getValorSinal().compareTo(BigDecimal.ZERO) > 0)
                ? dto.getValorSinal()
                : (dto.getValor() != null ? dto.getValor().multiply(new BigDecimal("0.20")).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);

        String pixCopiaECola = "00020126580014BR.GOV.BCB.PIX0136tattos-pix-sinal-" + UUID.randomUUID().toString().substring(0, 8) + "5204000053039865802BR5910TATTOS ERP6009SAO PAULO62070503***6304";

        Agendamento agendamento = Agendamento.builder()
                .cliente(cliente)
                .tatuador(tatuador)
                .dataHora(inicio)
                .duracaoMinutos(duracao)
                .dataHoraFim(fim)
                .cabine(dto.getCabine().trim())
                .status(StatusAgendamento.AGENDADO)
                .valor(dto.getValor())
                .valorSinal(sinal)
                .sinalPago(false)
                .codigoPixSinal(pixCopiaECola)
                .descontoAplicado(desconto)
                .observacoes(dto.getObservacoes())
                .build();

        Agendamento salvo = agendamentoRepository.save(agendamento);
        return AgendamentoResponseDTO.fromEntity(salvo);
    }

    /**
     * Retorna o código PIX Copia e Cola do sinal para o agendamento informado.
     * Lança ResourceNotFoundException se o agendamento não existir.
     */
    @Transactional(readOnly = true)
    public String obterCodigoPixSinal(Long id) {
        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado com ID: " + id));
        return agendamento.getCodigoPixSinal();
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
        if (dto.getCodigoIndicacao() != null && !dto.getCodigoIndicacao().trim().isEmpty()) {
            BigDecimal desconto = calcularDescontoIndicacao(dto.getCodigoIndicacao(), cliente.getId(), dto.getValor());
            agendamento.setDescontoAplicado(desconto);
        }
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
            BigDecimal desconto = agendamento.getDescontoAplicado() != null ? agendamento.getDescontoAplicado() : BigDecimal.ZERO;
            BigDecimal valorLiquidoTotal = agendamento.getValor().subtract(desconto);

            // Deduz o sinal que já foi recebido previamente para evitar duplicidade de receita
            BigDecimal valorSaldoRestante = valorLiquidoTotal;
            if (agendamento.isSinalPago() && agendamento.getValorSinal() != null) {
                valorSaldoRestante = valorSaldoRestante.subtract(agendamento.getValorSinal());
            }
            if (valorSaldoRestante.compareTo(BigDecimal.ZERO) < 0) {
                valorSaldoRestante = BigDecimal.ZERO;
            }

            TransacaoFinanceira transacao = TransacaoFinanceira.builder()
                    .descricao("Procedimento Concluído (Saldo) - Agendamento #" + agendamento.getId() + " (" + agendamento.getCliente().getNome() + ")")
                    .valor(valorSaldoRestante)
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

    private BigDecimal calcularDescontoIndicacao(String codigoIndicacao, Long clienteId, BigDecimal valorBase) {
        if (codigoIndicacao == null || codigoIndicacao.trim().isEmpty() || valorBase == null) {
            return BigDecimal.ZERO;
        }

        Usuario indicador = usuarioRepository.findByCodigoIndicacao(codigoIndicacao.trim().toUpperCase())
                .orElseThrow(() -> new BusinessException("Código de indicação inválido."));

        if (indicador.getId().equals(clienteId)) {
            throw new BusinessException("Não é permitido utilizar o próprio código de indicação.");
        }

        return valorBase.multiply(new BigDecimal("0.10")).setScale(2, RoundingMode.HALF_UP);
    }
}
