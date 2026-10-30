package com.tattos.repository;

import com.tattos.model.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {

    boolean existsByTrabalhoId(Long trabalhoId);

    List<Avaliacao> findByClienteIdOrderByDataDesc(Long clienteId);

    List<Avaliacao> findAllByOrderByDataDesc();

    /** Média de (notaTatuagem + notaTatuador)/2 e total, por tatuador. */
    @Query("SELECT AVG((a.notaTatuagem + a.notaTatuador) / 2.0), COUNT(a) FROM Avaliacao a "
            + "WHERE a.tatuador.id = :tatuadorId")
    List<Object[]> mediaETotalPorTatuador(@Param("tatuadorId") Long tatuadorId);
}
