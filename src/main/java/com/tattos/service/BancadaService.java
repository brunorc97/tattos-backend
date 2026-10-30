package com.tattos.service;

import com.tattos.model.Agendamento;
import com.tattos.model.enums.StatusAgendamento;
import com.tattos.repository.AgendamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Disponibilidade de bancada (cabine) por dia: grade 09:00–19:00 em slots de
 * 60min. Slot ocupado quando existe agendamento não cancelado sobreposto.
 */
@Service
@RequiredArgsConstructor
public class BancadaService {

    private static final LocalTime ABERTURA = LocalTime.of(9, 0);
    private static final LocalTime FECHAMENTO = LocalTime.of(19, 0);
    private static final int SLOT_MINUTOS = 60;

    private final AgendamentoRepository agendamentoRepository;
    private final com.tattos.repository.BancadaRepository bancadaRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> disponibilidade(String cabine, LocalDate data) {
        return grade(data, a -> cabine.equals(a.getCabine()),
                Map.of("cabine", cabine));
    }

    /** Disponibilidade por bancada cadastrada, com fallback para o nome (agendamentos legados). */
    @Transactional(readOnly = true)
    public Map<String, Object> disponibilidade(Long bancadaId, LocalDate data) {
        com.tattos.model.Bancada bancada = bancadaRepository.findById(bancadaId)
                .orElseThrow(() -> new com.tattos.exception.ResourceNotFoundException(
                        "Bancada não encontrada com ID: " + bancadaId));
        return grade(data,
                a -> (a.getBancada() != null && a.getBancada().getId().equals(bancadaId))
                        || bancada.getNome().equals(a.getCabine()),
                Map.of("bancadaId", bancada.getId(), "bancadaNome", bancada.getNome()));
    }

    private Map<String, Object> grade(LocalDate data, java.util.function.Predicate<Agendamento> daBancada,
                                      Map<String, Object> identidade) {
        LocalDateTime inicioDia = data.atStartOfDay();
        LocalDateTime fimDia = data.atTime(LocalTime.MAX);

        List<Agendamento> doDia = agendamentoRepository
                .findComFiltros(null, null, null, inicioDia, fimDia).stream()
                .filter(daBancada)
                .filter(a -> a.getStatus() != StatusAgendamento.CANCELADO)
                .toList();

        List<Map<String, Object>> horarios = new ArrayList<>();
        for (LocalTime slot = ABERTURA; slot.isBefore(FECHAMENTO); slot = slot.plusMinutes(SLOT_MINUTOS)) {
            LocalDateTime slotInicio = data.atTime(slot);
            LocalDateTime slotFim = slotInicio.plusMinutes(SLOT_MINUTOS);

            Agendamento ocupante = null;
            for (Agendamento a : doDia) {
                LocalDateTime agFim = a.getDataHoraFim() != null
                        ? a.getDataHoraFim()
                        : a.getDataHora().plusMinutes(a.getDuracaoMinutos() != null ? a.getDuracaoMinutos() : 60);
                if (slotInicio.isBefore(agFim) && slotFim.isAfter(a.getDataHora())) {
                    ocupante = a;
                    break;
                }
            }

            Map<String, Object> slotMap = new HashMap<>();
            slotMap.put("inicio", slot.toString());
            slotMap.put("fim", slot.plusMinutes(SLOT_MINUTOS).toString());
            slotMap.put("livre", ocupante == null);
            if (ocupante != null) {
                slotMap.put("agendamentoId", ocupante.getId());
            }
            horarios.add(slotMap);
        }

        Map<String, Object> resultado = new HashMap<>(identidade);
        resultado.put("data", data.toString());
        resultado.put("horarios", horarios);
        return resultado;
    }
}
