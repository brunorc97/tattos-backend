package com.tattos.dto.response;

import com.tattos.model.Artist;
import com.tattos.model.enums.ArtistStatus;
import com.tattos.model.enums.ArtistTag;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ArtistResponseDTO(
    Long id,
    String name,
    ArtistTag tag,
    String specialty,
    BigDecimal splitRate,
    String station,
    String phone,
    String instagram,
    String email,
    ArtistStatus status,
    String bio,
    String originStudio,
    LocalDate guestPeriodStart,
    LocalDate guestPeriodEnd,
    Integer totalSessions,
    LocalDateTime createdAt
) {
    public static ArtistResponseDTO fromEntity(Artist artist) {
        if (artist == null) return null;
        return new ArtistResponseDTO(
            artist.getId(),
            artist.getName(),
            artist.getTag(),
            artist.getSpecialty(),
            artist.getSplitRate(),
            artist.getStation(),
            artist.getPhone(),
            artist.getInstagram(),
            artist.getEmail(),
            artist.getStatus(),
            artist.getBio(),
            artist.getOriginStudio(),
            artist.getGuestPeriodStart(),
            artist.getGuestPeriodEnd(),
            artist.getTotalSessions(),
            artist.getCreatedAt()
        );
    }
}
