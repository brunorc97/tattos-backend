package com.tattos.metrics;

import com.tattos.model.enums.StatusAgendamento;
import com.tattos.repository.AgendamentoRepository;
import com.tattos.repository.TransacaoFinanceiraRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Métricas de negócio expostas via Micrometer (Actuator + Prometheus):
 * agendamentos por status, faturamento bruto diário e taxa de sinais pagos.
 */
@Component
public class BusinessMetricsService {

    public BusinessMetricsService(MeterRegistry registry,
                                  AgendamentoRepository agendamentoRepository,
                                  TransacaoFinanceiraRepository transacaoRepository) {
        for (StatusAgendamento status : StatusAgendamento.values()) {
            registry.gauge("tattos.agendamentos.status",
                    Tags.of("status", status.name()),
                    agendamentoRepository,
                    repo -> repo.countByStatus(status));
        }

        registry.gauge("tattos.faturamento.diario",
                transacaoRepository,
                repo -> {
                    LocalDate hoje = LocalDate.now();
                    return repo.sumReceitasByPeriodo(hoje, hoje).doubleValue();
                });

        registry.gauge("tattos.sinais.taxa_pagamento",
                agendamentoRepository,
                repo -> {
                    long comSinal = repo.countByValorSinalGreaterThan(BigDecimal.ZERO);
                    if (comSinal == 0) {
                        return 0.0;
                    }
                    return (double) repo.countBySinalPagoTrue() / comSinal;
                });
    }
}
