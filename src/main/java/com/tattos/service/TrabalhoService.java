package com.tattos.service;

import com.tattos.dto.request.TrabalhoRequestDTO;
import com.tattos.dto.response.TrabalhoResponseDTO;
import com.tattos.exception.BusinessException;
import com.tattos.model.Trabalho;
import com.tattos.model.Usuario;
import com.tattos.repository.TrabalhoRepository;
import com.tattos.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrabalhoService {

    private final TrabalhoRepository trabalhoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<TrabalhoResponseDTO> listar() {
        return trabalhoRepository.findAllByOrderByDataDesc().stream()
                .map(TrabalhoResponseDTO::fromEntity)
                .toList();
    }

    @Transactional
    public TrabalhoResponseDTO criar(TrabalhoRequestDTO dto, Usuario logado) {
        if (logado != null && logado.isTatuador() && !logado.getId().equals(dto.getTatuadorId())) {
            throw new AccessDeniedException("Tatuador só pode publicar trabalhos do próprio perfil.");
        }
        Usuario tatuador = usuarioRepository.findById(dto.getTatuadorId())
                .filter(Usuario::isTatuador)
                .orElseThrow(() -> new BusinessException("Tatuador não encontrado com ID: " + dto.getTatuadorId()));

        Trabalho trabalho = Trabalho.builder()
                .tatuador(tatuador)
                .titulo(dto.getTitulo().trim())
                .estilo(dto.getEstilo())
                .descricao(dto.getDescricao())
                .fotoUrl(dto.getFotoUrl())
                .data(LocalDate.now())
                .build();
        return TrabalhoResponseDTO.fromEntity(trabalhoRepository.save(trabalho));
    }
}
