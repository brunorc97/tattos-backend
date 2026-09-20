package com.tattos.service;

import com.tattos.dto.request.AppointmentRequestDTO;
import com.tattos.dto.response.AppointmentResponseDTO;
import com.tattos.exception.BusinessException;
import com.tattos.exception.ConflictException;
import com.tattos.exception.ResourceNotFoundException;
import com.tattos.model.Appointment;
import com.tattos.model.Artist;
import com.tattos.model.enums.AppointmentStatus;
import com.tattos.model.enums.ArtistTag;
import com.tattos.repository.AppointmentRepository;
import com.tattos.repository.ArtistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final ArtistRepository artistRepository;
    private final FinancialService financialService;

    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> findAll(Long artistId, LocalDate date, AppointmentStatus status) {
        List<Appointment> list;
        if (artistId != null) {
            list = appointmentRepository.findByArtistIdOrderByDateDescStartTimeAsc(artistId);
        } else if (date != null) {
            list = appointmentRepository.findByDateOrderByStartTimeAsc(date);
        } else if (status != null) {
            list = appointmentRepository.findByStatusOrderByDateDescStartTimeAsc(status);
        } else {
            list = appointmentRepository.findAll();
        }

        return list.stream().map(AppointmentResponseDTO::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public AppointmentResponseDTO findResponseById(Long id) {
        return AppointmentResponseDTO.fromEntity(findEntityById(id));
    }

    @Transactional(readOnly = true)
    public Appointment findEntityById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado com o ID: " + id));
    }

    @Transactional
    public AppointmentResponseDTO create(AppointmentRequestDTO dto) {
        Artist artist = artistRepository.findById(dto.artistId())
                .orElseThrow(() -> new ResourceNotFoundException("Tatuador não encontrado com o ID: " + dto.artistId()));

        LocalTime startTime = dto.startTime();
        long durationMinutes = Math.round(dto.durationHours() * 60);
        LocalTime endTime = startTime.plusMinutes(durationMinutes);

        // Valida as 3 regras críticas de negócio e prevenção de conflitos
        validateConflictAndRules(artist, dto.station(), dto.date(), startTime, endTime, null);

        BigDecimal deposit = dto.getEffectiveDeposit();
        AppointmentStatus status = dto.getEffectiveStatus();

        Appointment appointment = Appointment.builder()
                .clientName(dto.clientName())
                .clientPhone(dto.clientPhone())
                .artist(artist)
                .station(dto.station())
                .date(dto.date())
                .startTime(startTime)
                .durationHours(dto.durationHours())
                .endTime(endTime)
                .style(dto.style())
                .price(dto.price())
                .deposit(deposit)
                .status(status)
                .notes(dto.notes())
                .build();

        Appointment saved = appointmentRepository.save(appointment);

        // Integração ERP: Se houver sinal pago no agendamento, registra receita automática
        if (deposit.compareTo(BigDecimal.ZERO) > 0) {
            financialService.recordIncome(
                    "Sinal de Agendamento: " + saved.getClientName(),
                    deposit,
                    "Sinal de Agendamento",
                    "Pix / Reserva",
                    saved.getDate(),
                    saved.getClientName(),
                    artist,
                    "Sinal registrado automaticamente na criação do agendamento #" + saved.getId()
            );
        }

        return AppointmentResponseDTO.fromEntity(saved);
    }

    @Transactional
    public AppointmentResponseDTO update(Long id, AppointmentRequestDTO dto) {
        Appointment existing = findEntityById(id);

        Artist artist = artistRepository.findById(dto.artistId())
                .orElseThrow(() -> new ResourceNotFoundException("Tatuador não encontrado com o ID: " + dto.artistId()));

        LocalTime startTime = dto.startTime();
        long durationMinutes = Math.round(dto.durationHours() * 60);
        LocalTime endTime = startTime.plusMinutes(durationMinutes);

        // Validação das regras excluindo o próprio ID atual
        validateConflictAndRules(artist, dto.station(), dto.date(), startTime, endTime, id);

        existing.setClientName(dto.clientName());
        existing.setClientPhone(dto.clientPhone());
        existing.setArtist(artist);
        existing.setStation(dto.station());
        existing.setDate(dto.date());
        existing.setStartTime(startTime);
        existing.setDurationHours(dto.durationHours());
        existing.setEndTime(endTime);
        existing.setStyle(dto.style());
        existing.setPrice(dto.price());
        if (dto.deposit() != null) {
            existing.setDeposit(dto.deposit());
        }
        if (dto.status() != null) {
            existing.setStatus(dto.status());
        }
        existing.setNotes(dto.notes());

        Appointment saved = appointmentRepository.save(existing);
        return AppointmentResponseDTO.fromEntity(saved);
    }

    @Transactional
    public AppointmentResponseDTO updateStatus(Long id, AppointmentStatus newStatus) {
        Appointment existing = findEntityById(id);
        AppointmentStatus oldStatus = existing.getStatus();

        if (oldStatus == newStatus) {
            return AppointmentResponseDTO.fromEntity(existing);
        }

        existing.setStatus(newStatus);
        Appointment saved = appointmentRepository.save(existing);

        // Integração ERP ao concluir sessão:
        if (oldStatus != AppointmentStatus.CONCLUIDO && newStatus == AppointmentStatus.CONCLUIDO) {
            // 1. Incrementa total de sessões do tatuador
            Artist artist = existing.getArtist();
            if (artist != null) {
                artist.setTotalSessions(artist.getTotalSessions() + 1);
                artistRepository.save(artist);
            }

            // 2. Registra quitação do valor restante no módulo financeiro (se houver)
            BigDecimal remainingAmount = existing.getPrice().subtract(existing.getDeposit());
            if (remainingAmount.compareTo(BigDecimal.ZERO) > 0) {
                financialService.recordIncome(
                        "Quitação de Sessão: " + existing.getClientName(),
                        remainingAmount,
                        "Sessão Tatuagem",
                        "Pix / Acerto Final",
                        existing.getDate(),
                        existing.getClientName(),
                        artist,
                        "Quitação automática após conclusão da sessão #" + existing.getId()
                );
            }
        }

        return AppointmentResponseDTO.fromEntity(saved);
    }

    @Transactional
    public void delete(Long id) {
        Appointment existing = findEntityById(id);
        appointmentRepository.delete(existing);
    }

    /**
     * Validação das 3 Regras Críticas:
     * 1. Impedir sobreposição para o mesmo tatuador
     * 2. Impedir sobreposição de uso da mesma bancada (station)
     * 3. Se for VISITANTE, validar obrigatoriamente se a data está dentro do período de Guest Spot
     */
    private void validateConflictAndRules(Artist artist,
                                         String station,
                                         LocalDate date,
                                         LocalTime startTime,
                                         LocalTime endTime,
                                         Long excludeId) {
        // Regra Crítica 3: Validação de Período do Tatuador Visitante (Guest Spot)
        if (artist.getTag() == ArtistTag.VISITANTE) {
            if (artist.getGuestPeriodStart() == null || artist.getGuestPeriodEnd() == null) {
                throw new BusinessException("O tatuador convidado (VISITANTE) " + artist.getName()
                        + " não possui período de Guest Spot definido no cadastro.");
            }

            if (date.isBefore(artist.getGuestPeriodStart()) || date.isAfter(artist.getGuestPeriodEnd())) {
                throw new BusinessException(String.format(
                        "Data fora do período permitido! O tatuador visitante %s só atende entre %s e %s (data solicitada: %s).",
                        artist.getName(),
                        artist.getGuestPeriodStart(),
                        artist.getGuestPeriodEnd(),
                        date
                ));
            }
        }

        // Regra Crítica 1: Impedir sobreposição de horário para o mesmo tatuador
        List<Appointment> conflictingArtist = appointmentRepository.findConflictingArtistAppointments(
                artist.getId(), date, startTime, endTime, excludeId
        );
        if (!conflictingArtist.isEmpty()) {
            Appointment c = conflictingArtist.get(0);
            throw new ConflictException(String.format(
                    "Conflito de agenda: O tatuador %s já possui o agendamento '%s' das %s às %s neste dia.",
                    artist.getName(),
                    c.getClientName(),
                    c.getStartTime(),
                    c.getEndTime()
            ));
        }

        // Regra Crítica 2: Impedir sobreposição de uso da mesma bancada (station)
        List<Appointment> conflictingStation = appointmentRepository.findConflictingStationAppointments(
                station, date, startTime, endTime, excludeId
        );
        if (!conflictingStation.isEmpty()) {
            Appointment c = conflictingStation.get(0);
            throw new ConflictException(String.format(
                    "Conflito de espaço: A '%s' já está reservada para o cliente '%s' das %s às %s.",
                    station,
                    c.getClientName(),
                    c.getStartTime(),
                    c.getEndTime()
            ));
        }
    }
}
