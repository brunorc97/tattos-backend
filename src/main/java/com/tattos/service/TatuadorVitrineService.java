package com.tattos.service;

import com.tattos.dto.response.TatuadorVitrineResponseDTO;
import com.tattos.model.Usuario;
import com.tattos.repository.AvaliacaoRepository;
import com.tattos.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TatuadorVitrineService {

    private final UsuarioRepository usuarioRepository;
    private final AvaliacaoRepository avaliacaoRepository;

    @Transactional(readOnly = true)
    public List<TatuadorVitrineResponseDTO> listar() {
        return usuarioRepository.findTatuadoresAtivos().stream()
                .map(this::toVitrine)
                .toList();
    }

    private TatuadorVitrineResponseDTO toVitrine(Usuario t) {
        BigDecimal media = null;
        long total = 0;
        List<Object[]> resultado = avaliacaoRepository.mediaETotalPorTatuador(t.getId());
        if (resultado != null && !resultado.isEmpty() && resultado.get(0)[1] != null) {
            total = ((Number) resultado.get(0)[1]).longValue();
            if (resultado.get(0)[0] != null) {
                media = BigDecimal.valueOf(((Number) resultado.get(0)[0]).doubleValue())
                        .setScale(1, RoundingMode.HALF_UP);
            }
        }
        return TatuadorVitrineResponseDTO.builder()
                .id(t.getId())
                .nome(t.getNome())
                .especialidade(t.getEspecialidade())
                .bio(t.getBio())
                .fotoUrl(t.getFotoUrl())
                .mediaAvaliacao(media)
                .totalAvaliacoes(total)
                .build();
    }
}
