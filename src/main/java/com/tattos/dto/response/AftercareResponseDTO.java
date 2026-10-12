package com.tattos.dto.response;

import com.tattos.model.Aftercare;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de resposta para o recurso Aftercare.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AftercareResponseDTO {
    private Long id;
    private Long clienteId;
    private Long agendamentoId;
    private String notes;
    private Integer healingProgress;
    private Long daysSinceProcedure;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AftercareResponseDTO fromEntity(Aftercare aftercare) {
        return AftercareResponseDTO.builder()
                .id(aftercare.getId())
                .clienteId(aftercare.getCliente() != null ? aftercare.getCliente().getId() : null)
                .agendamentoId(aftercare.getAgendamento() != null ? aftercare.getAgendamento().getId() : null)
                .notes(aftercare.getNotes())
                .healingProgress(aftercare.getHealingProgress())
                .daysSinceProcedure(aftercare.getDaysSinceProcedure())
                .createdAt(aftercare.getCreatedAt())
                .updatedAt(aftercare.getUpdatedAt())
                .build();
    }
}
