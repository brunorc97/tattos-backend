package com.tattos.dto.response;

import com.tattos.model.Appointment;
import com.tattos.model.enums.AppointmentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record AppointmentResponseDTO(
    Long id,
    String client,
    String clientName,
    String phone,
    String clientPhone,
    Long artistId,
    String artist,
    String artistName,
    String station,
    LocalDate date,
    LocalTime startTime,
    Double durationHours,
    LocalTime endTime,
    String style,
    BigDecimal price,
    BigDecimal deposit,
    AppointmentStatus status,
    String notes,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static AppointmentResponseDTO fromEntity(Appointment a) {
        if (a == null) return null;
        String artistNameStr = a.getArtist() != null ? a.getArtist().getName() : null;
        Long artistIdVal = a.getArtist() != null ? a.getArtist().getId() : null;

        return new AppointmentResponseDTO(
            a.getId(),
            a.getClientName(),
            a.getClientName(),
            a.getClientPhone(),
            a.getClientPhone(),
            artistIdVal,
            artistNameStr,
            artistNameStr,
            a.getStation(),
            a.getDate(),
            a.getStartTime(),
            a.getDurationHours(),
            a.getEndTime(),
            a.getStyle(),
            a.getPrice(),
            a.getDeposit(),
            a.getStatus(),
            a.getNotes(),
            a.getCreatedAt(),
            a.getUpdatedAt()
        );
    }
}
