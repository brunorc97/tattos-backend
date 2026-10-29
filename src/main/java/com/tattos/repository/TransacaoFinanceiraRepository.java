package com.tattos.repository;

import com.tattos.model.TransacaoFinanceira;
import com.tattos.model.enums.StatusTransacao;
import com.tattos.model.enums.TipoTransacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransacaoFinanceiraRepository extends JpaRepository<TransacaoFinanceira, Long> {

    List<TransacaoFinanceira> findByTatuadorId(Long tatuadorId);

    List<TransacaoFinanceira> findByTipo(TipoTransacao tipo);

    List<TransacaoFinanceira> findByStatus(StatusTransacao status);

    List<TransacaoFinanceira> findByDataBetween(LocalDate inicio, LocalDate fim);

    @Query("SELECT COALESCE(SUM(t.valor), 0) FROM TransacaoFinanceira t " +
           "WHERE t.tipo = com.tattos.model.enums.TipoTransacao.RECEITA " +
           "AND t.status = com.tattos.model.enums.StatusTransacao.CONCLUIDO " +
           "AND t.data BETWEEN :inicio AND :fim")
    BigDecimal sumReceitasByPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT COALESCE(SUM(t.valor), 0) FROM TransacaoFinanceira t " +
           "WHERE t.tipo = com.tattos.model.enums.TipoTransacao.DESPESA " +
           "AND t.status = com.tattos.model.enums.StatusTransacao.CONCLUIDO " +
           "AND t.data BETWEEN :inicio AND :fim")
    BigDecimal sumDespesasByPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT COALESCE(SUM(t.valorLiquido), 0) FROM TransacaoFinanceira t " +
           "WHERE t.tipo = com.tattos.model.enums.TipoTransacao.RECEITA " +
           "AND t.status = com.tattos.model.enums.StatusTransacao.CONCLUIDO " +
           "AND t.data BETWEEN :inicio AND :fim")
    BigDecimal sumReceitasLiquidasByPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT COALESCE(SUM(t.taxaValor), 0) FROM TransacaoFinanceira t " +
           "WHERE t.tipo = com.tattos.model.enums.TipoTransacao.RECEITA " +
           "AND t.status = com.tattos.model.enums.StatusTransacao.CONCLUIDO " +
           "AND t.data BETWEEN :inicio AND :fim")
    BigDecimal sumTaxasByPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT COALESCE(SUM(t.valorRepasse), 0) FROM TransacaoFinanceira t " +
           "WHERE t.tatuador.id = :tatuadorId " +
           "AND t.status = com.tattos.model.enums.StatusTransacao.CONCLUIDO " +
           "AND t.data BETWEEN :inicio AND :fim")
    BigDecimal sumRepassesByTatuadorAndPeriodo(
            @Param("tatuadorId") Long tatuadorId,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim);

    @Query("SELECT COALESCE(SUM(t.valorRepasse), 0) FROM TransacaoFinanceira t " +
           "WHERE t.status = com.tattos.model.enums.StatusTransacao.CONCLUIDO " +
           "AND t.data BETWEEN :inicio AND :fim")
    BigDecimal sumTotalRepassesByPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT t FROM TransacaoFinanceira t WHERE " +
           "(:tipo IS NULL OR t.tipo = :tipo) AND " +
           "(:tatuadorId IS NULL OR t.tatuador.id = :tatuadorId) AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:inicio IS NULL OR t.data >= :inicio) AND " +
           "(:fim IS NULL OR t.data <= :fim) " +
           "ORDER BY t.data DESC, t.id DESC")
    List<TransacaoFinanceira> findComFiltros(
            @Param("tipo") TipoTransacao tipo,
            @Param("tatuadorId") Long tatuadorId,
            @Param("status") StatusTransacao status,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim);

    @Query("SELECT t FROM TransacaoFinanceira t WHERE " +
           "(:tipo IS NULL OR t.tipo = :tipo) AND " +
           "(:tatuadorId IS NULL OR t.tatuador.id = :tatuadorId) AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:inicio IS NULL OR t.data >= :inicio) AND " +
           "(:fim IS NULL OR t.data <= :fim) " +
           "ORDER BY t.data DESC, t.id DESC")
    Page<TransacaoFinanceira> findComFiltrosPaginado(
            @Param("tipo") TipoTransacao tipo,
            @Param("tatuadorId") Long tatuadorId,
            @Param("status") StatusTransacao status,
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim,
            Pageable pageable);
}
