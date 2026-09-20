package com.tattos.service;

import com.tattos.dto.request.ArtistRequestDTO;
import com.tattos.dto.response.ArtistResponseDTO;
import com.tattos.exception.ResourceNotFoundException;
import com.tattos.model.Artist;
import com.tattos.model.enums.ArtistStatus;
import com.tattos.model.enums.ArtistTag;
import com.tattos.repository.ArtistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArtistService {

    private final ArtistRepository artistRepository;

    @Transactional(readOnly = true)
    public List<ArtistResponseDTO> findAll(ArtistTag tag) {
        List<Artist> list = (tag != null) ? artistRepository.findByTag(tag) : artistRepository.findAll();
        return list.stream().map(ArtistResponseDTO::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public ArtistResponseDTO findResponseById(Long id) {
        return ArtistResponseDTO.fromEntity(findEntityById(id));
    }

    @Transactional(readOnly = true)
    public Artist findEntityById(Long id) {
        return artistRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Tatuador não encontrado com o ID: " + id));
    }

    @Transactional
    public ArtistResponseDTO create(ArtistRequestDTO dto) {
        ArtistStatus status = dto.status() != null ? dto.status() :
            (dto.tag() == ArtistTag.VISITANTE ? ArtistStatus.EM_GUEST : ArtistStatus.ATIVO);

        Artist artist = Artist.builder()
            .name(dto.name())
            .tag(dto.tag())
            .specialty(dto.specialty())
            .splitRate(dto.splitRate())
            .station(dto.station())
            .phone(dto.phone())
            .instagram(dto.instagram())
            .email(dto.email())
            .status(status)
            .bio(dto.bio())
            .originStudio(dto.originStudio())
            .guestPeriodStart(dto.guestPeriodStart())
            .guestPeriodEnd(dto.guestPeriodEnd())
            .totalSessions(0)
            .build();

        Artist saved = artistRepository.save(artist);
        return ArtistResponseDTO.fromEntity(saved);
    }

    @Transactional
    public ArtistResponseDTO update(Long id, ArtistRequestDTO dto) {
        Artist existing = findEntityById(id);

        existing.setName(dto.name());
        existing.setTag(dto.tag());
        existing.setSpecialty(dto.specialty());
        existing.setSplitRate(dto.splitRate());
        existing.setStation(dto.station());
        existing.setPhone(dto.phone());
        existing.setInstagram(dto.instagram());
        existing.setEmail(dto.email());
        if (dto.status() != null) {
            existing.setStatus(dto.status());
        }
        existing.setBio(dto.bio());
        existing.setOriginStudio(dto.originStudio());
        existing.setGuestPeriodStart(dto.guestPeriodStart());
        existing.setGuestPeriodEnd(dto.guestPeriodEnd());

        Artist saved = artistRepository.save(existing);
        return ArtistResponseDTO.fromEntity(saved);
    }

    @Transactional
    public void delete(Long id) {
        if (!artistRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tatuador não encontrado com o ID: " + id);
        }
        artistRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ArtistResponseDTO> search(String query) {
        return artistRepository.searchArtists(query).stream()
            .map(ArtistResponseDTO::fromEntity)
            .toList();
    }
}
