package com.tattos.repository;

import com.tattos.model.FichaAnamnese;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FichaAnamneseRepository extends JpaRepository<FichaAnamnese, Long> {

    List<FichaAnamnese> findByClienteId(Long clienteId);

    Optional<FichaAnamnese> findByAgendamentoId(Long agendamentoId);

    List<FichaAnamnese> findByAlertaClinicoTrue();

    List<FichaAnamnese> findByRiscoDermatologicoTrue();

    boolean existsByClienteIdAndTermoAssinadoTrue(Long clienteId);
}
