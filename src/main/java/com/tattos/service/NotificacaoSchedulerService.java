package com.tattos.service;

import com.tattos.model.Agendamento;
import com.tattos.model.Insumo;
import com.tattos.model.enums.StatusAgendamento;
import com.tattos.repository.AgendamentoRepository;
import com.tattos.repository.InsumoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacaoSchedulerService {

    private final AgendamentoRepository agendamentoRepository;
    private final InsumoRepository insumoRepository;
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;

    /**
     * Rotina executada de hora em hora para verificar agendamentos nas próximas 24h
     * e enviar lembrete com garantia de idempotência.
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public int processarLembretesAgendamentos() {
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime limite24h = agora.plusHours(24);

        List<StatusAgendamento> statusValidos = List.of(
                StatusAgendamento.AGENDADO,
                StatusAgendamento.CONFIRMADO
        );

        List<Agendamento> agendamentos = agendamentoRepository.findAgendamentosPendentesLembrete(
                statusValidos, agora, limite24h
        );

        int totalEnviados = 0;
        for (Agendamento agendamento : agendamentos) {
            eventPublisher.publishEvent(new com.tattos.event.LembreteAgendamentoEvent(agendamento.getId()));
            agendamento.setLembreteEnviado(true);
            agendamentoRepository.save(agendamento);
            totalEnviados++;
        }

        if (totalEnviados > 0) {
            log.info("Lembretes de agendamento (24h) enviados com sucesso: {} agendamentos notificados.", totalEnviados);
        }

        return totalEnviados;
    }

    /**
     * Rotina executada diariamente para alertar os gestores sobre insumos que
     * atingiram ou caíram abaixo do nível crítico de estoque.
     */
    @Scheduled(cron = "0 0 8 * * *")
    @Transactional(readOnly = true)
    public int processarAlertasEstoqueCritico() {
        List<Insumo> insumosCriticos = insumoRepository.findInsumosAbaixoNivelCritico();

        if (insumosCriticos.isEmpty()) {
            return 0;
        }

        for (Insumo insumo : insumosCriticos) {
            eventPublisher.publishEvent(new com.tattos.event.EstoqueCriticoEvent(insumo.getId()));
        }

        log.warn("Alerta de estoque crítico executado: {} insumos requerem reposição imediata.", insumosCriticos.size());
        return insumosCriticos.size();
    }
}
