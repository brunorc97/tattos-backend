package com.tattos.dto.response;

import com.tattos.model.Anamnese;
import com.tattos.model.enums.AnamneseStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AnamneseResponseDTO(
    Long id,
    String code,
    String clientName,
    String cpf,
    String phone,
    LocalDate birthDate,
    String procedureType,
    String bodyLocation,
    String artistName,
    LocalDate date,
    AnamneseStatus status,
    Boolean hasMedicalAlert,
    String allergies,
    String signatureData,
    Boolean diabetes,
    Boolean hemofilia,
    Boolean keloids,
    Boolean hepatiteHiv,
    Boolean pregnantNursing,
    Boolean alcohol24h,
    Boolean fedWell,
    Boolean cardiacCondition,
    Boolean medicationsInUse,
    String notes,
    LocalDateTime createdAt
) {
    public static AnamneseResponseDTO fromEntity(Anamnese a) {
        if (a == null) return null;
        return new AnamneseResponseDTO(
            a.getId(),
            a.getCode(),
            a.getClientName(),
            a.getCpf(),
            a.getPhone(),
            a.getBirthDate(),
            a.getProcedureType(),
            a.getBodyLocation(),
            a.getArtistName(),
            a.getDate(),
            a.getStatus(),
            a.getHasMedicalAlert(),
            a.getAllergies(),
            a.getSignatureData(),
            a.getDiabetes(),
            a.getHemofilia(),
            a.getKeloids(),
            a.getHepatiteHiv(),
            a.getPregnantNursing(),
            a.getAlcohol24h(),
            a.getFedWell(),
            a.getCardiacCondition(),
            a.getMedicationsInUse(),
            a.getNotes(),
            a.getCreatedAt()
        );
    }
}
