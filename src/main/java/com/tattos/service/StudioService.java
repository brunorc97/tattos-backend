package com.tattos.service;

import com.tattos.dto.request.StudioInfoRequestDTO;
import com.tattos.dto.response.StudioInfoResponseDTO;
import com.tattos.model.StudioInfo;
import com.tattos.repository.StudioInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudioService {

    private final StudioInfoRepository studioInfoRepository;

    @Transactional
    public StudioInfoResponseDTO obter() {
        return StudioInfoResponseDTO.fromEntity(registro());
    }

    @Transactional
    public StudioInfoResponseDTO atualizar(StudioInfoRequestDTO dto) {
        StudioInfo info = registro();
        if (dto.getNome() != null) info.setNome(dto.getNome());
        if (dto.getDescricao() != null) info.setDescricao(dto.getDescricao());
        if (dto.getEndereco() != null) info.setEndereco(dto.getEndereco());
        if (dto.getHorario() != null) info.setHorario(dto.getHorario());
        if (dto.getInstagram() != null) info.setInstagram(dto.getInstagram());
        return StudioInfoResponseDTO.fromEntity(studioInfoRepository.save(info));
    }

    private StudioInfo registro() {
        return studioInfoRepository.findFirstByOrderByIdAsc()
                .orElseGet(() -> studioInfoRepository.save(StudioInfo.builder()
                        .nome("TattOS Studio").build()));
    }
}
