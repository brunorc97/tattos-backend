package com.tattos.event.listener;

import com.tattos.event.EstoqueCriticoEvent;
import com.tattos.event.LembreteAgendamentoEvent;
import com.tattos.model.Agendamento;
import com.tattos.model.Insumo;
import com.tattos.model.Usuario;
import com.tattos.model.enums.UserRole;
import com.tattos.repository.AgendamentoRepository;
import com.tattos.repository.InsumoRepository;
import com.tattos.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Entrega de notificações de domínio (lembretes e alertas de estoque).
 * Hoje a entrega é apenas em log; integrações reais (SMTP/WhatsApp) entram aqui.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificacaoListener {

    private final AgendamentoRepository agendamentoRepository;
    private final InsumoRepository insumoRepository;
    private final UsuarioRepository usuarioRepository;

    @EventListener
    public void onLembreteAgendamento(LembreteAgendamentoEvent event) {
        Agendamento agendamento = agendamentoRepository.findById(event.agendamentoId()).orElse(null);
        if (agendamento == null) {
            return;
        }
        String emailCliente = agendamento.getCliente() != null ? agendamento.getCliente().getEmail() : "N/A";
        String nomeCliente = agendamento.getCliente() != null ? agendamento.getCliente().getNome() : "Cliente";
        String nomeTatuador = agendamento.getTatuador() != null ? agendamento.getTatuador().getNome() : "Tatuador";

        log.info("[LEMBRETE 24H] Enviado para {} ({}) sobre agendamento #{} com {} em {}.",
                nomeCliente, emailCliente, agendamento.getId(), nomeTatuador, agendamento.getDataHora());
    }

    @EventListener
    public void onEstoqueCritico(EstoqueCriticoEvent event) {
        Insumo insumo = insumoRepository.findById(event.insumoId()).orElse(null);
        if (insumo == null) {
            return;
        }
        List<Usuario> gestores = usuarioRepository.findByRole(UserRole.ROLE_GESTOR);
        for (Usuario gestor : gestores) {
            log.warn("[ALERTA ESTOQUE CRÍTICO] Gestor: {} ({}) | Insumo: '{}' (ID: {}, Código: {}) | Qtd Atual: {} {} | Nível Crítico: {}",
                    gestor.getNome(), gestor.getEmail(), insumo.getNome(), insumo.getId(), insumo.getCodigo(),
                    insumo.getQuantidadeAtual(), insumo.getUnidadeMedida(), insumo.getNivelCritico());
        }
    }
}
