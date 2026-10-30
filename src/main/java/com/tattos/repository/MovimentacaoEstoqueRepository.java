package com.tattos.repository;

import com.tattos.model.MovimentacaoEstoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long> {

    List<MovimentacaoEstoque> findByInsumoIdOrderByCreatedAtDesc(Long insumoId);

    /** Média simples dos custos unitários das entradas que informaram custo. */
    @Query("SELECT AVG(m.custoUnitario) FROM MovimentacaoEstoque m "
            + "WHERE m.insumo.id = :insumoId AND m.tipo = 'ENTRADA' AND m.custoUnitario IS NOT NULL")
    BigDecimal mediaCustosEntrada(@Param("insumoId") Long insumoId);
}
