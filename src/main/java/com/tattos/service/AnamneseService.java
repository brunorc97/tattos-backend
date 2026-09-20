package com.tattos.service;

import com.tattos.dto.request.AnamneseRequestDTO;
import com.tattos.dto.response.AnamneseResponseDTO;
import com.tattos.exception.ResourceNotFoundException;
import com.tattos.model.Anamnese;
import com.tattos.model.enums.AnamneseStatus;
import com.tattos.repository.AnamneseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnamneseService {

    private final AnamneseRepository anamneseRepository;

    @Transactional(readOnly = true)
    public List<AnamneseResponseDTO> findAll() {
        return anamneseRepository.findAllByOrderByDateDescCreatedAtDesc().stream()
                .map(AnamneseResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public AnamneseResponseDTO findById(Long id) {
        Anamnese anamnese = anamneseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ficha de Anamnese não encontrada com o ID: " + id));
        return AnamneseResponseDTO.fromEntity(anamnese);
    }

    @Transactional(readOnly = true)
    public List<AnamneseResponseDTO> findByCpf(String cpf) {
        // Busca tanto com pontuação quanto limpo
        String normalizedCpf = cpf.replaceAll("[^0-9]", "");
        List<Anamnese> list = anamneseRepository.findByCpfOrderByDateDesc(cpf);
        if (list.isEmpty() && !normalizedCpf.equals(cpf)) {
            list = anamneseRepository.findByCpfOrderByDateDesc(normalizedCpf);
        }

        return list.stream().map(AnamneseResponseDTO::fromEntity).toList();
    }

    @Transactional
    public AnamneseResponseDTO create(AnamneseRequestDTO dto) {
        // Geração de código ANAM-ANO-NUMERO (ex: ANAM-2026-042)
        int currentYear = LocalDate.now().getYear();
        long count = anamneseRepository.count() + 1;
        String code = String.format("ANAM-%d-%03d", currentYear, count);

        // Regra de biossegurança: computa alertas clínicos e aptidão
        boolean hasConditionAlert = dto.hasAnyConditionAlert();
        boolean hasAllergiesAlert = dto.allergies() != null
                && !dto.allergies().isBlank()
                && !dto.allergies().equalsIgnoreCase("nenhuma")
                && !dto.allergies().equalsIgnoreCase("nenhuma conhecida");

        boolean hasMedicalAlert = hasConditionAlert || hasAllergiesAlert;

        // Se o cliente ingeriu álcool em 24h ou está gestante/lactante, status INAPTO;
        // Se possui histórico de diabetes, queloides, etc., status ALERTA_CLINICO;
        // Caso contrário, APTO.
        AnamneseStatus status;
        if (Boolean.TRUE.equals(dto.alcohol24h()) || Boolean.TRUE.equals(dto.pregnantNursing())) {
            status = AnamneseStatus.INAPTO;
        } else if (hasMedicalAlert) {
            status = AnamneseStatus.ALERTA_CLINICO;
        } else {
            status = AnamneseStatus.APTO;
        }

        Anamnese anamnese = Anamnese.builder()
                .code(code)
                .clientName(dto.clientName())
                .cpf(dto.cpf())
                .phone(dto.phone())
                .birthDate(dto.birthDate())
                .procedureType(dto.procedureType() != null ? dto.procedureType() : "Tatuagem")
                .bodyLocation(dto.bodyLocation())
                .artistName(dto.artistName())
                .date(dto.date())
                .status(status)
                .hasMedicalAlert(hasMedicalAlert)
                .allergies(dto.allergies())
                .signatureData(dto.signatureData())
                .diabetes(Boolean.TRUE.equals(dto.diabetes()))
                .hemofilia(Boolean.TRUE.equals(dto.hemofilia()))
                .keloids(Boolean.TRUE.equals(dto.keloids()))
                .hepatiteHiv(Boolean.TRUE.equals(dto.hepatiteHiv()))
                .pregnantNursing(Boolean.TRUE.equals(dto.pregnantNursing()))
                .alcohol24h(Boolean.TRUE.equals(dto.alcohol24h()))
                .fedWell(dto.fedWell() == null || Boolean.TRUE.equals(dto.fedWell()))
                .cardiacCondition(Boolean.TRUE.equals(dto.cardiacCondition()))
                .medicationsInUse(Boolean.TRUE.equals(dto.medicationsInUse()))
                .notes(dto.notes())
                .build();

        Anamnese saved = anamneseRepository.save(anamnese);
        return AnamneseResponseDTO.fromEntity(saved);
    }
}
