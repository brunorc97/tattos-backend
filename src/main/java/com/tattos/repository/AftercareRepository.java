package com.tattos.repository;

import com.tattos.model.Aftercare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AftercareRepository extends JpaRepository<Aftercare, Long> {
    List<Aftercare> findByClienteId(Long clienteId);
    List<Aftercare> findByAgendamentoId(Long agendamentoId);
}
