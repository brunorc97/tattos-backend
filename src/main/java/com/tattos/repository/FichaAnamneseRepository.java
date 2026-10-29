package com.tattos.repository;

import com.tattos.model.FichaAnamnese;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FichaAnamneseRepository extends JpaRepository<FichaAnamnese, Long> {

    List<FichaAnamnese> findByClienteId(Long clienteId);

    List<FichaAnamnese> findByClienteIdOrderByDataCriacaoDesc(Long clienteId);

    Optional<FichaAnamnese> findByAgendamentoId(Long agendamentoId);

    List<FichaAnamnese> findByAlertaClinicoTrue();

    List<FichaAnamnese> findByRiscoDermatologicoTrue();

    boolean existsByClienteIdAndTermoAssinadoTrue(Long clienteId);

    @org.springframework.data.jpa.repository.Query("""
            SELECT f FROM FichaAnamnese f
            WHERE (:clienteId IS NULL OR f.cliente.id = :clienteId)
              AND (:tatuadorId IS NULL OR EXISTS (
                    SELECT a FROM Agendamento a
                    WHERE a.cliente.id = f.cliente.id AND a.tatuador.id = :tatuadorId))
              AND (:mes IS NULL OR MONTH(f.dataCriacao) = :mes)
              AND (:ano IS NULL OR YEAR(f.dataCriacao) = :ano)
            ORDER BY f.dataCriacao DESC
            """)
    List<FichaAnamnese> buscarComFiltros(
            @org.springframework.data.repository.query.Param("clienteId") Long clienteId,
            @org.springframework.data.repository.query.Param("tatuadorId") Long tatuadorId,
            @org.springframework.data.repository.query.Param("mes") Integer mes,
            @org.springframework.data.repository.query.Param("ano") Integer ano);
}
