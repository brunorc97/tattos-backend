package com.tattos.repository;

import com.tattos.model.Cupom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CupomRepository extends JpaRepository<Cupom, Long> {

    Optional<Cupom> findByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCase(String codigo);

    List<Cupom> findByAtivoTrue();

    @org.springframework.data.jpa.repository.Query(
            "SELECT c FROM Cupom c WHERE c.ativo = true AND (c.tatuador IS NULL OR c.tatuador.id = :tatuadorId)")
    List<Cupom> findAtivosVisiveisAoTatuador(@org.springframework.data.repository.query.Param("tatuadorId") Long tatuadorId);

    /** Incremento atômico do contador de usos; 0 linhas = limite atingido. */
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query(
            "UPDATE Cupom c SET c.usos = c.usos + 1 WHERE c.id = :id AND (c.limiteUsos IS NULL OR c.usos < c.limiteUsos)")
    int incrementarUsoSeDisponivel(@org.springframework.data.repository.query.Param("id") Long id);
}
