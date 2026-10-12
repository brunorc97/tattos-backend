package com.tattos.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Scheduler that updates the healing progress of all Aftercare records.
 * Executes daily at 02:00 AM (adjustable via cron expression).
 * Future enhancements may send reminder emails or WhatsApp messages.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AftercareSchedulerService {

    private final AftercareService aftercareService;

    /**
     * Runs daily at 02:00 AM to recalculate healing percentages.
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void atualizarProgressosDiarios() {
        log.info("[AftercareScheduler] Atualizando progressos de cura para todos os registros Aftercare.");
        aftercareService.atualizarProgressos();
        log.info("[AftercareScheduler] Atualização concluída.");
    }
}
