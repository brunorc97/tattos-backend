package com.tattos.service;

import com.tattos.dto.request.AgendamentoRequestDTO;
import com.tattos.dto.request.PixSinalRequestDTO;
import com.tattos.dto.request.FinalPaymentRequestDTO;
import com.tattos.dto.response.PixSinalResponseDTO;
import com.tattos.dto.response.AgendamentoResponseDTO;
import com.tattos.exception.BusinessException;
import com.tattos.exception.ConflictException;
import com.tattos.exception.ResourceNotFoundException;
import com.tattos.model.Agendamento;
import com.tattos.model.Usuario;
import com.tattos.model.enums.StatusAgendamento;
import com.tattos.repository.AgendamentoRepository;
import com.tattos.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;
    private final com.tattos.service.CupomService cupomService;
    private final com.tattos.repository.BancadaRepository bancadaRepository;

    /** Resolve e valida a bancada do DTO; devolve null quando não informada (fluxo legado por cabine). */
    private com.tattos.model.Bancada resolverBancada(Long bancadaId) {
        if (bancadaId == null) {
            return null;
        }
        com.tattos.model.Bancada bancada = bancadaRepository.findById(bancadaId)
                .orElseThrow(() -> new BusinessException("Bancada não encontrada com ID: " + bancadaId));
        if (!bancada.isAtiva()) {
            throw new BusinessException("A bancada '" + bancada.getNome() + "' está inativa.");
        }
        return bancada;
    }

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
        validarJanelaHorario(inicio, fim);
        com.tattos.model.Bancada bancada = resolverBancada(dto.getBancadaId());
        String cabineEfetiva = bancada != null ? bancada.getNome() : dto.getCabine().trim();
        validarConflitos(tatuador.getId(), cabineEfetiva, bancada, inicio, fim, null);
        BigDecimal desconto = calcularDescontoIndicacao(dto.getCodigoIndicacao(), cliente.getId(), dto.getValor());
        // cupom de desconto (promoção): soma ao da indicação, nunca além do valor do procedimento
        com.tattos.model.Cupom cupom = null;
        if (dto.getCodigoCupom() != null && !dto.getCodigoCupom().isBlank()) {
            cupom = cupomService.buscarPorCodigo(dto.getCodigoCupom());
            BigDecimal descontoCupom = cupomService.validarECalcular(cupom.getCodigo(), tatuador.getId(), dto.getValor());
            desconto = desconto.add(descontoCupom).min(dto.getValor());
        }
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
                .cabine(cabineEfetiva)
                .bancada(bancada)
                .status(StatusAgendamento.AGENDADO)
                .valor(dto.getValor())
                .valorSinal(sinal)
                .sinalPago(false)
                .codigoPixSinal(pixCopiaECola)
                .descontoAplicado(desconto)
                .observacoes(dto.getObservacoes())
                .build();
        Agendamento salvo = agendamentoRepository.save(agendamento);
        if (cupom != null) {
            cupomService.registrarUso(cupom);
        }
        return AgendamentoResponseDTO.fromEntity(salvo);
    }

    @Transactional
    public PixSinalResponseDTO gerarPixSinal(Long id, PixSinalRequestDTO request) {
        if (request.getPercentual() == null) {
            throw new BusinessException("Percentual do sinal é obrigatório.");
        }
        int perc = request.getPercentual();
        if (perc < 10 || perc > 50) {
            throw new BusinessException("Percentual do sinal deve estar entre 10 e 50.");
        }
        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado com ID: " + id));
        if (agendamento.getValor() == null) {
            throw new BusinessException("Agendamento não possui valor definido para cálculo do sinal.");
        }
        BigDecimal valorSinal = agendamento.getValor()
                .multiply(new BigDecimal(perc).divide(new BigDecimal(100)))
                .setScale(2, RoundingMode.HALF_UP);
        String pix = "00020126580014BR.GOV.BCB.PIX0136tattos-pix-sinal-" + UUID.randomUUID().toString().substring(0, 8) + "5204000053039865802BR5910TATTOS ERP6009SAO PAULO62070503***6304";
        agendamento.setValorSinal(valorSinal);
        agendamento.setCodigoPixSinal(pix);
        agendamentoRepository.save(agendamento);
        // Constrói o DTO de resposta com os campos corretos
        Long valorCentavos = valorSinal.movePointRight(2).longValueExact();
        return PixSinalResponseDTO.builder()
                .codigo(pix)
                .valorCentavos(valorCentavos)
                .percentual(perc)
                .build();
    }

    @Transactional
    public void marcarSinalPago(Long id) {
        confirmarPagamentoSinal(id);
    }

    @Transactional
    public AgendamentoResponseDTO confirmarPagamentoSinal(Long id) {
        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado com ID: " + id));

        // Idempotência: Se o sinal já estiver marcado como pago, retorna sem duplicar lançamento
        if (agendamento.isSinalPago()) {
            return AgendamentoResponseDTO.fromEntity(agendamento);
        }

        agendamento.setSinalPago(true);
        agendamento.setDataPagamento(LocalDateTime.now());
        if (agendamento.getStatus() == StatusAgendamento.AGENDADO) {
            agendamento.setStatus(StatusAgendamento.CONFIRMADO);
        }

        Agendamento salvo = agendamentoRepository.save(agendamento);
        // Transação financeira do sinal é gerada pelo TransacaoAutomaticaListener após o commit
        eventPublisher.publishEvent(new com.tattos.event.SinalPagoEvent(salvo.getId()));
        return AgendamentoResponseDTO.fromEntity(salvo);
    }

    @Transactional
    public void finalizarPagamento(Long id, FinalPaymentRequestDTO request) {
        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado com ID: " + id));
        agendamento.setValorCobrancaAdicional(request.getValorCobrancaAdicional());
        agendamento.setPagamentoCompleto(true);
        agendamento.setDataPagamento(LocalDateTime.now());
        agendamentoRepository.save(agendamento);
    }

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
        validarJanelaHorario(inicio, fim);
        com.tattos.model.Bancada bancada = resolverBancada(dto.getBancadaId());
        String cabineEfetiva = bancada != null ? bancada.getNome() : dto.getCabine().trim();
        validarConflitos(tatuador.getId(), cabineEfetiva, bancada, inicio, fim, id);
        agendamento.setCliente(cliente);
        agendamento.setTatuador(tatuador);
        agendamento.setDataHora(inicio);
        agendamento.setDuracaoMinutos(duracao);
        agendamento.setDataHoraFim(fim);
        agendamento.setCabine(cabineEfetiva);
        agendamento.setBancada(bancada);
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
        Agendamento atualizado = agendamentoRepository.save(agendamento);
        if (novoStatus == StatusAgendamento.REALIZADO && atualizado.getValor() != null) {
            // Transação do saldo é gerada pelo TransacaoAutomaticaListener após o commit
            eventPublisher.publishEvent(new com.tattos.event.AgendamentoRealizadoEvent(atualizado.getId()));
        }
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
    public Page<AgendamentoResponseDTO> listarPaginado(Long tatuadorId, Long clienteId, StatusAgendamento status, LocalDate data, Pageable pageable) {
        LocalDateTime dataInicio = data != null ? data.atStartOfDay() : null;
        LocalDateTime dataFim = data != null ? data.atTime(LocalTime.MAX) : null;
        return agendamentoRepository.findComFiltrosPaginado(tatuadorId, clienteId, status, dataInicio, dataFim, pageable)
                .map(AgendamentoResponseDTO::fromEntity);
    }

    /**
     * Listagem com escopo por role: TATUADOR sempre vê a própria agenda
     * (parâmetro tatuadorId alheio é ignorado); futuras=true corta o passado.
     */
    @Transactional(readOnly = true)
    public Page<AgendamentoResponseDTO> listarComEscopo(Long tatuadorId, Long clienteId, StatusAgendamento status,
                                                        LocalDate data, String cabine, boolean futuras,
                                                        Usuario logado, Pageable pageable) {
        if (logado != null && logado.isTatuador()) {
            tatuadorId = logado.getId();
        } else if (logado != null && logado.isCliente()) {
            clienteId = logado.getId();
        }
        LocalDateTime dataInicio = data != null ? data.atStartOfDay()
                : futuras ? LocalDateTime.now() : null;
        LocalDateTime dataFim = data != null ? data.atTime(LocalTime.MAX) : null;
        return agendamentoRepository.findComFiltrosECabinePaginado(tatuadorId, clienteId, status, cabine, dataInicio, dataFim, pageable)
                .map(AgendamentoResponseDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public java.util.List<AgendamentoResponseDTO> agendaPorPeriodo(java.time.LocalDate inicio, java.time.LocalDate fim, Usuario logado) {
        if (inicio == null || fim == null || inicio.isAfter(fim)) {
            throw new BusinessException("Período inválido: 'inicio' e 'fim' são obrigatórios e inicio deve ser menor ou igual a fim.");
        }
        Long tatuadorId = null;
        Long clienteId = null;
        if (logado != null && logado.isTatuador()) {
            tatuadorId = logado.getId();
        } else if (logado != null && logado.isCliente()) {
            clienteId = logado.getId();
        }
        return agendamentoRepository.findComFiltros(tatuadorId, clienteId, null,
                        inicio.atStartOfDay(), fim.atTime(java.time.LocalTime.MAX)).stream()
                .map(AgendamentoResponseDTO::fromEntity)
                .toList();
    }

    @Transactional
    public void deletar(Long id) {
        if (!agendamentoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Agendamento não encontrado com ID: " + id);
        }
        agendamentoRepository.deleteById(id);
    }

    /**
     * Janela de funcionamento: não aceita horário no passado e respeita a
     * grade comercial 09:00–19:00 (bordas inclusivas), alinhada à bancada.
     */
    private void validarJanelaHorario(LocalDateTime inicio, LocalDateTime fim) {
        LocalDateTime ini = inicio.truncatedTo(java.time.temporal.ChronoUnit.MINUTES);
        LocalDateTime f = fim.truncatedTo(java.time.temporal.ChronoUnit.MINUTES);
        if (ini.isBefore(LocalDateTime.now())) {
            throw new BusinessException("Não é possível agendar em data/hora no passado.");
        }
        if (ini.getHour() < 9) {
            throw new BusinessException("O horário de início deve ser a partir das 09:00.");
        }
        if (f.isAfter(ini.toLocalDate().atTime(19, 0))) {
            throw new BusinessException("O atendimento deve terminar até as 19:00.");
        }
    }

    private void validarConflitos(Long tatuadorId, String cabine, LocalDateTime inicio, LocalDateTime fim, Long id) {
        validarConflitos(tatuadorId, cabine, null, inicio, fim, id);
    }

    private void validarConflitos(Long tatuadorId, String cabine, com.tattos.model.Bancada bancada,
                                  LocalDateTime inicio, LocalDateTime fim, Long id) {
        if (agendamentoRepository.existeConflitoTatuador(tatuadorId, inicio, fim, id)) {
            throw new ConflictException("Conflito de agenda: o tatuador já possui agendamento confirmado neste intervalo.");
        }
        boolean conflitoEspaco = bancada != null
                ? agendamentoRepository.existeConflitoBancada(bancada.getId(), inicio, fim, id)
                : agendamentoRepository.existeConflitoCabine(cabine.trim(), inicio, fim, id);
        if (conflitoEspaco) {
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
