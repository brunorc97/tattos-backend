package com.tattos.repository;

import com.tattos.model.Trabalho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TrabalhoRepository extends JpaRepository<Trabalho, Long> {

    List<Trabalho> findAllByOrderByDataDesc();

    /** Trabalhos avaliáveis do cliente (com cliente vinculado) ainda sem avaliação. */
    @Query("SELECT t FROM Trabalho t WHERE t.cliente.id = :clienteId "
            + "AND NOT EXISTS (SELECT 1 FROM Avaliacao a WHERE a.trabalho.id = t.id) "
            + "ORDER BY t.data DESC")
    List<Trabalho> findPendentesAvaliacaoDoCliente(@Param("clienteId") Long clienteId);
}
